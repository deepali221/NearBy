package com.nearby.shubh.nearby.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nearby.shubh.nearby.Activities.SearchActivity;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.adapter.NearbyAdapter;
import com.nearby.shubh.nearby.models.Place;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String URL_STRING = "http://nbplaces.in/api/places/Places.php";
    private static final String KEY_RESULT = "result";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_TAG = "place_tag";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PLACE_URL= "url_thumbnail";

    private ImageView refreshBtn;
    private NearbyAdapter adapter;
    private TextView volleyErrorMessage;
    private ProgressBar loading_prog;
    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private VolleySingleton volleySingleton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            getActivity().getWindow().setSharedElementExitTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.fab_pressed));
        }

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_home, container, false);
        FrameLayout mainLayout = (FrameLayout) view.findViewById(R.id.mainFrameLayout);
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(R.drawable.ic_search_black_24dp);

        FloatingActionButton actionButton = new FloatingActionButton.Builder(getActivity()).setContentView(imageView).build();
        actionButton.detach();
        mainLayout.addView(actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.setTransitionName("start_search");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, view.getTransitionName());
                    Intent intent = new Intent(getContext(), SearchActivity.class);
                    startActivity(intent, optionsCompat.toBundle());
                } else {
                    Intent intent = new Intent(getContext(), SearchActivity.class);
                    startActivity(intent);
                }
            }
        });
        volleyErrorMessage = (TextView) view.findViewById(R.id.volley_error_message);
        loading_prog = (ProgressBar) view.findViewById(R.id.loading_prog);
        recyclerView = (RecyclerView) view.findViewById(R.id.rcv_places);
        refreshBtn = (ImageView) view.findViewById(R.id.refresh);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendJsonRequest();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new NearbyAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        sendJsonRequest();
        return view;
    }

    private void sendJsonRequest() {
        volleyErrorMessage.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        loading_prog.setVisibility(View.VISIBLE);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_STRING, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                volleyErrorMessage.setVisibility(View.GONE);
                //parseJsonResponse(response, adapter);
                new getPlaces().execute(response);
                Log.e("JSON", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyErrorMessage.setVisibility(View.VISIBLE);
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    volleyErrorMessage.setText(R.string.error_timeout);
                    loading_prog.setVisibility(View.GONE);
                    refreshBtn.setVisibility(View.VISIBLE);
                } else if (error instanceof AuthFailureError) {
                    volleyErrorMessage.setText(R.string.error_auth_fail);
                    loading_prog.setVisibility(View.GONE);
                    refreshBtn.setVisibility(View.VISIBLE);

                } else if (error instanceof ServerError) {
                    volleyErrorMessage.setText(R.string.error_server);
                    loading_prog.setVisibility(View.GONE);
                    refreshBtn.setVisibility(View.VISIBLE);

                } else if (error instanceof NetworkError) {
                    volleyErrorMessage.setText(R.string.error_network);
                    loading_prog.setVisibility(View.GONE);
                    refreshBtn.setVisibility(View.VISIBLE);

                } else if (error instanceof ParseError) {
                    volleyErrorMessage.setText(R.string.error_parse);
                    loading_prog.setVisibility(View.GONE);
                    refreshBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        requestQueue.add(request);
    }

    class getPlaces extends AsyncTask<JSONObject,Void,String>{

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            JSONObject response = jsonObjects[0];
            ArrayList<Place> list = new ArrayList<>();
            if (response != null || response.length() != 0) {

                try {
                    int status = response.getInt(KEY_STATUS);
                    if (status == 200) {
                        JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                        for (int i = 0; i < arrayResult.length(); i++) {
                            JSONObject currentPlace = arrayResult.getJSONObject(i);
                            String id = currentPlace.getString(KEY_PLACE_ID);
                            String name = currentPlace.getString(KEY_PLACE_NAME);
                            String place_url = currentPlace.getString(KEY_PLACE_URL);
                            JSONArray tags = currentPlace.getJSONArray(KEY_PLACE_TAG);
                            String placeTags = "";
                            for (int j = 0; j < tags.length(); j++) {
                                placeTags = placeTags + tags.getString(j);
                                if (j != tags.length() - 1) {
                                    placeTags = placeTags + ",";
                                }
                            }
                            list.add(new Place(id, name, placeTags,place_url));
                        }
                        adapter.setList(list);
                        return "OK";
                    }
                    else{
                        return "No Places found";
                    }
                }
                catch (JSONException ex){
                    return "Could not Connect at the Moment";
                }
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("OK")) {
                adapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                loading_prog.setVisibility(View.GONE);
            }
            else {
                refreshBtn.setVisibility(View.VISIBLE);
                volleyErrorMessage.setText(s);
                volleyErrorMessage.setVisibility(View.VISIBLE);
                loading_prog.setVisibility(View.GONE);
            }
        }
    }
}
