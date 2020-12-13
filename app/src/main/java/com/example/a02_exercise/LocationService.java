package com.example.a02_exercise;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class LocationService extends Service implements SensorEventListener {

    SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    Handler handler;
    double R = 6378.1;



    private int interval = 5000;

    double orientation = 0;
    int counter = 0;
    Context context;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    public static boolean mLastAccelerometerSet = false;
    public static boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    List<String[]> data = new ArrayList<String[]>();

    List<String> gpsBearings = new ArrayList<String>();
    List<String> senBearings = new ArrayList<String>();

    List<Long> gpsTime = new ArrayList<>();
    List<Long> senTime = new ArrayList<>();
    List<Double> lats = new ArrayList<>();
    List<Double> lons = new ArrayList<>();


    TreeMap<Long, String> map = new TreeMap();


    //Method for getting Last location
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double bearing = locationResult.getLastLocation().getBearing();
                System.out.println("GPS Bearing " + bearing + " at time " + System.currentTimeMillis());
                gpsBearings.add(bearing + "");
                gpsTime.add(locationResult.getLastLocation().getTime());
                lats.add(locationResult.getLastLocation().getLatitude());
                lons.add(locationResult.getLastLocation().getLongitude());
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void startLocationService() {
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
        context = this;

        handler = new Handler(this.getApplicationContext().getMainLooper());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        context = this;


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        // builder.setSmallIcon(R.mipmap.ic_launcher);
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

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(interval);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void stopLocationService() throws IOException {
        saveData();
        mSensorManager.unregisterListener(this);
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    try {
                        stopLocationService();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (counter < 10) {

            counter++;

            if (event.sensor == mAccelerometer) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            }
            if (event.sensor == mMagnetometer) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);

                double radian = mOrientation[0];
                orientation += radian;

                if (counter == 9) {
                    orientation = orientation / counter;
                    orientation = Math.toDegrees(orientation);
                    orientation = (orientation + 360) % 360;
                    System.out.println("Sen Bearing " + orientation + " at time " + System.currentTimeMillis());
                    senBearings.add(orientation + "");
                    System.out.println("sen bearing: " + orientation);
                    senTime.add(System.currentTimeMillis());

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
                    }, interval);

                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveData() {

        try {

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + "sems/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            System.out.println("Before permission check");
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission granted");

                String fullName;
                File file;

                FileWriter fw;
                BufferedWriter bw;

                String str = "gpsBearing, latitude, longitude, gpsTimeMillis, sensor, sensorTimeMillis, averageSpeedKmH\n";

                double speed = 0;

                for (int i = 0; i < gpsBearings.size() && i < senBearings.size(); i ++) {
                    if (i > 0) {
                        if (speed == 0) {
                            speed = Utils.calculateMetersPerMillisSecond(lats.get(i-1), lons.get(i-1), lats.get(i), lons.get(i), gpsTime.get(i-1), gpsTime.get(i));
                        } else {
                            speed = (speed + Utils.calculateMetersPerMillisSecond(lats.get(i-1), lons.get(i-1), lats.get(i), lons.get(i), gpsTime.get(i-1), gpsTime.get(i))) / 2;
                        }

                    }

                    str += gpsBearings.get(i) + "," + lats.get(i) + "," + lons.get(i) + "," + gpsTime.get(i) + "," + senBearings.get(i) + "," + senTime.get(i) + "," + speed + "\n";
                }

                fullName = path + "semsGpsAndSensorDataV2" + System.currentTimeMillis() / 1000 + ".csv";
                file = new File(fullName);

                fw = new FileWriter(file.getAbsoluteFile());
                bw = new BufferedWriter(fw);
                bw.write(str);

                bw.close();

            }
            System.out.println("after permission check");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
