package com.example.a02_exercise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    SensorManager sensorManager;
    Sensor accelSensor;
    Intent intent;

    AppCompatActivity context = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelSensor, sensorManager.SENSOR_DELAY_NORMAL);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (LocationService.mLastAccelerometerSet != false) {
                    TextView tv = context.findViewById(R.id.tvaccellerometer);
                    tv.setText("accelerometer: true");
                }

                if (LocationService.mLastMagnetometerSet != false) {
                    TextView tv = context.findViewById(R.id.tv_magnetometer);
                    tv.setText("magnetometer: true");
                }
            }
        }, 20000 );

        if (LocationService.mLastAccelerometerSet != false) {
            TextView tv = this.findViewById(R.id.tvaccellerometer);
            tv.setText("accelerometer: true");
        }

        if (LocationService.mLastMagnetometerSet != false) {
            TextView tv = this.findViewById(R.id.tv_magnetometer);
            tv.setText("magnetometer: true");
        }

        findViewById(R.id.buttonStartLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    startLocationService();
                }
            }
        });
        findViewById(R.id.buttonStopLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
            }
        });

        Context mContext=this;

        int REQUEST = 112;


        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
            }
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            } else {
                Toast.makeText(this, "Permission denied! :(", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (LocationService.mLastAccelerometerSet != false) {
            TextView tv = this.findViewById(R.id.tvaccellerometer);
            tv.setText("accelerometer: true");
        }

        if (LocationService.mLastMagnetometerSet != false) {
            TextView tv = this.findViewById(R.id.tv_magnetometer);
            tv.setText("magnetometer: true");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //implement something
    }

    public boolean LocationServiceRunning(){
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null){
            for (ActivityManager.RunningServiceInfo service:
             activityManager.getRunningServices(Integer.MAX_VALUE)){
                if  (LocationService.class.getName().equals(service.service.getClassName())){
                    if (service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService(){
        if (!LocationServiceRunning()){
            intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "location service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(LocationServiceRunning()){
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            System.out.println("I WAS HERE");
            Toast.makeText(this, "location service stopped", Toast.LENGTH_SHORT).show();

        }
    }





}