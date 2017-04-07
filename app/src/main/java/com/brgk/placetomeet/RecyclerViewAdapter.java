package com.brgk.placetomeet;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    MainActivity mainActivity;
    View mView;
    ViewHolder mViewHolder;
    List<Place> places;

    public RecyclerViewAdapter(Context context, MainActivity mainActivity, List<Place> places) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.places = places;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        mView = LayoutInflater.from(context).inflate(R.layout.place, parent, false);
        mViewHolder = new ViewHolder(mView);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView name = (TextView) holder.textView.findViewById(R.id.place_label);
        ImageView img = (ImageView) holder.imageView.findViewById(R.id.place_image);
        RelativeLayout placeContainer = holder.placeContainer;

        final Place place = places.get(position);
        name.setText(place.getName());
        img.setImageResource(place.getImg());
        if (place.getName().length() > 13) {
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, mainActivity.getPixelsFromDp(75), 0, 0);
            name.setLayoutParams(llp);
        }

        if (place.isChecked()) {
            placeContainer.setBackgroundColor(Constants.CHECKED_COLOR);
        } else {
            placeContainer.setBackgroundColor(Constants.UNCHECKED_COLOR);

        }

        placeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.namesCheckedPlaces.contains(place.getName())) {
                    place.setChecked(true);
                    view.findViewById(R.id.place_element).setBackgroundColor(Constants.CHECKED_COLOR);
                    mainActivity.namesCheckedPlaces.add(place.getName());
                } else {
                    place.setChecked(false);
                    view.findViewById(R.id.place_element).setBackgroundColor(Constants.UNCHECKED_COLOR);
                    mainActivity.namesCheckedPlaces.remove(place.getName());
                }
                mainActivity.updateFooter();
                Log.v("CheckedPlaces", mainActivity.namesCheckedPlaces.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public RelativeLayout placeContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.place_label);
            imageView = (ImageView) itemView.findViewById(R.id.place_image);
            placeContainer = (RelativeLayout) itemView.findViewById(R.id.place_element);
        }
    }


}
