package com.example.routes;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Route.class, LocationPoint.class}, version = 3)
public abstract class DatabaseHandler extends RoomDatabase {
    private static DatabaseHandler instance;

    public abstract UserDao userDao();

    public static DatabaseHandler getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context,
                    DatabaseHandler.class, "database-name")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

