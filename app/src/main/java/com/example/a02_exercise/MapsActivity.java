package com.example.a02_exercise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.example.routes.DataConverter;
import com.example.routes.LocationPoint;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DataConverter dataConverter;
    private final int padding = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataConverter = new DataConverter();
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        String locationsString = (String)getIntent().getExtras().get("locations");
        List<LocationPoint> locations = dataConverter.toOptionValuesList(locationsString);

        List<LatLng> latlngs = new ArrayList<>();
        for(LocationPoint locationPoint : locations){
            latlngs.add(new LatLng(locationPoint.getLatitude(), locationPoint.getLongitude()));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latlngs) {
            builder.include(latLng);
        }

        PolylineOptions rectOptions = new PolylineOptions().addAll(latlngs).color(Color.BLUE);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(locations.get(locations.size()/2).getLatitude(), locations.get(locations.size()/2).getLongitude())));
        //mMap.setMinZoomPreference(12);
        Polyline line = mMap.addPolyline(rectOptions);


        LatLngBounds bounds = builder.build();
        final CameraUpdate cameraUpdater = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                /**set animated zoom camera into map*/
                mMap.animateCamera(cameraUpdater);
            }
        });

    }

}