package com.nearby.shubh.nearby.Database;

import android.content.Context;
import android.content.SharedPreferences;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.nearby.shubh.nearby.models.FbUserClass;

/**
 * Created by sh on 3/17/2016.
 */
public class SharedDataClass {

    private static final String FILENAME = "nb_profile";
    private static final String LOGGED_STATUS = "logged_status";
    private static final String FB_USERNAME = "fb_username";
    private static final String FB_EMAIL = "fb_email";
    private static final String FB_URL = "fb_url";
    private static final String USER_ID = "user_id";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";
    private static final String LOCATION_STATUS = "location_status";

    public interface SharedDataListener {
        void onComplete(boolean result);
    }

    public static void putLoggedStatus(Context context,boolean loggedStatus) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGGED_STATUS,loggedStatus);
        editor.commit();
    }
    public static void getLoggedStatus(Context context, SharedDataListener sdListener){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        sdListener.onComplete(sharedPreferences.getBoolean(LOGGED_STATUS, false));
    }
    public static void putUserDetails(Context context,String name, String email, String url) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FB_USERNAME, name);
        editor.putString(FB_EMAIL,email);
        editor.putString(FB_URL,url);
        editor.commit();
    }
    public static FbUserClass getFbUser(Context context){
        FbUserClass user;
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(FB_USERNAME, "null");
        String email = sharedPreferences.getString(FB_EMAIL, "null");
        String url = sharedPreferences.getString(FB_URL, "null");
        user = new FbUserClass(name, email,url);
        return user;
    }
    public static String getUserId(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME,context.MODE_PRIVATE);
        String id = sharedPreferences.getString(USER_ID,"null");
        return id;
    }
    public static void setUserId(Context context,String id){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME,context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID,id);
        editor.commit();
    }
    public static void putLocationStatus(Context context,boolean locationStatus) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOCATION_STATUS,locationStatus);
        editor.commit();
    }
    public static boolean getLocationStatus(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(LOGGED_STATUS, false);
    }

    public static void setLocation(Context context,String latitude,String longitude){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME,context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LATITUDE,latitude);
        editor.putString(LONGITUDE,longitude);
        editor.putBoolean(LOCATION_STATUS,true);
        editor.commit();
    }
    public static LatLng getSavedLocation(Context context){
        LatLng latLng;
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME,context.MODE_PRIVATE);
        String lat = sharedPreferences.getString(LATITUDE,"null");
        String lon = sharedPreferences.getString(LONGITUDE,"null");
        if(!lat.equals("null")&&!lon.equals("null")) {
            latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
        }else {
            latLng = null;
        }
        return latLng;
    }
}
