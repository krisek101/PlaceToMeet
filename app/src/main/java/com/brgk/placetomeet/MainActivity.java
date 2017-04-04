package com.brgk.placetomeet;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Collections
    private Map<String, Integer> places = new HashMap<>();
    private Map<String, Integer> checkedPlaces = new HashMap<>();
    public List<String> placesNames = new ArrayList<>();
    public List<Integer> placesImages = new ArrayList<>();

    // UI
    private PlaceAdapter placeAdapter;
    private Button nextButton;
    private LinearLayout entertainmentContainer;
    String placeName;
    Integer placeImage;
    View gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        //gridPlaces = (GridView) findViewById(R.id.gridOfPlaces);
        nextButton = (Button) findViewById(R.id.next);
        entertainmentContainer = (LinearLayout) findViewById(R.id.container_entertainment);

        // init functions
        requestPermissions();
        setPlaces();

        // listeners
        setListeners();
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
        // food and drinks
        places.put("Restauracja", R.drawable.place_restaurant);
        places.put("Kebab", R.drawable.place_kebab);
        places.put("Pizza", R.drawable.place_pizza);
        places.put("Bar", R.drawable.place_bar);
        places.put("Kawiarnia", R.drawable.place_cafe);

        // entertainment
        places.put("Kręgielnia", R.drawable.place_bowling);
        places.put("Lodowisko", R.drawable.place_rink);
        places.put("Bilard", R.drawable.place_billiards);

        // sport
        places.put("Basen", R.drawable.place_pool);
        places.put("Kort Tenisowy", R.drawable.place_tenis);
        places.put("Hala sportowa", R.drawable.place_sports_hall);

        // relax
        places.put("Park", R.drawable.place_park);
        places.put("Kino", R.drawable.place_cinema);
        places.put("Centrum handlowe", R.drawable.place_shopping_centre);

        //placeAdapter = new PlaceAdapter(this, places, null);
        //gridPlaces.setAdapter(placeAdapter);

        placesNames.addAll(places.keySet());
        placesImages.addAll(places.values());

        for (int i = 0; i < places.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            gridView = inflater.inflate(R.layout.place, null);
            placeImage = placesImages.get(i);
            placeName = placesNames.get(i);
            ImageView imageView = (ImageView) gridView.findViewById(R.id.place_image);
            imageView.setImageResource(placeImage);
            imageView.setTag(placeImage);
            TextView textView = (TextView) gridView.findViewById(R.id.place_label);
            textView.setText(placeName);
            textView.setTag(placeName);
            entertainmentContainer.addView(gridView);
        }

    }

    private void setListeners(){
        entertainmentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeName = (String) view.findViewById(R.id.place_label).getTag();
                placeImage = (Integer) view.findViewById(R.id.place_image).getTag();
                Log.v("Place name", placeName);
                if(((ColorDrawable)view.findViewById(R.id.place_element).getBackground()).getColor() != Constants.CHECKED_COLOR){
                    // checked
                    view.setBackgroundColor(Constants.CHECKED_COLOR);
                    checkedPlaces.put(placeName, placeImage);
                }else{
                    // unchecked
                    checkedPlaces.remove(placeName);
                    view.setBackgroundColor(Constants.UNCHECKED_COLOR);
                }
                updateFooter(checkedPlaces.size());
                Log.v("CheckedPlaces", checkedPlaces.toString());
            }
        });
    }

    private void updateFooter(int count){
        TextView counter = (TextView) findViewById(R.id.counter_of_checked);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.footer);
        if(count>0){
            linearLayout.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            counter.setText(count + " zaznaczonych elementów");
        }else{
            nextButton.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
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
}