package com.example.Kalman;

import com.example.routes.LocationPoint;
import com.example.routes.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class comes from stack overflow
 * https://stackoverflow.com/questions/1134579/smooth-gps-data
 */
public class RoutesHelper {
    private final float MinAccuracy = 1;

    private float Q_metres_per_second;
    private long TimeStamp_milliseconds;
    private double lat;
    private double lng;
    private float variance; // P matrix.  Negative means object uninitialised.  NB: units irrelevant, as long as same units used throughout

    public RoutesHelper(float Q_metres_per_second) { this.Q_metres_per_second = Q_metres_per_second; variance = -1; }

    public long get_TimeStamp() { return TimeStamp_milliseconds; }
    public double get_lat() { return lat; }
    public double get_lng() { return lng; }
    public float get_accuracy() { return (float)Math.sqrt(variance); }

    public void SetState(double lat, double lng, float accuracy, long TimeStamp_milliseconds) {
        this.lat=lat; this.lng=lng; variance = accuracy * accuracy; this.TimeStamp_milliseconds=TimeStamp_milliseconds;
    }


    /**
     * Process a point with kalman filter functionality
     * Kalman uses the accuracy / variance to estimate
     * @param lat_measurement
     * @param lng_measurement
     * @param accuracy
     * @param TimeStamp_milliseconds
     */
    public void Process(double lat_measurement, double lng_measurement, float accuracy, long TimeStamp_milliseconds) {
        if (accuracy < MinAccuracy) accuracy = MinAccuracy;
        if (variance < 0) {
            // if variance < 0, object is unitialised, so initialise with current values
            this.TimeStamp_milliseconds = TimeStamp_milliseconds;
            lat=lat_measurement; lng = lng_measurement; variance = accuracy*accuracy;
        } else {
            // else apply Kalman filter methodology

            long TimeInc_milliseconds = TimeStamp_milliseconds - this.TimeStamp_milliseconds;
            if (TimeInc_milliseconds > 0) {
                // time has moved on, so the uncertainty in the current position increases
                variance += TimeInc_milliseconds * Q_metres_per_second * Q_metres_per_second / 1000;
                this.TimeStamp_milliseconds = TimeStamp_milliseconds;

                //CAN USE SPEED FROM THE LOCATION RESULT HERE
                // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE OF CURRENT POSITION
            }

            // Kalman gain matrix K = Covarariance * Inverse(Covariance + MeasurementVariance)
            // NB: because K is dimensionless, it doesn't matter that variance has different units to lat and lng
            float K = variance / (variance + accuracy * accuracy);
            // apply K
            lat += K * (lat_measurement - lat);
            lng += K * (lng_measurement - lng);
            // new Covarariance  matrix is (IdentityMatrix - K) * Covarariance
            variance = (1 - K) * variance;
        }
    }

    /**
     * Loses 4 locations points every time as it takes the first takes:
     * 0 to 5
     * 1 to 6
     * 2 to 7
     * and so on where only one points is made all the time
     * So 10 locatation points gets turned into 6 etc.
     * The longer the route is the less impact this has
     *
     * It simply uses the average time of the 5 points its looking at
     * @param locationPointList
     * @return
     */
    public List<LocationPoint> medianFilter(List<LocationPoint> locationPointList) {
        if (locationPointList.size() <= 5) {
            return locationPointList;
        }

        ArrayList<LocationPoint> toReturn = new ArrayList<>();

        for (int i = 0; i <= locationPointList.size() - 4; i++) {
            ArrayList<Double> lats = new ArrayList<>();
            ArrayList<Double> lons = new ArrayList<>();
            ArrayList<Long> times = new ArrayList<>();
            for (int j = i;j < i + 5; j++) {
                lats.add(locationPointList.get(j).getLatitude());
                lons.add(locationPointList.get(j).getLongitude());
                times.add(locationPointList.get(j).getTimeStamp());
            }
            Collections.sort(lats);
            Collections.sort(lons);
            long timeAvg = 0;
            for (Long timeStamp: times) {
                timeAvg += timeStamp;
            }
            toReturn.add(new LocationPoint(timeAvg / times.size(), lats.get(lats.size() / 2), lons.get(lons.size() / 2)));


        }
        return toReturn;
    }
}
