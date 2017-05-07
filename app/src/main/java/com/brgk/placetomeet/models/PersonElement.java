package com.brgk.placetomeet.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@SuppressWarnings("unused")
public class PersonElement implements Parcelable {
    private String address;
    private String name;
    private int id;
    private transient Marker marker = null;
    private LatLng position;
    private boolean isFavourite = false;
    public String addressID;
    private boolean displayed = true;

    public PersonElement(String address, String addressID) {
        this.address = address;
        this.addressID = addressID;
    }

    public PersonElement(String address, Marker marker) {
        this.address = address;
        this.marker = marker;
        if (marker != null) {
            position = marker.getPosition();
        }
        setDefaultName();
    }

    public PersonElement(String address, String name, Marker marker) {
        this.address = address;
        this.name = name;
        this.marker = marker;
        if (marker != null) {
            position = marker.getPosition();
            marker.setTitle(getName());
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultName() {
        this.name = "Osoba " + getId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void decreaseNumber() {
        this.id--;
        this.getMarker().setTitle("OSOBA " + id);
    }

    public void increaseNumber() {
        this.id++;
        this.getMarker().setTitle("OSOBA " + id);
    }

    public Marker getMarker() {
        return marker;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void favourite(boolean f) {
        isFavourite = f;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void changeFavouriteState() {
        isFavourite = !isFavourite;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void displayed(boolean displayed) {
        this.displayed = displayed;
    }

    //PARCEL
    protected PersonElement(Parcel in) {
        address = in.readString();
        name = in.readString();
        id = in.readInt();
        position = in.readParcelable(LatLng.class.getClassLoader());
        isFavourite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeParcelable(position, flags);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PersonElement> CREATOR = new Creator<PersonElement>() {
        @Override
        public PersonElement createFromParcel(Parcel in) {
            return new PersonElement(in);
        }

        @Override
        public PersonElement[] newArray(int size) {
            return new PersonElement[size];
        }
    };
}
