package com.brgk.placetomeet.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.brgk.placetomeet.models.CategoryElement;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.PlaceElement;
import com.brgk.placetomeet.R;
import com.brgk.placetomeet.adapters.CategoryAdapter;
import com.brgk.placetomeet.adapters.PersonAdapter;
import com.brgk.placetomeet.adapters.PlaceAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    // Collections
    public List<PersonElement> persons = new ArrayList<>();
    public ArrayList<PersonElement> favouritePersons = new ArrayList<>();
    public List<CategoryElement> categories = new ArrayList<>();
    public List<String> checkedCategories = new ArrayList<>();
    public List<PlaceElement> places = new ArrayList<>();
    public List<String> autoCompleteAddresses = new ArrayList<>();
    public List<String> autoCompleteIDs = new ArrayList<>();
    public List<String> autoCompleteIDsCopy;
    public List<PlaceElement> orderedPlaces;

    // Action Bar
    private ActionBar mActionBar;
    private boolean isAddingPerson = false;
    private com.brgk.placetomeet.contants.ClearableAutoCompleteTextView addressField;

    // Right slider
    private RelativeLayout rightSlider;
    private View rightHandle;
    private float rightHandleDefaultX;
    private float rightHandleWidth;
    private float rightTotalWidth;
    private boolean rightSliderOpened = false;
    private PersonAdapter personAdapter;
    private PersonElement person;

    // Left slider
    private RelativeLayout leftSlider;
    private View leftHandle;
    private float leftHandleDefaultX;
    private float leftSliderWidth;
    private float leftTotalWidth;
    private float screenWidth;
    private boolean leftSliderOpened = false;
    CategoryAdapter categoryAdapter;

    // Footer
    private TextView footer;
    private boolean footerOpened = false;
    private float footerTop;
    private LinearLayout footerSlider;
    private PlaceAdapter placeAdapter;
    private boolean footerShowed = false;
    private float screenHeight;
    ListView placesList;

    // Map
    private GoogleMap mGoogleMap = null;
    private boolean mapReady = false;
    private Circle centerCircle;
    private pl.droidsonroids.gif.GifTextView loading;

    // Location
    private LocationRequest locationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng center;
    private LatLng userLocation;
    private boolean updateLocation = true;

    //Seek Bar
    private RelativeLayout seekBarContainer;
    private SeekBar radiusSeekBar;
    private TextView radiusText;

    // Others
    private RequestQueue queue;
    private TextView internetInfoTextView;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //init
        footer = (TextView) findViewById(R.id.footer);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        internetInfoTextView = (TextView) findViewById(R.id.internet_info);
        radiusSeekBar = (SeekBar) findViewById(R.id.radius_seekbar);
        radiusText = (TextView) findViewById(R.id.radius_text);
        seekBarContainer = (RelativeLayout) findViewById(R.id.seek_bar_container);
        queue = Volley.newRequestQueue(this);
        loading = (pl.droidsonroids.gif.GifTextView) findViewById(R.id.loading);

        // location - preparation
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        } else {
            Log.v("Error", "Google Play Services unavailable");
        }

        requestPermissions();
        arrangeSliders();
        setCategories();
        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);
        setListeners();
        setupActionBar();
        checkUsersSettingGPS();
        guide();
        restoreFav();
    }

    // Design functions
    void guide() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "showcase_id");
        sequence.setConfig(config);
        sequence.addSequenceItem((findViewById(R.id.left_handle)),
                "Tutaj wybierasz interesujące Cię miejsca.", "Rozumiem");
        sequence.addSequenceItem((findViewById(R.id.right_handle)),
                "Tutaj znajduje się lista uczestników spotkania.", "Rozumiem");
        sequence.addSequenceItem((findViewById(R.id.floatingActionButton)),
                "Dodawaj nowe osoby za pomocą plusa lub poprzez przytrzymanie palca na mapie.", "Rozumiem");
        sequence.start();
    }

    void setListeners() {
        //show favourites button
        findViewById(R.id.show_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFav();
            }
        });

        //floating button (add person)
        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAddingPerson && userLocation != null) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
                } else {
                    showActionBar();
                    isAddingPerson = true;
                    closeBothSliders();
                }
            }
        });

        //left slider listener
        View.OnTouchListener leftListener = new View.OnTouchListener() {
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
        };
        leftHandle.setOnTouchListener(leftListener);

        //right slider listener
        View.OnTouchListener rightListener = new View.OnTouchListener() {
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
        };
        rightHandle.setOnTouchListener(rightListener);

        //footer listener
        footer.setOnTouchListener(new View.OnTouchListener() {
            float y;

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
//                            y = view.getY() - motionEvent.getRawY();
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            if (footerOpened) {
//                                toY = getPixelsFromDp(512);
//                                footerOpened = false;
//                            } else {
//                                toY = footerTop + footer.getHeight();
//                                footerOpened = true;
//                            }
//                            footerSlider.animate().y(toY).setDuration(100).start();
//                            view.animate().y(toY - footer.getHeight()).setDuration(100).start();
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            if (footerOpened) {
//                                toY = getPixelsFromDp(512);
//                                footerOpened = false;
//                            } else {
//                                toY = footerTop + footer.getHeight();
//                                footerOpened = true;
//                            }
//                            footerSlider.animate().y(toY).setDuration(100).start();
//                            view.animate().y(toY - footer.getHeight()).setDuration(100).start();
//                            break;
//                        default:
//                            return false;
//                    }
                }
                return true;
            }
        });

        //radius SeekBar listener
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                radiusText.setText(progress + "m");
                updateList(places);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void arrangeSliders() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        //right slider
        rightHandleWidth = getPixelsFromDp(30);
        float rightSliderWidth = getPixelsFromDp(250);
        rightTotalWidth = rightSliderWidth + rightHandleWidth;
        float rightSliderDefaultX = screenWidth; // panel width
        rightHandleDefaultX = screenWidth - rightHandleWidth;
        rightHandle = findViewById(R.id.right_handle);
        rightSlider = (RelativeLayout) findViewById(R.id.right_container);
        rightSlider.setX(rightSliderDefaultX);
        rightHandle.setX(rightHandleDefaultX);

        //adapter for right slider
        ListView personList = (ListView) findViewById(R.id.right_slider_persons);
        personAdapter = new PersonAdapter(this, R.layout.right_slider_item, persons, this);
        personList.setAdapter(personAdapter);

        //left slider
        float leftHandleWidth = getPixelsFromDp(30);
        leftSliderWidth = getPixelsFromDp(250);
        leftTotalWidth = leftSliderWidth + leftHandleWidth;
        float leftSliderDefaultX = getPixelsFromDp(-leftSliderWidth);
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

        addressField = (com.brgk.placetomeet.contants.ClearableAutoCompleteTextView) view.findViewById(R.id.action_bar_address_field);
        addressField.setClearButton(getResources().getDrawable(R.drawable.clear));
        addressField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() >= 3) {
                    if (person != null) {
                        if (!persons.contains(person)) {
                            person.getMarker().remove();
                            person = null;
                        }
                    }
                    autoCompleteIDs.clear();
                    autoCompleteAddresses.clear();
                    updateAutoCompleteTextView(s.toString());
                }
                addressField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String placeID = autoCompleteIDsCopy.get(position);
                        String link = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + Constants.API_KEY;
                        JsonObjectRequest placeDetailsRequest = new JsonObjectRequest(Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                boolean exists = false;
                                try {
                                    JSONObject resultObj = response.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
                                    Double latitude = resultObj.getDouble("lat");
                                    Double longitude = resultObj.getDouble("lng");
                                    String address = response.getJSONObject("result").getString("formatted_address");
                                    address = address.replaceAll(", Polska", "");
                                    for(PersonElement p : persons){
                                        if(p.getAddress().equals(address)){
                                            exists = true;
                                        }
                                    }
                                    if(!exists) {
                                        person = new PersonElement(address, persons.size() + 1, mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("OSOBA " + (persons.size()))));
                                        person.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    }else{
                                        person = null;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        placeDetailsRequest.setTag(Constants.TAG_PLACE_DETAILS);
                        queue.add(placeDetailsRequest);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // add person button
        view.findViewById(R.id.action_bar_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleMap != null && person != null) {
                    person.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    persons.add(person);
                    personAdapter.notifyDataSetChanged();
                    updateMapElements();
                    addressField.setText("");
                }
            }
        });
    }

    public void updateAutoCompleteTextView(String place) {
        String link = getPlaceAutoCompleteUrl(place);
        Log.v("LINK", link);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray ja = response.getJSONArray("predictions");
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject c = ja.getJSONObject(i);
                        String description = c.getString("description");
                        description = description.replaceAll(", Polska", "");
                        String place_id = c.getString("place_id");
                        autoCompleteAddresses.add(description);
                        autoCompleteIDs.add(place_id);
                    }
                    autoCompleteIDsCopy = new ArrayList<>(autoCompleteIDs);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, autoCompleteAddresses) {
                        @Override
                        @NonNull
                        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                            View view = super.getView(position,
                                    convertView, parent);
                            TextView text = (TextView) view
                                    .findViewById(android.R.id.text1);
                            text.setTextColor(Color.BLACK);
                            return view;
                        }
                    };
                    addressField.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        jsonObjRequest.setTag(Constants.TAG_AUTOCOMPLETE);
        queue.add(jsonObjRequest);
    }

    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&language=pl&components=country:pl");
        urlString.append("&key=" + Constants.API_KEY);
        return urlString.toString();
    }

    void showActionBar() {
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.ic_my_location_white_24dp);
        View view = mActionBar.getCustomView();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        EditText addressField = (EditText) view.findViewById(R.id.action_bar_address_field);
        addressField.setText("");
        addressField.requestFocus();

        //showKeyboard();

        //TODO dopracowac animacje
        view.animate().setDuration(1000).start();
    }

    void hideActionBar() {
        hideKeyboard();
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.ic_add_white_24dp);
        mActionBar.setDisplayShowCustomEnabled(false);
    }

    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 0);
    }

    void showFooter() {
        //update height
//        leftSlider.getLayoutParams().height = leftSlider.getMeasuredHeight() - footer.getHeight();
//        rightSlider.getLayoutParams().height = rightSlider.getMeasuredHeight() - footer.getHeight();
        findViewById(R.id.sliders).getLayoutParams().height = findViewById(R.id.sliders).getMeasuredHeight() - footer.getHeight();
        //show footer
        footer.setVisibility(View.VISIBLE);
        footerShowed = true;
        //show seekbar
        radiusSeekBar.setProgress(radiusSeekBar.getProgress());
        seekBarContainer.setVisibility(View.VISIBLE);
    }

    void hideFooter() {
        //update height
//        leftSlider.setLayoutParams(new RelativeLayout.LayoutParams(getPixelsFromDp(250), RelativeLayout.LayoutParams.MATCH_PARENT));
//        rightSlider.setLayoutParams(new RelativeLayout.LayoutParams(getPixelsFromDp(250), RelativeLayout.LayoutParams.MATCH_PARENT));
        findViewById(R.id.sliders).setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        //hide footer
        footer.setVisibility(View.INVISIBLE);
        footerShowed = false;
        //hide seekBar
        seekBarContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (person != null) {
                if (!persons.contains(person)) {
                    person.getMarker().remove();
                    person = null;
                }
            }
            isAddingPerson = false;
            hideActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (rightSliderOpened || leftSliderOpened || footerOpened) {
            closeBothSliders();
            if (footerOpened) {
                float toY = getPixelsFromDp(512);
                footerOpened = false;
                footerSlider.animate().y(toY).setDuration(100).start();
                footer.animate().y(toY - footer.getHeight()).setDuration(100).start();
            }
        } else if (isAddingPerson) {
            if (person != null) {
                if (!persons.contains(person)) {
                    person.getMarker().remove();
                    person = null;
                }
            }
            isAddingPerson = false;
            hideActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
        } else if(!checkedCategories.isEmpty()) {
            for(CategoryElement ce: categories){
                try {
                    if(checkedCategories.contains(ce.getName())) {
                        deletePlaces(ce.getName());
                        ce.setChecked(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            checkedCategories.clear();
            categoryAdapter.notifyDataSetChanged();
        } else {
            finish();
        }
    }

    // Places
    @Override
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
            case Constants.REQUEST_FAVOURITES:
                if (resultCode == RESULT_OK) {
                    ArrayList<Integer> positions = data.getIntegerArrayListExtra("positions");
                    Log.d("MACIEK_DEBUG", "map: " + positions.toString());
                    for( int i = 0; i < favouritePersons.size(); i++) {
                        PersonElement p = favouritePersons.get(i);
                        if(persons.contains(p) && !positions.contains(i)) {
                            persons.remove(p);
                        } else if( !persons.contains(p) && positions.contains(i)) {
                            moveFavToElem(favouritePersons.get(i));
                        }

                        Log.d("","");
                    }
                    personAdapter.notifyDataSetChanged();
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
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 13));
        if (persons.isEmpty() && userLocation != null) {
            center = userLocation;
        }
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

    private void setCategories() {
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            if (i < 5) {
                categories.add(new CategoryElement(Constants.CATEGORIES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[1]}));
            } else if (i < 8) {
                categories.add(new CategoryElement(Constants.CATEGORIES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[0]}));
            } else if (i < 11) {
                categories.add(new CategoryElement(Constants.CATEGORIES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[3]}));
            } else if (i < 14) {
                categories.add(new CategoryElement(Constants.CATEGORIES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[4]}));
            } else {
                categories.add(new CategoryElement(Constants.CATEGORIES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[2]}));
            }
        }

        ListView categoryList = (ListView) findViewById(R.id.list_places);
        categoryAdapter = new CategoryAdapter(this, R.layout.left_slider_item, categories, this);
        categoryList.setAdapter(categoryAdapter);
    }

    // Maps
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MACIEK-DEBUG", "Map ready!");
        mapReady = true;
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (int i = 0; i < persons.size(); i++) {
                    PersonElement r = persons.get(i);
                    if (r.getMarker().equals(marker)) {
                        marker.remove();
                        persons.remove(r);
                        updateMapElements();
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
                    if (p.getMarker() != null) {
                        p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker());
                    }
                    p.setChecked(false);
                    placeAdapter.notifyDataSetChanged();
                }
            }
        });

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // adding person
                if (!isAddingPerson) {
                    showActionBar();
                    isAddingPerson = true;
                }
                String address = getAddressFromLatLng(latLng);
                addressField.setText(address);
                person = new PersonElement(address, persons.size() + 1, mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("OSOBA " + (persons.size() + 1))));
                person.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for (PlaceElement place : places) {
                    if (place.getMarker() != null) {
                        if (place.getMarker().equals(marker)) {
                            tickPlaceOnList(marker);
                            float toY = footerTop + footer.getHeight();
                            footerOpened = true;
                            footerSlider.animate().y(toY).setDuration(100).start();
                            footer.animate().y(toY - footer.getHeight()).setDuration(100).start();
                            placesList.setSelection(orderedPlaces.indexOf(place));
                        }
                    }
                }
            }
        });

        // check Internet status changes
        final Handler h = new Handler();
        final int delay = 1000; //milliseconds
        final Runnable[] runnable = new Runnable[1];
        h.postDelayed(new Runnable() {
            public void run() {
                if (isOnline()) {
                    internetInfoTextView.setVisibility(View.INVISIBLE);
                } else {
                    internetInfoTextView.setVisibility(View.VISIBLE);
                    h.removeCallbacks(runnable[0]);
                }
                runnable[0] = this;

                h.postDelayed(runnable[0], delay);
            }
        }, delay);

        // go to last location
        if (!PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "").isEmpty() && !PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "").isEmpty()) {
            center = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "No Latitude Value Stored")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "No Longitude Value Stored")));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13));
        }
    }

    private String getAddressFromLatLng(LatLng position) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressArray = new ArrayList<>();
        String addressBuilder = "";
        try {
            addressArray = geocoder.getFromLocation(position.latitude, position.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressArray != null) {
            addressBuilder += addressArray.get(0).getAddressLine(0) + ", ";
            addressBuilder += addressArray.get(0).getLocality();
        }
        return addressBuilder;
    }

    public void updatePlaces(String category) throws JSONException {
        if (center != null) {
            if (checkedCategories != null) {
                if (checkedCategories.contains(category)) {
                    if (isOnline()) {
                        // add places from one category
                        loading.setVisibility(View.VISIBLE);
                        String url = getPlaceUrl(category.toLowerCase(), center);
                        setJsonArray(url, category);
                    }
                } else {
                    // delete places from one category
                    deletePlaces(category);
                }
            }
        }
    }

    public void setJsonArray(String link, final String category) {
        Log.v("LINK", link);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                double lat, lng;
                LatLng position;
                try {
                    JSONArray ja = response.getJSONArray("results");
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject c = ja.getJSONObject(i);
                        lat = c.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        lng = c.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        position = new LatLng(lat, lng);
                        PlaceElement p = new PlaceElement(c, category, getDistanceFromCenter(position));
                        if (!places.contains(p)) {
                            places.add(p);
                        }
                    }
                    updateList(places);
                    loading.setVisibility(View.INVISIBLE);
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
            if (!footerShowed) {
                showFooter();
            }
            int counter = 0;
            orderedPlaces = new ArrayList<>();
            for (PlaceElement p : places) {
                if (p.getMarker() != null) {
                    p.getMarker().remove();
                }
                if (p.getDistanceFromCenter() <= radiusSeekBar.getProgress()) {
                    counter++;
                    p.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(p.getPosition()).title(p.getName())));
                    orderedPlaces.add(0, p);
                } else {
                    orderedPlaces.add(p);
                }
                if (p.isChecked()) {
                    highlightMarker(p);
                }
            }
            footer.setText("Liczba zaznaczonych miejsc: " + counter + "/" + places.size());
            placesList = (ListView) findViewById(R.id.list_found_places);
            placeAdapter = new PlaceAdapter(this, R.layout.footer_slider_item, orderedPlaces, this);
            placesList.setAdapter(placeAdapter);
        } else {
            hideFooter();
        }
        loading.setVisibility(View.INVISIBLE);
    }

    public void deletePlaces(String category) throws JSONException {
        if (queue != null) {
            queue.cancelAll(Constants.TAG);
        }
        Iterator<PlaceElement> i = places.iterator();
        while (i.hasNext()) {
            PlaceElement s = i.next();
            if (s.getCategory().equals(category)) {
                if (s.getMarker() != null) {
                    s.getMarker().remove();
                }
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
        if (place.getMarker() != null) {
            if (place.isChecked()) {
                try {
                    place.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                place.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker());
            }
        }
    }

    public void tickPlaceOnList(Marker marker) {
        for (PlaceElement p : places) {
            if (p.getMarker() != null) {
                if (p.getMarker().equals(marker)) {
                    p.setChecked(true);
                    highlightMarker(p);
                    placeAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public float getDistanceFromCenter(LatLng position) {
        float[] results = new float[1];
        Location.distanceBetween(position.latitude, position.longitude, center.latitude, center.longitude, results);
        return results[0];
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
            if (isOnline() && updateLocation && mGoogleMap != null) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {

                    // set lat and lng
                    double latitude = mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();
                    userLocation = new LatLng(latitude, longitude);
                    center = userLocation;
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Latitude", String.valueOf(latitude)).apply();
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Longitude", String.valueOf(longitude)).apply();
                    mGoogleMap.addMarker(new MarkerOptions().position(center)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

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
                    checkUsersSettingGPS();
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

    public void saveFav() {
        String json = new Gson().toJson(favouritePersons);
        Log.d("MACIEK_DEBUG", json);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("Fav", json).apply();
    }

    void restoreFav() {
        String json = PreferenceManager.getDefaultSharedPreferences(this).getString("Fav", "none");
        Log.d("MACIEK_DEBUG", json);
        favouritePersons = new Gson().fromJson(json, new TypeToken<ArrayList<PersonElement>>(){}.getType());
    }

    public void moveFavToElem(PersonElement fav) {
        if( mapReady ) {
            if( !persons.contains(fav) ) {
                fav.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(fav.getPosition())));
                fav.setNumber(persons.size()+1);
                fav.favourite(true);
                persons.add(fav);
            }
        } else {
            Log.e("MACIEK_DEBUG", "Map not ready!");
        }
    }

    void showFav() {
        Intent intent = new Intent(this, FavouritesActivity.class);
        Log.d("MACIEK_DEBUG", "currentActivity: " + favouritePersons.toString());
        intent.putParcelableArrayListExtra("Fav", favouritePersons);
        ArrayList<Integer> added = new ArrayList<>();
        for( int i = 0; i < persons.size(); i++ ) {
            if( persons.get(i).isFavourite() ) {
                added.add(i);
            }
        }
        intent.putIntegerArrayListExtra("Added", added);
        startActivityForResult(intent, Constants.REQUEST_FAVOURITES);
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

    }
}