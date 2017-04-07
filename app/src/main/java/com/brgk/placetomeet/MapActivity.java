package com.brgk.placetomeet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{
    //TEMP
    int PLACE_PICKER_REQUEST = 1;

    MapActivity activity = this;
    GoogleMap mGoogleMap = null;
    List<Place> places;

    List<Marker> markers;
    Set<Polyline> lines;

    Circle centerCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        markers = new ArrayList<>();
        lines = new HashSet<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String[] strings = bundle.getStringArray(Constants.EXTRA_CHECKED_PLACES);
        for( String s :  strings ) {
            Log.d("MACIEK_DEBUG", s);
            //TODO: process with places
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //PLACES
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MACIEK_DEBUG", "Connection failed");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if( mGoogleMap != null ) {
                    Place place = PlacePicker.getPlace(this, data);

                    markers.add(mGoogleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("TUTAJ " + markers.size())));
                    centerCircle.setCenter(calculateMidPoint());

                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                }
            }
        }
    }

    private LatLng calculateMidPoint() {
        double xSum = 0, ySum = 0, zSum = 0;
        double xAvg, yAvg, zAvg;

        for( Marker marker : markers ) {
            double latRad = marker.getPosition().latitude * Math.PI / 180;
            double lonRad = marker.getPosition().longitude * Math.PI / 180;

            xSum += Math.cos(latRad) * Math.cos(lonRad);//x
            ySum += Math.cos(latRad) * Math.sin(lonRad);               //y
            zSum += Math.sin(latRad);                                //z
        }
        int size = markers.size();
        xAvg = xSum/size;
        yAvg = ySum/size;
        zAvg = zSum/size;

        double midLat = Math.atan2(zAvg, Math.sqrt(Math.pow(xAvg, 2) + Math.pow(yAvg, 2)));
        double midLon = Math.atan2(yAvg, xAvg);

        double midLatDeg = midLat * 180 / Math.PI;
        double midLonDeg = midLon * 180 / Math.PI;

        Log.d("MACIEK_DEBUG", "center: lat: " + midLatDeg + ", lon: " + midLonDeg);

        return new LatLng(midLatDeg, midLonDeg);
    }

    //MAPS
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MACIEK-DEBUG", "Map ready!");
        mGoogleMap = googleMap;
        LatLng here = new LatLng(52.155922, 21.036642);
        markers.add(mGoogleMap.addMarker(new MarkerOptions().position(here).title("DUDY")));
        centerCircle = mGoogleMap.addCircle(new CircleOptions().center(here).radius(10));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16));
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(markers.contains(marker)) {
                    Log.d("MACIEK-DEBUG", marker.getTitle());
                    markers.remove(marker);
                    marker.remove();
                }
                return false;
            }
        });
    }
}
