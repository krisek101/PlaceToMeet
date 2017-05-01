package com.brgk.placetomeet.adapters;

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

import com.brgk.placetomeet.models.CategoryElement;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.R;

import org.json.JSONException;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<CategoryElement> {

    private List<CategoryElement> categories;
    private Context mContext;
    private MapActivity mapActivity;

    public CategoryAdapter(@NonNull Context context, @LayoutRes int resource, List<CategoryElement> categories, MapActivity mapActivity) {
        super(context, resource, categories);
        this.categories = categories;
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
        final CategoryElement category = categories.get(position);
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

        if (category.isChecked()) {
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
                if(!category.isChecked()) {
                    category.setChecked(true);
                    mapActivity.checkedCategories.add(category.getName());
                    holder.placeContainer.setBackgroundColor(Constants.CHECKED_COLOR);
                    holder.placeName.setTextColor(Color.WHITE);
                    holder.placeImage.setColorFilter(Color.WHITE);
                }else{
                    category.setChecked(false);
                    mapActivity.checkedCategories.remove(category.getName());
                    holder.placeContainer.setBackgroundColor(Constants.UNCHECKED_COLOR);
                    holder.placeName.setTextColor(Color.BLACK);
                    holder.placeImage.setColorFilter(null);
                }
                try {
                    mapActivity.updatePlaces(holder.placeName.getText().toString());
                }catch (JSONException e){
                    Log.v("JSON Eception", e.toString());
                }
                Log.v("CHECKED CATEGORIES: ", mapActivity.checkedCategories.toString());
            }
        });

        holder.placeName.setText(category.getName());
        holder.placeImage.setImageResource(category.getImg());

        return convertView;
    }
}