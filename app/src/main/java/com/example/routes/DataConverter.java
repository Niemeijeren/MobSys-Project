package com.example.routes;

import android.location.Location;

import androidx.room.TypeConverter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataConverter implements Serializable {

    @TypeConverter
    public String fromOptionValuesList(List<LocationPoint> optionValues) {
        if (optionValues == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<LocationPoint>>() {
        }.getType();
        String json = gson.toJson(optionValues, type);
        return json;
    }

    @TypeConverter
    public List<LocationPoint> toOptionValuesList(String optionValuesString) {
        if (optionValuesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<LocationPoint>>() {
        }.getType();
        List<LocationPoint> LocationPointsList = gson.fromJson(optionValuesString, type);
        return LocationPointsList;
    }

}
