package com.brgk.placetomeet.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@SuppressWarnings("unused")
public class PersonElement implements Parcelable {
    private String address;
    private String name;
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
        setDefaultName();
    }

    public PersonElement(String address, String name, int number, Marker marker) {
        this.address = address;
        this.name = name;
        this.number = number;
        this.marker = marker;
        if (marker != null) {
            position = marker.getPosition();
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
        this.name = "Osoba " + getNumber();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void decreaseNumber() {
        this.number--;
        this.getMarker().setTitle("OSOBA " + number);
    }

    public void increaseNumber() {
        this.number++;
        this.getMarker().setTitle("OSOBA " + number);
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
    protected PersonElement(Parcel in) {
        address = in.readString();
        name = in.readString();
        number = in.readInt();
        position = in.readParcelable(LatLng.class.getClassLoader());
        isFavourite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
        dest.writeInt(number);
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
