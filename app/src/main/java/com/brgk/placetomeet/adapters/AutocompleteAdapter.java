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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.models.PersonElement;
import com.brgk.placetomeet.models.RequestToQueue;

import java.util.List;

public class AutocompleteAdapter extends ArrayAdapter<PersonElement> {

    private MapActivity mapActivity;
    private Context context;
    private List<PersonElement> autocompletePersons;

    public AutocompleteAdapter(@NonNull Context context, @LayoutRes int resource, List<PersonElement> autocompletePersons, MapActivity mapActivity) {
        super(context, resource, autocompletePersons);
        this.context = context;
        this.mapActivity = mapActivity;
        this.autocompletePersons = autocompletePersons;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.autocomplete_item, null);
        }

        final PersonElement person = autocompletePersons.get(position);

        TextView address = (TextView) convertView.findViewById(R.id.autocomplete_address);
        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.autocomplete_person);
        if (mapActivity.favouritePersons.contains(person) && !person.getName().isEmpty()) {
            address.setText(person.getName());
        }else{
            address.setText(person.getAddress());
        }

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapActivity.addressField.setText(person.getAddress());
                if(mapActivity.favouritePersons.contains(person) || mapActivity.lastChosenPersons.contains(person)){
                    mapActivity.addPerson(person);
                }else {
                    RequestToQueue placeDetailsRequest = new RequestToQueue(Constants.TAG_PLACE_DETAILS, "", mapActivity);
                    placeDetailsRequest.setPlaceDetailsUrl(person.addressID, person);
                    placeDetailsRequest.doRequest();
                }
                mapActivity.hideKeyboard();
                mapActivity.addressField.dismissDropDown();
            }
        });

        return convertView;
    }
}