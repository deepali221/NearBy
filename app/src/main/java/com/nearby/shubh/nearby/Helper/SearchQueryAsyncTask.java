package com.nearby.shubh.nearby.Helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.adapter.SearchAdapter;
import com.nearby.shubh.nearby.models.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sh on 4/21/2016.
 */
public class SearchQueryAsyncTask {

    private static final String URL_STRING = "http://nbplaces.in/api/places/placesForMap.php?";
    private static final String KEY_URL_LAT = "lat";
    private static final String KEY_URL_LONG = "long";
    private static final String KEY_URL_RAD = "rad";
    private static final String KEY_PLACE_AREA = "place_area";
    private static final String KEY_PLACE_ADDRESS = "place_address";
    private static final String KEY_RESULT = "result";
    private static final String KEY_PHONE_NO = "phone_no";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_TAG = "place_tag";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PLACE_LATITUDE = "latitude";
    private static final String KEY_PLACE_LONGITUDE = "longitude";
    private static final String KEY_PLACE_GEO_LOCATION = "geo_location";
    private static final String KEY_PLACE_URL = "url_thumbnail";

    private RequestQueue requestQueue;
    private OnSearchResultListener onSearchResultListener;
    private static ArrayList<Place> placeList;
    private static String lastQuery="";
    private static String newQuery;
    private Context context;
    private String generatedUrl;

    public SearchQueryAsyncTask(Context context, String query,String lat, String lon ,String rad, OnSearchResultListener searchResultListener) {
        this.context = context;
        requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        newQuery = query;
        this.onSearchResultListener = searchResultListener;
        if(lastQuery.equals(newQuery)&&placeList!=null&&!placeList.isEmpty()){
            onSearchResultListener.onResult(placeList);
        }
        else {
            if(placeList!=null) {
                placeList.clear();
            }
            generatedUrl = makeUrl(lat,lon,rad);
            Log.e("shubh",makeUrl(lat,lon,rad));
            placeList=new ArrayList<>();
            startSearch();
        }
    }
    private String makeUrl(String lat, String lon ,String rad){
        return URL_STRING+KEY_URL_LAT+"="+lat+"&"+KEY_URL_LONG+"="+lon+"&"+KEY_URL_RAD+"="+rad+"&search_query="+newQuery;
    }
    private void startSearch(){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, generatedUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                new searchAsync().execute(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onSearchResultListener.onError("Serer down");
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    class searchAsync extends AsyncTask<JSONObject, Void, String> {


        @Override
        protected void onPreExecute() {
            placeList=new ArrayList<>();
        }

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            JSONObject response = jsonObjects[0];
            if (response != null || response.length() != 0) {
                try {
                    int status = response.getInt(KEY_STATUS);
                    if (status == 200) {
                        JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                        for (int i = 0; i < arrayResult.length(); i++) {
                            JSONObject currentPlace = arrayResult.getJSONObject(i);
                            String id = currentPlace.getString(KEY_PLACE_ID);
                            String name = currentPlace.getString(KEY_PLACE_NAME);
                            String area = currentPlace.getString(KEY_PLACE_AREA);
                            String address = currentPlace.getString(KEY_PLACE_ADDRESS);
                            String phoneNo = currentPlace.getString(KEY_PHONE_NO);
                            String likes = currentPlace.getString(KEY_LIKES);
                            String placeUrl = currentPlace.getString(KEY_PLACE_URL);
                            JSONArray tags = currentPlace.getJSONArray(KEY_PLACE_TAG);
                            String placeTags = "";
                            for (int j = 0; j < tags.length(); j++) {
                                placeTags = placeTags + tags.getString(j);
                                if (j != tags.length() - 1) {
                                    placeTags = placeTags + ",";
                                }
                            }
                            JSONObject geoLocation = currentPlace.getJSONObject(KEY_PLACE_GEO_LOCATION);
                            String lat = geoLocation.getString(KEY_PLACE_LATITUDE);
                            String lon = geoLocation.getString(KEY_PLACE_LONGITUDE);
                            placeList.add(new Place(id, name, placeTags,area,address,phoneNo, placeUrl,likes,lat,lon));
                        }
                    } else {
                        return "No Places Found";
                    }
                } catch (JSONException ex) {
                    return "Could Not Connect At the Moment";
                }
            }
            if(placeList.isEmpty()){
                return "No Places Found";
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("OK")) {
                onSearchResultListener.onResult(placeList);
                lastQuery = newQuery;
            }
            else{
                onSearchResultListener.onError(s);
            }
        }
    }

    public interface OnSearchResultListener {
        void onResult(ArrayList<Place> places);

        void onError(String error);
    }
}
