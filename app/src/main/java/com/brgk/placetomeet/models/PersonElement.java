package com.brgk.placetomeet.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@SuppressWarnings("unused")
public class PersonElement implements Parcelable {
    private String address;
    private int number;
    private transient Marker marker = null;
    private LatLng position;
    private boolean isFavourite = false;

    public PersonElement(String address, int number, Marker marker) {
        this.address = address;
        this.number = number;
        this.marker = marker;
        if (marker != null) {
            position = marker.getPosition();
        }
    }

    protected PersonElement(Parcel in) {
        address = in.readString();
        number = in.readInt();
        position = in.readParcelable(LatLng.class.getClassLoader());
        isFavourite = in.readByte() != 0;
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

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(double lat, double lng) {
        this.position = new LatLng(lat, lng);
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

    //PARCEL
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt(number);
        dest.writeParcelable(position, flags);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
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
