package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.R;

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
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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
                changeFavourite(p);
            }
        });

        addressView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openDialog(v, p);
                return false;
            }
        });

        return convertView;
    }

    private void changeFavourite(PersonElement p) {
                p.changeFavouriteState();
        if( !p.isFavourite() ) {
            activity.favouritePersons.remove(p);
        } else {
            activity.favouritePersons.add(p);
        }
        notifyDataSetInvalidated();
        activity.saveFav();
    }

    private void openDialog(View view, final PersonElement p) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.persons_popup_menu, popup.getMenu());
        final String toastText;
        if( p.isFavourite() ) {
            popup.getMenu().getItem(0).setTitle("Usuń z ulubionych");
            toastText = "Usunięto!";
        } else {
            popup.getMenu().getItem(0).setTitle("Dodaj do ulubionych");
            toastText = "Dodano!";
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch( item.getItemId() ) {
                    case R.id.persons_menu_fav:
                        changeFavourite(p);
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.persons_menu_changeAddress:
                        Toast.makeText(context, "TODO: change address", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.persons_menu_delete:
                        removePerson(p);
                        Toast.makeText(context, "Usunięto!", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    private void removePerson(PersonElement p) {
        for( int i = persons.indexOf(p); i < persons.size(); i++ ) {
            persons.get(i).decreaseNumber();
        }
        p.getMarker().remove();
        persons.remove(p);
        notifyDataSetChanged();
        activity.updateMapElements();
    }
}
