package com.brgk.placetomeet.adapters;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.models.DownloadImageTask;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.PlaceElement;
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
        PlaceElement place = searchPlaceByMarker(marker);

        if (person != null) {
            myContentsView = LayoutInflater.from(mapActivity.getApplicationContext()).inflate(R.layout.marker_info_window_persons, null);
            ImageView avatar = ((ImageView) myContentsView.findViewById(R.id.avatar));
            avatar.setColorFilter(Color.GRAY);
            if (person.getImage() != 0) {
                avatar.setImageResource(person.getImage());
            }
            if (person.getPosition().equals(mapActivity.userLocation)) {
                avatar.setColorFilter(Color.BLACK);
            } else if (mapActivity.favouritePersons.contains(person)) {
                if (person.getImage() != 0) {
                    avatar.setImageResource(person.getImage());
                } else {
                    avatar.clearColorFilter();
                }
            }
        } else if(place != null){
            myContentsView = LayoutInflater.from(mapActivity.getApplicationContext()).inflate(R.layout.marker_info_window_places, null);
//            ImageView photo = ((ImageView) myContentsView.findViewById(R.id.photo));
//            new DownloadImageTask(photo)
//                    .execute("https://maps.googleapis.com/maps/api/place/photo?maxwidth=70&photoreference=" + place.getPhoto() + "&key=" + Constants.API_KEY);
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

    private PlaceElement searchPlaceByMarker(Marker marker) {
        for (PlaceElement p : mapActivity.places) {
            if (p.getMarker().equals(marker)) {
                return p;
            }
        }
        return null;
    }
}