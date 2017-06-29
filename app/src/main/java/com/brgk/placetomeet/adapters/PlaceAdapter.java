package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.models.PlaceElement;
import com.brgk.placetomeet.R;
import com.brgk.placetomeet.models.RequestToQueue;

import java.util.List;

public class PlaceAdapter extends ArrayAdapter<PlaceElement> {

    private List<PlaceElement> places;
    private Context mContext;
    private MapActivity mapActivity;

    public PlaceAdapter(@NonNull Context context, @LayoutRes int resource, List<PlaceElement> places, MapActivity mapActivity) {
        super(context, resource, places);
        this.places = places;
        this.mContext = context;
        this.mapActivity = mapActivity;
    }

    private class ViewHolder {
        TextView placeName;
        TextView rating;
        TextView address;
        RatingBar ratingStars;
        RelativeLayout placeOnList;
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
            holder = new ViewHolder();
        }
        holder.placeName = (TextView) convertView.findViewById(R.id.footer_slider_place_name);
        holder.rating = (TextView) convertView.findViewById(R.id.rating);
        holder.address = (TextView) convertView.findViewById(R.id.address);
        holder.ratingStars = (RatingBar) convertView.findViewById(R.id.rating_stars);
        holder.placeOnList = (RelativeLayout) convertView.findViewById(R.id.place_on_list);

        holder.placeName.setText(place.getName());
        holder.rating.setText(String.valueOf(place.getRate()));
        holder.address.setText(place.getAddress());
        holder.ratingStars.setRating((float) place.getRate());
        holder.placeOnList.setTag(place);

        if (place.isChecked()) {
            holder.placeOnList.setBackgroundColor(Constants.CHECKED_COLOR);
            holder.rating.setTextColor(Color.WHITE);
            holder.address.setTextColor(Color.WHITE);
            holder.placeName.setTextColor(Color.WHITE);
        } else {
            holder.placeOnList.setBackgroundColor(Constants.UNCHECKED_COLOR);
            holder.rating.setTextColor(Color.BLACK);
            holder.address.setTextColor(Color.BLACK);
            holder.placeName.setTextColor(Color.BLACK);
        }

        holder.placeOnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaceElement place = (PlaceElement) view.getTag();
                if (place.isChecked()) {
                    place.setChecked(false);
                    view.setBackgroundColor(Constants.UNCHECKED_COLOR);
                    holder.rating.setTextColor(Color.BLACK);
                    holder.address.setTextColor(Color.BLACK);
                    holder.placeName.setTextColor(Color.BLACK);
                } else {
                    place.setChecked(true);
                    view.setBackgroundColor(Constants.CHECKED_COLOR);
                    holder.rating.setTextColor(Color.WHITE);
                    holder.address.setTextColor(Color.WHITE);
                    holder.placeName.setTextColor(Color.WHITE);
                    RequestToQueue placeDetailsRequest = new RequestToQueue(Constants.TAG_PLACE_DETAILS, "", mapActivity);
                    placeDetailsRequest.setPlaceDetailsUrl(place);
                    placeDetailsRequest.doRequest();
                    int topOffset = mapActivity.getActionBarHeight() + mapActivity.getStatusBarHeight();
                    float toY = mapActivity.screenHeight - topOffset;
                    mapActivity.footerOpened = false;
                    mapActivity.footerSlider.animate().y(toY).setDuration(100).start();
                    mapActivity.footer.animate().y(toY - mapActivity.footer.getHeight()).setDuration(100).start();
                    if(place.getMarker() != null) {
                        place.getMarker().showInfoWindow();
                    }
                }
                mapActivity.highlightMarker(place);
            }
        });

        return convertView;
    }
}