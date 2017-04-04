package com.brgk.placetomeet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    //TEMP
    int PLACE_PICKER_REQUEST = 1;

    MapActivity activity = this;
    GoogleMap mGoogleMap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String[] strings = bundle.getStringArray(Constants.EXTRA_CHECKED_PLACES);
        for( String s :  strings ) {
            Log.d("MACIEK_DEBUG", s);
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
                Place place = PlacePicker.getPlace(this, data);
                mGoogleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("TUTAJ!"));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }
        }
    }

    //MAPS
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MACIEK-DEBUG", "Map ready!");
        mGoogleMap = googleMap;
        LatLng here = new LatLng(52.754255, 21.892705);
        mGoogleMap.addMarker(new MarkerOptions().position(here).title("DUDY"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(here));
    }
}
