package com.nearby.shubh.nearby.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sh on 4/20/2016.
 */
public class MapBoxActivity extends AppCompatActivity {

    private static final String URL_STRING = "http://nbplaces.in/api/places/placesForMap.php?";
    private static final String KEY_URL_LAT = "lat";
    private static final String KEY_URL_LONG = "long";
    private static final String KEY_URL_RAD = "rad";
    private static final String KEY_RESULT = "result";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_TAG = "place_tag";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PLACE_LATITUDE = "latitude";
    private static final String KEY_PLACE_LONGITUDE = "longitude";
    private static final String KEY_PLACE_GEO_LOCATION= "geo_location";

    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;

    private MapView mapView;
    private LatLng currentLocationLatLng;
    private ImageButton getLocationBtn;
    private ProgressBar locationProg;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private MapboxMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_box);

        getLocationBtn = (ImageButton) findViewById(R.id.get_location_btn);
        locationProg = (ProgressBar) findViewById(R.id.loading_prog);
        locationProg.setVisibility(View.GONE);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mMap = mapboxMap;
                getLocationBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(currentLocationLatLng==null) {
                            locationProg.setVisibility(View.VISIBLE);
                            getCurrentLocation();
                        }
                        else{
                            animateCameraToCurrent(currentLocationLatLng);
                        }
                    }
                });
                // Customize map with markers, polylines, etc.
            }
        });

        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getRequestQueue();
    }

    private void getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng latLong = new LatLng( location.getLatitude(),location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLong).title("Your Location"));
                locationProg.setVisibility(View.GONE);
                animateCameraToCurrent(latLong);
                sendJsonRequest(makeUrl(location.getLatitude()+"",location.getLongitude()+"","1"));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        locationManager.requestLocationUpdates("gps", 50000, 50, locationListener);
    }

    private void animateCameraToCurrent(LatLng latLng){
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(13).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),3000);
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private String makeUrl(String lat, String lon ,String rad){
        return URL_STRING+KEY_URL_LAT+"="+lat+"&"+KEY_URL_LONG+"="+lon+"&"+KEY_URL_RAD+"="+rad;
    }

    private void sendJsonRequest(String url) {
        locationProg.setVisibility(View.VISIBLE);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //parseJsonResponse(response, adapter);
                new getPlaces().execute(response);
                Log.e("JSON", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Could nnot fetch Data",Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }

    class getPlaces extends AsyncTask<JSONObject,Void,ArrayList<LatLng>> {

        @Override
        protected ArrayList<LatLng> doInBackground(JSONObject... jsonObjects) {
            JSONObject response = jsonObjects[0];
            Log.e("shubh",response.toString());
            ArrayList<LatLng> list = new ArrayList<>();
            if (response != null || response.length() != 0) {

                try {
                    int status = response.getInt(KEY_STATUS);
                    if (status == 200) {
                        JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                        for (int i = 0; i < arrayResult.length(); i++) {
                            JSONObject currentPlace = arrayResult.getJSONObject(i);
                            /*String id = currentPlace.getString(KEY_PLACE_ID);
                            String name = currentPlace.getString(KEY_PLACE_NAME);
                            String place_url = currentPlace.getString(KEY_PLACE_URL);
                            JSONArray tags = currentPlace.getJSONArray(KEY_PLACE_TAG);*/
                            JSONObject geoLocation = currentPlace.getJSONObject(KEY_PLACE_GEO_LOCATION);
                            String lat = geoLocation.getString(KEY_PLACE_LATITUDE);
                            String lon = geoLocation.getString(KEY_PLACE_LONGITUDE);
                            list.add(new LatLng(Double.parseDouble(lat) ,Double.parseDouble(lon)));
                        }
                        return list;
                    }
                    else{
                        return null;
                    }
                }
                catch (JSONException ex){
                    return null;
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> list) {
            if(list !=null) {
                Log.e("shubh",list.toString());
                for (LatLng latLng :list) {
                    Log.e("Shubh","added");
                    mMap.addMarker(new MarkerOptions().position(latLng));
                }
            }
        }
    }
}
