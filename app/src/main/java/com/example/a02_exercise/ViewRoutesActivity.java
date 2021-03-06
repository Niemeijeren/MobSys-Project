package com.example.a02_exercise;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.RouteTracking.Utils;
import com.example.ScrollView.RoutesAdapter;
import com.example.routes.DataConverter;
import com.example.routes.DatabaseHandler;
import com.example.routes.LocationPoint;
import com.example.routes.Route;

import java.util.ArrayList;
import java.util.List;

public class ViewRoutesActivity extends AppCompatActivity implements RoutesAdapter.ItemClickListener {

    DatabaseHandler db;
    RoutesAdapter adapter;
    DataConverter dataConverter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_activity);

        db = DatabaseHandler.getInstance(this);
        dataConverter = new DataConverter();

        ArrayList<Route> routes;
        routes = (ArrayList<Route>) db.userDao().getAllRoutes();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new RoutesAdapter(routes);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {

        Route route = adapter.getRoute(position);

        System.out.println("Before: " + route.getLocationPoints().size());

        //List<LocationPoint> points = Utils.reduceNumberOfPointsByProximity(route.getLocationPoints());
        List<LocationPoint> points = route.getLocationPoints();

        System.out.println("After: " + points.size());

        Intent myIntent = new Intent(ViewRoutesActivity.this, MapsActivity.class);
        String RouteToLocationsJson = dataConverter.fromOptionValuesList(points);
        myIntent.putExtra("locations", RouteToLocationsJson); //Optional parameters
        ViewRoutesActivity.this.startActivity(myIntent);

    }

    @Override
    /**
     * Should delete the route that has been long pressed
     * but only after a confirmation popup
     */
    public void onLongPress(View view, int position) {
         Route route = adapter.getRoute(position);

         db.userDao().deleteRoute(route);

         adapter.removeItem(position);
    }


}
