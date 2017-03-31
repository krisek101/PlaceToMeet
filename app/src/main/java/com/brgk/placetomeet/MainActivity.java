package com.brgk.placetomeet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
    }

    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if( checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ) {
                if( shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {

                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constans.REQUEST_PERMISSIONS_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PERMISSIONS_SWITCH:
        switch( requestCode ) {
            case Constans.REQUEST_PERMISSIONS_CODE:
                if( grantResults.length > 0 ) {
                    for( int i = 0; i < grantResults.length; i++ ) {
                        Log.d("DEBUG", "Permission: " + permissions[i]);
                        if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                            Log.d("DEBUG", "Any permission denied!");
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