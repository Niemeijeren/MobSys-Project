package com.example.a02_exercise;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.routes.DataConverter;
import com.example.routes.LocationPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DataConverter dataConverter;

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

        PolylineOptions rectOptions = new PolylineOptions().addAll(latlngs);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(locations.get(0).getLatitude(), locations.get(0).getLongitude())));
        mMap.setMinZoomPreference(14);
        Polyline line = mMap.addPolyline(rectOptions);

        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}