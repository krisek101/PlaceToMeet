package com.brgk.placetomeet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Collections
    private Map<String, Integer> places = new HashMap<>();
    private Map<String, Integer> filteredPlaces = new HashMap<>();

    // UI
    private GridView gridPlaces;
    private EditText placeField;
    private Button clearPlaceField;

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
        placeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.v("PLACES", places.toString());

                if (charSequence.toString().isEmpty()) {
                    gridPlaces.setAdapter(new PlaceAdapter(MainActivity.this, places));
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
                        gridPlaces.setAdapter(new PlaceAdapter(MainActivity.this, filteredPlaces));
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

        // TODO: onClickListener for every place
//        for (int i=0; i<gridPlaces.getAdapter().getCount(); i++) {
//            gridPlaces.getAdapter().getItem(i);
//        }

    }

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

    private void setPlaces(){
        places.put("Restauracja", R.drawable.restaurant);
        places.put("Park", R.drawable.park);
        places.put("Siłownia", R.drawable.gym);
        places.put("Basen", R.drawable.pool);
        places.put("Kręgle", R.drawable.restaurant);
        places.put("Kebab", R.drawable.park);
        places.put("Lodowisko", R.drawable.gym);
        places.put("Kawiarnia", R.drawable.pool);
        gridPlaces.setAdapter(new PlaceAdapter(this, places));
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
}