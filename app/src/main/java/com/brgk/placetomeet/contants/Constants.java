package com.brgk.placetomeet.contants;

//Here we'll store constants

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;

@SuppressWarnings("WeakerAccess")
public class Constants {

    // Permissions request codes
    public static final int REQUEST_PERMISSIONS_CODE = 2;
    public static final int REQUEST_CHECK_PLAY_SERVICES = 3;
    public static final int REQUEST_CHECK_SETTINGS = 4;
    public static final int REQUEST_FAVOURITES = 5;

    // Colors
    public static final int CHECKED_COLOR = Color.parseColor("#0099FF");
    public static final int UNCHECKED_COLOR = Color.parseColor("#EEEEEE");

    // Places
    public static final int[] CATEGORIES = {R.string.restaurant, R.string.kebab, R.string.pizza, R.string.bar, R.string.cafe, R.string.bowling,
            R.string.rink, R.string.billiards, R.string.pool, R.string.tennis, R.string.sports_hall, R.string.park, R.string.cinema, R.string.shopping_centre};
    public static final int[] IMAGES = {R.drawable.place_restaurant, R.drawable.place_kebab, R.drawable.place_pizza, R.drawable.place_bar, R.drawable.place_cafe, R.drawable.place_bowling,
    R.drawable.place_rink, R.drawable.place_billiards, R.drawable.place_pool, R.drawable.place_tenis, R.drawable.place_sports_hall, R.drawable.place_park, R.drawable.place_cinema,
    R.drawable.place_shopping_centre};
    public static final int DEFAULT_CATEGORY = R.string.restaurant;
    public static final int RADIUS = 1500;
    public static final String API_KEY = "AIzaSyDflSpLotdTPuGtyeZEPNHHNZGVDsft040";

    // Tags
    public static final String TAG_CATEGORY = "CategoryTag";
    public static final String TAG_AUTOCOMPLETE = "AutocompleteTag";
    public static final String TAG_PLACE_DETAILS = "PlaceDetailsTag";

    // Loaders
    public static final String LOADER_PLACES = "loaderPlaces";
    public static final String LOADER_PLACE_DETAILS = "loaderPlaceDetails";
    public static final String LOADER_LOCATION = "loaderLocation";

    // Others
    public static final int SPLASH_TIME = 1000;
    public static final int UNKNOWN_ADDRESS = R.string.unknown_address;

}