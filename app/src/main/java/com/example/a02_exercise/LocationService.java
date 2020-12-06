package com.example.a02_exercise;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
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

public class LocationService extends Service implements SensorEventListener {

    DatabaseHandler db;
    Route route;
    ArrayList<LocationPoint> locationpoints;
    Long now;
    MedianRoute medianRoute;
    Handler handler;
    double R = 6378.1;

    Boolean sensorChanged = true;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

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



        handler = new Handler(this.getApplicationContext().getMainLooper());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);


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
        //builder.setSmallIcon(R.mipmap.ic_launcher);
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
        mSensorManager.unregisterListener(this);
        stopSelf();
    }

    private void insertRouteToDb() {
        route.setTimeEnd(System.currentTimeMillis());
        route.setLocationPoints(medianRoute.getMedianPoints());

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

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (sensorChanged) {
            sensorChanged = false;

            if (event.sensor == mAccelerometer) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            } else if (event.sensor == mMagnetometer) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);
                //System.out.println("OrientationTestActivity" + String.format("Orientation: %f, %f, %f", mOrientation[0], mOrientation[1], mOrientation[2]));

                double radian = mOrientation[0];
                if (locationpoints.size() > 10) {
                    LocationPoint lastLocationPoint =  this.locationpoints.get(locationpoints.size() - 1);
                    // to from millis to seconds
                    Long currentTime = System.currentTimeMillis();

                    double length = medianRoute.getAverageSpeedms() * ((currentTime - lastLocationPoint.getTimeStamp()) / 1000) ;

                    if(length >= 0.05) {
                        System.out.println("Length 50 meters or more");
                    }

                    Double lat1 = lastLocationPoint.getLatitude();
                    Double lon1 = lastLocationPoint.getLongitude();

                    lat1 = Math.toRadians(lat1);
                    lon1 = Math.toRadians(lon1);

                    Double lat2 = Math.asin( Math.sin(lat1) * Math.cos(length / R) + Math.cos(lat1) * Math.sin(length / R) * Math.cos(radian));
                    Double lon2 = lon1 + (Math.atan2(Math.sin(radian)*Math.sin(length/R)*Math.cos(lat1), Math.cos(length/R)-Math.sin(lat1)*Math.sin(lat2)));

                    //New points to input
                    lat2 = Math.toDegrees(lat2);
                    lon2 = Math.toDegrees(lon2);

                    System.out.println(lastLocationPoint.getLatitude());
                    System.out.println(lastLocationPoint.getLongitude());
                    System.out.println(lat2);
                    System.out.println(lon2);
                    LocationPoint newPoint = new LocationPoint(currentTime, lat2, lon2);
                    medianRoute.addPoint(newPoint);
                }


            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sensorChanged = true;

                }
            }, 2000);
        }




    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
