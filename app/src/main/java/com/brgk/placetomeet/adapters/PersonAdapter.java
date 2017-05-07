package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.models.PersonElement;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;


public class PersonAdapter extends ArrayAdapter<PersonElement> {
    private Context context;
    private List<PersonElement> persons;
    private MapActivity activity;

    public PersonAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PersonElement> persons, MapActivity activity) {
        super(context, resource, persons);
        this.context = context;
        this.persons = persons;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.right_slider_item, null);
        }
        TextView addressView = (TextView) convertView.findViewById(R.id.right_slider_item_address);
        TextView nameView = (TextView) convertView.findViewById(R.id.right_slider_item_name);
        TextView numberView = (TextView) convertView.findViewById(R.id.right_slider_item_avatar);
        final ImageView favouriteStar = (ImageView) convertView.findViewById(R.id.right_slider_item_favouriteStar);
        Switch personOnOff = (Switch) convertView.findViewById(R.id.right_slider_item_switch);

        View touchField = convertView.findViewById(R.id.right_slider_item_container);

        final PersonElement p = persons.get(position);
        p.setId(position + 1);
        if (!p.getPosition().equals(activity.userLocation) && !activity.favouritePersons.contains(p)) {
            p.setName("OSOBA " + (position + 1));
        } else if(p.getPosition().equals(activity.userLocation)){
            p.setName("Ja");
            p.getMarker().setTitle("Ja");
        }
        addressView.setText(p.getAddress());
        nameView.setText(p.getName());
        numberView.setText(String.valueOf(p.getId()));

        favouriteStar.setImageResource(p.isFavourite() ? R.drawable.favourite_on : R.drawable.favourite_off);

        personOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                p.displayed(isChecked);
                                p.getMarker().setVisible(isChecked);
                                activity.updateMapElements();
                          }
         });

        touchField.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openDialog(v, p, favouriteStar);
                return false;
            }
        });

        touchField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.goToPerson(p);
            }
        });

        return convertView;
    }

    private void openDialog(View view, final PersonElement p, final ImageView favouriteStar) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.persons_popup_menu, popup.getMenu());

        // show only delete option for user-person
        if (p.getPosition().equals(activity.userLocation)) {
            popup.getMenu().getItem(0).setVisible(false);
            popup.getMenu().getItem(1).setVisible(false);
        }

        // check if person is favourite
        if (p.isFavourite()) {
            popup.getMenu().getItem(0).setTitle("Usuń z ulubionych");
        } else {
            popup.getMenu().getItem(0).setTitle("Dodaj do ulubionych");
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.persons_menu_fav:
                        if (p.isFavourite()) {
                            changeFavourite(p, favouriteStar);
                        } else {
                            askForName(p, favouriteStar);
                        }
                        break;
                    case R.id.persons_menu_changeAddress:
                        changeAddress(p);
                        break;
                    case R.id.persons_menu_delete:
                        removePerson(p);
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    private void askForName(final PersonElement p, final ImageView favouriteStar) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                boolean exists = false;
                for (PersonElement favPerson : activity.favouritePersons) {
                    if (favPerson.getName().toLowerCase().equals(name.toLowerCase())) {
                        exists = true;
                    }
                }
                if (name.equals("")) {
                    Toast.makeText(context, "Nieprawidłowa nazwa.", Toast.LENGTH_SHORT).show();
                    askForName(p, favouriteStar);
                } else if (exists) {
                    Toast.makeText(context, "Osoba o takiej nazwie już istnieje.", Toast.LENGTH_SHORT).show();
                    askForName(p, favouriteStar);
                } else {
                    p.setName(name);
                    p.getMarker().setTitle(name);
                    changeFavourite(p, favouriteStar);
                }
            }
        }).setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void changeFavourite(PersonElement p, ImageView favouriteStar) {
        if (!p.isFavourite()) {
            activity.favouritePersons.add(p);
            favouriteStar.setImageResource(R.drawable.favourite_on);
        } else {
            activity.favouritePersons.remove(p);
            p.setDefaultName();
            p.getMarker().setTitle(p.getName());
            favouriteStar.setImageResource(R.drawable.favourite_off);
        }
        p.changeFavouriteState();
        notifyDataSetInvalidated();
        activity.saveFav();
    }

    private void removePerson(PersonElement p) {
        if (p.getPosition().equals(activity.userLocation)) {
            p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            p.getMarker().remove();
        }
        for (int i = persons.indexOf(p); i < persons.size(); i++) {
            if (!persons.get(i).getPosition().equals(activity.userLocation)) {
                persons.get(i).decreaseNumber();
            }
        }
        persons.remove(p);
        notifyDataSetChanged();
        activity.updateMapElements();
    }

    private void changeAddress(PersonElement p) {
        activity.closeBothSliders();
        if (!activity.isAddingPerson) {
            activity.showActionBar();
            activity.isAddingPerson = true;
        }
        activity.addressField.setText(p.getAddress());
        p.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        activity.editPerson = p;
    }

}
