package com.brgk.placetomeet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Collections
    public List<Place> places = new ArrayList<>();
    public List<String> namesCheckedPlaces = new ArrayList<>();

    // UI
    private Button nextButton;
    TextView counter;
    LinearLayout footer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        nextButton = (Button) findViewById(R.id.next);
        counter = (TextView) findViewById(R.id.counter_of_checked);
        footer = (LinearLayout) findViewById(R.id.footer);

        // init functions
        requestPermissions();
        setPlaces();

        // listeners
        setListeners();
    }

    private void setPlaces() {
        for (int i = 0; i < Constants.PLACES.length; i++) {
            if (i < 5) {
                places.add(new Place(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[1]}));
            } else if (i < 8) {
                places.add(new Place(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[0]}));
            } else if (i < 11) {
                places.add(new Place(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[3]}));
            } else if (i < 14) {
                places.add(new Place(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[4]}));
            } else {
                places.add(new Place(Constants.PLACES[i], i, Constants.IMAGES[i], new String[]{Constants.CATEGORIES[2]}));
            }
        }

        RecyclerView rv_food = (RecyclerView) findViewById(R.id.recycler_view_food);
        rv_food.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_food.setAdapter(new RecyclerViewAdapter(this, this, getPlacesByCategory(Constants.CATEGORIES[1])));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv_food.getContext(), LinearLayoutManager.HORIZONTAL);
        rv_food.addItemDecoration(dividerItemDecoration);

        RecyclerView rv_entertainment = (RecyclerView) findViewById(R.id.recycler_view_entertainment);
        rv_entertainment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_entertainment.setAdapter(new RecyclerViewAdapter(this, this, getPlacesByCategory(Constants.CATEGORIES[0])));
        rv_entertainment.addItemDecoration(dividerItemDecoration);

        RecyclerView rv_relax = (RecyclerView) findViewById(R.id.recycler_view_relax);
        rv_relax.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_relax.setAdapter(new RecyclerViewAdapter(this, this, getPlacesByCategory(Constants.CATEGORIES[4])));
        rv_relax.addItemDecoration(dividerItemDecoration);

        RecyclerView rv_sport = (RecyclerView) findViewById(R.id.recycler_view_sport);
        rv_sport.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_sport.setAdapter(new RecyclerViewAdapter(this, this, getPlacesByCategory(Constants.CATEGORIES[3])));
        rv_sport.addItemDecoration(dividerItemDecoration);

    }

    private List<Place> getPlacesByCategory(String category) {
        List<Place> placesByCategory = new ArrayList<>();
        for (Place place : places) {
            if (place.getCategories().contains(category)) {
                placesByCategory.add(place);
            }

        }
        return placesByCategory;
    }

    private void setListeners() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);

                String[] array = new String[namesCheckedPlaces.size()];
                namesCheckedPlaces.toArray(array);
                intent.putExtra(Constants.EXTRA_CHECKED_PLACES, array);

                startActivity(intent);
            }
        });
    }

    public void updateFooter() {
        RelativeLayout scrollContainer = (RelativeLayout) findViewById(R.id.scrollViewContainer);
        if (namesCheckedPlaces.size() > 0) {
            footer.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            scrollContainer.setPadding(0, 0, 0, getPixelsFromDp(70));
            counter.setText(namesCheckedPlaces.size() + " zaznaczonych elementów");
        } else {
            scrollContainer.setPadding(0, 0, 0, getPixelsFromDp(20));
            nextButton.setVisibility(View.INVISIBLE);
            footer.setVisibility(View.INVISIBLE);
        }
    }

    int getPixelsFromDp(int sizeDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (sizeDp * scale + 0.5f);
    }

    // PERMISSIONS
    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //TODO: Permissions: any dangerous permissions here! :D
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
            //TODO: Show ratinoale
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PERMISSIONS_SWITCH:
        switch (requestCode) {
            case Constants.REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d("DEBUG", "Permission: " + permissions[i]);
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d("DEBUG", "Any permission denied!");
                            requestPermissions();
                            break PERMISSIONS_SWITCH;
                        }
                    }
                    Log.d("DEBUG", "All permissions granted!");
                } else {
                    Log.d("DEBUG", "Request cancelled");
                }
                break;
            default:
                Log.d("DEBUG", "unhandled permission");
                break;
        }
    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d("MACIEK-DEBUG", item.getItemId() + "");
        switch (item.getItemId()) {
            case R.id.main_menu_help:
                Toast.makeText(this, "POMOC", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_menu_quit:
                Toast.makeText(this, "WYSCIE", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                Toast.makeText(this, "NOT IMPLEMENTED YET", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}