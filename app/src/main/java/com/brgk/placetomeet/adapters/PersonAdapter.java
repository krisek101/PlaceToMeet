package com.brgk.placetomeet.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.PlaceElement;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;


public class PersonAdapter extends ArrayAdapter<PersonElement> {
    private Context context;
    private List<PersonElement> persons;
    private MapActivity activity;
    public SparseBooleanArray negativeStateMap = new SparseBooleanArray();

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
        final TextView addressView = (TextView) convertView.findViewById(R.id.right_slider_item_address);
        final TextView nameView = (TextView) convertView.findViewById(R.id.right_slider_item_name);
        ImageView avatar = (ImageView) convertView.findViewById(R.id.right_slider_item_avatar);
        avatar.setColorFilter(Color.GRAY);
        final ImageView favouriteStar = (ImageView) convertView.findViewById(R.id.right_slider_item_favouriteStar);
        final Switch personOnOff = (Switch) convertView.findViewById(R.id.right_slider_item_switch);
        TextView distanceText = (TextView) convertView.findViewById(R.id.right_slider_item_distance);
        personOnOff.setChecked(true);
        if (negativeStateMap.get(position)) {
            personOnOff.setChecked(false);
        }

        View touchField = convertView.findViewById(R.id.right_slider_item_container);

        final PersonElement p = persons.get(position);

        if (p.getDistanceToCurrentPlace() != 0) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(activity.getPixelsFromDp(40), activity.getPixelsFromDp(35));
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            avatar.setLayoutParams(layoutParams);
            distanceText.setVisibility(View.VISIBLE);
            String distance;
            if (p.getDistanceToCurrentPlace() < 1000) {
                distance = p.getDistanceToCurrentPlace() + "m";
            } else {
                double result = (double) (p.getDistanceToCurrentPlace()) / 1000;
                DecimalFormat toFormat = new DecimalFormat("#.##");
                distance = (toFormat.format(result)) + "km";
                if (p.getDistanceToCurrentPlace() >= 100000) {
                    distance = ">100km";
                }
            }
            distanceText.setText(distance);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(activity.getPixelsFromDp(50), activity.getPixelsFromDp(50));
            avatar.setLayoutParams(layoutParams);
            distanceText.setVisibility(View.GONE);
        }
        p.setId(position + 1);
        if (!p.getPosition().equals(activity.userLocation) && !activity.favouritePersons.contains(p)) {
            p.setName("OSOBA " + (position + 1));
        } else if (p.getPosition().equals(activity.userLocation)) {
            avatar.setColorFilter(Color.BLACK);
            favouriteStar.setVisibility(View.INVISIBLE);
            p.setName("Ja");
            p.getMarker().setTitle("Ja");
        } else if (activity.favouritePersons.contains(p)) {
            if (p.getImageResId() != 0) {
                avatar.setImageResource(p.getImageResId());
            } else {
                avatar.clearColorFilter();
            }
        }
        addressView.setSelected(true);
        nameView.setSelected(true);
        addressView.setText(p.getAddress());
        nameView.setText(p.getName());

        favouriteStar.setImageResource(p.isFavourite() ? R.drawable.favourite_on : R.drawable.favourite_off);

        personOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (personOnOff.isChecked()) {
                    negativeStateMap.delete(position);
                    p.displayed(true);
                    p.getMarker().setVisible(true);
                } else {
                    negativeStateMap.put(position, true);
                    p.displayed(false);
                    p.getMarker().setVisible(false);
                }
                activity.updateMapElements();
            }
        });

        if (!p.getPosition().equals(activity.userLocation)) {
            touchField.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openDialog(v, p, favouriteStar);
                    return false;
                }
            });
        }


        touchField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressView.isSelected()) {
                    addressView.setSelected(false);
                    nameView.setSelected(false);
                } else {
                    addressView.setSelected(true);
                    nameView.setSelected(true);
                }
                activity.goToPerson(p);
            }
        });

        return convertView;
    }

    private void openDialog(View view, final PersonElement p, final ImageView favouriteStar) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.persons_popup_menu, popup.getMenu());

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
        final TextView title = new TextView(context);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        title.setText("Wpisz nazwę osoby");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setCustomTitle(title);
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
            if(activity.lastChosenPersons.contains(p)){
                activity.lastChosenPersons.remove(p);
            }
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
        p.getMarker().showInfoWindow();
        activity.editPerson = p;
    }

}
