package com.brgk.placetomeet.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.models.PersonElement;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View myContentsView;
    private MapActivity mapActivity;

    public MarkerInfoWindowAdapter(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        PersonElement person = searchPersonByMarker(marker);

        if (person != null) {
            myContentsView = LayoutInflater.from(mapActivity.getApplicationContext()).inflate(R.layout.marker_info_window_persons, null);
            ImageView avatar = ((ImageView) myContentsView.findViewById(R.id.avatar));
            if (person.getImage() != 0) {
                avatar.setImageResource(person.getImage());
            }
            if (person.getPosition().equals(mapActivity.userLocation)) {
                avatar.setColorFilter(Color.parseColor("#33FF00"));
            } else if (mapActivity.favouritePersons.contains(person)) {
                if (person.getImage() != 0) {
                    avatar.setImageResource(person.getImage());
                } else {
                    avatar.setColorFilter(Color.MAGENTA);
                }
            }
        } else {
            myContentsView = LayoutInflater.from(mapActivity.getApplicationContext()).inflate(R.layout.marker_info_window, null);
        }

        TextView title = ((TextView) myContentsView.findViewById(R.id.title));
        title.setText(marker.getTitle());

        return myContentsView;
    }

    private PersonElement searchPersonByMarker(Marker marker) {
        for (PersonElement p : mapActivity.persons) {
            if (p.getMarker().equals(marker)) {
                return p;
            }
        }
        return null;
    }
}