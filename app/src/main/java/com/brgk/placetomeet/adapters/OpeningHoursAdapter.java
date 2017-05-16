package com.brgk.placetomeet.adapters;


import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brgk.placetomeet.R;

public class OpeningHoursAdapter extends ArrayAdapter<String>{

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
            convertView = LayoutInflater.from(context).inflate(R.layout.place_details_hours, null);
        }

        TextView hour = (TextView) convertView.findViewById(R.id.place_details_open_hour);
        String hourElement = openingHours[position];
        hour.setText(hourElement);

        return convertView;
    }
}
