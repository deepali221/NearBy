package com.nearby.shubh.nearby.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.facebook.login.LoginManager;
import com.nearby.shubh.nearby.Activities.AddPlaceActivity;
import com.nearby.shubh.nearby.Activities.LoginActivity;
import com.nearby.shubh.nearby.Activities.MapBoxActivity;
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.extras.Keys;
import com.nearby.shubh.nearby.models.FbUserClass;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserProfile extends Fragment {

    private Button logOutBtn,btn_addPlace,mapBox;
    private TextView tvUsername, tvEmail;
    private FbUserClass currentUser;
    private ImageLoader imageLoader;
    private CircleImageView profilePic;
    private VolleySingleton volleySingleton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FragmentUserProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentUserProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentUserProfile newInstance(String param1, String param2) {
        FragmentUserProfile fragment = new FragmentUserProfile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_user_profile, container, false);
        logOutBtn = (Button) view.findViewById(R.id.logout);
        volleySingleton = VolleySingleton.getsInstance();
        imageLoader = volleySingleton.getmImageLoader();
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                SharedDataClass.putLoggedStatus(getActivity(), false);
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        btn_addPlace = (Button) view.findViewById(R.id.add_places_btn);
        btn_addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPlaceClick();
            }
        });
        currentUser = SharedDataClass.getFbUser(getActivity());
        profilePic = (CircleImageView) view.findViewById(R.id.profile_picture);

        tvUsername = (TextView) view.findViewById(R.id.profile_name);
        tvUsername.setText(currentUser.getName());

        tvEmail = (TextView) view.findViewById(R.id.profile_email);
        tvEmail.setText(currentUser.getEmail());
        getProfilePicture(currentUser.getUrl());
        Log.e("shubh", currentUser.getUrl());
        return view;
    }

    private void onAddPlaceClick(){
        final CharSequence[] items = {Keys.TAG_FOOD, "Money", "Transportation","Medical","Hangout","Utilities","Daily_needs", "Sports", "Lodging","Worship"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                Toast.makeText(getActivity(), items[item], Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), AddPlaceActivity.class);
                intent.putExtra("Tag",items[item]);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getProfilePicture(String url) {
        String urlDp = url;
        if(urlDp!= null || urlDp !="null"){
            imageLoader.get(urlDp, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    profilePic.setImageBitmap(response.getBitmap());
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(),"Error While Retrieving the Profile pic",Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
