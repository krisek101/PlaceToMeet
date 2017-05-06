package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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

    public FavouritePersonAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PersonElement> favs, FavouritesActivity parentActivity) {
        super(context, resource, favs);
        this.context = context;
        this.parentActivity = parentActivity;
        this.favs = favs;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if( convertView == null ) {
            convertView = LayoutInflater.from(context).inflate(R.layout.favourite_person_item, null);
        }
        final Switch item = (Switch) convertView.findViewById(R.id.favourite_person_switch);
        final PersonElement p = favs.get(position);
        Log.d("MACIEK_DEBUG", "name: " + p.getName());
        item.setText(p.getName());

        if( parentActivity.added.contains(position) ) {
            item.setChecked(true);
            parentActivity.positions.add(position);
        }

        item.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MACIEK_DEBUG", item.getText().toString() + "checked: " + item.isChecked());
                if( item.isChecked() ) {
                    parentActivity.positions.add(position);
                } else {
                    parentActivity.positions.remove((Integer) position);
                }
            }
        });
        return convertView;
    }
}
