package com.brgk.placetomeet.models;

import android.util.Log;

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
    private PlaceElement place;

    public RequestToQueue(String tag, String category, MapActivity mapActivity) {
        this.tag = tag;
        this.category = category;
        this.mapActivity = mapActivity;
    }

    public void doRequest() {
        Log.v("LINK", link);
        switch (tag) {
            case Constants.TAG_CATEGORY:
                mapActivity.loaderHelper.startLoading(Constants.LOADER_PLACES);
                break;
            case Constants.TAG_PLACE_DETAILS:
                mapActivity.loaderHelper.startLoading(Constants.LOADER_PLACE_DETAILS);
                break;
        }

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    switch (tag) {
                        case Constants.TAG_CATEGORY:
                            onResponsePlaces(response);
                            mapActivity.loaderHelper.cancelLoading(Constants.LOADER_PLACES);
                            break;
                        case Constants.TAG_AUTOCOMPLETE:
                            onResponseAutocomplete(response);
                            break;
                        case Constants.TAG_PLACE_DETAILS:
                            onResponsePlaceDetails(response);
                            mapActivity.loaderHelper.cancelLoading(Constants.LOADER_PLACE_DETAILS);
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
    }

    private void onResponseAutocomplete(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("predictions");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String address = c.getString("description").replaceAll(", Polska", "");
            String place_id = c.getString("place_id");
            boolean exists = false;
            for(PersonElement pe : mapActivity.autoCompletePersons){
                if(pe.getAddress().equals(address)){
                    exists = true;
                }
            }
            if(!exists) {
                mapActivity.autoCompletePersons.add(new PersonElement(address, place_id));
            }
        }
        Log.v("AUTOCOMPLETE PERSONS", mapActivity.autoCompletePersons.toString());
        mapActivity.autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompletePersons, mapActivity);
        mapActivity.addressField.setAdapter(mapActivity.autocompleteAdapter);
        mapActivity.autocompleteAdapter.notifyDataSetChanged();
    }

    private void onResponsePlaceDetails(JSONObject response) throws JSONException {
        if (person != null) {
            JSONObject positionObject = response.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
            person.setPosition(new LatLng(positionObject.getDouble("lat"), positionObject.getDouble("lng")));
            mapActivity.addPerson(person.getAddress(), person.getPosition());
        } else if (place != null) {
            JSONObject placeInfo = response.getJSONObject("result");
            if (!placeInfo.isNull("website")) {
                place.setWebsite(placeInfo.getString("website"));
            }
            if (!placeInfo.isNull("formatted_phone_number")) {
                place.setPhoneNumber(placeInfo.getString("formatted_phone_number"));
            }
            if (!placeInfo.isNull("reviews")) {
                place.setReviews(placeInfo.getJSONArray("reviews"));
            }
            if (!placeInfo.isNull("opening_hours")) {
                String openHours[] = new String[7];
                for (int i = 0; i < 7; i++) {
                    if (!placeInfo.getJSONObject("opening_hours").getJSONArray("weekday_text").getString(i).isEmpty()) {
                        openHours[i] = placeInfo.getJSONObject("opening_hours").getJSONArray("weekday_text").getString(i);
                    } else {
                        openHours[i] = "null";
                    }
                }
                place.setOpenHours(openHours);
            }

            if (!placeInfo.isNull("photos")) {
                String photos[] = new String[placeInfo.getJSONArray("photos").length()];
                for (int i = 0; i < placeInfo.getJSONArray("photos").length(); i++) {
                    photos[i] = placeInfo.getJSONArray("photos").getJSONObject(i).getString("photo_reference");
                }
                place.setPhotos(photos);
            }

            mapActivity.updatePlaceInfo(place);
        }
    }

    public void setCategoryUrl(boolean byDistance) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json");
        urlString.append("?keyword=");
        try {
            urlString.append(URLEncoder.encode(category, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&language=pl&location=" + mapActivity.center.latitude + "," + mapActivity.center.longitude);
        urlString.append("&key=" + Constants.API_KEY);
        if (byDistance) {
            urlString.append("&rankby=distance");
        } else {
            urlString.append("&radius=" + Constants.RADIUS);
        }
        setLink(urlString.toString());
    }

    public void setPlaceDetailsUrl(String placeID, PersonElement person) {
        setLink("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + Constants.API_KEY);
        this.person = person;
    }

    public void setPlaceDetailsUrl(PlaceElement place) {
        setLink("https://maps.googleapis.com/maps/api/place/details/json?language=pl&placeid=" + place.getId() + "&key=" + Constants.API_KEY);
        this.place = place;
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
