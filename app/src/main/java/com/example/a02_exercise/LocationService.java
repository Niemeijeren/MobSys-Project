package com.example.a02_exercise;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.RouteTracking.MedianRoute;
import com.example.RouteTracking.Utils;
import com.example.routes.DatabaseHandler;
import com.example.routes.LocationPoint;
import com.example.routes.Route;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class LocationService extends Service implements SensorEventListener {

    DatabaseHandler db;
    Route route;
    ArrayList<LocationPoint> locationpoints;
    Long now;
    MedianRoute medianRoute;
    Handler handler;
    double R = 6378.1;

    long cycleTimer = 8000;
    long startTimer = 8000;

    Context context;

    private boolean stopSensors = false;

    float lastBearing = 0;
    double orientation = 0;
    int counter = 0;
    LocationRequest locationRequest;

    Boolean sensorChanged = true;

    SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    List<Float> gpsBearings = new ArrayList<>();
    List<Double> senBearings = new ArrayList<>();

    TreeMap<Long, String> map = new TreeMap();

    //Method for getting Last location
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                Double latitude = locationResult.getLastLocation().getLatitude();
                Double longitude = locationResult.getLastLocation().getLongitude();
                Long time = locationResult.getLastLocation().getTime();
                lastBearing = locationResult.getLastLocation().getBearing();
                gpsBearings.add(lastBearing);
                locationpoints.add(new LocationPoint(time, latitude, longitude));
                map.put(time, "gps");
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
        stopSensors = false;
        context = this;
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
        mSensorManager.registerListener((SensorEventListener) context, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener((SensorEventListener) context, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(6000);
        locationRequest.setFastestInterval(3000);
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
        stopSensors = true;
        stopSelf();
    }

    private void insertRouteToDb() {
        route.setTimeEnd(System.currentTimeMillis());

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float) scale;


        //this.saveData();

        if (medianRoute.checkValidity()) {
            //100 points for kilometer
            //2 points per minute
            int points = (int) (medianRoute.getLengthOfRouteKm() * 100 + medianRoute.getTimeOfRoute() * 2);
            route.setPoints(points);
        } else {
            route.setPoints(0);
        }


        PowerManager powerManager = (PowerManager)
                this.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager.isPowerSaveMode() || batteryPct <= 25) {
            //Not CPU heavy
            route.setLocationPoints(medianRoute.getMedianPoints());
            db.userDao().insertRoute(route);
        } else {
            //CPU heavy
            route.setLocationPoints(medianRoute.getMedianPoints());
            //route.setLocationPoints(Utils.reduceNumberOfPointsByProximity(medianRoute.getMedianPoints()));
            db.userDao().insertRoute(route);
        }

        //Points come in chronological order so its possible to simply take every 3 location points
        //And add them together to reduce the overall number of location points saved
        //And thereby save space on the decive though sacrificing some CPU
        //Another approach can be to find every within 1 point for example 5 meters and
        //Turn it into one point then do that until no points are within 5 meters of each each
        //But that would take O(n*n) time approximately.
        //And also consume alot more resources compared to just averaging points O(n)
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopSensors() {
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (stopSensors) {
            stopSensors();
        }

        if (counter < 10) {

            counter++;

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


                double radian = mOrientation[0];
                orientation += radian;

                //System.out.println(counter + " orientation read: " + orientation);

                if (counter == 9) {
                    orientation = orientation / counter;
                    orientation = Math.toDegrees(orientation);
                    orientation = (orientation + 360) % 360;
                    senBearings.add(orientation);
                    System.out.println("last orientation: " + orientation);
                    System.out.println("last bearing: " + lastBearing);
                    int interval = 15;
                    if ((((Math.abs((lastBearing - orientation) % 360) < interval)
                            || (Math.abs((lastBearing - orientation) % 360) > 360 - interval))
                            || (Math.abs((orientation - lastBearing) % 360) < interval))
                            || (Math.abs((orientation - lastBearing) % 360) > 360 - interval)) {
                        cycleTimer += 4000;
                        System.out.println("interval increased to: " + cycleTimer);
                        if (locationpoints.size() > 10) {
                            LocationPoint lastLocationPoint = this.locationpoints.get(locationpoints.size() - 1);
                            Long currentTime = System.currentTimeMillis();
                            //to km
                            double length = medianRoute.getAverageSpeed() * (currentTime - lastLocationPoint.getTimeStamp());
                            length = length / 1000;

                            Double lat1 = Math.toRadians(lastLocationPoint.getLatitude());
                            Double lon1 = Math.toRadians(lastLocationPoint.getLongitude());

                            Double lat2 = Math.asin(Math.sin(lat1) * Math.cos(length / R) + Math.cos(lat1) * Math.sin(length / R) * Math.cos(radian));
                            Double lon2 = lon1 + (Math.atan2(Math.sin(radian) * Math.sin(length / R) * Math.cos(lat1), Math.cos(length / R) - Math.sin(lat1) * Math.sin(lat2)));

                            //New points to input
                            lat2 = Math.toDegrees(lat2);
                            lon2 = Math.toDegrees(lon2);
                            System.out.println(lat2);
                            System.out.println(lon2);
                            LocationPoint newPoint = new LocationPoint(currentTime, lat2, lon2);
                            map.put(currentTime, "compass");
                            //lastBearing = (float) orientation;
                            medianRoute.addPoint(newPoint);

                            locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationRequest.setInterval(60000);
                            locationRequest.setFastestInterval(45000);
                            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                            System.out.println("Gps will start again in: " + ((System.currentTimeMillis() - System.currentTimeMillis()) + (1000 * (2 * cycleTimer)) / 1000) + " Seconds");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    cycleTimer = startTimer;
                                    System.out.println("cycletimer reset: " + cycleTimer);
                                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    locationRequest.setInterval(5000);
                                    locationRequest.setFastestInterval(3000);
                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                                }
                            }, 2 * cycleTimer);
                        }

                    } else {
                        System.out.println("Cycletimer reset to " + startTimer);
                        cycleTimer = startTimer;
                    }
                    mSensorManager.unregisterListener((SensorEventListener) context, mMagnetometer);
                    mSensorManager.unregisterListener((SensorEventListener) context, mAccelerometer);
                    counter = 0;
                    orientation = 0d;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSensorManager.registerListener((SensorEventListener) context, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
                            mSensorManager.registerListener((SensorEventListener) context, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }, cycleTimer);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveData() {

        try {

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + "sems/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission granted");

                String fullName;
                File file;

                FileWriter fw;
                BufferedWriter bw;

                String str = "Timestamp, Tag\n";

                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pair = (HashMap.Entry) it.next();
                    Long timeStamp = (Long) pair.getKey();
                    String value = (String) pair.getValue();

                    str += timeStamp + "," + value + "\n";
                    it.remove(); // avoids a ConcurrentModificationException
                }

                fullName = path + "gpsVsCompass" + System.currentTimeMillis() / 1000 + ".csv";
                file = new File(fullName);

                fw = new FileWriter(file.getAbsoluteFile());
                bw = new BufferedWriter(fw);
                bw.write(str);

                bw.close();
            }

        } catch (
                IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
