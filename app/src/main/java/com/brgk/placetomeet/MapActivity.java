package com.brgk.placetomeet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

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

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    //Collections
    List<RightSliderItem> persons = new ArrayList<>();
    List<PlaceElement> places = new ArrayList<>();
    List<String> checkedPlaces = new ArrayList<>();

    //UI
    //Right slider
    RelativeLayout rightSlider;
    View rightHandle;
    float rightSliderDefaultX, rightHandleDefaultX;
    float rightSliderWidth, rightHandleWidth;
    float rightTotalWidth;

    //Left slider
    RelativeLayout leftSlider;
    View leftHandle;
    float leftSliderDefaultX, leftHandleDefaultX;
    float leftSliderWidth, leftHandleWidth;
    float leftTotalWidth;
    float screenWidth;
    PlaceAdapter placeAdapter;
    ListView placesList;

    //Map
    MapActivity activity = this;
    GoogleMap mGoogleMap = null;
    Circle centerCircle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        arrangeSliders();
        setPlaces();

        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);

        setListeners();
    }

    int getPixelsFromDp(float sizeDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (sizeDp * scale + 0.5f);
    }

    void setListeners() {
        //add another person (and place)
        findViewById(R.id.right_slider_footer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(activity), Constants.PLACE_PICKER_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //right slider listener
        rightHandle.setOnTouchListener(new View.OnTouchListener() {
            float x;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;
                switch( motionEvent.getActionMasked() ) {
                    case MotionEvent.ACTION_DOWN:
                        x = view.getX() - motionEvent.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if( (x + motionEvent.getRawX()) < (screenWidth - (rightTotalWidth))) {
                            toX = screenWidth - (rightTotalWidth);
                        } else {
                            toX = x+motionEvent.getRawX();
                        }
                        view.animate().x(toX).setDuration(0).start();
                        rightSlider.animate().x(toX+rightHandleWidth).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        if( (x + motionEvent.getRawX()) < (screenWidth - 0.5*(rightTotalWidth))) {
                            toX = screenWidth - (rightTotalWidth);
                        } else {
                            toX = rightHandleDefaultX;
                        }
                        view.animate().x(toX).setDuration(100).start();
                        rightSlider.animate().x(toX+rightHandleWidth).setDuration(100).start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        //left slider listener
        leftHandle.setOnTouchListener(new View.OnTouchListener() {
            float x;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;
                switch( motionEvent.getActionMasked() ) {
                    case MotionEvent.ACTION_DOWN:
                        x = view.getX() - motionEvent.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if( (x + motionEvent.getRawX()) > leftSliderWidth) {
                            toX = leftSliderWidth;
                        } else {
                            toX = x+motionEvent.getRawX();
                        }
                        view.animate().x(toX).setDuration(0).start();
                        leftSlider.animate().x(toX-leftSliderWidth).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        if( (x + motionEvent.getRawX()) > 0.5*leftTotalWidth) {
                            toX = leftSliderWidth;
                        } else {
                            toX = leftHandleDefaultX;
                        }
                        view.animate().x(toX).setDuration(100).start();
                        leftSlider.animate().x(toX-leftSliderWidth).setDuration(100).start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

    }

    void arrangeSliders() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;

        //right panel
        rightHandleWidth = getPixelsFromDp(30);
        rightSliderWidth = getPixelsFromDp(250);
        rightTotalWidth = rightSliderWidth+rightHandleWidth;
        rightSliderDefaultX = screenWidth; // panel width
        rightHandleDefaultX = screenWidth-rightHandleWidth;
        rightHandle = findViewById(R.id.right_handle);
        rightSlider = (RelativeLayout) findViewById(R.id.right_container);
        rightSlider.setX(rightSliderDefaultX);
        rightHandle.setX(rightHandleDefaultX);

        //left slider
        leftHandleWidth = getPixelsFromDp(30);
        leftSliderWidth = getPixelsFromDp(250);
        leftTotalWidth = leftSliderWidth+leftHandleWidth;
        leftSliderDefaultX = getPixelsFromDp(-leftSliderWidth);
        leftHandleDefaultX = getPixelsFromDp(leftHandleDefaultX);
        leftHandle = findViewById(R.id.left_handle);
        leftSlider = (RelativeLayout) findViewById(R.id.left_container);
        leftHandle.setX(leftHandleDefaultX);
        leftSlider.setX(leftSliderDefaultX);


    }

    //PLACES
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MACIEK_DEBUG", "Connection failed");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PLACE_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if( mGoogleMap != null ) {
                    Place place = PlacePicker.getPlace(this, data);
                    RightSliderItem r = new RightSliderItem(this);
                    r.setAddress(place.getAddress().toString());
                    r.setNumber(persons.size()+1);
                    r.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("TUTAJ " + persons.size())));
                    ((LinearLayout) findViewById(R.id.right_slider_persons)).addView(r);
                    persons.add(r);

                    updateMapElements();
                }
            }
        }
    }

    void updateMapElements() {
        centerCircle.setCenter(calculateMidPoint());
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 16));
    }

    private LatLng calculateMidPoint() {
        double xSum = 0, ySum = 0, zSum = 0;
        double xAvg, yAvg, zAvg;

        for( RightSliderItem r : persons ) {
            Marker marker = r.getMarker();
            double latRad = marker.getPosition().latitude * Math.PI / 180;
            double lonRad = marker.getPosition().longitude * Math.PI / 180;

            xSum += Math.cos(latRad) * Math.cos(lonRad);//x
            ySum += Math.cos(latRad) * Math.sin(lonRad);               //y
            zSum += Math.sin(latRad);                                //z
        }
        int size = persons.size();
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

    private void setPlaces() {
        for (int i = 0; i < Constants.PLACES.length; i++) {
            if (i < 5) {
                places.add(new PlaceElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[1]}));
            } else if (i < 8) {
                places.add(new PlaceElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[0]}));
            } else if (i < 11) {
                places.add(new PlaceElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[3]}));
            } else if (i < 14) {
                places.add(new PlaceElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[4]}));
            } else {
                places.add(new PlaceElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[2]}));
            }
        }

        placesList = (ListView) findViewById(R.id.list_places);
        placeAdapter = new PlaceAdapter(this, R.layout.left_slider_item, places, this);
        placesList.setAdapter(placeAdapter);
    }

    //MAPS
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MACIEK-DEBUG", "Map ready!");
        mGoogleMap = googleMap;
        LatLng here = new LatLng(52.155922, 21.036642);
        centerCircle = mGoogleMap.addCircle(new CircleOptions().center(here).radius(10));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16));
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for( int i = 0; i < persons.size(); i++ ) {
                    RightSliderItem r = persons.get(i);
                    if( r.getMarker().equals(marker) ) {

                        marker.remove();
                        ((LinearLayout) rightSlider.findViewById(R.id.right_slider_persons)).removeView(persons.get(i));
                        persons.remove(r);
                        break;
                    }
                }
                return false;
            }
        });
    }
}
