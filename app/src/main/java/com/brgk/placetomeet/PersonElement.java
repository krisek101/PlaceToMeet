package com.brgk.placetomeet;

import com.google.android.gms.maps.model.Marker;

@SuppressWarnings("unused")
public class PersonElement {
    private String address;
    private int number;
    private Marker marker = null;
    private boolean isFavourite = false;

    public PersonElement(String address, int number, Marker marker) {
        this.address = address;
        this.number = number;
        this.marker = marker;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void favourite( boolean f ) {
        isFavourite = f;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void changeFavouriteState() {
        isFavourite = !isFavourite;
    }


}
