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

import org.json.JSONException;

import java.util.List;

class CategoryAdapter extends ArrayAdapter<CategoryElement> {

    private List<CategoryElement> places;
    private Context mContext;
    private MapActivity mapActivity;

    CategoryAdapter(@NonNull Context context, @LayoutRes int resource, List<CategoryElement> places, MapActivity mapActivity) {
        super(context, resource, places);
        this.places = places;
        this.mContext = context;
        this.mapActivity = mapActivity;
    }

    private class ViewHolder {
        TextView placeName;
        ImageView placeImage;
        LinearLayout placeContainer;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final CategoryElement place = places.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.left_slider_item, null);
            holder = new ViewHolder();
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.placeName = (TextView) convertView.findViewById(R.id.left_slider_place_name);
        holder.placeImage = (ImageView) convertView.findViewById(R.id.left_slider_place_image);
        holder.placeContainer = (LinearLayout) convertView.findViewById(R.id.left_slider_item_container);

        convertView.setTag(holder);

        if (place.isChecked()) {
            holder.placeContainer.setBackgroundColor(Constants.CHECKED_COLOR);
            holder.placeName.setTextColor(Color.WHITE);
            holder.placeImage.setColorFilter(Color.WHITE);
        } else {
            holder.placeContainer.setBackgroundColor(Constants.UNCHECKED_COLOR);
            holder.placeName.setTextColor(Color.BLACK);
            holder.placeImage.setColorFilter(null);
        }

        holder.placeName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!place.isChecked()) {
                    place.setChecked(true);
                    mapActivity.checkedCategories.add(place.getName());
                    holder.placeContainer.setBackgroundColor(Constants.CHECKED_COLOR);
                    holder.placeName.setTextColor(Color.WHITE);
                    holder.placeImage.setColorFilter(Color.WHITE);
                }else{
                    place.setChecked(false);
                    mapActivity.checkedCategories.remove(place.getName());
                    holder.placeContainer.setBackgroundColor(Constants.UNCHECKED_COLOR);
                    holder.placeName.setTextColor(Color.BLACK);
                    holder.placeImage.setColorFilter(null);
                }
                try {
                    mapActivity.updatePlaces(holder.placeName.getText().toString());
                }catch (JSONException e){
                    Log.v("JSON Eception", e.toString());
                }
                Log.v("CHECKED PLACES: ", mapActivity.checkedCategories.toString());
            }
        });

        holder.placeName.setText(place.getName());
        holder.placeImage.setImageResource(place.getImg());

        return convertView;
    }
}