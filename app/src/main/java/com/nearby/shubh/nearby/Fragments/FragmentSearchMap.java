package com.nearby.shubh.nearby.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.directions.v4.models.DirectionsResponse;
import com.nearby.shubh.nearby.Activities.MyApplication;
import com.nearby.shubh.nearby.Activities.SearchActivity;
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.Helper.SearchQueryAsyncTask;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.adapter.SearchAdapter;
import com.nearby.shubh.nearby.models.Place;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v4.DirectionsCriteria;
import com.mapbox.services.directions.v4.MapboxDirections;
import com.mapbox.services.directions.v4.models.DirectionsResponse;
import com.mapbox.services.directions.v4.models.DirectionsRoute;
import com.mapbox.services.directions.v4.models.Waypoint;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sh on 4/21/2016.
 */
public class FragmentSearchMap extends Fragment implements SearchActivity.OnDirectionButtonListener{

    private SearchActivity activity;
    private ArrayList<Place> placesList;
    private SearchView searchView;
    private MapView mapView;
    private FrameLayout placeContainer;
    private ImageButton getLocationBtn;
    private ProgressBar locationProg;
    private MapboxMap mMap;
    private DirectionsRoute currentRoute;
    private LatLng mCurrentPosition;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_map, container, false);
        activity = (SearchActivity) getActivity();

        searchView = (SearchView) getActivity().findViewById(R.id.searchView);
        placeContainer = (FrameLayout) view.findViewById(R.id.single_place_map_frame_container);
        getLocationBtn = (ImageButton) view.findViewById(R.id.get_location_btn);
        locationProg = (ProgressBar) view.findViewById(R.id.loading_prog);
        locationProg.setVisibility(View.GONE);
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        final LatLng currentPosition = SharedDataClass.getSavedLocation(getContext());
        mCurrentPosition = currentPosition;

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mMap = mapboxMap;
                getLocationBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        currentLocationButtonClicked(currentPosition);

                    }
                });
                if (currentPosition != null) {
                    animateCameraToCurrent(currentPosition);
                }

                if (!searchView.getQuery().toString().trim().equals("") && searchView != null) {
                    locationProg.setVisibility(View.VISIBLE);
                    searchView.setQuery(searchView.getQuery(), true);
                }
                mMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        String name = marker.getTitle();
                        Place clickPlace = null;
                        for (Place p :
                                placesList) {
                            if (p.getName().equals(name)) {
                                clickPlace = p;
                                break;
                            }
                        }
                        if (clickPlace == null) {
                            return false;
                        }

                        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                        FragmentMarkerPlace frag = new FragmentMarkerPlace();
                        Bundle bundle = new Bundle();
                        bundle.putString("id", clickPlace.getId());
                        bundle.putString("name", clickPlace.getName());
                        bundle.putString("address", clickPlace.getAddress());
                        bundle.putString("url", clickPlace.getUrl_thumbnail());
                        bundle.putString("lat", clickPlace.getLatitude());
                        bundle.putString("lon", clickPlace.getLongitude());
                        frag.setArguments(bundle);
                        placeContainer.setVisibility(View.VISIBLE);
                        ft.replace(R.id.single_place_map_frame_container, frag).addToBackStack("null").commit();
                        getActivity().findViewById(R.id.map_fab).setVisibility(View.GONE);
                        return false;
                    }
                });
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mMap.removeAnnotations();
                if (query.length() > 2) {
                    locationProg.setVisibility(View.VISIBLE);
                    new SearchQueryAsyncTask(getContext(), query, currentPosition.getLatitude() + "", currentPosition.getLongitude() + "", activity.getRadius()+"", new SearchQueryAsyncTask.OnSearchResultListener() {
                        @Override
                        public void onResult(ArrayList<Place> places) {
                            placesList = places;
                            for (Place p : places) {
                                placeContainer.setVisibility(View.VISIBLE);
                                double lat = Double.parseDouble(p.getLatitude());
                                double lon = Double.parseDouble(p.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(p.getName()).snippet(p.getName()));
                            }
                            locationProg.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(String error) {
                            locationProg.setVisibility(View.GONE);
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return view;
    }

    private void currentLocationButtonClicked(LatLng currentPosition){
        IconFactory iconFactory = IconFactory.getInstance(getActivity());
        Drawable iconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.current_position);
        Icon icon = iconFactory.fromDrawable(iconDrawable);
        mMap.addMarker(new MarkerOptions().position(currentPosition).icon(icon));
        animateCameraToCurrent(currentPosition);
    }

    private void getRoute(Waypoint origin, Waypoint destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setAccessToken(getResources().getString(R.string.map_box_access_key))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d("shubh", "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e("shubh", "No routes found, make sure you set the right user and access token.");
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Toast.makeText(getContext(), "Route is " +  currentRoute.getDistance() + " meters long.", Toast.LENGTH_SHORT).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e("shubh", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V4);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        mMap.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    private void startLocationService(LocalLocationListener locationListener) {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Getting Your Location...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage("Location is disabled");
            dialog.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, 0);
                    MyApplication.setRecentlyCheckedLocation(true);
                    progressDialog.dismiss();
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
        progressDialog.dismiss();
    }


    private void animateCameraToCurrent(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000);
    }

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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDirectionButtonClicked(double placeLatitude,double placeLongitude) {
        if(mCurrentPosition == null){
            return;
        }
        currentLocationButtonClicked(mCurrentPosition);
        Waypoint origin = new Waypoint(mCurrentPosition.getLatitude(),mCurrentPosition.getLongitude());
        Waypoint des = new Waypoint(placeLatitude,placeLongitude);
        Log.e("shubh","origin- "+ origin.getLatitude()+" "+origin.getLongitude()+"");
        Log.e("shubh","des- " +des.getLatitude()+" "+des.getLongitude()+"");
        try {
            getRoute(origin, des);
        } catch (ServicesException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),"Could not draw route",Toast.LENGTH_LONG).show();
        }
    }

    interface LocalLocationListener {
        void onLocationReceived();
    }
}
