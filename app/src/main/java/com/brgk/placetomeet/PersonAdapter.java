package com.brgk.placetomeet;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class PersonAdapter extends ArrayAdapter<PersonElement> {
    private Context context;
    private List<PersonElement> persons;
    private MapActivity activity;


    public PersonAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PersonElement> persons, MapActivity activity ) {
        super(context, resource, persons);
        this.context = context;
        this.persons = persons;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if( convertView == null ) {
          convertView = LayoutInflater.from(context).inflate(R.layout.right_slider_item, null);
        }
        TextView addressView = (TextView) convertView.findViewById(R.id.right_slider_item_address);
        TextView numberView = (TextView) convertView.findViewById(R.id.right_slider_item_number);
        ImageView favouriteStar = (ImageView) convertView.findViewById(R.id.right_slider_item_favourite);

        final PersonElement p = persons.get(position);
        addressView.setText(p.getAddress());
        numberView.setText(p.getNumber()+"");

        if( p.isFavourite() )
            favouriteStar.setImageResource(R.drawable.favourite_on);
        else
            favouriteStar.setImageResource(R.drawable.favourite_off);

        favouriteStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if( p.isFavourite() ) {
//                    activity.favouritePersons.remove(p);
//                } else {
//                    activity.favouritePersons.add(p);
//                }
                p.changeFavouriteState();
                notifyDataSetInvalidated();
            }
        });


        return convertView;
    }
}
