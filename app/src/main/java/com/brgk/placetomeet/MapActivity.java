package com.brgk.placetomeet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String[] strings = bundle.getStringArray(Constants.EXTRA_CHECKED_PLACES);
        for( String s :  strings ) {
            Log.d("MACIEK_DEBUG", s);
        }

    }
}
