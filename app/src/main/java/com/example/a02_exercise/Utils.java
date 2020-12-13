package com.example.a02_exercise;

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
     * @return distance in meters;
     */
    public static double calculateDistance(Double latitude, Double longitude, Double latitude2, Double longitude2) {
        double c
                = sin(toRadians(latitude))
                * sin(toRadians(latitude2))
                + cos(toRadians(latitude))
                * cos(toRadians(latitude2))
                * cos(toRadians(longitude2)
                - toRadians(longitude));
        c = c > 0 ? min(1, c) : max(-1, c);
        return EARTH_CURVATURE * acos(c);
    }

    /**
     * Calculates the speed in meters per second between two points
     * @return double meters per milisecond
     */
    public static double calculateMetersPerMillisSecond(Double latitude, Double longitude, Double latitude2, Double longitude2, Long timestamp1, Long timestamp2) {
        double distance = calculateDistance(latitude, longitude, latitude2, longitude2);
        double timePassedMiliInSeconds = timestamp2 - timestamp1;

        return (distance / timePassedMiliInSeconds) * 1000 * 3.6;


    }
}
