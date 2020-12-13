package com.example.routes;

import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Route {

    //@PrimaryKey
    //private int ID;

    @PrimaryKey
    @ColumnInfo(name ="timeStart")
    private Long timeStart;

    @ColumnInfo(name ="timeEnd")
    private Long timeEnd;

    @TypeConverters(DataConverter.class) // add here
    @ColumnInfo(name = "locationsPoints")
    private List<LocationPoint> locationPoints;

    @ColumnInfo(name = "points")
    private int points;

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }


    public void setLocationPoints(List<LocationPoint> list) {
        this.locationPoints = list;
    }

    public List<LocationPoint> getLocationPoints() { return this.locationPoints; }

    //public int ID() {
    //    return ID;
    //}

    //public void setID(int id) {
    //    this.ID = id;
    //}

    public Long timeStart() {
        return timeStart;
    }

    public void setTimeStart(Long timeStart) {
        this.timeStart = timeStart;
    }

    public Long timeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }

}
