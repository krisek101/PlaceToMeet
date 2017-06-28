package com.brgk.placetomeet.tasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.contants.UsefulFunctions;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.PlaceElement;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocoderTask extends AsyncTask<Void, Void, String>{

    private MapActivity mapActivity;
    private LatLng position;
    private String tag;
    private PersonElement personElement;
    private PlaceElement placeElement;

    public GeocoderTask(MapActivity mapActivity, LatLng position, String tag){
        this.mapActivity = mapActivity;
        this.position = position;
        this.tag = tag;
    }

    public GeocoderTask(MapActivity mapActivity, LatLng position, String tag, PersonElement personElement){
        this.mapActivity = mapActivity;
        this.position = position;
        this.tag = tag;
        this.personElement = personElement;
    }

    public GeocoderTask(Context mapActivity, LatLng position, String tag, PlaceElement placeElement){
        this.mapActivity = (MapActivity) mapActivity;
        this.position = position;
        this.tag = tag;
        this.placeElement = placeElement;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String address = mapActivity.getString(Constants.UNKNOWN_ADDRESS);
        if(UsefulFunctions.isOnline(mapActivity)) {
            Geocoder geocoder = new Geocoder(mapActivity, Locale.getDefault());
            List<Address> addressArray = new ArrayList<>();
            int failed = 0;
            while (address.equals(mapActivity.getString(Constants.UNKNOWN_ADDRESS)) && failed < 2) {
                address = "";
                try {
                    addressArray = geocoder.getFromLocation(position.latitude, position.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressArray != null) {
                    if (!addressArray.isEmpty()) {
                        if (addressArray.get(0) != null) {
                            address += addressArray.get(0).getAddressLine(0);
                            if (addressArray.get(0).getLocality() != null) {
                                address += ", " + addressArray.get(0).getLocality();
                            }
                        }
                    }
                } else {
                    address = mapActivity.getString(Constants.UNKNOWN_ADDRESS);
                    failed++;
                }
            }
        }else{
            address = mapActivity.getString(Constants.UNKNOWN_ADDRESS);
        }
        return address;
    }

    @Override
    protected void onPostExecute(String address) {
        super.onPostExecute(address);
        switch(tag){
            case "userLocation":
                mapActivity.addUser(address);
                break;
            case "addTempPerson":
                mapActivity.addTempPerson(address, position);
                break;
            case "resetAddress":
                personElement.setAddress(address);
                break;
            case "changeUserAddress":
                mapActivity.changeUserAddress(address);
                break;
            case "placeAddress":
                placeElement.setAddress(address);
                break;
        }
    }
}