package com.example.routes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM Route")
    List<Route> getAllRoutes();

    //@Query("SELECT * FROM Route WHERE ID LIKE :first")
    //Route findById(String first);

    @Insert
    public void insertRoute(Route route);


}
