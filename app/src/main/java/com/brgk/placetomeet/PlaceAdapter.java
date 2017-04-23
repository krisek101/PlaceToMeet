package com.brgk.placetomeet;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PlaceAdapter extends ArrayAdapter<PlaceElement> {

    private List<PlaceElement> places;
    private Context mContext;
    private MapActivity mapActivity;

    PlaceAdapter(@NonNull Context context, @LayoutRes int resource, List<PlaceElement> places, MapActivity mapActivity) {
        super(context, resource, places);
        this.places = places;
        this.mContext = context;
        this.mapActivity = mapActivity;
    }

    private class ViewHolder {
        TextView placeName;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PlaceElement place = places.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.footer_slider_item, null);
            holder = new ViewHolder();
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.placeName = (TextView) convertView.findViewById(R.id.footer_slider_place_name);
        convertView.setTag(holder);
        holder.placeName.setText(place.getName());

        return convertView;
    }
}