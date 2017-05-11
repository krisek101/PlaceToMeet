package com.brgk.placetomeet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.adapters.FavouritePersonAdapter;
import com.brgk.placetomeet.models.PersonElement;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {
    public ArrayList<Integer> positions = new ArrayList<>();
    private ArrayList<Integer> toBeDeleted = new ArrayList<>();
    public ArrayList<Integer> added;

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

        ArrayList<PersonElement> favs = getIntent().getParcelableArrayListExtra("Fav");
        added = getIntent().getIntegerArrayListExtra("Added");
        if(!favs.isEmpty()){
            findViewById(R.id.no_favourites_info).setVisibility(View.INVISIBLE);
        }

        ListView l = (ListView) findViewById(R.id.f);
        l.setAdapter(new FavouritePersonAdapter(this, R.layout.favourite_person_item, favs, this));
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
                break;
            default: return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
