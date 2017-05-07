package com.brgk.placetomeet.models;

import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.brgk.placetomeet.adapters.AutocompleteAdapter;
import com.brgk.placetomeet.contants.Constants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RequestToQueue {

    private String link;
    private String tag;
    private MapActivity mapActivity;
    private String category;
    private PersonElement person;

    public RequestToQueue(String tag, String category, MapActivity mapActivity) {
        this.tag = tag;
        this.category = category;
        this.mapActivity = mapActivity;
    }

    public void doRequest() {
        Log.v("LINK", link);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    switch (tag) {
                        case Constants.TAG_CATEGORY:
                            onResponsePlaces(response);
                            break;
                        case Constants.TAG_AUTOCOMPLETE:
                            onResponseAutocomplete(response);
                            break;
                        case Constants.TAG_PLACE_DETAILS:
                            onResponsePlaceDetails(response);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void onResponsePlaces(JSONObject response) throws JSONException {
        double lat, lng;
        LatLng position;
        JSONArray ja = response.getJSONArray("results");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            lat = c.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            lng = c.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            position = new LatLng(lat, lng);
            PlaceElement p = new PlaceElement(c, category, mapActivity.getDistanceFromCenter(position));
            if (!mapActivity.places.contains(p)) {
                mapActivity.places.add(p);
            }
        }
        mapActivity.updateList(mapActivity.places);
        mapActivity.loading.setVisibility(View.INVISIBLE);
    }

    private void onResponseAutocomplete(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("predictions");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String address = c.getString("description").replaceAll(", Polska", "");
            String place_id = c.getString("place_id");
            mapActivity.autoCompletePersons.add(new PersonElement(address, place_id));
        }
        mapActivity.autocompleteAdapter = new AutocompleteAdapter(mapActivity, R.layout.autocomplete_item, mapActivity.autoCompletePersons, mapActivity);
        mapActivity.addressField.setAdapter(mapActivity.autocompleteAdapter);
        mapActivity.autocompleteAdapter.notifyDataSetChanged();
    }

    private void onResponsePlaceDetails(JSONObject response) throws JSONException {
        JSONObject positionObject = response.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
        person.setPosition(new LatLng(positionObject.getDouble("lat"), positionObject.getDouble("lng")));
        mapActivity.addPerson(person.getAddress(), person.getPosition());
    }

    public void setCategoryUrl() {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json");
        urlString.append("?keyword=");
        try {
            urlString.append(URLEncoder.encode(category, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&language=pl&location=" + mapActivity.center.latitude + "," + mapActivity.center.longitude + "&radius=" + Constants.RADIUS);
        urlString.append("&key=" + Constants.API_KEY);
        setLink(urlString.toString());
    }

    public void setPlaceDetailsUrl(String placeID, PersonElement person) {
        setLink("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + Constants.API_KEY);
        this.person = person;
    }

    public void setPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&language=pl&components=country:pl");
        urlString.append("&key=" + Constants.API_KEY);
        setLink(urlString.toString());
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
