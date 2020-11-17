package com.nearby.shubh.nearby.Activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nearby.shubh.nearby.Database.DbManager;
import com.nearby.shubh.nearby.Network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sh on 11/25/2015.
 */
public class MyApplication extends MultiDexApplication {
    private static MyApplication sInstance;
    private static boolean recentlyCheckedLocation=false;

    RequestQueue requestQueue;
    VolleySingleton volleySingleton;

    DbManager dbManager;
    Map<Integer,String> globalTagMap;
    String globalLatitude;
    String globalLongitude;

    public interface LoadListener{
        public void onLoad();
    }

    public static void setRecentlyCheckedLocation(boolean checked){
        recentlyCheckedLocation = checked;
    }
    public static boolean isRecentlyCheckedLocation(){
        return recentlyCheckedLocation;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("shubh", "Application on create");
        sInstance=this;
        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getRequestQueue();

        dbManager = new DbManager(getApplicationContext());
        globalTagMap = new HashMap<Integer,String>();

    }

    public static MyApplication getInstance(){
        return sInstance;
    }

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }

    public void getServerTags(final LoadListener mListener){

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://nbplaces.in/api/tags/", new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null || response.length() != 0) {
                        Log.e("shubh", "got response");
                        try {
                            int status = response.getInt("status");
                            if (status == 200) {
                                JSONArray result = response.getJSONArray("result");
                                int tag_id;
                                String tag_name;
                                for (int i = 0; i < result.length(); i++) {
                                    JSONObject obj = result.getJSONObject(i);
                                    tag_id = Integer.parseInt(obj.getString("tag_code"));
                                    tag_name = obj.getString("tag_name");
                                    globalTagMap.put(tag_id, tag_name);
                                }
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dbManager.insertTags(globalTagMap)) {
                                        }
                                    }
                                });
                                thread.run();
                                Log.e("shubh", globalTagMap.get(1));
                                Toast.makeText(getAppContext(), globalTagMap.get(1), Toast.LENGTH_LONG).show();
                            }
                            Log.e("shubh", "done");

                        } catch (JSONException ex) {
                            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                        }
                        mListener.onLoad();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            // Show timeout error message
                            Toast.makeText(getApplicationContext(),
                                    "Oops. Timeout error!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
    }

    public void getTags(final LoadListener mListener){
        final int[] res = new int[1];
        final int dbcount = dbManager.getTagsDbCount();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://nbplaces.in/api/tags/tagsCount.php", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null || response.length() != 0) {
                    Log.e("shubh","server Count");
                    try {
                        int status = response.getInt("status");
                        if (status == 200) {
                            JSONObject result = response.getJSONObject("result");
                            int count;
                            count = Integer.parseInt(result.getString("total_tags"));
                            res[0] = count;
                            if(dbcount<res[0]){
                                Log.e("shubh","db db"+dbcount);
                                getServerTags(mListener);
                                return;
                            }
                        }
                    } catch (JSONException ex) {
                        Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                    mListener.onLoad();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        // Show timeout error message
                        Toast.makeText(getApplicationContext(),
                                "Oops. Timeout error!",
                                Toast.LENGTH_LONG).show();
                    }
                }
                mListener.onLoad();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}
