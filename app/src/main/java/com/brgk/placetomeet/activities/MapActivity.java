package com.brgk.placetomeet.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.brgk.placetomeet.R;
import com.brgk.placetomeet.adapters.AutocompleteAdapter;
import com.brgk.placetomeet.adapters.CategoryAdapter;
import com.brgk.placetomeet.adapters.MarkerInfoWindowAdapter;
import com.brgk.placetomeet.adapters.OpeningHoursAdapter;
import com.brgk.placetomeet.adapters.PersonAdapter;
import com.brgk.placetomeet.adapters.PlaceAdapter;
import com.brgk.placetomeet.adapters.ReviewsAdapter;
import com.brgk.placetomeet.contants.ClearableAutoCompleteTextView;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.contants.UsefulFunctions;
import com.brgk.placetomeet.models.CategoryElement;
import com.brgk.placetomeet.models.LoaderHelper;
import com.brgk.placetomeet.tasks.DownloadImageTask;
import com.brgk.placetomeet.models.ListenerHelper;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.PlaceElement;
import com.brgk.placetomeet.models.RequestToQueue;
import com.brgk.placetomeet.tasks.GeocoderTask;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

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

    public int screenWidth;
    public int screenHeight;

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
    private ToggleButton rankByButton;

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
    public LoaderHelper loaderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        preLocation();
        Handler mainHandler = new Handler(this.getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                initUI();
                setLoaders();
                requestPermissions();
                restoreData();
                arrangeSliders();
                setListeners();
                setCategories();
            }
        });


        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);
        setupActionBar();
        checkUsersSettingGPS();
        guide();
    }

    // Design functions
    void preLocation(){
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        } else {
            Log.e("Google Play Services", "Unavailable");
        }
    }

    void initUI(){
        footer = (TextView) findViewById(R.id.footer);
        internetInfoTextView = (TextView) findViewById(R.id.internet_info);
        radiusSeekBar = (SeekBar) findViewById(R.id.radius_seekbar);
        radiusText = (TextView) findViewById(R.id.radius_text);
        seekBarContainer = (RelativeLayout) findViewById(R.id.seek_bar_container);
        queue = Volley.newRequestQueue(this);
        loading = (pl.droidsonroids.gif.GifTextView) findViewById(R.id.loading);
        getMyLocationButton = (ImageView) findViewById(R.id.getMyLocationButton);
        rankByButton = (ToggleButton) findViewById(R.id.rankby_button);
    }

    void guide() {
        final FancyShowCaseView welcome = new FancyShowCaseView.Builder(this)
                .title(getString(R.string.welcome))
                .showOnce(getString(R.string.welcome))
                .build();
        final FancyShowCaseView categories = new FancyShowCaseView.Builder(this)
                .title(getString(R.string.guide_1))
                .focusOn(findViewById(R.id.left_handle))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(90)
                .focusCircleRadiusFactor(1.5)
                .disableFocusAnimation()
                .showOnce(getString(R.string.guide_1))
                .build();
        final FancyShowCaseView people = new FancyShowCaseView.Builder(this)
                .title(getString(R.string.guide_2))
                .focusOn(findViewById(R.id.right_handle))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(90)
                .focusCircleRadiusFactor(1.5)
                .disableFocusAnimation()
                .showOnce(getString(R.string.guide_2))
                .build();
        final FancyShowCaseView plus = new FancyShowCaseView.Builder(this)
                .title(getString(R.string.guide_3))
                .focusOn(findViewById(R.id.floatingActionButton))
                .disableFocusAnimation()
                .showOnce(getString(R.string.guide_3))
                .build();

        new FancyShowCaseQueue()
                .add(welcome)
                .add(categories)
                .add(people)
                .add(plus)
                .show();
    }

    public void setLoaders(){
        loaderHelper = new LoaderHelper(this);
        loaderHelper.setLoader(Constants.LOADER_PLACES);
        loaderHelper.setLoader(Constants.LOADER_PLACE_DETAILS);
        loaderHelper.setLoader(Constants.LOADER_LOCATION);
    }

    public void onFloatingButtonClick() {
        if (isAddingPerson) {
            // on confirmation icon click
            hideKeyboard();
            if (editPerson != null && editMarker != null) {
                editMarker.remove();
                editMarker = null;
                lastPosition = null;
                editPerson.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(UsefulFunctions.buildMarkerIcon(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.default_person))));
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
                    if (p.getPosition().equals(person.getPosition())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    if (mGoogleMap != null) {
                        if (!person.equals(user)) {
                            person.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(UsefulFunctions.buildMarkerIcon(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.default_person))));
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
        if (centerCircle != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 13));
        }
        checkUsersSettingGPS();
    }

    void setListeners() {
        ListenerHelper listenerHelper = new ListenerHelper(this);
        listenerHelper.setListener(findViewById(R.id.show_fav), "click");
        listenerHelper.setListener(findViewById(R.id.floatingActionButton), "click");
        listenerHelper.setListener(leftHandle, "touch");
        listenerHelper.setListener(findViewById(R.id.list_places), "touch");
        listenerHelper.setListener(rightHandle, "touch");
        listenerHelper.setListener(findViewById(R.id.right_slider_persons), "touch");
        listenerHelper.setListener(findViewById(R.id.show_fav), "touch");
        listenerHelper.setListener(footer, "touch");
        listenerHelper.setListener(footerSlider, "touch");
        listenerHelper.setListener(radiusSeekBar, "seekBarChange");
        listenerHelper.setListener(getMyLocationButton, "click");
        listenerHelper.setListener(rankByButton, "click");
        listenerHelper.setListener(rightSlider, "touch");
    }

    void addLastChosenPerson(PersonElement personElement) {
        if (!personElement.getAddress().equals(getString(Constants.UNKNOWN_ADDRESS)) && !lastChosenPersons.contains(personElement)) {
            if (lastChosenPersons.size() > 10) {
                lastChosenPersons.remove(0);
            }
            lastChosenPersons.add(personElement);
            saveLastChosen();
        }
    }

    void arrangeSliders() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

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
        footerSlider.setY(screenHeight - getActionBarHeight() - getStatusBarHeight());
        footerSlider.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, screenHeight - getActionBarHeight() - getStatusBarHeight() - footer.getLayoutParams().height));
    }

    void setupActionBar() {
        mActionBar = getSupportActionBar();
        View view = LayoutInflater.from(this).inflate(R.layout.action_bar, null);
        mActionBar.setCustomView(view);

        addressField = (ClearableAutoCompleteTextView) view.findViewById(R.id.action_bar_address_field);
        addressField.setClearButton(ResourcesCompat.getDrawable(getResources(), R.drawable.clear, null));

        addressField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (byMapAdding) {
                    // delete last added person on map if exists
                    if (person != null) {
                        if (!persons.contains(person)) {
                            person.getMarker().remove();
                            person = null;
                        }
                    }

                    // clear all last results
                    autoCompletePersons.clear();


                    if(s.length()<=3) {
                        // update list from favourites
                        if (!favouritePersons.isEmpty()) {
                            for (PersonElement favPerson : favouritePersons) {
                                if ((favPerson.getAddress().toLowerCase().contains(s.toString().toLowerCase()) || favPerson.getName().toLowerCase().contains(s.toString().toLowerCase())) && !autoCompletePersons.contains(favPerson)) {
                                    favPerson.setId(persons.size() + 1);
                                    autoCompletePersons.add(favPerson);
                                }
                            }
                        }

                        // update list from last selected
                        if (!lastChosenPersons.isEmpty()) {
                            for (PersonElement lastChosen : lastChosenPersons) {
                                if (lastChosen.getAddress() != null) {
                                    if (lastChosen.getAddress().toLowerCase().contains(s.toString().toLowerCase()) && !autoCompletePersons.contains(lastChosen)) {
                                        lastChosen.setId(persons.size() + 1);
                                        autoCompletePersons.add(lastChosen);
                                    }
                                }
                            }
                        }
                    }

                    // update list from Google
                    if (queue != null) {
                        queue.cancelAll(Constants.TAG_AUTOCOMPLETE);
                    }
                    RequestToQueue autocompleteRequest = new RequestToQueue(Constants.TAG_AUTOCOMPLETE, "", MapActivity.this);
                    autocompleteRequest.setPlaceAutoCompleteUrl(s.toString());
                    autocompleteRequest.doRequest();
                } else {
                    byMapAdding = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
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

    public int getActionBarHeight() {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int result = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return result;
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
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

    public void hideFooter() {
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
        switch (item.getItemId()) {
            case android.R.id.home:
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
//                editPerson.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                editPerson.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(UsefulFunctions.buildMarkerIcon(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.default_person))));
                editMarker.remove();
                editMarker = null;
                editPerson = null;
                lastPosition = null;
            } else if (editPerson != null) {
//                editPerson.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                editPerson.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(UsefulFunctions.buildMarkerIcon(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.default_person))));
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
            case Constants.REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    getLocation();
                }
                break;
            case Constants.REQUEST_FAVOURITES:
                if (resultCode == RESULT_OK) {
                    //remove favourites
                    ArrayList<Integer> toBeDeleted = data.getIntegerArrayListExtra("deletions");
                    for (int i = toBeDeleted.size() - 1; i >= 0; i--) {
                        int index = toBeDeleted.get(i);
                        PersonElement p = favouritePersons.get(index);
                        if (p.getMarker() != null) {
                            p.getMarker().remove();
                        }
                        if (persons.contains(p)) {
                            persons.remove(p);
                        }
                        favouritePersons.remove(index);
                    }
                    saveFav();

                    //add selected favourites
                    ArrayList<Integer> positions = data.getIntegerArrayListExtra("positions");
                    for (int i = 0; i < favouritePersons.size(); i++) {
                        PersonElement p = favouritePersons.get(i);
                        if (persons.contains(p) && !positions.contains(i)) {
                            p.getMarker().remove();
                            persons.remove(p);
                        } else if (!persons.contains(p) && positions.contains(i)) {
                            moveFavToElem(favouritePersons.get(i));
                        }
                    }

                    updateMapElements();
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
                    .radius(radiusSeekBar.getProgress() / 30)
                    .center(center)
                    .strokeWidth(0)
                    .fillColor(Color.parseColor("#00aef4")));
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 13));

        resetCheckedCategories();
    }

    private void resetCheckedCategories() {
        if (checkedCategories != null) {
            String c;
            for (int i = 0; i < checkedCategories.size(); i++) {
                c = checkedCategories.get(i);
                try {
                    deletePlaces(c);
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

        if (Double.isNaN(midLatDeg) || Double.isNaN(midLonDeg)) {
            if (userLocation != null) {
                return userLocation;
            } else {
                return lastLocation;
            }
        }
        Log.d("MACIEK_DEBUG", "center: lat: " + midLatDeg + ", lon: " + midLonDeg);

        return new LatLng(midLatDeg, midLonDeg);
    }

    private void setCategories() {
        ListView categoryList = (ListView) findViewById(R.id.list_places);
        categoryAdapter = new CategoryAdapter(this, R.layout.left_slider_item, categories, this);
        String category;
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            category = getString(Constants.CATEGORIES[i]);
            categories.add(new CategoryElement(category, i, Constants.IMAGES[i]));
            if (category.equals(getString(Constants.DEFAULT_CATEGORY))) {
                categories.get(i).setChecked(true);
                checkedCategories.add(categories.get(i).getName());
                Log.v("CHECKED CATEGORIES", checkedCategories.toString());
                categoryAdapter.notifyDataSetChanged();
                try {
                    updatePlaces(categories.get(i).getName());
                } catch (JSONException e) {
                    Log.v("JSON Exception", e.toString());
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
            Marker m = mGoogleMap.addMarker(new MarkerOptions().position(position).title(getString(R.string.person) + (persons.size() + 1)));
            person = new PersonElement(address, m);
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
        mGoogleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // adding person
                if (!isAddingPerson) {
                    showActionBar();
                    isAddingPerson = true;
                }
                byMapAdding = false;
                new GeocoderTask(MapActivity.this, latLng, "addTempPerson").execute();
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (places != null) {
                    for (PlaceElement p : places) {
                        if (p.getMarker() != null) {
                            p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker());
                            p.setChecked(false);
                        }
                    }
                    if (placeAdapter != null) {
                        placeAdapter.notifyDataSetChanged();
                    }
                }
                for (PersonElement person : persons) {
                    person.setDistanceToCurrentPlace(0);
                }
                personAdapter.notifyDataSetChanged();
            }
        });


        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (PersonElement person : persons) {
                    person.setDistanceToCurrentPlace((int) getDistanceFromCenter(marker.getPosition(), person.getPosition()));
                }
                personAdapter.notifyDataSetChanged();
                return false;
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()

        {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (places != null) {
                    for (PlaceElement place : places) {
                        if (place.getMarker() != null) {
                            if (place.getMarker().equals(marker)) {
                                RequestToQueue placeDetailsRequest = new RequestToQueue(Constants.TAG_PLACE_DETAILS, "", MapActivity.this);
                                placeDetailsRequest.setPlaceDetailsUrl(place);
                                placeDetailsRequest.doRequest();
                                break;
                            }
                        }
                    }
                }
            }
        });

        checkInternetChanges();
        getLastSavedLocation();
    }

    public void checkInternetChanges(){
        final Handler h = new Handler();
        final int delay = 2000;
        final Runnable[] runnable = new Runnable[1];
        final boolean[] connected = {true};
        h.postDelayed(new Runnable() {
            public void run() {
                if (isOnline()) {
                    internetInfoTextView.setVisibility(View.INVISIBLE);
                    if (!connected[0]) {
                        resetCheckedCategories();
                        resetPersonsAddresses();
                        connected[0] = true;
                    }
                } else {
                    connected[0] = false;
                    internetInfoTextView.setVisibility(View.VISIBLE);
                    h.removeCallbacks(runnable[0]);
                }
                runnable[0] = this;

                h.postDelayed(runnable[0], delay);
            }
        }, delay);
    }

    public void getLastSavedLocation(){
        if (!PreferenceManager.getDefaultSharedPreferences(this).getString("Latitude", "").isEmpty() && !PreferenceManager.getDefaultSharedPreferences(this).getString("Longitude", "").isEmpty()) {
            center = userLocation = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "No Latitude Value Stored")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "No Longitude Value Stored")));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));

            //update last location marker
            userLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(userLocation));
            userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            new GeocoderTask(MapActivity.this, userLocation, "userLocation").execute();
        } else {
            user = null;
        }
    }

    public void addUser(String address){
        user = new PersonElement(address, getString(R.string.me), userLocationMarker);
        persons.add(user);
        personAdapter.notifyDataSetChanged();
        updateMapElements();
    }

    public void addTempPerson(String address, LatLng position){
        addressField.setText(address);
        addPerson(address, position);
    }

    public void resetPersonsAddresses() {
        if (persons != null) {
            for (PersonElement p : persons) {
                if (p.getAddress().equals(getString(Constants.UNKNOWN_ADDRESS))) {
                    new GeocoderTask(this, p.getPosition(), "resetAddress", p).execute();
                }
            }
        }
        checkUsersSettingGPS();
    }

    public void updatePlaceInfo(final PlaceElement place) {
        final AlertDialog.Builder placeInfoWindow = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.place_details_window, null);
        placeInfoWindow.setView(convertView);

        TextView title = (TextView) convertView.findViewById(R.id.place_details_title);
        RatingBar rating = (RatingBar) convertView.findViewById(R.id.place_details_rating);
        TextView rating_text = (TextView) convertView.findViewById(R.id.place_details_rating_text);
        final TextView address = (TextView) convertView.findViewById(R.id.place_details_address);
        final ListView openingHours = (ListView) convertView.findViewById(R.id.place_details_opening_hours);
        final ImageView photo = (ImageView) convertView.findViewById(R.id.place_details_photo);
        final TextView reviewsHandler = (TextView) convertView.findViewById(R.id.place_details_reviews_handler);
        final ImageView reviewsArrow = (ImageView) convertView.findViewById(R.id.place_details_reviews_arrow);
        final RelativeLayout reviewsHandlerContainer = (RelativeLayout) convertView.findViewById(R.id.place_details_reviews_handler_container);
        final ListView reviewsList = (ListView) convertView.findViewById(R.id.place_details_reviews);
        final TextView openNow = (TextView) convertView.findViewById(R.id.place_details_open_status);
        final RelativeLayout ratingContainer = (RelativeLayout) convertView.findViewById(R.id.place_details_rating_container);
        ImageView call = (ImageView) convertView.findViewById(R.id.place_details_call_icon);
        ImageView website = (ImageView) convertView.findViewById(R.id.place_details_website_icon);
        ImageView exit = (ImageView) convertView.findViewById(R.id.place_details_exit);
        RelativeLayout openContainer = (RelativeLayout) convertView.findViewById(R.id.place_details_open_container);
        pl.droidsonroids.gif.GifTextView loadingPhoto = (pl.droidsonroids.gif.GifTextView) convertView.findViewById(R.id.place_details_loading);
        final RelativeLayout openingHoursContainer = (RelativeLayout) convertView.findViewById(R.id.place_details_opening_hours_container);
        final boolean[] reviewsOpened = {false};
        final boolean[] hoursClicked = {false};

        final AlertDialog ad = placeInfoWindow.show();
        if(ad.getWindow() != null) {
            ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        title.setText(place.getName());
        rating.setRating((float) place.getRate());
        rating_text.setText(String.valueOf(place.getRate()));
        address.setText(place.getAddress());
        exit.setColorFilter(Color.BLACK);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.cancel();
            }
        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getPosition().latitude + "," + place.getPosition().longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + place.getPosition().latitude + "," + place.getPosition().longitude + "&destination_place_id=" + place.getId());
                    Log.v("ADRES", gmmIntentUri.toString());
                    Intent callIntent = new Intent(Intent.ACTION_VIEW);
                    callIntent.setData(gmmIntentUri);
                    startActivity(callIntent);
                }
            }
        });

        if (place.getPhoneNumber() != null) {
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + place.getPhoneNumber()));
                    startActivity(callIntent);
                }
            });
            call.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return false;
                }
            });
        } else {
            call.setVisibility(View.GONE);
        }
        if (place.getReviews() != null) {
            final List<JSONObject> reviews = new ArrayList<>();
            for (int i = 0; i < place.getReviews().length(); i++) {
                try {
                    reviews.add((JSONObject) place.getReviews().get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            final ReviewsAdapter reviewsAdapter = new ReviewsAdapter(this, R.layout.place_details_review, reviews);
            reviewsArrow.setColorFilter(Color.WHITE);
            final int num_rev = place.getReviews().length();
            reviewsHandler.setText(getString(R.string.show_opinions, num_rev));
            reviewsHandlerContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (reviewsOpened[0]) {
                        reviewsHandlerContainer.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.review_handler_rounded));
                        reviewsArrow.animate().rotation(0).setDuration(100).start();
                        reviewsHandler.setText(getString(R.string.show_opinions, num_rev));
                        reviewsOpened[0] = false;
                        reviewsList.setVisibility(View.GONE);
                    } else {
                        reviewsArrow.animate().rotation(-90).setDuration(100).start();
                        reviewsHandler.setText(getString(R.string.hide_opinions, num_rev));
                        reviewsHandlerContainer.setBackgroundColor(Color.parseColor("#280c21"));
                        reviewsOpened[0] = true;
                        reviewsList.setVisibility(View.VISIBLE);
                        reviewsList.setAdapter(reviewsAdapter);
                        openingHoursContainer.setVisibility(View.GONE);
                        hoursClicked[0] = false;
                    }
                }
            });
            ratingContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (reviewsOpened[0]) {
                        reviewsArrow.animate().rotation(0).setDuration(100).start();
                        reviewsHandler.setText(getString(R.string.show_opinions, num_rev));
                        reviewsOpened[0] = false;
                        reviewsHandlerContainer.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.review_handler_rounded));
                        reviewsList.setVisibility(View.GONE);
                    } else {
                        reviewsArrow.animate().rotation(-90).setDuration(100).start();
                        reviewsHandler.setText(getString(R.string.hide_opinions, num_rev));
                        reviewsHandlerContainer.setBackgroundColor(Color.parseColor("#280c21"));
                        reviewsOpened[0] = true;
                        reviewsList.setVisibility(View.VISIBLE);
                        reviewsList.setAdapter(reviewsAdapter);
                        openingHoursContainer.setVisibility(View.GONE);
                        hoursClicked[0] = false;
                    }
                }
            });
        } else {
            reviewsHandlerContainer.setVisibility(View.INVISIBLE);
            reviewsHandlerContainer.getLayoutParams().height = 0;
        }

        if (place.getWebsite() != null) {
            website.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_VIEW);
                    callIntent.setData(Uri.parse(place.getWebsite()));
                    startActivity(callIntent);
                }
            });
            website.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return false;
                }
            });
        } else {
            website.setVisibility(View.GONE);
        }
        if (place.getOpenHours() != null) {
            openContainer.setVisibility(View.VISIBLE);
            if (place.isOpenNow()) {
                openNow.setText(R.string.open_now);
                openNow.setTextColor(Color.parseColor("#FF20AB22"));
            } else {
                openNow.setText(R.string.closed_now);
                openNow.setTextColor(Color.parseColor("#FFA91E1E"));
            }
            final OpeningHoursAdapter adapter = new OpeningHoursAdapter(this, R.layout.place_details_hours, place.getOpenHours());

            openNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hoursClicked[0]) {
                        openingHoursContainer.setVisibility(View.GONE);
                        hoursClicked[0] = false;
                    } else {
                        if (place.getReviews() != null) {
                            int num_rev = place.getReviews().length();
                            reviewsArrow.animate().rotation(0).setDuration(100).start();
                            reviewsHandler.setText(getString(R.string.show_opinions, num_rev));
                            reviewsOpened[0] = false;
                            reviewsList.setVisibility(View.GONE);
                            reviewsHandlerContainer.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.review_handler_rounded));
                        }
                        openingHoursContainer.setVisibility(View.VISIBLE);
                        openingHours.setAdapter(adapter);
                        Calendar calendar = Calendar.getInstance();
                        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        openingHours.setSelection(day);
                        hoursClicked[0] = true;
                    }
                }
            });
        } else {
            openingHours.getLayoutParams().height = 0;
            openingHours.setVisibility(View.INVISIBLE);
        }

        if (place.getPhotos() != null) {
            loadingPhoto.setVisibility(View.VISIBLE);
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=700&photoreference=" + place.getPhotos()[0] + "&key=" + Constants.API_KEY;
            new DownloadImageTask(photo, loadingPhoto, this).execute(url);
        } else {
            photo.getLayoutParams().height = 0;
        }
    }

    public void updatePlaces(String category) throws JSONException {
        if (center != null) {
            if (checkedCategories != null) {
                if (checkedCategories.contains(category)) {
                    if (isOnline()) {
                        // add places from one category
                        if (centerCircle != null) {
                            center = centerCircle.getCenter();
                        }
                        setJsonArray(category);
                    }
                } else {
                    // delete places from one category
                    deletePlaces(category);
                }
            } else {
                if (isOnline()) {
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
        Log.d("RANKBY", (rankByButton.isChecked() ? "distance" : "popular"));
        placesRequest.setCategoryUrl(rankByButton.isChecked());
        placesRequest.doRequest();
    }

    public void updateList(List<PlaceElement> places) {
        if (places.size() > 0 && mGoogleMap != null) {
            if (!footerShowed) {
                showFooter();
            }
            List<PlaceElement> placesOnMap = new ArrayList<>();
            for (PlaceElement p : places) {
                if (p.getMarker() != null) {
                    p.getMarker().remove();
                    p.setMarker(null);
                }
                if (p.getDistanceFromCenter() <= radiusSeekBar.getProgress()) {
                    p.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(p.getPosition()).title(p.getName())));
                    if (p.isChecked()) {
                        highlightMarker(p);
                    }
                    placesOnMap.add(p);
                }
            }

            footer.setText(getString(R.string.footer_text, placesOnMap.size()));
            placesList = (ListView) findViewById(R.id.list_found_places);
            placeAdapter = new PlaceAdapter(this, R.layout.footer_slider_item, placesOnMap, this);
            placesList.setAdapter(placeAdapter);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lp.setMargins(screenWidth - getPixelsFromDp(40), getPixelsFromDp(60), getPixelsFromDp(10), 0);
            getMyLocationButton.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpLoading = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lpLoading.setMargins(screenWidth - getPixelsFromDp(40), getPixelsFromDp(60), getPixelsFromDp(10), 0);
            loading.setLayoutParams(lpLoading);
        } else {
            hideFooter();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lp.setMargins(screenWidth - getPixelsFromDp(40), getPixelsFromDp(10), getPixelsFromDp(10), 0);
            getMyLocationButton.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpLoading = new RelativeLayout.LayoutParams(getPixelsFromDp(30), getPixelsFromDp(30));
            lpLoading.setMargins(screenWidth - getPixelsFromDp(40), getPixelsFromDp(10), getPixelsFromDp(10), 0);
            loading.setLayoutParams(lpLoading);
        }
    }

    public void deletePlaces(String category) throws JSONException {
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
    }

    public float getDistanceFromCenter(LatLng from, LatLng to) {
        float[] results = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results);
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
        if (isOnline()) {
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
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // OK
                            getLocation();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // ASK FOR GPS
                            try {
                                status.startResolutionForResult(
                                        MapActivity.this,
                                        Constants.REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    private void getLocation() {
        mGoogleApiClient.disconnect();
        if (mGoogleApiClient != null && isOnline()) {
            loaderHelper.startLoading(Constants.LOADER_LOCATION);
            updateLocation = true;
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

                    // save current location
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Latitude", String.valueOf(latitude)).apply();
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Longitude", String.valueOf(longitude)).apply();

                    if (user != null && userLocationMarker != null) {
                        userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        if (!user.getPosition().equals(userLocation)) {
                            // on location changed
                            user.getMarker().remove();
                            userLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(userLocation).title(getString(R.string.me)));
                            userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            user.setMarker(userLocationMarker);
                            user.setPosition(userLocation);
                            new GeocoderTask(this, userLocation, "changeUserAddress").execute();
                        }
                    } else {
                        // if no location saved in Preferences
                        userLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(userLocation).title(getString(R.string.me)));
                        userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        new GeocoderTask(this, userLocation, "userLocation").execute();
                    }

                    // move Camera
                    if (centerCircle != null) {
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerCircle.getCenter(), 13));
                    } else {
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
                    }
                    updateLocation = false;
                    mGoogleApiClient.disconnect();
                    loaderHelper.cancelLoading(Constants.LOADER_LOCATION);
                    Log.v("LOCATION CHANGED", "LAT:" + latitude + ", LNG:" + longitude);
                }
            }
        }
    }

    public void changeUserAddress(String address){
        user.setAddress(address);
        personAdapter.notifyDataSetChanged();
        updateMapElements();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MACIEK_DEBUG", "Connection failed");
    }

    // PERMISSIONS
    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
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
//                        .icon(BitmapDescriptorFactory.fromBitmap(UsefulFunctions.buildMarkerIcon(getResources(), BitmapFactory.decodeResource(getResources(),R.drawable.default_person))))
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .icon(getMarkerIcon(R.drawable.default_person))
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
        return (int) (sizeDp * getResources().getDisplayMetrics().density);
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
        String json = PreferenceManager.getDefaultSharedPreferences(this).getString("Fav", null);
