package com.brgk.placetomeet;

import com.google.android.gms.maps.model.Marker;

public class PersonElement {
    private String address;
    private int number;
    private Marker marker = null;

    public PersonElement(String address, int number, Marker marker) {
        this.address = address;
        this.number = number;
        this.marker = marker;
    }

    @SuppressWarnings("unused")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @SuppressWarnings("unused")
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

}
