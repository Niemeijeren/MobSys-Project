package com.example.RouteTracking;

import com.example.routes.LocationPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianRoute {

    private List<LocationPoint> locationPointsInternal;
    private List<LocationPoint> medianPoints;

    private double averageSpeed;

    private Utils utils = new Utils();

    public MedianRoute() {
        this.medianPoints = new ArrayList<>();
        this.locationPointsInternal = new ArrayList<>();
        averageSpeed = 0;
    }

    public List<LocationPoint> getMedianPoints() {
        return this.medianPoints;
    }

    public void addPoint(LocationPoint locationPoint) {
        locationPointsInternal.add(locationPoint);
        if (locationPointsInternal.size() <= 5) {
            //Do nothing
        } else {
            medianPoints.add(this.medianSinglePoint(locationPointsInternal, locationPointsInternal.size() - 5));
        }
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    /**
     * median filter a whole list of locationPoints
     *
     * @param locationPointList
     * @return
     */
    public List<LocationPoint> medianFilter(List<LocationPoint> locationPointList) {
        if (locationPointList.size() <= 5) {
            return locationPointList;
        }

        ArrayList<LocationPoint> toReturn = new ArrayList<>();

        for (int i = 0; i <= locationPointList.size() - 4; i++) {
            toReturn.add(this.medianSinglePoint(locationPointList, i));
        }
        return toReturn;
    }

    private LocationPoint medianSinglePoint(List<LocationPoint> locationPointList, int i) {
        ArrayList<Double> lats = new ArrayList<>();
        ArrayList<Double> lons = new ArrayList<>();
        ArrayList<Long> times = new ArrayList<>();
        for (int j = i; j < i + 5; j++) {
            lats.add(locationPointList.get(j).getLatitude());
            lons.add(locationPointList.get(j).getLongitude());
            times.add(locationPointList.get(j).getTimeStamp());
        }
        Collections.sort(lats);
        Collections.sort(lons);
        long timeAvg = 0;
        for (Long timeStamp : times) {
            timeAvg += timeStamp;
        }

        //Speed
        LocationPoint first = locationPointList.get(i);
        double accumulatedSpeed = 0;
        for (int j = i + 1; j < i + 5; j++) {
            accumulatedSpeed += utils.calculateMetersPerMillisSecond(first, locationPointList.get(j));
            first = locationPointList.get(j);
        }
        accumulatedSpeed = accumulatedSpeed / 4;
        if (this.averageSpeed != 0) {
            this.averageSpeed = (averageSpeed + accumulatedSpeed) / 2;
        } else {
            averageSpeed = accumulatedSpeed;
        }

        return new LocationPoint(timeAvg / times.size(), lats.get(lats.size() / 2), lons.get(lons.size() / 2));
    }

    public double getAverageSpeedKmh() {
        return this.averageSpeed * 1000 * 3.6;
    }

    /**
     * Makes various checks to check the validity of the route for now only average speed over at least 10 location points
     *
     * @return
     */
    public Boolean checkValidity() {
        if (this.getAverageSpeedKmh() >= 15) {
            return false;
        }
        return true;
    }

    public double getLengthOfRouteKm() {
        double length = 0;
        if (medianPoints.size() < 10) {
            return 0;
        }
        LocationPoint first = this.medianPoints.get(0);
        medianPoints.remove(0);
        length = 0;
        for (LocationPoint locationPoint : medianPoints) {
            length += utils.calculateDistance(first, locationPoint);
            first = locationPoint;
        }
        return length / 1000;
    }

    /**
     * @return Minutes
     */
    public double getTimeOfRoute() {
        if (medianPoints.size() < 10) {
            return 0;
        }
        Long timestampFirst = medianPoints.get(0).getTimeStamp();


        Long timestmapLast = medianPoints.get(medianPoints.size() - 1).getTimeStamp();

        return (timestmapLast - timestampFirst) / 1000 / 60;

    }
}
