package com.example.routes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM Route")
    List<Route> getAllRoutes();

    @Query("SELECT * FROM Route WHERE ID LIKE :first")
    Route findById(String first);

    @Query("SELECT * FROM LocationPoint WHERE routeForeignKey LIKE :first")
    List<LocationPoint> getAllLocationPoints(String first);

    @Insert
    void insertRoute(Route route);


}
