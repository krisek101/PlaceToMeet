package com.brgk.placetomeet.contants;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsefulFunctions {

    public static String getAddressFromLatLng(Context context, LatLng position) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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
            addressBuilder = "Adres nieznany";
        }
        return addressBuilder;
    }

}