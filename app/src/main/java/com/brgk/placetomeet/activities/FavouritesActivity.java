package com.brgk.placetomeet.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.adapters.FavouritePersonAdapter;
import com.brgk.placetomeet.models.PersonElement;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {
    public ArrayList<Integer> positions = new ArrayList<>();
    private ArrayList<Integer> toBeDeleted = new ArrayList<>();
    public ArrayList<Integer> added;
    private ArrayList<PersonElement> favs;
    private ListView l;
    private removeFavoriteAdapter removeAdapter;

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putIntegerArrayListExtra("positions", positions);

        result.putIntegerArrayListExtra("deletions", toBeDeleted);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);


        favs = getIntent().getParcelableArrayListExtra("Fav");
        added = getIntent().getIntegerArrayListExtra("Added");
        if(!favs.isEmpty()){
            findViewById(R.id.no_favourites_info).setVisibility(View.INVISIBLE);
        }


        l = (ListView) findViewById(R.id.f);
        setListDefaultAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favourites_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.add_fav:

                break;
            case R.id.remove_fav:
                ArrayList<PersonElement> favs = getIntent().getParcelableArrayListExtra("Fav");
                removeAdapter = new removeFavoriteAdapter(this, R.layout.remove_favourite_item, favs);
                l.setAdapter(removeAdapter);
                startActionMode(new ActionModeCallback());
                break;
            default: return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class removeFavoriteAdapter extends ArrayAdapter<PersonElement> {
        SparseBooleanArray stateArray = new SparseBooleanArray();
        public removeFavoriteAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PersonElement> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if( convertView == null ) {
                convertView = getLayoutInflater().inflate(R.layout.remove_favourite_item, null);
            }
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.rf_checkBox);
            checkBox.setText(favs.get(position).getName());
//            checkBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if( checkBox.isChecked() ) {
//                        stateArray.put(position, true);
//                    } else {
//                        stateArray.delete(position);
//                    }
//                }
//            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if( isChecked ) {
                        stateArray.put(position, true);
                        if( !toBeDeleted.contains(position) )
                            toBeDeleted.add(position);

                    } else {
                        stateArray.delete(position);
                        toBeDeleted.remove((Integer) position);
                    }
                }
            });

            return convertView;
        }

        public SparseBooleanArray getStates() {
            return stateArray;
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.list_selection_contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if( item.getItemId() == R.id.contextual_delete ) {
                SparseBooleanArray checked = removeAdapter.getStates();
                for( int i = favs.size()-1; i >= 0; i-- ) {
                    if( checked.get(i) ) {
                        favs.remove(i);

                    }
                }
//                setListDefaultAdapter();
                mode.finish();
                return true;
            } else {
                return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            setListDefaultAdapter();
        }
    }

    void setListDefaultAdapter() {
        l.setAdapter(new FavouritePersonAdapter(this, R.layout.favourite_person_item, favs, this));
    }
}
