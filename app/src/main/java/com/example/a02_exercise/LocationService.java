package com.example.a02_exercise;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.RouteTracking.MedianRoute;
import com.example.routes.DatabaseHandler;
import com.example.routes.LocationPoint;
import com.example.routes.Route;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class LocationService extends Service {

    DatabaseHandler db;
    Route route;
    ArrayList<LocationPoint> locationpoints;
    Long now;
    MedianRoute medianRoute;

    //Method for getting Last location
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                Double latitude = locationResult.getLastLocation().getLatitude();
                Double longitude = locationResult.getLastLocation().getLongitude();
                Long time = locationResult.getLastLocation().getTime();
                locationpoints.add(new LocationPoint(time, latitude, longitude));
                medianRoute.addPoint(new LocationPoint(time, latitude, longitude));
            }
            if (!medianRoute.checkValidity()) {
                routeInvalid();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void startLocationService() {
        now = System.currentTimeMillis();
        db = DatabaseHandler.getInstance(this.getApplicationContext());
        route = new Route();
        route.setTimeStart(System.currentTimeMillis());
        locationpoints = new ArrayList<LocationPoint>();
        medianRoute = new MedianRoute();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        this.setNotification();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    private void setNotification() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("this channel is used by the location service");
                notificationManager.createNotificationChannel(notificationChannel);

            }
        }
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    /**
     * Stops the service but doesnt save the route
     */
    private void routeInvalid() {
        route = null;
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopSelf();
    }

    /**
     * Stops the service AND saves the route
     */
    private void stopLocationService() {
        this.insertRouteToDb();
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopSelf();
    }

    private void insertRouteToDb() {
        route.setTimeEnd(System.currentTimeMillis());
        //route.setLocationPoints(locationpoints);
        route.setLocationPoints(medianRoute.getLocationPointsInternal());

        System.out.println(route);
        System.out.println(medianRoute.getLocationPointsInternal());

        db.userDao().insertRoute(route);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
