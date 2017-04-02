package com.brgk.placetomeet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brgk.placetomeet.Constants.*;

public class PlaceAdapter extends BaseAdapter {
    private Context mContext;
    private Map<String, Integer> places = new HashMap<>();
    private List<String> placesNames;
    private List<Integer> placesImages;

    public PlaceAdapter(Context c, Map<String, Integer> places) {
        mContext = c;
        this.places = places;
        placesNames = new ArrayList<>();
        placesNames.addAll(this.places.keySet());
        placesImages = new ArrayList<>();
        placesImages.addAll(this.places.values());
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

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.place, null);

            String placeName = placesNames.get(position);
            Integer placeImage = placesImages.get(position);
            TextView textView = (TextView) gridView.findViewById(R.id.place_label);
            textView.setText(placeName);
            ImageView imageView = (ImageView) gridView.findViewById(R.id.place_image);
            imageView.setImageResource(placeImage);
        } else {
            gridView = convertView;
        }

        return gridView;
    }
}