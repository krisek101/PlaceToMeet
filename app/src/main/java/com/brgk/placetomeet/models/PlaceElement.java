package com.brgk.placetomeet.models;

import android.content.Context;

import com.brgk.placetomeet.tasks.GeocoderTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceElement {

    private JSONObject place;
    private String id;
    private int img;
    private LatLng position;
    private Marker marker;
    private double rate = 0;
    private String name;
    private String category;
    private boolean openNow = false;
    private String address;
    private Context context;
    private boolean checked;
    private float distanceFromCenter;
    private String[] photos;
    private String[] openHours;
    private String website;
    private String phoneNumber;
    private JSONArray reviews;

    PlaceElement(JSONObject place, String category, float distanceFromCenter) {
        this.place = place;
        this.category = category;
        this.distanceFromCenter = distanceFromCenter;
        this.getData();
    }

    private void getData() {
        try {
            position = new LatLng(place.getJSONObject("geometry").getJSONObject("location").getDouble("lat"), place.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
            id = place.getString("place_id");
            name = place.getString("name");
            if (!place.isNull("rating")) {
                rate = place.getDouble("rating");
            }
            if (!place.isNull("opening_hours")) {
                openNow = place.getJSONObject("opening_hours").getBoolean("open_now");
            }
            if (!place.isNull("vicinity")) {
                address = place.getString("vicinity");
            } else {
                new GeocoderTask(this.context, position, "placeAddress", this).execute();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public float getDistanceFromCenter() {
        return distanceFromCenter;
    }

    public void setDistanceFromCenter(int distanceFromCenter) {
        this.distanceFromCenter = distanceFromCenter;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String[] getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String[] openHours) {
        this.openHours = openHours;
    }

    public JSONArray getReviews() {
        return reviews;
    }

    public void setReviews(JSONArray reviews) {
        this.reviews = reviews;
    }
}