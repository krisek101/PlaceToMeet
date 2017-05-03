package com.brgk.placetomeet.contants;

//Here we'll store constants

import android.graphics.Color;

import com.brgk.placetomeet.R;

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
    public static final String[] CATEGORIES = {"Restauracja", "Kebab", "Pizza", "Bar", "Kawiarnia", "Kręgielnia",
            "Lodowisko", "Bilard", "Basen", "Kort Tenisowy", "Hala sportowa", "Park", "Kino", "Centrum handlowe"};
    public static final int[] IMAGES = {R.drawable.place_restaurant, R.drawable.place_kebab, R.drawable.place_pizza, R.drawable.place_bar, R.drawable.place_cafe, R.drawable.place_bowling,
    R.drawable.place_rink, R.drawable.place_billiards, R.drawable.place_pool, R.drawable.place_tenis, R.drawable.place_sports_hall, R.drawable.place_park, R.drawable.place_cinema,
    R.drawable.place_shopping_centre};
    public static final String DEFAULT_CATEGORY = "Restauracja";
    public static final int RADIUS = 1500;
    public static final String API_KEY = "AIzaSyDTmOH9Pi8frp-_JU9gZfUxN7yWdW7yNEM";

    // Tags
    public static final String TAG = "PlacesTag";
    public static final String TAG_AUTOCOMPLETE = "AutocompleteTag";
    public static final String TAG_PLACE_DETAILS = "PlaceDetailsTag";

    // Activity request codes
    public static final int PLACE_PICKER_REQUEST_CODE = 1;

    // Others
    public static final int SPLASH_TIME = 1000;
}