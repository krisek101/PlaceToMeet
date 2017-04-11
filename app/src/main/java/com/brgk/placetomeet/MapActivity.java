package com.brgk.placetomeet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
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

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,  LocationListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

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
    GestureDetector gestureDetector;
    LocationRequest locationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    double latitude = 52.155922, longitude = 21.036642;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        // location
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        } else {
            Log.v("OPS", "checkPlayServices error");
        }

        requestPermissions();

        arrangeSliders();
        setPlaces();

        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);

        setListeners();
        checkUsersSettingGPS();

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
            boolean clicked = true;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (clicked) {
                        toX = screenWidth - (rightTotalWidth);
                        clicked = false;
                    } else {
                        toX = rightHandleDefaultX;
                        clicked = true;
                    }
                    view.animate().x(toX).setDuration(100).start();
                    rightSlider.animate().x(toX + rightHandleWidth).setDuration(100).start();
                    return true;
                } else {
                    switch (motionEvent.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            x = view.getX() - motionEvent.getRawX();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if ((x + motionEvent.getRawX()) < (screenWidth - (rightTotalWidth))) {
                                toX = screenWidth - (rightTotalWidth);
                            } else {
                                toX = x + motionEvent.getRawX();
                            }
                            view.animate().x(toX).setDuration(0).start();
                            rightSlider.animate().x(toX + rightHandleWidth).setDuration(0).start();
                            break;
                        case MotionEvent.ACTION_UP:
                            if ((x + motionEvent.getRawX()) < (screenWidth - 0.5 * (rightTotalWidth))) {
                                toX = screenWidth - (rightTotalWidth);
                                clicked = false;
                            } else {
                                clicked = true;
                                toX = rightHandleDefaultX;
                            }
                            view.animate().x(toX).setDuration(100).start();
                            rightSlider.animate().x(toX + rightHandleWidth).setDuration(100).start();
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }
        });

        //left slider listener
        leftHandle.setOnTouchListener(new View.OnTouchListener() {
            float x;
            boolean clicked = true;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (clicked) {
                        toX = leftSliderWidth;
                        clicked = false;
                    } else {
                        toX = leftHandleDefaultX;
                        clicked = true;
                    }
                    view.animate().x(toX).setDuration(100).start();
                    leftSlider.animate().x(toX - leftSliderWidth).setDuration(100).start();
                    return true;
                } else {
                    switch (motionEvent.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            x = view.getX() - motionEvent.getRawX();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if ((x + motionEvent.getRawX()) > leftSliderWidth) {
                                toX = leftSliderWidth;
                            } else {
                                toX = x + motionEvent.getRawX();
                            }
                            view.animate().x(toX).setDuration(0).start();
                            leftSlider.animate().x(toX - leftSliderWidth).setDuration(0).start();
                            break;
                        case MotionEvent.ACTION_UP:
                            if ((x + motionEvent.getRawX()) > 0.5 * leftTotalWidth) {
                                toX = leftSliderWidth;
                                clicked = false;
                            } else {
                                toX = leftHandleDefaultX;
                                clicked = true;
                            }
                            view.animate().x(toX).setDuration(100).start();
                            leftSlider.animate().x(toX - leftSliderWidth).setDuration(100).start();
                            break;
                        default:
                            return false;
                    }
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
        if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                Log.w("resultCode == OK", "success");
                getLocation();
                // Do something with the contact here (bigger example below)
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
        LatLng here = new LatLng(latitude, longitude);
        //centerCircle = mGoogleMap.addCircle(new CircleOptions().center(here).radius(10));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13));
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

    public boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, Constants.REQUEST_CHECK_PLAY_SERVICES).show();
            }
            return false;
        }
        return true;
    }
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private void checkUsersSettingGPS() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        //builder.setAlwaysShow(true);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result2) {
                final Status status = result2.getStatus();
                // final LocationSettingsStates result3 = result2.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapActivity.this,
                                    Constants.REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }
    private void getLocation() {
        mGoogleApiClient.disconnect();
        if (mGoogleApiClient != null && isOnline()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS_CODE);
        } else {
            if (isOnline()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.v("Connections suspended", "ERROR");
    }
    @Override
    public void onLocationChanged(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS_CODE);
        } else {
            if (isOnline()) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    // set Location
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    LatLng here = new LatLng(latitude, longitude);
                    centerCircle = mGoogleMap.addCircle(new CircleOptions().center(here).radius(10));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13));
                    mGoogleApiClient.disconnect();
                    Log.v("LOCATION CHANGED", "LAT:"+latitude+", LNG:"+longitude);
                }
            }
        }
    }

    // PERMISSIONS
    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //TODO: Permissions: any dangerous permissions here! :D
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                        //TODO: Show ratinoale
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                    }
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PERMISSIONS_SWITCH:
        switch (requestCode) {
            case Constants.REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d("DEBUG", "Permission: " + permissions[i]);
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d("DEBUG", "Any permission denied!");
                            requestPermissions();
                            break PERMISSIONS_SWITCH;
                        }
                    }
                    Log.d("DEBUG", "All permissions granted!");
                } else {
                    Log.d("DEBUG", "Request cancelled");
                }
                break;
            default:
                Log.d("DEBUG", "unhandled permission");
                break;
        }
    }

    // Activity actions
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null && isOnline()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && isOnline()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}

class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }
}
