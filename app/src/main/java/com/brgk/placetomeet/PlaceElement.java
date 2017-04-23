package com.brgk.placetomeet;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaceElement {

    private JSONObject place;
    private String id;
    private int img;
    private LatLng position;
    private Marker marker;
    private double rate;
    private String name;
    private String category;
    private boolean openNow;

    public PlaceElement(JSONObject place, String category) throws JSONException {
        this.place = place;
        this.category = category;
        this.getData();
    }

    private void getData() throws JSONException {
        position = new LatLng(place.getJSONObject("geometry").getJSONObject("location").getDouble("lat"), place.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
        id = place.getString("place_id");
        //rate = place.getDouble("rating");
        name = place.getString("name");
        //openNow = place.getJSONObject("opening_hours").getBoolean("open_now");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlaceElement)) {
            return false;
        }
        PlaceElement other = (PlaceElement) obj;
        return (this.getPosition().equals(other.getPosition()));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public JSONObject getPlace() {
        return place;
    }

    public void setPlace(JSONObject place) {
        this.place = place;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }
}