package com.brgk.placetomeet;

//Here we'll store constants

import android.graphics.Color;

@SuppressWarnings("WeakerAccess")
public class Constants {

    //Permissions request codes
    public static final int REQUEST_PERMISSIONS_CODE = 2;
    public static final int REQUEST_CHECK_PLAY_SERVICES = 3;
    public static final int REQUEST_CHECK_SETTINGS = 4;

    //Colors
    public static final int CHECKED_COLOR = Color.parseColor("#0099FF");
    public static final int UNCHECKED_COLOR = Color.parseColor("#EEEEEE");

    //Left Side
    public static final String[] CATEGORIES = {"Rozrywka", "Jedzenie i picie", "Impreza", "Sport", "Relaks"};
    public static final String[] PLACES = {"Restauracja", "Kebab", "Pizza", "Bar", "Kawiarnia", "Kręgielnia",
            "Lodowisko", "Bilard", "Basen", "Kort Tenisowy", "Hala sportowa", "Park", "Kino", "Centrum handlowe"};
    public static final int[] IMAGES = {R.drawable.place_restaurant, R.drawable.place_kebab, R.drawable.place_pizza, R.drawable.place_bar, R.drawable.place_cafe, R.drawable.place_bowling,
    R.drawable.place_rink, R.drawable.place_billiards, R.drawable.place_pool, R.drawable.place_tenis, R.drawable.place_sports_hall, R.drawable.place_park, R.drawable.place_cinema,
    R.drawable.place_shopping_centre};

    //Extras
    public static final String EXTRA_CHECKED_PLACES = "EXTRA_PLACES";

    //Activity request codes
    public static final int PLACE_PICKER_REQUEST_CODE = 1;

    //Others
    public static final int SPLASH_TIME = 1000;

}
