package com.brgk.placetomeet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.brgk.placetomeet.adapters.CategoryAdapter;
import com.brgk.placetomeet.contants.Constants;
import com.brgk.placetomeet.R;
import com.brgk.placetomeet.models.CategoryElement;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        ActivityStarter starter = new ActivityStarter();
        starter.start();
    }

    private class ActivityStarter extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(Constants.SPLASH_TIME);
            } catch (Exception e) {
                Log.e("SplashScreen", e.getMessage());
            }

            Intent intent = new Intent(SplashScreen.this, MapActivity.class);
            SplashScreen.this.startActivity(intent);
            SplashScreen.this.finish();
        }
    }
}