package com.example.RouteTracking;

import android.widget.ArrayAdapter;

import com.example.routes.LocationPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class Utils {

    private static final double EARTH_CURVATURE = 3959 * 1.609 * 1000; // meters

    /**
     * When your radius is defined in meters, you will need the Haversine
     * formula. This formula will calculate the distance between two points (in
     * meters) while taking into account the earth curvature.
     *
     * @param locationPoint1
     * @param locationPoint2
     * @return distance in meters;
     */
    public static double calculateDistance(LocationPoint locationPoint1, LocationPoint locationPoint2) {
        double c
                = sin(toRadians(locationPoint1.getLatitude()))
                * sin(toRadians(locationPoint2.getLatitude()))
                + cos(toRadians(locationPoint1.getLatitude()))
                * cos(toRadians(locationPoint2.getLatitude()))
                * cos(toRadians(locationPoint2.getLongitude())
                - toRadians(locationPoint1.getLongitude()));
        c = c > 0 ? min(1, c) : max(-1, c);
        return EARTH_CURVATURE * acos(c);
    }

    /**
     * Calculates the speed in meters per second between two points
     *
     * @param locationPoint1 first lat and lon
     * @param locationPoint2 second lat and lon
     * @return double meters per milisecond
     */
    public static double calculateMetersPerMillisSecond(LocationPoint locationPoint1, LocationPoint locationPoint2) {
        double distance = calculateDistance(locationPoint1, locationPoint2);
        double timePassedInSeconds = locationPoint2.getTimeStamp() - locationPoint1.getTimeStamp();

        return (distance / timePassedInSeconds);


    }


    public static List<LocationPoint> reduceNumberOfPointsByProximity(List<LocationPoint> locationPointsList) {
        ArrayList<LocationPoint> toReturn = new ArrayList<>();
        if (locationPointsList.size() > 10) {
            LocationPoint first = null;
            while (locationPointsList.size() > 1) {
                first = locationPointsList.get(0);
                locationPointsList.remove(first);


                ArrayList<LocationPoint> toGroup = new ArrayList<>();

                for (LocationPoint locationPoint : locationPointsList) {
                    if (calculateDistance(first, locationPoint) < 15) {
                        toGroup.add(locationPoint);
                    }
                }
                locationPointsList.removeAll(toGroup);
                if (toGroup.size() > 0) {
                    toGroup.add(first);
                } else {
                    toReturn.add(first);
                    first = null;
                    continue;
                }

                double lat = 0;
                double lon = 0;
                long timestamp = 0;
                for (LocationPoint locationPoint2 : toGroup) {
                    lat += locationPoint2.getLatitude();
                    lon += locationPoint2.getLongitude();
                    timestamp += locationPoint2.getTimeStamp();
                }

                lat = lat / toGroup.size();
                lon = lon / toGroup.size();
                timestamp = timestamp / toGroup.size();

                toReturn.add(new LocationPoint(timestamp, lat, lon));
                first = null;

            }
        }
        if (toReturn.size() == 0) {
            return locationPointsList;
        }
        return toReturn;

    }

}
