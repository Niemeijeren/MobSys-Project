package com.example.routes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity
public class LocationPoint {

    public LocationPoint(Long timeStamp, Double latitude, Double longitude) {
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    @PrimaryKey
    private int ID;

    /**
     * When the Point was created
     */
    private Long timeStamp;

    /**
     * The latitude value
     */
    private Double latitude;

    /**
     * The longitude value
     */
    private Double longitude;

    public int getID() {
        return ID;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