//        String json = [{"address":"Zaorskiego 1, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 4a","position":{"latitude":52.16212338072353,"longitude":21.019675098359585}},{"address":"aleja Komisji Edukacji Narodowej 48, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 5a","position":{"latitude":52.14250818646051,"longitude":21.055312603712085}},{"address":"Powsińska 13, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 6a","position":{"latitude":52.18253373437499,"longitude":21.067824102938175}},{"address":"Błonia Wilanowskie, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 7a","position":{"latitude":52.15192826938311,"longitude":21.076964400708675}},{"address":"Bukowińska 26C, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 8a","position":{"latitude":52.18474976529246,"longitude":21.02487254887819}},{"address":"Południowa Obwodnica Warszawy, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 9a","position":{"latitude":52.1396523681538,"longitude":21.025453582406044}},{"address":"Taborowa 33C, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 10a","position":{"latitude":52.1616472563183,"longitude":21.004199422895912}},{"address":"Bocheńska 1, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 11a","position":{"latitude":52.179068240920245,"longitude":21.030993014574047}},{"address":"aleja Komisji Edukacji Narodowej 60, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 12a","position":{"latitude":52.14914588299161,"longitude":21.048004254698753}},{"address":"Politechnika, Warszawa","displayed":true,"id":4,"image":0,"isFavourite":true,"name":"osoba 13a","position":{"latitude":52.2176246,"longitude":21.0143614}},{"address":"Jana Rosoła 61B, Warszawa","displayed":true,"id":2,"image":0,"isFavourite":true,"name":"osoba 14a","position":{"latitude":52.15286405845385,"longitude":21.05514295399189}},{"address":"Marco Polo 1, Warszawa","displayed":true,"id":3,"image":0,"isFavourite":true,"name":"osoba 15a","position":{"latitude":52.14725561255478,"longitude":21.03886429220438}}]
//        Log.d("MACIEK_DEBUG", json);
        if (json != null) {
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

    private BitmapDescriptor getMarkerIcon(int drawableId) {

        return BitmapDescriptorFactory.fromBitmap(UsefulFunctions.buildMarkerIcon(getResources(), BitmapFactory.decodeResource(getResources(), drawableId)));
    }
}