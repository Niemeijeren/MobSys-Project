package com.example.a02_exercise;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.routes.DatabaseHandler;
import com.example.routes.LocationPoint;
import com.example.routes.Route;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.a02_exercise", appContext.getPackageName());
    }

    @Test
    public void assertWriteAndReadToDb() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHandler db = DatabaseHandler.getInstance(appContext);

        Route route = new Route();
        route.setTimeStart(System.currentTimeMillis());
        route.setTimeEnd(System.currentTimeMillis()+ 20000);

        ArrayList<LocationPoint> ar = new ArrayList<>();
        ar.add(new LocationPoint(System.currentTimeMillis(), 55.3740649, 10.4274271));

        route.setLocationPoints(ar);

        db.userDao().insertRoute(route);

        //Assert route is saved
        assertNotEquals(0, db.userDao().getAllRoutes().size());
        //Assert location points are saved on the route
        assertNotEquals(null, db.userDao().getAllRoutes().get(0).getLocationPoints());
    }
}