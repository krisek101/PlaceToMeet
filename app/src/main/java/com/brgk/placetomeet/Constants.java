package com.brgk.placetomeet;

//Here we'll store constants

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    //Permissions request codes
    public static final int REQUEST_PERMISSIONS_CODE = 2;

    //Colors
    public static final int CHECKED_COLOR = Color.parseColor("#0099FF");
    public static final int UNCHECKED_COLOR = Color.parseColor("#EEEEEE");

    //Others
    private static final String[] categories = {"Rozrywka", "Jedzenie i picie", "Impreza", "Sport", "Relaks"};

    //Extras
    public static final String EXTRA_CHECKED_PLACES = "EXTRA_PLACES";

}
