package com.brgk.placetomeet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Collections
    private Map<String, Integer> places = new HashMap<>();
    private Map<String, Integer> filteredPlaces = new HashMap<>();
    private Map<String, Integer> checkedPlaces = new HashMap<>();

    // UI
    private GridView gridPlaces;
    private EditText placeField;
    private Button clearPlaceField;
    private PlaceAdapter pa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        placeField = (EditText) findViewById(R.id.placeField);
        gridPlaces = (GridView) findViewById(R.id.gridOfPlaces);
        clearPlaceField = (Button) findViewById(R.id.clearPlaceField);

        // init functions
        requestPermissions();
        setPlaces();

        // listeners
        setListeners();
    }

    private void setPlaces(){
        places.put("Restauracja", R.drawable.place_restaurant);
        places.put("Park", R.drawable.place_park);
        places.put("Basen", R.drawable.place_pool);
        places.put("Kręgielnia", R.drawable.place_bowling);
        places.put("Kebab", R.drawable.place_kebab);
        places.put("Lodowisko", R.drawable.place_rink);
        places.put("Kawiarnia", R.drawable.place_cafe);
        places.put("Bar", R.drawable.place_bar);
        places.put("Kino", R.drawable.place_cinema);
        places.put("Kort Tenisowy", R.drawable.place_tenis);
        places.put("Bilard", R.drawable.place_billiards);
        places.put("Pizza", R.drawable.place_pizza);
        places.put("Centrum handlowe", R.drawable.place_shopping_centre);
        places.put("Hala sportowa", R.drawable.place_sports_hall);
        pa = new PlaceAdapter(this, places, null);
        gridPlaces.setAdapter(pa);
    }

    private void setListeners(){
        placeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.v("PLACES", places.toString());

                if (charSequence.toString().isEmpty()) {
                    pa = new PlaceAdapter(MainActivity.this, places, checkedPlaces);
                    gridPlaces.setAdapter(pa);
                    clearPlaceField.setVisibility(View.INVISIBLE);
                    Log.v("puste pole", "brak");
                } else {
                    clearPlaceField.setVisibility(View.VISIBLE);
                    for (Map.Entry<String, Integer> place : places.entrySet()) {
                        String placeName = place.getKey();
                        Integer placeImage = place.getValue();
                        Log.v("JESTEM W FOR EACH", "wohoo");
                        if (!placeName.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                            filteredPlaces.remove(placeName);
                            Log.v("Usuwamy element", placeName);
                        } else if (!filteredPlaces.containsValue(placeName)) {
                            filteredPlaces.put(placeName, placeImage);
                            Log.v("dodajemy element", placeName);
                        }
                        pa = new PlaceAdapter(MainActivity.this, filteredPlaces, checkedPlaces);
                        gridPlaces.setAdapter(pa);
                    }
                    Log.v("FILTERED PLACES", filteredPlaces.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        clearPlaceField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeField.setText("");
            }
        });

        gridPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(((ColorDrawable)view.getBackground()).getColor() != Constants.CHECKED_COLOR){
                    // checked
                    adapterView.getChildAt(position).setBackgroundColor(Constants.CHECKED_COLOR);
                    checkedPlaces.put(pa.placesNames.get(position), pa.placesImages.get(position));
                }else{
                    // unchecked
                    checkedPlaces.remove(pa.placesNames.get(position));
                    adapterView.getChildAt(position).setBackgroundColor(Constants.UNCHECKED_COLOR);
                }
                updateFooter(checkedPlaces.size());
                Log.v("CheckedPlaces", checkedPlaces.toString());
            }
        });

        findViewById(R.id.submitBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);

                Set<String> l = checkedPlaces.keySet();
                String[] strings = new String[l.size()];
                l.toArray(strings);

//                for( String s : strings )
//                    Log.d("MACIEK_DEBUG", s);

                intent.putExtra(Constants.EXTRA_CHECKED_PLACES, strings);
                startActivity(intent);

            }
        });
    }

    private void updateFooter(int count){
        TextView counter = (TextView) findViewById(R.id.counter_of_checked);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.footer);
        if(count>0){
            relativeLayout.setVisibility(View.VISIBLE);
            counter.setText(count + " zaznaczonych elementów");
        }else{
            relativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    // PERMISSIONS
    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//TODO: Permissions: any dangerous permissions here! :D
            if( checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ) {
                if( shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
//TODO: Show ratinoale
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                } else {
//TODO: Permissions: as above :D
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PERMISSIONS_SWITCH:
        switch( requestCode ) {
            case Constants.REQUEST_PERMISSIONS_CODE:
                if( grantResults.length > 0 ) {
                    for( int i = 0; i < grantResults.length; i++ ) {
                        Log.d("DEBUG", "Permission: " + permissions[i]);
                        if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
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
        return true;
    }
}