package com.nearby.shubh.nearby.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
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
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by sh on 3/11/2016.
 */
public class SplashScreenActivity extends Activity{

    private DbManager dbManager;
    RequestQueue requestQueue;
    VolleySingleton volleySingleton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activirty_splash);

        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getRequestQueue();

        dbManager = new DbManager(getApplicationContext());

        init();

    }
    private void startLocationService(){
        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
            dialog.setMessage("Location is disabled");
            dialog.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    startLocationService();
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    return;
                }
            });
            dialog.show();
        }
    }

    private void init(){
        if(isNetworkAvailable()) {
            MyApplication myApp = MyApplication.getInstance();
            getTags(myApp, new MyApplication.LoadListener() {
                @Override
                public void onLoad() {
                    SharedDataClass.getLoggedStatus(SplashScreenActivity.this, new SharedDataClass.SharedDataListener() {
                        @Override
                        public void onComplete(boolean result) {
                            if (result) {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                            finish();
                        }
                    });
                }
            });
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle("Connection Error");
            builder.setMessage("Please Check Connection and Try Again");
            builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    init();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            //Toast.makeText(SplashScreenActivity.this, "Please make sure you are connected Internet and Restart.", Toast.LENGTH_LONG).show();
            //finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void getTags(final MyApplication myApp, final MyApplication.LoadListener mListener){
        final int[] res = new int[1];
        final int dbcount = dbManager.getTagsDbCount();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://nbplaces.in/api/tags/tagsCount.php", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null || response.length() != 0) {
                    Log.e("shubh", "server Count");
                    try {
                        int status = response.getInt("status");
                        if (status == 200) {
                            JSONObject result = response.getJSONObject("result");
                            int count;
                            count = Integer.parseInt(result.getString("total_tags"));
                            res[0] = count;
                            if(dbcount<res[0]){
                                Log.e("shubh","db db"+dbcount);
                                myApp.getServerTags(mListener);
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
