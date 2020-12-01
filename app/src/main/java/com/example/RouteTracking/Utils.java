package com.example.RouteTracking;

import com.example.routes.LocationPoint;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

class Utils {

    private static final double EARTH_CURVATURE = 3959 * 1.609 * 1000; // meters

    /**
     * When your radius is defined in meters, you will need the Haversine
     * formula. This formula will calculate the distance between two points (in
     * meters) while taking into account the earth curvature.
     * @param locationPoint1
     * @param locationPoint2
     * @return
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
     * @param locationPoint1 first lat and lon
     * @param locationPoint2 second lat and lon
     * @return double m/s
     */
    public static double calculateMetersPerSecond(LocationPoint locationPoint1, LocationPoint locationPoint2) {
        double distance = calculateDistance(locationPoint1, locationPoint2);
        double timePassedInSeconds = locationPoint2.getTimeStamp() - locationPoint1.getTimeStamp();

        return distance / timePassedInSeconds;


    }

}
