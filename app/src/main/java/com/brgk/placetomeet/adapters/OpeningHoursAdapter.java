package com.brgk.placetomeet.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brgk.placetomeet.R;

import java.util.Calendar;

public class OpeningHoursAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] openingHours;

    public OpeningHoursAdapter(@NonNull Context context, @LayoutRes int resource, String[] openingHours) {
        super(context, resource, openingHours);
        this.context = context;
        this.openingHours = openingHours;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.place_details_hours, parent, false);
        }
        final TextView hour = (TextView) convertView.findViewById(R.id.place_details_open_hour);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        Log.i("DAY NUM", position + " , "+day);
        String hourElement = openingHours[position];
        hour.setText(hourElement);

        if (position == day) {
            hour.setTypeface(null, Typeface.BOLD);
        }
        return convertView;
    }
}
