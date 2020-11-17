package com.nearby.shubh.nearby.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nearby.shubh.nearby.Activities.SearchActivity;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;

/**
 * Created by sh on 4/22/2016.
 */
public class FragmentMarkerPlace extends Fragment {

    ImageView placeImage;
    TextView placeName,placeAddress;
    ImageLoader imageLoader;
    FloatingActionButton directionButton;
    SearchActivity.OnDirectionButtonListener mOnDirectionButtonListener = null;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try{
            mOnDirectionButtonListener = (SearchActivity.OnDirectionButtonListener) getParentFragment();
        }
        catch(Exception ex){

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_place_overlap_map,container,false);
        imageLoader = VolleySingleton.getsInstance().getmImageLoader();
        Bundle bundle = getArguments();
        String id = bundle.getString("id");
        String name = bundle.getString("name");
        String address = bundle.getString("address");
        String url = bundle.getString("url");
        final String lat = bundle.getString("lat");
        final String lon = bundle.getString("lon");

        placeImage = (ImageView) view.findViewById(R.id.place_image);
        placeName = (TextView) view.findViewById(R.id.place_name);
        placeAddress = (TextView) view.findViewById(R.id.place_address);
        directionButton = (FloatingActionButton) view.findViewById(R.id.fab);

        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnDirectionButtonListener!=null){
                    mOnDirectionButtonListener.onDirectionButtonClicked(Double.parseDouble(lat),Double.parseDouble(lon));
                }
            }
        });

        placeName.setText(name);
        placeAddress.setText(address);
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                placeImage.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        return view;
    }
}
