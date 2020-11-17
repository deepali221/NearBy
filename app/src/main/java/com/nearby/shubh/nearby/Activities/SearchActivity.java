package com.nearby.shubh.nearby.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;

import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.nearby.shubh.nearby.Fragments.FragmentSearchList;
import com.nearby.shubh.nearby.Fragments.FragmentSearchMap;
import com.nearby.shubh.nearby.R;

public class SearchActivity extends AppCompatActivity {

    private static final String URL_STRING = "http://nbplaces.in/api/places/Places.php";
    private static final String KEY_RESULT = "result";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_TAG = "place_tag";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PLACE_URL= "url_thumbnail";
    private static final int FRAG_LIST= 0;
    private static final int FRAG_MAP= 1;
    private static int radius = 1;

    FloatingActionButton fab;
    FrameLayout mainContainer;
    private static int CURRENT_FRAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.fab_pressed));
        }
        setContentView(R.layout.activity_search);

        mainContainer = (FrameLayout) findViewById(R.id.search_frame_container);
        Fragment searchListFragment = new FragmentSearchList();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.search_frame_container,searchListFragment).commit();
        CURRENT_FRAG = FRAG_LIST;

        ImageButton filterButton = (ImageButton) findViewById(R.id.filter_btn);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterClick();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.map_fab);
        setFabIcon(BitmapFactory.decodeResource(getResources(),R.drawable.map_icon));
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Fragment fragment;
                if(CURRENT_FRAG==FRAG_LIST){
                    fragment = new FragmentSearchMap();
                    CURRENT_FRAG = FRAG_MAP;
                    setFabIcon(BitmapFactory.decodeResource(getResources(),R.drawable.list_icon));
                }else{
                    fragment = new FragmentSearchList();
                    CURRENT_FRAG=FRAG_LIST;
                    setFabIcon(BitmapFactory.decodeResource(getResources(),R.drawable.map_icon));
                }
                FragmentTransaction fragT = getSupportFragmentManager().beginTransaction();
                fragT.replace(R.id.search_frame_container,fragment,"frag").commit();
            }
        });
    }
    private Bundle createBundle(){
        Bundle bundle = new Bundle();
        bundle.putString("edttext", "From Activity");

        return bundle;
    }
    private void onFilterClick(){
        final View update_layout = getLayoutInflater().inflate(
                R.layout.filter_layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your title");

        final Spinner spinner = (Spinner) update_layout.findViewById(R.id.spinner);
        String[] items = { "1",  "2",  "3",  "4",  "5",
                "6",  "7",  "8",  "9",  "10",
                "11", "12", "13", "14", "15" };
        spinner.setSelection(getRadius()-1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,
                android.R.layout.simple_spinner_dropdown_item, items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(getRadius()-1);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                putRadius(spinner.getSelectedItemPosition()+1);
            }
        });


        builder.setView(update_layout);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setFabIcon(Bitmap bm){
        fab.setImageBitmap(bm);
    }

    @Override
    public void onBackPressed() {
        try {
            Fragment frag = (FragmentSearchMap) getSupportFragmentManager().findFragmentByTag("frag");
            if (frag != null && frag.isVisible()) {
                if (frag.getView().findViewById(R.id.single_place_map_frame_container).getVisibility() == View.VISIBLE) {
                    frag.getView().findViewById(R.id.single_place_map_frame_container).setVisibility(View.GONE);
                    fab.setVisibility(View.VISIBLE);
                } else {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
        catch (Exception ex){
            super.onBackPressed();
        }
    }

    public interface OnDirectionButtonListener{
        void onDirectionButtonClicked(double placeLatitude,double placeLongitude);
    }

    public static int getRadius(){
        return radius;
    }
    private void putRadius(int r){
        radius = r;
    }
}
