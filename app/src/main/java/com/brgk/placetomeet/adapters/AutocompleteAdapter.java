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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.models.PersonElement;

import java.util.List;

public class AutocompleteAdapter extends ArrayAdapter<PersonElement>{

    private MapActivity mapActivity;
    private Context context;
    private List<PersonElement> persons;

    public AutocompleteAdapter(@NonNull Context context, @LayoutRes int resource, List<PersonElement> persons, MapActivity mapActivity) {
        super(context, resource, persons);
        this.context = context;
        this.mapActivity = mapActivity;
        this.persons = persons;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent){
        if( convertView == null ) {
            convertView = LayoutInflater.from(context).inflate(R.layout.autocomplete_item, null);
        }
        final PersonElement person = persons.get(position);

        TextView address = (TextView) convertView.findViewById(R.id.autocomplete_address);
        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.autocomplete_person);
        address.setText(person.getAddress());

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapActivity.addressField.setText(person.getAddress());
                mapActivity.addPerson(person.getAddress(), person.getPosition());
                mapActivity.hideKeyboard();
                mapActivity.addressField.dismissDropDown();
            }
        });

        return convertView;
    }
}