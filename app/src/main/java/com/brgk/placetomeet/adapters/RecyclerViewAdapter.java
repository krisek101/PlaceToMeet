package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.models.DownloadImageTask;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    MapActivity mapActivity;
    View mView;
    ViewHolder mViewHolder;
    String[] photos;

    public RecyclerViewAdapter(MapActivity mapActivity, String[] photos) {
        this.context = mapActivity.getApplicationContext();
        this.mapActivity = mapActivity;
        this.photos = photos;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        mView = LayoutInflater.from(context).inflate(R.layout.place_details_photo, parent, false);
        mViewHolder = new ViewHolder(mView);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageView img = (ImageView) holder.imageView.findViewById(R.id.place_details_photo);
        RelativeLayout placeContainer = holder.placeContainer;

        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=" + photos[position] + "&key=" + Constants.API_KEY;
        new DownloadImageTask(img).execute(url);
    }

    @Override
    public int getItemCount() {
        return photos.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public RelativeLayout placeContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.place_details_photo);
            placeContainer = (RelativeLayout) itemView.findViewById(R.id.place_details_photo_element);
        }
    }


}
