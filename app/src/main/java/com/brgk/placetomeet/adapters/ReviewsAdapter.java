package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.brgk.placetomeet.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReviewsAdapter extends ArrayAdapter<JSONObject>{

    private List<JSONObject> reviews;
    private Context context;

    public ReviewsAdapter(@NonNull Context context, @LayoutRes int resource, List<JSONObject> reviews) {
        super(context, resource, reviews);
        this.reviews = reviews;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.place_details_review, null);
        }

        TextView author = (TextView) convertView.findViewById(R.id.review_author_name);
        TextView text = (TextView) convertView.findViewById(R.id.review_text);
        TextView timeAgo = (TextView) convertView.findViewById(R.id.review_time_ago);
        RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.review_rating);


        JSONObject review = reviews.get(position);
        try {
            String authorName = review.getString("author_name");
            String authorText = review.getString("text");
            String time = review.getString("relative_time_description");
            double rating = review.getDouble("rating");

            author.setText(authorName);
            text.setText(authorText);
            timeAgo.setText(time);
            ratingBar.setRating((float) rating);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

}