package com.brgk.placetomeet.models;

import android.support.v4.util.ArrayMap;
import android.view.View;

import com.brgk.placetomeet.activities.MapActivity;

import java.util.Map;

public class LoaderHelper {

    private MapActivity mapActivity;
    private Map<String, Boolean> loaders;

    public LoaderHelper(MapActivity mapActivity){
        this.mapActivity = mapActivity;
        loaders = new ArrayMap<>();
    }

    public void setLoader(String tag){
        loaders.put(tag, false);
    }

    public void cancelLoading(String tag){
        loaders.put(tag, false);
        if(!loaders.containsValue(true)){
            mapActivity.loading.setVisibility(View.INVISIBLE);
        }
    }

    public void startLoading(String tag){
        loaders.put(tag, true);
        mapActivity.loading.setVisibility(View.VISIBLE);
    }

}