package com.example.a02_exercise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.routes.DatabaseHandler;
import com.example.routes.LocationPoint;
import com.example.routes.Route;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    SensorManager sensorManager;
    Sensor accelSensor;
    private static final int REQUEST_CODE = 101;
    DatabaseHandler db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DatabaseHandler.getInstance(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //sensorManager.registerListener(MainActivity.this, accelSensor, sensorManager.SENSOR_DELAY_NORMAL);
        this.getPermission();
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    startLocationService();
                }
            }
        });

        findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
            }
        });

        findViewById(R.id.btnViewRoutes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ViewRoutesActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });

    }

    public void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
            return;
        }
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

    // @Override
    // public void onSensorChanged(SensorEvent event) {
    //  Log.d(TAG, "onSensorChanged: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
    // }

    // @Override
    // public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //implement something
    // }

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
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnStart).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnStop).setVisibility(View.VISIBLE);

        }
    }

    private void stopLocationService(){
        if(LocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Trip ended", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnStop).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnStart).setVisibility(View.VISIBLE);
        }

        System.out.println("Read from DB");
        if (db.userDao().getAllRoutes() != null) {
            for (Route route : db.userDao().getAllRoutes()) {
                System.out.println("Route start time:");
                System.out.println(route.timeStart());
                System.out.println("Route end time:");
                System.out.println(route.timeEnd());
                if (route.getLocationPoints() != null) {
                    for (LocationPoint locationPoint : route.getLocationPoints()) {
                        System.out.println("LocationPoint timestamp");
                        System.out.println(locationPoint.getTimeStamp());
                        System.out.println("Latitude");
                        System.out.println(locationPoint.getLatitude());
                        System.out.println("Longitude");
                        System.out.println(locationPoint.getLongitude());
                    }
                }
            }
        }

    }





}