package com.example.routes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM Route")
    List<Route> getAllRoutes();

    @Insert
    public void insertRoute(Route route);

    @Delete
    public void deleteRoute(Route route);

}
