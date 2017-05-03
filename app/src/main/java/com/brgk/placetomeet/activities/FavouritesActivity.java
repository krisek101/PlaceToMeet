package com.brgk.placetomeet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.adapters.FavouritePersonAdapter;
import com.brgk.placetomeet.models.PersonElement;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {
    public ArrayList<Integer> positions = new ArrayList<>();
    public ArrayList<Integer> added;

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putIntegerArrayListExtra("positions", positions);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        ArrayList<PersonElement> favs = getIntent().getParcelableArrayListExtra("Fav");
        added = getIntent().getIntegerArrayListExtra("Added");

        Log.d("MACIEK_DEBUG", "otheractivity: " + favs.toString());

        ListView l = (ListView) findViewById(R.id.f);
        l.setAdapter(new FavouritePersonAdapter(this, R.layout.favourite_person_item, favs, this));
    }


}
