package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.FavouritesActivity;
import com.brgk.placetomeet.models.PersonElement;

import java.util.List;


public class FavouritePersonAdapter extends ArrayAdapter<PersonElement> {
    private Context context;
    private List<PersonElement> favs;
    private FavouritesActivity parentActivity;
    private SparseBooleanArray stateMap = new SparseBooleanArray();

    public FavouritePersonAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PersonElement> favs, FavouritesActivity parentActivity) {
        super(context, resource, favs);
        this.context = context;
        this.parentActivity = parentActivity;
        this.favs = favs;
        for (Integer i : parentActivity.positions) {
            stateMap.put(i, true);
        }
    }

    private class ViewHolder {
        Switch mSwitch;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.favourite_person_item, null);
            holder = new ViewHolder();
            holder.mSwitch = (Switch) convertView.findViewById(R.id.favourite_person_switch);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final PersonElement p = favs.get(position);
        holder.mSwitch.setText(p.getName());
        holder.mSwitch.setChecked(false);

        if (stateMap.get(position)) {
            holder.mSwitch.setChecked(true);
            if (!parentActivity.positions.contains(position)) {
                parentActivity.positions.add(position);
            }
        }

        holder.mSwitch.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MACIEK_DEBUG", holder.mSwitch.getText().toString() + "checked: " + holder.mSwitch.isChecked());
                if (holder.mSwitch.isChecked()) {
                    if (!parentActivity.positions.contains(position)) {
                        parentActivity.positions.add(position);
                    }
                    stateMap.put(position, true);
                } else {
                    parentActivity.positions.remove((Integer) position);
                    stateMap.delete(position);
                }
            }
        });
        return convertView;
    }
}