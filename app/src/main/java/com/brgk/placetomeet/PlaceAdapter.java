package com.brgk.placetomeet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceAdapter extends BaseAdapter {
    private Context mContext;
    private Map<String, Integer> places = new HashMap<>();
    private Map<String, Integer> checkedPlaces = new HashMap<>();
    public List<String> placesNames;
    public List<Integer> placesImages;

    public PlaceAdapter(Context c, Map<String, Integer> places, Map<String, Integer> checkedPlaces) {
        mContext = c;
        this.places = places;
        placesNames = new ArrayList<>();
        placesNames.addAll(this.places.keySet());
        placesImages = new ArrayList<>();
        placesImages.addAll(this.places.values());
        this.checkedPlaces = checkedPlaces;
    }

    public int getCount() {
        return places.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        String placeName = placesNames.get(position);
        Integer placeImage = placesImages.get(position);

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.place, null);
        } else {
            gridView = convertView;
        }

        ImageView imageView = (ImageView) gridView.findViewById(R.id.place_image);
        imageView.setImageResource(placeImage);
        TextView textView = (TextView) gridView.findViewById(R.id.place_label);
        textView.setText(placeName);

        if(checkedPlaces != null){
            for (Map.Entry<String, Integer> place : checkedPlaces.entrySet()) {
                if(place.getKey().equals(placeName)){
                    gridView.setBackgroundColor(Constants.CHECKED_COLOR);
                }
            }
        }

        return gridView;
    }
}