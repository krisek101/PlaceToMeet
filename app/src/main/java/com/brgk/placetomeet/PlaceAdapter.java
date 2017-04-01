package com.brgk.placetomeet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import static com.brgk.placetomeet.Constants.*;

public class PlaceAdapter extends BaseAdapter {
    private Context mContext;
    private Integer[] placesIds = {
            R.drawable.park, R.drawable.gym,
            R.drawable.restaurant, R.drawable.pool
    };

    public PlaceAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return placesIds.length;
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

            TextView textView = (TextView) gridView.findViewById(R.id.place_label);
            textView.setText(places[position]);
            ImageView imageView = (ImageView) gridView.findViewById(R.id.place_image);
            String place = places[position];

            if (place.equals("Park")) {
                imageView.setImageResource(R.drawable.park);
            } else if (place.equals("Basen")) {
                imageView.setImageResource(R.drawable.pool);
            } else if (place.equals("Siłownia")) {
                imageView.setImageResource(R.drawable.gym);
            } else {
                imageView.setImageResource(R.drawable.restaurant);
            }

        } else {
            gridView = convertView;
        }

        return gridView;
    }
}