package com.example.a02_exercise;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ScrollView.RoutesAdapter;
import com.example.routes.DatabaseHandler;
import com.example.routes.Route;

import java.util.ArrayList;

public class ViewRoutesActivity extends AppCompatActivity implements RoutesAdapter.ItemClickListener {

    DatabaseHandler db;
    RoutesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_activity);

        db = DatabaseHandler.getInstance(this);

        // data to populate the RecyclerView with
        ArrayList<Route> routes;

        routes = (ArrayList<Route>) db.userDao().getAllRoutes();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new RoutesAdapter(routes);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {

        Route route = adapter.getRoute(position);
        System.out.println(route);
    }


}
