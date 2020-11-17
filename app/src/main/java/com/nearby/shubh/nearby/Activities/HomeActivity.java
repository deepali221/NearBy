package com.nearby.shubh.nearby.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.Fragments.FragmentHome;
import com.nearby.shubh.nearby.Fragments.FragmentNearby;
import com.nearby.shubh.nearby.Fragments.FragmentUserProfile;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.Services.LocationService;

import hkm.ui.materialtabs.MaterialTab;
import hkm.ui.materialtabs.MaterialTabHost;
import hkm.ui.materialtabs.MaterialTabListener;

/**
 * Created by sh on 3/18/2016.
 */
public class HomeActivity extends AppCompatActivity implements MaterialTabListener {

    private MaterialTabHost mTabHost;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Nearby");

        //setting up host and pager
        mTabHost = (MaterialTabHost) findViewById(R.id.materialTabHost);
        mTabHost.setAccentColor(ContextCompat.getColor(this, R.color.accentColor));
        mTabHost.setPrimaryColor(ContextCompat.getColor(this, R.color.primaryColorDark));
        mPager = (ViewPager) findViewById(R.id.viewPager);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            mTabHost.addTab(
                    mTabHost.newTab()
                            // .setIcon(getResources().getDrawable(R.drawable.user,null))
                            .setText(mPagerAdapter.getTitle(i))
                            .setTabListener(this)
            );
        }
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTabHost.setSelectedNavigationItem(position);
                toolbar.setTitle(mPagerAdapter.getTitle(position));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
       startLocationService();
    }

    private void startLocationService() {
        if (!isCheckNetworkAvail()) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
            dialog.setMessage("Location is disabled");
            dialog.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, 0);
                    MyApplication.setRecentlyCheckedLocation(true);
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
        else{
            Intent intent = new Intent(this,LocationService.class);
            startService(intent);
        }
    }

    public boolean isCheckNetworkAvail() {
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled || !network_enabled) {
            return false;
        } else return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startLocationService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("shubh", "home destroyed");
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        mPager.setCurrentItem(materialTab.getPosition());
        toolbar.setTitle(mPagerAdapter.getTitle(materialTab.getPosition()));
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }

//-------The pager adapter------

    class MyPagerAdapter extends FragmentStatePagerAdapter {

        String items[] = {"Nearby", "Home", "User"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = null;
            switch (position) {
                case 0:
                    frag = FragmentNearby.newInstance("", "");
                    break;
                case 1:
                    frag = FragmentHome.newInstance("" + position, "");
                    break;
                case 2:
                    frag = FragmentUserProfile.newInstance("", "");
                    break;
            }
            return frag;
        }

        @Override
        public int getCount() {
            return 3;
        }

        public CharSequence getTitle(int pos) {
            return items[pos];
        }
        /*String[] tabs;

        public MyPagerAdapter(FragmentManager fm) {

            super(fm);
            tabs = getResources().getStringArray(R.array.tabs);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            switch (position) {
                case HOME_TAB:
                    fragment = FragmentHomeTab.newInstance("", "");
                    break;
                case FAV_TAB:
                    fragment = FragmentFavoriteTab.newInstance("", "");
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }*/
    }
}
