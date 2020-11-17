package com.example.routes;

import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Route {

    @PrimaryKey
    private int ID;

    @ColumnInfo(name ="timeStart")
    private Long timeStart;

    @ColumnInfo(name ="timeEnd")
    private Long timeEnd;

    @Ignore
    private List<LocationPoint> locationPoints;

    public void setLocationPoints(List<LocationPoint> list) {
        this.locationPoints = list;
    }

    public int ID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

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
