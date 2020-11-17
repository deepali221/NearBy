package com.nearby.shubh.nearby.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.nearby.shubh.nearby.Activities.SearchActivity;
import com.nearby.shubh.nearby.Database.DbManager;
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.Helper.SearchQueryAsyncTask;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.adapter.NearbyAdapter;
import com.nearby.shubh.nearby.adapter.SearchAdapter;
import com.nearby.shubh.nearby.models.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sh on 4/21/2016.
 */
public class FragmentSearchList extends Fragment {

    private static final String URL_STRING = "http://nbplaces.in/api/places/Places.php";
    private static final String KEY_RESULT = "result";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_TAG = "place_tag";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PLACE_URL= "url_thumbnail";

    SearchActivity activity;
    ArrayList<String> tags;
    RecyclerView searchSuggestionView;
    SearchAdapter searchAdapter;
    RecyclerView rc;
    SearchView searchView;
    ProgressBar loadingProg;
    TextView errorTextView;
    VolleySingleton volleySingleton;
    RequestQueue requestQueue;
    NearbyAdapter adapter;
    DbManager dbManager;
    String lat;
    String lon;
    String rad = "1";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volleySingleton =VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getRequestQueue();
        dbManager = new DbManager(getContext());
        adapter = new NearbyAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list,container,false);
        activity = (SearchActivity) getActivity();

        searchSuggestionView = (RecyclerView) getActivity().findViewById(R.id.search_suggestion_list_view);
        searchSuggestionView.setLayoutManager(new LinearLayoutManager(getContext()));
        errorTextView = (TextView) view.findViewById(R.id.volley_error_message);
        loadingProg = (ProgressBar) view.findViewById(R.id.loading_prog);

        //retrieving saved location
        if(SharedDataClass.getLocationStatus(getContext())){
            LatLng latLng = SharedDataClass.getSavedLocation(getContext());
            lat = latLng.getLatitude()+"";
            lon = latLng.getLongitude()+"";
        }
        else{
            lat = "";
            lon="";
        }

        tags = dbManager.getTags();
        searchAdapter = new SearchAdapter(getContext(), tags, new SearchAdapter.OnMySuggestionListener() {
            @Override
            public void onSuggestionClick(String itemText) {
                startSearchWithQuery(itemText);
            }
        });
        searchSuggestionView.setAdapter(searchAdapter);
        searchSuggestionView.setVisibility(View.GONE);

        rc = (RecyclerView) view.findViewById(R.id.rcv_places);
        rc.setLayoutManager(new LinearLayoutManager(getContext()));
        rc.setAdapter(adapter);
        rc.setVisibility(View.GONE);

        searchView = (SearchView) getActivity().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2) {

                    initSearch();

                    new SearchQueryAsyncTask(getContext(), query, lat, lon, activity.getRadius()+"", new SearchQueryAsyncTask.OnSearchResultListener() {
                        @Override
                        public void onResult(ArrayList<Place> places) {
                            adapter.setList(places);
                            adapter.notifyDataSetChanged();
                            rc.setVisibility(View.VISIBLE);
                            loadingProg.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(String error) {
                            errorTextView.setText(error);
                            errorTextView.setVisibility(View.VISIBLE);
                            loadingProg.setVisibility(View.GONE);
                        }
                    });
                    //String searchQuery = urlBuilder(query);
                    //Log.e("shubh", searchQuery);
                    //startSearch(searchQuery);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText.length() >0){
                    searchSuggestionView.setVisibility(View.VISIBLE);
                    searchAdapter.filter(newText);
                }
                else{
                    searchAdapter.filter("");
                    searchSuggestionView.setVisibility(View.GONE);
                }
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    //showInputMethod(view);
                }else{
                    searchSuggestionView.setVisibility(View.GONE);

                }
            }
        });

        Bundle extras = getActivity().getIntent().getExtras();
        String search;
        if (extras != null) {
            searchView.setIconified(false);
            //searchView.clearFocus();
            search = extras.getString("search_query");
            startSearchWithQuery(search);
        }else {

            if (!searchView.getQuery().toString().trim().equals("") && searchView != null) {
                startSearchWithQuery(searchView.getQuery().toString());
            } else {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchView.setIconified(false);
                    }
                }, 50);
                searchView.requestFocus();
            }
        }

        return  view;
    }

    private void initSearch(){
        searchSuggestionView.setVisibility(View.GONE);
        rc.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        loadingProg.setVisibility(View.VISIBLE);
    }

    private void startSearchWithQuery(String query){
        searchSuggestionView.setVisibility(View.GONE);
        loadingProg.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        searchView.setQuery(query,true);
        searchView.clearFocus();
    }

    private String urlBuilder(String query){
        return URL_STRING+"?search_query="+query;
    }

    private void startSearch(String searchQuery){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, searchQuery, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                new PutResultsTask().execute(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorTextView.setText("Could not Connect at the Moment");
                errorTextView.setVisibility(View.VISIBLE);
                loadingProg.setVisibility(View.GONE);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    class PutResultsTask extends AsyncTask<JSONObject,Void,String> {
        @Override
        protected void onPreExecute() {
            rc.setVisibility(View.GONE);
            errorTextView.setVisibility(View.GONE);
            loadingProg.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            JSONObject response = jsonObjects[0];
            ArrayList<Place> list = new ArrayList<>();
            if (response != null || response.length() != 0) {

                try {
                    int status = response.getInt(KEY_STATUS);
                    if(status == 200) {
                        JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                        for (int i = 0; i < arrayResult.length(); i++) {
                            JSONObject currentPlace = arrayResult.getJSONObject(i);
                            String id = currentPlace.getString(KEY_PLACE_ID);
                            String name = currentPlace.getString(KEY_PLACE_NAME);
                            String placeUrl = currentPlace.getString(KEY_PLACE_URL);
                            JSONArray tags = currentPlace.getJSONArray(KEY_PLACE_TAG);
                            String placeTags ="";
                            for (int j =0; j < tags.length();j++){
                                placeTags = placeTags+tags.getString(j);
                                if(j!= tags.length()-1){
                                    placeTags = placeTags+",";
                                }
                            }
                            list.add(new Place(id,name, placeTags,placeUrl));
                        }
                        adapter.setList(list);
                    }
                    else{
                        return "No Places Found";
                    }
                } catch (JSONException ex) {
                    return "Could Not Connect At the Moment";
                }
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result=="OK"){
                adapter.notifyDataSetChanged();
                rc.setVisibility(View.VISIBLE);
                loadingProg.setVisibility(View.GONE);
            }
            else {
                errorTextView.setText(result);
                errorTextView.setVisibility(View.VISIBLE);
                loadingProg.setVisibility(View.GONE);
            }
        }
    }
}
