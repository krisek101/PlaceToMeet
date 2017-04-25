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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    //Collections
    List<PersonElement> persons = new ArrayList<>();
    List<CategoryElement> categories = new ArrayList<>();
    List<String> checkedCategories = new ArrayList<>();
    List<PlaceElement> places = new ArrayList<>();

    //UI
    ActionBar mActionBar;
    boolean isAddingPerson = false;

    //Right slider
    RelativeLayout rightSlider;
    View rightHandle;
    float rightSliderDefaultX, rightHandleDefaultX;
    float rightSliderWidth, rightHandleWidth;
    float rightTotalWidth;
    boolean rightSliderOpened = false;
    PersonAdapter personAdapter;
    ListView personList;

    //Left slider
    RelativeLayout leftSlider;
    View leftHandle;
    float leftSliderDefaultX, leftHandleDefaultX;
    float leftSliderWidth, leftHandleWidth;
    float leftTotalWidth;
    float screenWidth;
    float screenHeight;
    boolean leftSliderOpened = false;
    CategoryAdapter categoryAdapter;
    ListView categoryList;

    //Footer
    TextView footer;
    boolean footerOpened = false;
    float navigationBarHeight;
    float statusBarHeight;
    float footerTop;
    LinearLayout footerSlider;
    ListView placesList;
    PlaceAdapter placeAdapter;
    boolean footerChanged = true;

    //Map
    MapActivity activity = this;
    GoogleMap mGoogleMap = null;
    Circle centerCircle;
    GestureDetector gestureDetector;
    LocationRequest locationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng center;
    LatLng userLocation;
    boolean updateLocation = true;
    String url;
    JsonObjectRequest jsonObjReq;
    RequestQueue queue;
    TextView internetInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        footer = (TextView) findViewById(R.id.footer);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        internetInfoTextView = (TextView) findViewById(R.id.internet_info);

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

        setupActionBar();

        checkUsersSettingGPS();
    }

    // Design functions
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
        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( isAddingPerson ) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));

                } else {
                    ((FloatingActionButton) view).setImageResource(R.drawable.ic_my_location_white_24dp);
                    showActionBar();
                    isAddingPerson = true;
                }
            }
        });

        //left slider listener
        leftHandle.setOnTouchListener(new View.OnTouchListener() {
            float x;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;

                if (!rightSliderOpened) {
                    if (gestureDetector.onTouchEvent(motionEvent)) {
                        if (leftSliderOpened) {
                            toX = leftHandleDefaultX;
                            leftSliderOpened = false;
                        } else {
                            toX = leftSliderWidth;
                            leftSliderOpened = true;
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
                                if (leftSliderOpened) {
                                    if ((x + motionEvent.getRawX()) < 0.9 * leftTotalWidth) {
                                        toX = leftHandleDefaultX;
                                        leftSliderOpened = false;
                                    } else {
                                        toX = leftSliderWidth;
                                        leftSliderOpened = true;
                                    }
                                } else {
                                    if ((x + motionEvent.getRawX()) > 0.1 * leftTotalWidth) {
                                        toX = leftSliderWidth;
                                        leftSliderOpened = true;
                                    } else {
                                        toX = leftHandleDefaultX;
                                        leftSliderOpened = false;
                                    }
                                }
                                view.animate().x(toX).setDuration(100).start();
                                leftSlider.animate().x(toX - leftSliderWidth).setDuration(100).start();
                                break;
                            default:
                                return false;
                        }
                    }
                }
                return true;
            }
        });

        //right slider listener
        rightHandle.setOnTouchListener(new View.OnTouchListener() {
            float x;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;

                if (!leftSliderOpened) {
                    if (gestureDetector.onTouchEvent(motionEvent)) {
                        if (rightSliderOpened) {
                            toX = rightHandleDefaultX;
                            rightSliderOpened = false;
                        } else {
                            toX = screenWidth - rightTotalWidth;
                            rightSliderOpened = true;
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
                                if (rightSliderOpened) {
                                    if ((x + motionEvent.getRawX()) > (screenWidth - 0.9 * (rightTotalWidth))) {
                                        toX = rightHandleDefaultX;
                                        rightSliderOpened = false;
                                    } else {
                                        toX = screenWidth - rightTotalWidth;
                                        rightSliderOpened = true;
                                    }
                                } else {
                                    if ((x + motionEvent.getRawX()) < (screenWidth - 0.1 * (rightTotalWidth))) {
                                        toX = screenWidth - rightTotalWidth;
                                        rightSliderOpened = true;
                                    } else {
                                        toX = rightTotalWidth;
                                        rightSliderOpened = false;
                                    }
                                }
                                view.animate().x(toX).setDuration(100).start();
                                rightSlider.animate().x(toX + rightHandleWidth).setDuration(100).start();
                                break;
                            default:
                                return false;
                        }
                    }
                }
                return true;
            }
        });

        //footer listener
        footer.setOnTouchListener(new View.OnTouchListener() {
            float x;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toY;
                closeBothSliders();

                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (footerOpened) {
                        toY = getPixelsFromDp(512);
                        footerOpened = false;
                    } else {
                        toY = footerTop + footer.getHeight();
                        footerOpened = true;
                    }
                    footerSlider.animate().y(toY).setDuration(100).start();
                    view.animate().y(toY - footer.getHeight()).setDuration(100).start();
                    return true;
                } else {
//                    switch (motionEvent.getActionMasked()) {
//                        case MotionEvent.ACTION_DOWN:
//                            x = view.getX() - motionEvent.getRawX();
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            if ((x + motionEvent.getRawX()) < (screenWidth - (rightTotalWidth))) {
//                                toX = screenWidth - (rightTotalWidth);
//                            } else {
//                                toX = x + motionEvent.getRawX();
//                            }
//                            view.animate().x(toX).setDuration(0).start();
//                            rightSlider.animate().x(toX + rightHandleWidth).setDuration(0).start();
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            if (rightSliderOpened) {
//                                if ((x + motionEvent.getRawX()) > (screenWidth - 0.9 * (rightTotalWidth))) {
//                                    toX = rightHandleDefaultX;
//                                    rightSliderOpened = false;
//                                } else {
//                                    toX = screenWidth - rightTotalWidth;
//                                    rightSliderOpened = true;
//                                }
//                            } else {
//                                if ((x + motionEvent.getRawX()) < (screenWidth - 0.1 * (rightTotalWidth))) {
//                                    toX = screenWidth - rightTotalWidth;
//                                    rightSliderOpened = true;
//                                } else {
//                                    toX = rightTotalWidth;
//                                    rightSliderOpened = false;
//                                }
//                            }
//                            view.animate().x(toX).setDuration(100).start();
//                            rightSlider.animate().x(toX + rightHandleWidth).setDuration(100).start();
//                            break;
//                        default:
//                            return false;
//                    }
                }
                return true;
            }
        });
    }

    void arrangeSliders() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        // navigation bar height
        int resourceId;
        resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        //right panel
        rightHandleWidth = getPixelsFromDp(30);
        rightSliderWidth = getPixelsFromDp(250);
        rightTotalWidth = rightSliderWidth + rightHandleWidth;
        rightSliderDefaultX = screenWidth; // panel width
        rightHandleDefaultX = screenWidth - rightHandleWidth;
        rightHandle = findViewById(R.id.right_handle);
        rightSlider = (RelativeLayout) findViewById(R.id.right_container);
        rightSlider.setX(rightSliderDefaultX);
        rightHandle.setX(rightHandleDefaultX);

        //adapter for right slider
        personList = (ListView) findViewById(R.id.right_slider_persons);
        personAdapter = new PersonAdapter(this, R.layout.right_slider_item, persons, this);
        personList.setAdapter(personAdapter);

        //left slider
        leftHandleWidth = getPixelsFromDp(30);
        leftSliderWidth = getPixelsFromDp(250);
        leftTotalWidth = leftSliderWidth + leftHandleWidth;
        leftSliderDefaultX = getPixelsFromDp(-leftSliderWidth);
        leftHandleDefaultX = getPixelsFromDp(leftHandleDefaultX);
        leftHandle = findViewById(R.id.left_handle);
        leftSlider = (RelativeLayout) findViewById(R.id.left_container);
        leftHandle.setX(leftHandleDefaultX);
        leftSlider.setX(leftSliderDefaultX);

        // footer
        footerTop = footer.getY();
        footerSlider = (LinearLayout) findViewById(R.id.footer_slider);
        footerSlider.setY(getPixelsFromDp(512));
    }

    void setupActionBar() {
        mActionBar = getSupportActionBar();
        View view = LayoutInflater.from(this).inflate(R.layout.action_bar, null);
        mActionBar.setCustomView(view);

        final EditText field = (EditText) view.findViewById(R.id.action_bar_address_field);
        view.findViewById(R.id.action_bar_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO dodawanie osoby
                Log.d("MACIEK_DEBUG", field.getText().toString());

                isAddingPerson = false;
                hideActionBar();
            }
        });

        view.findViewById(R.id.action_bar_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddingPerson = false;
                hideActionBar();
            }
        });
    }

    void showActionBar() {
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.ic_my_location_white_24dp);
        View view = mActionBar.getCustomView();
        EditText addressField = (EditText) view.findViewById(R.id.action_bar_address_field);
        addressField.setText("");
        addressField.requestFocus();
        Log.d("MACIEK_DEBUG", "focused: " + addressField.hasFocus());
        showKeyboard();
        view.setAlpha(0);
        mActionBar.setDisplayShowCustomEnabled(true);
        //TODO dopracowac animacje
        view.animate().alpha(1).setDuration(1000).start();

    }

    void hideActionBar() {
        hideKeyboard();
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.ic_add_white_24dp);
        mActionBar.setDisplayShowCustomEnabled(false);
    }

    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 0);
    }

    // Places
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.PLACE_PICKER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (mGoogleMap != null) {
                        Place place = PlacePicker.getPlace(this, data);
                        PersonElement r = new PersonElement(place.getAddress().toString(),
                                persons.size() + 1,
                                mGoogleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("TUTAJ " + persons.size())));
                        persons.add(r);
                        personAdapter.notifyDataSetChanged();
                        updateMapElements();
                    }
                }
                break;
            case Constants.REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    // The user picked a contact.
                    // The Intent's data Uri identifies which contact was selected.
                    Log.w("resultCode == OK", "success");
                    getLocation();
                    // Do something with the contact here (bigger example below)
                }
                break;
            default:
                Log.d("MACIEK_DEBUG", "Not supported Activity result code");
                break;
        }
    }

    void updateMapElements() {
        calculateMidPoint();
        if (centerCircle != null) {
            centerCircle.setCenter(center);
        } else {
            centerCircle = mGoogleMap.addCircle(new CircleOptions().radius(10).center(center));
        }
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 16));
    }

    private void calculateMidPoint() {
        double xSum = 0, ySum = 0, zSum = 0;
        double xAvg, yAvg, zAvg;

        for (PersonElement r : persons) {
            Marker marker = r.getMarker();
            double latRad = marker.getPosition().latitude * Math.PI / 180;
            double lonRad = marker.getPosition().longitude * Math.PI / 180;

            xSum += Math.cos(latRad) * Math.cos(lonRad);//x
            ySum += Math.cos(latRad) * Math.sin(lonRad);               //y
            zSum += Math.sin(latRad);                                //z
        }
        int size = persons.size();
        xAvg = xSum / size;
        yAvg = ySum / size;
        zAvg = zSum / size;

        double midLat = Math.atan2(zAvg, Math.sqrt(Math.pow(xAvg, 2) + Math.pow(yAvg, 2)));
        double midLon = Math.atan2(yAvg, xAvg);

        double midLatDeg = midLat * 180 / Math.PI;
        double midLonDeg = midLon * 180 / Math.PI;

        Log.d("MACIEK_DEBUG", "center: lat: " + midLatDeg + ", lon: " + midLonDeg);

        center = new LatLng(midLatDeg, midLonDeg);
    }

    private void setPlaces() {
        for (int i = 0; i < Constants.PLACES.length; i++) {
            if (i < 5) {
                categories.add(new CategoryElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[1]}));
            } else if (i < 8) {
                categories.add(new CategoryElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[0]}));
            } else if (i < 11) {
                categories.add(new CategoryElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[3]}));
            } else if (i < 14) {
                categories.add(new CategoryElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[4]}));
            } else {
                categories.add(new CategoryElement(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[2]}));
            }
        }

        categoryList = (ListView) findViewById(R.id.list_places);
        categoryAdapter = new CategoryAdapter(this, R.layout.left_slider_item, categories, this);
        categoryList.setAdapter(categoryAdapter);
    }

    // Maps
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MACIEK-DEBUG", "Map ready!");
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (int i = 0; i < persons.size(); i++) {
                    PersonElement r = persons.get(i);
                    if (r.getMarker().equals(marker)) {
                        marker.remove();
                        persons.remove(r);
                        personAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                return false;
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (PlaceElement p : places) {
                    p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker());
                    p.setChecked(false);
                    placeAdapter.notifyDataSetChanged();
                }
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                tickPlaceOnList(marker);
                float toY = footerTop + footer.getHeight();
                footerOpened = true;
                footerSlider.animate().y(toY).setDuration(100).start();
                footer.animate().y(toY - footer.getHeight()).setDuration(100).start();
            }
        });

        final Handler h = new Handler();
        final int delay = 1000; //milliseconds
        final Runnable[] runnable = new Runnable[1];

        h.postDelayed(new Runnable(){
            public void run(){
                //do something
                if(isOnline()){
                    internetInfoTextView.setVisibility(View.INVISIBLE);
                }else{
                    internetInfoTextView.setVisibility(View.VISIBLE);
                    h.removeCallbacks(runnable[0]);
                }
                runnable[0] = this;

                h.postDelayed(runnable[0], delay);
            }
        }, delay);


        if(!PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "").isEmpty() && !PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "").isEmpty()){
            center = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "No Latitude Value Stored")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "No Longitude Value Stored")));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13));
        }
    }

    public void updatePlaces(String category) throws JSONException {
        if (center != null) {
            if (checkedCategories != null) {
                if (checkedCategories.contains(category)) {
                    // add places from one category
                    url = getPlaceUrl(category.toLowerCase(), center);
                    setJsonArray(url, category);
                } else {
                    // delete places from one category
                    deletePlaces(category);
                }
            }
        }
    }

    public void setJsonArray(String link, final String category) {
        queue = Volley.newRequestQueue(this);
        Log.v("LINK", link);
        jsonObjReq = new JsonObjectRequest(Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                double lat, lng;
                String name;
                LatLng position;
                try {
                    JSONArray ja = response.getJSONArray("results");
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject c = ja.getJSONObject(i);
                        name = c.getString("name");
                        lat = c.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        lng = c.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        position = new LatLng(lat, lng);
                        PlaceElement p = new PlaceElement(c, category);
                        if (!places.contains(p)) {
                            p.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(position).title(name)));
                            places.add(p);
                        }
                    }
                    updateList(places);
                } catch (Exception e) {
                    Log.v("Error", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        jsonObjReq.setTag(Constants.TAG);
        queue.add(jsonObjReq);
    }

    public void updateList(List<PlaceElement> places) {
        if (places.size() > 0) {
            if (footerChanged) {
                leftSlider.getLayoutParams().height = leftSlider.getMeasuredHeight() - footer.getHeight();
                rightSlider.getLayoutParams().height = rightSlider.getMeasuredHeight() - footer.getHeight();
                footerChanged = false;
            }
            footer.setVisibility(View.VISIBLE);
            footer.setText("Liczba znalezionych miejsc: " + places.size());
            placesList = (ListView) findViewById(R.id.list_found_places);
            placeAdapter = new PlaceAdapter(this, R.layout.footer_slider_item, places, this);
            placesList.setAdapter(placeAdapter);
        } else {
            footerChanged = true;
            leftSlider.setLayoutParams(new RelativeLayout.LayoutParams(getPixelsFromDp(250), RelativeLayout.LayoutParams.MATCH_PARENT));
            rightSlider.setLayoutParams(new RelativeLayout.LayoutParams(getPixelsFromDp(250), RelativeLayout.LayoutParams.MATCH_PARENT));
            footer.setVisibility(View.INVISIBLE);
        }
    }

    public void deletePlaces(String category) throws JSONException {
        if (queue != null) {
            queue.cancelAll(Constants.TAG);
        }
        Iterator<PlaceElement> i = places.iterator();
        while (i.hasNext()) {
            PlaceElement s = i.next();
            if (s.getCategory().equals(category)) {
                s.getMarker().remove();
                i.remove();
            }
        }
        updateList(places);
    }

    public String getPlaceUrl(String keyword, LatLng location) {
        // EXAMPLE: https://maps.googleapis.com/maps/api/place/nearbysearch/json?keyword=restauracja&language=pl&location=51.23324223,20.32554332&radius=500&key=KEY
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json");
        urlString.append("?keyword=");
        try {
            urlString.append(URLEncoder.encode(keyword, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&language=pl&location=" + location.latitude + "," + location.longitude + "&radius=" + Constants.RADIUS);
        urlString.append("&key=" + Constants.API_KEY);
        return urlString.toString();
    }

    public void highlightMarker(PlaceElement place) {
        if(place.isChecked()) {
            place.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }else{
            place.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker());
        }
    }

    public void tickPlaceOnList(Marker marker){
        for(PlaceElement p : places){
            if(p.getMarker().equals(marker)){
                p.setChecked(true);
                highlightMarker(p);
                placeAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    // Location
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
            public void onResult(@NonNull LocationSettingsResult result2) {
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

    // Connection Callbacks
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
        Log.v("Connection", "suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS_CODE);
        } else {
            if (isOnline() && updateLocation) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    // show getMyLocation button
                    mGoogleMap.setMyLocationEnabled(true);

                    // set lat and lng
                    double latitude = mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();
                    userLocation = new LatLng(latitude, longitude);
                    center = userLocation;
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Latitude", String.valueOf(latitude)).apply();
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Longitude", String.valueOf(longitude)).apply();

                    // move camera to user's location
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
                    updateLocation = false;
                    mGoogleApiClient.disconnect();
                    Log.v("LOCATION CHANGED", "LAT:" + latitude + ", LNG:" + longitude);
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MACIEK_DEBUG", "Connection failed");
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

    // Other
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    int getPixelsFromDp(float sizeDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (sizeDp * scale + 0.5f);
    }

    void closeBothSliders() {
        if (leftSliderOpened || rightSliderOpened) {
            rightSlider.animate().x(rightHandleDefaultX + rightHandleWidth).setDuration(100).start();
            leftSlider.animate().x(leftHandleDefaultX - leftSliderWidth).setDuration(100).start();
            rightHandle.animate().x(rightHandleDefaultX).setDuration(100).start();
            leftHandle.animate().x(leftHandleDefaultX).setDuration(100).start();
            leftSliderOpened = false;
            rightSliderOpened = false;
        }
    }

private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }

}
}