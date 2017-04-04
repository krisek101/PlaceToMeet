package com.brgk.placetomeet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    View mView;
    ViewHolder mViewHolder;
    List<String> names;
    List<Integer> images;

    public RecyclerViewAdapter(Context context, Map<String, Integer> map) {
        this.context = context;
        names = new ArrayList<>(map.keySet());
        images = new ArrayList<>(map.values());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(context).inflate(R.layout.place, parent,false);
        mViewHolder = new ViewHolder(mView);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MACIEK-DEBUG", ((TextView) view.findViewById(R.id.place_label)).getText().toString());
            }
        });
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(names.get(position));
        holder.imageView.setImageResource(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.place_label);
            imageView = (ImageView) itemView.findViewById(R.id.place_image);
        }
    }


}
