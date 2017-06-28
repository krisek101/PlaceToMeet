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
    private ArrayList<PersonElement> favorite;
    private ListView l;
    private removeFavoriteAdapter removeAdapter;
    private TextView noFavInfo;
    Menu me;

    @Override
    public void onBackPressed() {
        setResult();
        finish();
    }

    private void setResult() {
        Intent result = new Intent();
        result.putIntegerArrayListExtra("positions", positions);
        result.putIntegerArrayListExtra("deletions", toBeDeleted);
        setResult(RESULT_OK, result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        favorite = getIntent().getParcelableArrayListExtra("Fav");
        positions = getIntent().getIntegerArrayListExtra("Added");
        noFavInfo = (TextView) findViewById(R.id.no_favourites_info);
        l = (ListView) findViewById(R.id.f);
        showNoFavInfo();
        setListDefaultAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favourites_activity_menu, menu);
        me = menu;
        if(favorite.isEmpty()) {
            menu.getItem(0).setVisible(false);
        }
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle(R.string.favorite);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_fav:
                ArrayList<PersonElement> favs = getIntent().getParcelableArrayListExtra("Fav");
                removeAdapter = new removeFavoriteAdapter(this, R.layout.remove_favourite_item, favs);
                l.setAdapter(removeAdapter);
                startActionMode(new ActionModeCallback());
                break;
            case android.R.id.home:
                setResult();
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class removeFavoriteAdapter extends ArrayAdapter<PersonElement> {
        SparseBooleanArray stateArray = new SparseBooleanArray();

        removeFavoriteAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PersonElement> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.remove_favourite_item, parent, false);
            }
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.rf_checkBox);
            checkBox.setText(favorite.get(position).getName());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        stateArray.put(position, true);
                        if (!toBeDeleted.contains(position))
                            toBeDeleted.add(position);

                    } else {
                        stateArray.delete(position);
                        toBeDeleted.remove((Integer) position);
                    }
                }
            });
            return convertView;
        }

        private SparseBooleanArray getStates() {
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
            if (item.getItemId() == R.id.contextual_delete) {
                SparseBooleanArray checked = removeAdapter.getStates();
                for (int i = favorite.size() - 1; i >= 0; i--) {
                    if (checked.get(i)) {
                        favorite.remove(i);
                        positions.remove((Integer) i);
                        for (int j = positions.size() - 1; j >= 0; j--) {
                            int value = positions.get(j);
                            if (value > i) {
                                positions.set(j, value - 1);
                            } else {
                                break;
                            }
                        }
                    }
                }
                showNoFavInfo();
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

    void showNoFavInfo() {
        if (!favorite.isEmpty()) {
            noFavInfo.setVisibility(View.GONE);
        } else {
            noFavInfo.setVisibility(View.VISIBLE);
        }
    }

    void setListDefaultAdapter() {
        l.setAdapter(new FavouritePersonAdapter(this, R.layout.favourite_person_item, favorite, this));
        if(me != null && favorite.isEmpty()) {
            me.getItem(0).setVisible(false);
            me.getItem(0).setEnabled(false);
        }
    }
}
