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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.brgk.placetomeet.adapters.AutocompleteAdapter;
import com.brgk.placetomeet.contants.ClearableAutoCompleteTextView;
import com.brgk.placetomeet.models.CategoryElement;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.models.ListenerElement;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.PlaceElement;
import com.brgk.placetomeet.R;
import com.brgk.placetomeet.adapters.CategoryAdapter;
import com.brgk.placetomeet.adapters.PersonAdapter;
import com.brgk.placetomeet.adapters.PlaceAdapter;
import com.brgk.placetomeet.models.RequestToQueue;
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
import com.google.android.gms.location.places.Places;
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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    // Collections
    public List<PersonElement> persons = new ArrayList<>();
    public ArrayList<PersonElement> favouritePersons = new ArrayList<>();
    public List<PersonElement> autoCompletePersons = new ArrayList<>();
    public List<PersonElement> lastChosenPersons = new ArrayList<>();
    public List<CategoryElement> categories = new ArrayList<>();
    public List<String> checkedCategories = new ArrayList<>();
    public List<PlaceElement> places = new ArrayList<>();

    // Action Bar
    private ActionBar mActionBar;
    public boolean isAddingPerson = false;
    public ClearableAutoCompleteTextView addressField;

    // Right slider
    public RelativeLayout rightSlider;
    public View rightHandle;
    public float rightHandleDefaultX;
    public float rightHandleWidth;
    public float rightSliderWidth;
    public float rightTotalWidth;
    public boolean rightSliderOpened = false;
    private PersonAdapter personAdapter;
    public PersonElement person = null;
    public PersonElement editPerson = null;
    public LatLng lastPosition;
    public Marker editMarker;
    public AutocompleteAdapter autocompleteAdapter;
    PersonElement user;

    // Left slider
    public RelativeLayout leftSlider;
    public View leftHandle;
    public float leftHandleDefaultX;
    public float leftSliderWidth;
    public float leftTotalWidth;
    public float screenWidth;
    public boolean leftSliderOpened = false;
    CategoryAdapter categoryAdapter;

    // Footer
    public TextView footer;
    public boolean footerOpened = false;
    public float footerTop;
    public LinearLayout footerSlider;
    private PlaceAdapter placeAdapter;
    private boolean footerShowed = false;
    ListView placesList;

    // Map
    private GoogleMap mGoogleMap = null;
    private boolean mapReady = false;
    public Circle centerCircle;
    public Circle centerOfCircle;
    public pl.droidsonroids.gif.GifTextView loading;
    private boolean byMapAdding = true;

    // Location
    private LocationRequest locationRequest;
    private GoogleApiClient mGoogleApiClient;
    public LatLng center;
    public LatLng userLocation;
    private LatLng lastLocation;
    private boolean updateLocation = true;
    private ImageView getMyLocationButton;
    private Marker userLocationMarker;

    //Seek Bar
    private RelativeLayout seekBarContainer;
    private SeekBar radiusSeekBar;
    public TextView radiusText;

    // Others
    public RequestQueue queue;
    private TextView internetInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //init
        footer = (TextView) findViewById(R.id.footer);
        internetInfoTextView = (TextView) findViewById(R.id.internet_info);
        radiusSeekBar = (SeekBar) findViewById(R.id.radius_seekbar);
        radiusText = (TextView) findViewById(R.id.radius_text);
        seekBarContainer = (RelativeLayout) findViewById(R.id.seek_bar_container);
        queue = Volley.newRequestQueue(this);
        loading = (pl.droidsonroids.gif.GifTextView) findViewById(R.id.loading);
        getMyLocationButton = (ImageView) findViewById(R.id.getMyLocationButton);

        // location - preparation
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        } else {
            Log.e("Google Play Services", "Unavailable");
        }

        requestPermissions();
        arrangeSliders();
        restoreData();
        setCategories();
        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);
        setupActionBar();
        checkUsersSettingGPS();
        setListeners();
        guide();
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

    public void onFloatingButtonClick() {
        if (isAddingPerson) {
            // on confirmation icon click
            hideKeyboard();
            if (editPerson != null && editMarker != null) {
                editMarker.remove();
                editMarker = null;
                lastPosition = null;
                editPerson.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                personAdapter.notifyDataSetChanged();
                updateMapElements();
                addressField.setText("");
                if (favouritePersons.contains(editPerson)) {
                    saveFav();
                }
                if (!lastChosenPersons.contains(editPerson)) {
                    addLastChosenPerson(editPerson);
                }
                editPerson = null;
            } else if (person != null) {
                boolean exists = false;
                for (PersonElement p : persons) {
                    if (p.getAddress().equals(person.getAddress())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    if (mGoogleMap != null) {
                        if (!person.equals(user)) {
                            person.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        }
                        persons.add(person);
                        personAdapter.notifyDataSetChanged();
                        updateMapElements();
                        addressField.setText("");
                        if (!lastChosenPersons.contains(person)) {
                            addLastChosenPerson(person);
                        }
                        person = null;
                    }
                } else {
                    person.getMarker().remove();
                }
            }
            isAddingPerson = false;
            ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.ic_add_white_24dp);
            hideActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
        } else {
            // on plus icon click
            showActionBar();
            isAddingPerson = true;
            closeBothSliders();
        }
    }

    public void onGetMyLocationButtonClick() {
        setLoading(true);
        updateLocation = true;
        checkUsersSettingGPS();
        if (isAddingPerson && userLocation != null) {
            // adding person
            String address = getAddressFromLatLng(userLocation);
            addressField.setText(address);
            addPerson(address, userLocation);
        }
    }

    void setListeners() {
        ListenerElement showFavouritesButton = new ListenerElement(findViewById(R.id.show_fav), this, "click");
        ListenerElement floatingButtonListener = new ListenerElement(findViewById(R.id.floatingActionButton), this, "click");
        ListenerElement leftHandleListener = new ListenerElement(leftHandle, this, "touch");
        ListenerElement leftSliderListener = new ListenerElement(findViewById(R.id.list_places), this, "touch");
        ListenerElement rightHandleListener = new ListenerElement(rightHandle, this, "touch");
        ListenerElement rightSliderListener = new ListenerElement(findViewById(R.id.right_slider_persons), this, "touch");
        ListenerElement showFavouritesSlideListener = new ListenerElement(findViewById(R.id.show_fav), this, "touch");
        ListenerElement footerListener = new ListenerElement(footer, this, "touch");
        ListenerElement seekBarListener = new ListenerElement(radiusSeekBar, this, "seekBarChange");
        ListenerElement myLocationButton = new ListenerElement(getMyLocationButton, this, "click");
    }

    void addLastChosenPerson(PersonElement personElement) {
        if (lastChosenPersons.size() > 10) {
            lastChosenPersons.remove(0);
        }
        lastChosenPersons.add(personElement);
        saveLastChosen();
    }

    void arrangeSliders() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;

        //right slider
        rightHandleWidth = getPixelsFromDp(30);

        rightSliderWidth = getPixelsFromDp(250);
        rightTotalWidth = rightSliderWidth + rightHandleWidth;
        float rightSliderDefaultX = screenWidth; // panel width
        rightHandleDefaultX = screenWidth - rightHandleWidth;
        rightHandle = findViewById(R.id.right_handle);
        rightSlider = (RelativeLayout) findViewById(R.id.right_container);
        rightSlider.setX(rightSliderDefaultX);
        rightHandle.setX(rightHandleDefaultX);
        rightSlider.setZ(1000);

        //autocompleteAdapter for right slider
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

        addressField = (ClearableAutoCompleteTextView) view.findViewById(R.id.action_bar_address_field);
        addressField.setClearButton(getResources().getDrawable(R.drawable.clear));

        addressField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // delete last added person on map if exists
                if (byMapAdding) {
                    if (person != null) {
                        if (!persons.contains(person)) {
                            person.getMarker().remove();
                            person = null;
                        }
                    }

                    // clear all last results
                    autoCompletePersons.clear();

                    // update list from favourites
                    if (!favouritePersons.isEmpty()) {
                        for (PersonElement favPerson : favouritePersons) {
                            if ((favPerson.getAddress().toLowerCase().contains(s.toString().toLowerCase()) || favPerson.getName().toLowerCase().contains(s.toString().toLowerCase())) && !autoCompletePersons.contains(favPerson)) {
                                favPerson.setId(persons.size() + 1);
                                autoCompletePersons.add(favPerson);
                            }
                        }
                    }

                    // update list from last chosen
                    if (!lastChosenPersons.isEmpty()) {
                        for (PersonElement lastChosen : lastChosenPersons) {
                            if (lastChosen.getAddress().toLowerCase().contains(s.toString().toLowerCase()) && !autoCompletePersons.contains(lastChosen)) {
                                lastChosen.setId(persons.size() + 1);
                                autoCompletePersons.add(lastChosen);
                            }
                        }
                    }

                    // update list from Google
                    if (s.toString().length() >= 2) {
                        if (queue != null) {
                            queue.cancelAll(Constants.TAG_AUTOCOMPLETE);
                        }
                        RequestToQueue autocompleteRequest = new RequestToQueue(Constants.TAG_AUTOCOMPLETE, "", MapActivity.this);
                        autocompleteRequest.setPlaceAutoCompleteUrl(s.toString());
                        autocompleteRequest.doRequest();
                    }
                } else {
                    byMapAdding = true;
                }
            }


            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    void setLoading(boolean load) {
        if (load) {
            loading.setVisibility(View.VISIBLE);
        } else {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    public void showActionBar() {
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.check);
        View view = mActionBar.getCustomView();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        EditText addressField = (EditText) view.findViewById(R.id.action_bar_address_field);
        addressField.setText("");
        addressField.requestFocus();
    }

    void hideActionBar() {
        hideKeyboard();
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setImageResource(R.drawable.ic_add_white_24dp);
        mActionBar.setDisplayShowCustomEnabled(false);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void showFooter() {
        //update height
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
            if (editPerson != null && lastPosition != null) {
                editPerson.getMarker().remove();
                editPerson.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(lastPosition).title(editPerson.getName())));
                editPerson.setPosition(lastPosition);
                editPerson.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                editMarker.remove();
                editMarker = null;
                editPerson = null;
                lastPosition = null;
            } else if (editPerson != null) {
                editPerson.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                editPerson = null;
            }
            isAddingPerson = false;
            hideActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
        } else if (!checkedCategories.isEmpty()) {
            for (CategoryElement ce : categories) {
                try {
                    if (checkedCategories.contains(ce.getName())) {
                        deletePlaces(ce.getName(), false);
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
                    for (int i = 0; i < favouritePersons.size(); i++) {
                        PersonElement p = favouritePersons.get(i);
                        if (persons.contains(p) && !positions.contains(i)) {
                            p.getMarker().remove();
                            persons.remove(p);
                        } else if (!persons.contains(p) && positions.contains(i)) {
                            moveFavToElem(favouritePersons.get(i));
                        }
                    }
                    personAdapter.notifyDataSetChanged();
                }
                break;
            default:
                Log.d("MACIEK_DEBUG", "Not supported Activity result code");
                break;
        }
    }

    public void updateMapElements() {
        if (!persons.isEmpty()) {
            center = calculateMidPoint();
        } else {
            if (userLocation != null) {
                center = userLocation;
            } else {
                center = lastLocation;
            }
        }
        if (centerCircle != null) {
            centerCircle.setCenter(center);
            centerOfCircle.setCenter(center);
        } else {
            centerCircle = mGoogleMap.addCircle(new CircleOptions()
                    .radius(radiusSeekBar.getProgress())
                    .center(center)
                    .strokeColor(Color.parseColor("#00aef4"))
                    .fillColor(0x22146244)
                    .strokeWidth(2));
            centerOfCircle = mGoogleMap.addCircle(new CircleOptions()
                    .radius(10)
                    .center(center)
                    .strokeWidth(0)
                    .fillColor(Color.parseColor("#00aef4")));
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 13));

        //reset checked categories
        if (checkedCategories != null) {
            String c;
            for (int i = 0; i < checkedCategories.size(); i++) {
                c = checkedCategories.get(i);
                try {
                    Log.v("AAAAA", "BBBBB");
                    deletePlaces(c, true);
                    updatePlaces(c);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private LatLng calculateMidPoint() {
        double xSum = 0, ySum = 0, zSum = 0;
        double xAvg, yAvg, zAvg;
        int size = 0;
        for (PersonElement r : persons) {
            Log.d("MACIEK_DEBUG", "displayed: " + r.isDisplayed());
            if (r.isDisplayed()) {
                Marker marker = r.getMarker();
                double latRad = marker.getPosition().latitude * Math.PI / 180;
                double lonRad = marker.getPosition().longitude * Math.PI / 180;

                xSum += Math.cos(latRad) * Math.cos(lonRad);//x
                ySum += Math.cos(latRad) * Math.sin(lonRad);//y
                zSum += Math.sin(latRad);//z

                size++;
            }

        }
//        int size = persons.size();
        xAvg = xSum / size;
        yAvg = ySum / size;
        zAvg = zSum / size;

        double midLat = Math.atan2(zAvg, Math.sqrt(Math.pow(xAvg, 2) + Math.pow(yAvg, 2)));
        double midLon = Math.atan2(yAvg, xAvg);

        double midLatDeg = midLat * 180 / Math.PI;
        double midLonDeg = midLon * 180 / Math.PI;

        Log.d("MACIEK_DEBUG", "center: lat: " + midLatDeg + ", lon: " + midLonDeg);

        return new LatLng(midLatDeg, midLonDeg);
    }

    private void setCategories() {
        ListView categoryList = (ListView) findViewById(R.id.list_places);
        categoryAdapter = new CategoryAdapter(this, R.layout.left_slider_item, categories, this);

        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            categories.add(new CategoryElement(Constants.CATEGORIES[i], i, Constants.IMAGES[i]));
            if (Constants.CATEGORIES[i].equals(Constants.DEFAULT_CATEGORY)) {
                categories.get(i).setChecked(true);
                checkedCategories.add(categories.get(i).getName());
                Log.v("CHECKED CATEGORIES: ", checkedCategories.toString());
                categoryAdapter.notifyDataSetChanged();
                try {
                    updatePlaces(categories.get(i).getName());
                } catch (JSONException e) {
                    Log.v("JSON Eception", e.toString());
                }
            }
        }
        categoryList.setAdapter(categoryAdapter);
    }

    public void addPerson(String address, LatLng position) {
        if (editPerson != null) {
            if (lastPosition == null) {
                lastPosition = editPerson.getPosition();
                editMarker = mGoogleMap.addMarker(new MarkerOptions().position(lastPosition));
                editMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                editMarker.setTitle(editPerson.getName());
            }
            editPerson.getMarker().setPosition(position);
            editPerson.getMarker().setTitle(editPerson.getName());
            editPerson.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            editPerson.setAddress(address);
            editPerson.setPosition(position);
        } else {
            if (person != null) {
                person.getMarker().remove();
            }
            person = new PersonElement(address, mGoogleMap.addMarker(new MarkerOptions().position(position).title("OSOBA " + (persons.size() + 1))));
            person.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }
    }

    public void addPerson(PersonElement p) {
        p.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(p.getPosition()).title(p.getName())));
        p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        person = p;
    }

    // Maps
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MACIEK-DEBUG", "Map ready!");
        mapReady = true;
        mGoogleMap = googleMap;
        if (places != null && !places.isEmpty()) {
            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    for (PlaceElement p : places) {
                        if (p.getMarker() != null) {
                            try {
                                p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                        p.setChecked(false);
                        placeAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // adding person
                if (!isAddingPerson) {
                    showActionBar();
                    isAddingPerson = true;
                }
                String address = getAddressFromLatLng(latLng);
                addPerson(address, latLng);
                byMapAdding = false;
                addressField.setText(address);
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
                            placesList.setSelection(places.indexOf(place));
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

        // get last location
        if (!PreferenceManager.getDefaultSharedPreferences(this).getString("Latitude", "").isEmpty() && !PreferenceManager.getDefaultSharedPreferences(this).getString("Longitude", "").isEmpty()) {
            center = userLocation = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "No Latitude Value Stored")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "No Longitude Value Stored")));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));

            //update last location marker
            userLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(userLocation));
            userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            user = new PersonElement(getAddressFromLatLng(userLocation), "Ja", userLocationMarker);

            // add user
            persons.add(user);
            personAdapter.notifyDataSetChanged();
            updateMapElements();
        } else {
            user = null;
        }
    }

    public String getAddressFromLatLng(LatLng position) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressArray = new ArrayList<>();
        String addressBuilder = "";
        try {
            addressArray = geocoder.getFromLocation(position.latitude, position.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressArray != null && !addressArray.isEmpty()) {
            addressBuilder += addressArray.get(0).getAddressLine(0) + ", ";
            addressBuilder += addressArray.get(0).getLocality();
        } else {
            addressBuilder = "Lokalizacja nieokreślona";
        }
        return addressBuilder;
    }

    public void updatePlaces(String category) throws JSONException {
        if (center != null) {
            if (checkedCategories != null) {
                if (checkedCategories.contains(category)) {
                    if (isOnline()) {
                        // add places from one category
                        setLoading(true);
                        if (centerCircle != null) {
                            center = centerCircle.getCenter();
                        }
                        setJsonArray(category);
                    }
                } else {
                    // delete places from one category
                    deletePlaces(category, false);
                }
            } else {
                if (isOnline()) {
                    setLoading(true);
                    if (centerCircle != null) {
                        center = centerCircle.getCenter();
                    }
                    setJsonArray(category);
                }
            }
        }
    }

    public void setJsonArray(String category) {
        RequestToQueue placesRequest = new RequestToQueue(Constants.TAG_CATEGORY, category, this);
        placesRequest.setCategoryUrl();
        placesRequest.doRequest();
    }

    public void updateList(List<PlaceElement> places) {
        if (places.size() > 0) {
            if (!footerShowed) {
                showFooter();
            }
            List<PlaceElement> placesOnMap = new ArrayList<>();
            for (PlaceElement p : places) {
                if (p.getMarker() != null) {
                    p.getMarker().remove();
                }
                if (p.isChecked()) {
                    highlightMarker(p);
                }
                if (p.getDistanceFromCenter() <= radiusSeekBar.getProgress()) {
                    p.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(p.getPosition()).title(p.getName())));
                    placesOnMap.add(p);
                }
            }

            footer.setText("Liczba znalezionych miejsc: " + placesOnMap.size());
            placesList = (ListView) findViewById(R.id.list_found_places);
            placeAdapter = new PlaceAdapter(this, R.layout.footer_slider_item, placesOnMap, this);
            placesList.setAdapter(placeAdapter);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lp.setMargins((int) screenWidth - getPixelsFromDp(40), getPixelsFromDp(60), getPixelsFromDp(10), 0);
            getMyLocationButton.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpLoading = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lpLoading.setMargins((int) screenWidth - getPixelsFromDp(40), getPixelsFromDp(60), getPixelsFromDp(10), 0);
            loading.setLayoutParams(lpLoading);
        } else {
            hideFooter();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lp.setMargins((int) screenWidth - getPixelsFromDp(40), getPixelsFromDp(10), getPixelsFromDp(10), 0);
            getMyLocationButton.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpLoading = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lpLoading.setMargins((int) screenWidth - getPixelsFromDp(40), getPixelsFromDp(10), getPixelsFromDp(10), 0);
            loading.setLayoutParams(lpLoading);
        }
        setLoading(false);
    }

    public void deletePlaces(String category, boolean reset) throws JSONException {
        if (!reset) {
            if (queue != null) {
                queue.cancelAll(Constants.TAG_CATEGORY);
            }
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
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(position.latitude - center.latitude);
        double lngDiff = Math.toRadians(position.longitude - center.longitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(center.latitude)) * Math.cos(Math.toRadians(position.latitude)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return (float) (distance * meterConversion);
//        float[] results = new float[1];
//        Location.distanceBetween(position.latitude, position.longitude, center.latitude, center.longitude, results);
//        return results[0];
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
            setLoading(true);
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

                    // save last location
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Latitude", String.valueOf(latitude)).apply();
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Longitude", String.valueOf(longitude)).apply();

                    if (user != null && userLocationMarker != null) {
                        userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        if (!user.getPosition().equals(userLocation)) {
                            // on location changed
                            user.getMarker().remove();
                            userLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(userLocation).title("Ja"));
                            userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            user.setMarker(userLocationMarker);
                            user.setPosition(userLocation);
                            user.setAddress(getAddressFromLatLng(userLocation));
                            personAdapter.notifyDataSetChanged();
                            updateMapElements();
                        }
                    } else {
                        // if no location saved in Preferences
                        userLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(userLocation).title("Ja"));
                        userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        user = new PersonElement(getAddressFromLatLng(userLocation), "Ja", userLocationMarker);
                        persons.add(user);
                        personAdapter.notifyDataSetChanged();
                        updateMapElements();
                    }

                    // move camera to user's location
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
                    updateLocation = false;
                    mGoogleApiClient.disconnect();
                    setLoading(false);
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
        if (mGoogleApiClient != null && isOnline() && updateLocation) {
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
        if (mGoogleApiClient != null && isOnline() && updateLocation) {
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

    // Favourites
    public void moveFavToElem(PersonElement fav) {
        if (mapReady) {
            if (!persons.contains(fav)) {
                fav.setMarker(mGoogleMap.addMarker(new MarkerOptions()
                        .position(fav.getPosition())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .title(fav.getName())));
                fav.favourite(true);
                persons.add(fav);
                updateMapElements();
            }
        } else {
            Log.e("MACIEK_DEBUG", "Map not ready!");
        }
    }

    public void showFav() {
        Intent intent = new Intent(this, FavouritesActivity.class);
        Log.d("MACIEK_DEBUG", "currentActivity: " + favouritePersons.toString());
        intent.putParcelableArrayListExtra("Fav", favouritePersons);
        ArrayList<Integer> added = new ArrayList<>();
        for (int i = 0; i < favouritePersons.size(); i++) {
            if (persons.contains(favouritePersons.get(i))) {
                added.add(i);
            }
        }
        intent.putIntegerArrayListExtra("Added", added);
        startActivityForResult(intent, Constants.REQUEST_FAVOURITES);
    }

    public void saveFav() {
        String json = new Gson().toJson(favouritePersons);
        Log.d("MACIEK_DEBUG", json);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("Fav", json).apply();
    }

    // Last Chosen
    private void saveLastChosen() {
        String json = new Gson().toJson(lastChosenPersons);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("LastChosen", json).apply();
    }

    // Other
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public int getPixelsFromDp(float sizeDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (sizeDp * scale + 0.5f);
    }

    public void closeBothSliders() {
        if (leftSliderOpened || rightSliderOpened) {
            rightSlider.animate().x(rightHandleDefaultX + rightHandleWidth).setDuration(100).start();
            leftSlider.animate().x(leftHandleDefaultX - leftSliderWidth).setDuration(100).start();
            rightHandle.animate().x(rightHandleDefaultX).setDuration(100).start();
            leftHandle.animate().x(leftHandleDefaultX).setDuration(100).start();
            leftSliderOpened = false;
            rightSliderOpened = false;
        }
    }

    void restoreData() {
        //favourites
        String json = PreferenceManager.getDefaultSharedPreferences(this).getString("Fav", "none");
        Log.d("MACIEK_DEBUG", json);
        if (!json.equals("none")) {
            favouritePersons = new Gson().fromJson(json, new TypeToken<ArrayList<PersonElement>>() {
            }.getType());
        }

        //last chosen
        json = PreferenceManager.getDefaultSharedPreferences(this).getString("LastChosen", "none");
        if (!json.equals("none")) {
            lastChosenPersons = new Gson().fromJson(json, new TypeToken<ArrayList<PersonElement>>() {
            }.getType());
        }

        //last location
        lastLocation = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "0.00")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "0.00")));
        center = lastLocation;
    }

    public void goToPerson(PersonElement p) {
        if (mapReady) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p.getMarker().getPosition(), 14));
            p.getMarker().showInfoWindow();
        }
    }


}