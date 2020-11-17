package com.nearby.shubh.nearby.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sh on 9/11/2015.
 */
public class LoginActivity extends Activity{

    private static final String GET_USER_ID_URL ="http://nbplaces.in/api/users/getuserid.php?user_name=";
    private static final String ADD_USER_URL= "http://nbplaces.in/api/users/AddUser.php";
    private static final String KEY_USER_NAME= "user_name";
    private static final String KEY_FULL_NAME= "full_name";
    private static final String KEY_USER_URL = "user_url";

    private ProgressBar loadingProg;
    private RequestQueue requestQueue;
    private CallbackManager mCallBackManager;
    private FacebookCallback<LoginResult> mCallBack =  new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            //Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
            Profile profile = Profile.getCurrentProfile();
            //Log.e("shubh", "Welcome " + profile.getFirstName() + profile.getId());
            GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject user, GraphResponse response) {
                    loadingProg.setVisibility(View.VISIBLE);
                    if (response.getError() != null) {
                        Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                    } else {
                        JSONObject obj;
                        String name = null;
                           String email = null;
                        String url= null;
                        try {
                            obj = response.getJSONObject();
                            name = obj.getString("name");
                            email = obj.getString("email");
                            url = ((obj.getJSONObject("picture")).getJSONObject("data")).getString("url");
                            Log.e("shubh", user.optString("email")+name+email+url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("shubh", response + "");
                        SharedDataClass.putLoggedStatus(LoginActivity.this, true);
                        SharedDataClass.putUserDetails(LoginActivity.this, name, email, url);
                        serverLogIn(email, name,url);
                    }
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,picture");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "Cancelled By The User", Toast.LENGTH_LONG).show();
            loadingProg.setVisibility(View.GONE);
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(getApplicationContext(), "Unknown Error. Please try after some time.", Toast.LENGTH_LONG).show();
            loadingProg.setVisibility(View.GONE);
        }
    };

    private void serverLogIn(final String user_name, final String full_name,final String url) {
        StringRequest request = new StringRequest(Request.Method.POST, ADD_USER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_LONG).show();
                String id = response;
                SharedDataClass.setUserId(getApplicationContext(),id);
                Log.e("shubh",id);
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(KEY_USER_URL,url);
                params.put(KEY_USER_NAME,user_name);
                params.put(KEY_FULL_NAME,full_name);

                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);

        mCallBackManager = CallbackManager.Factory.create();

        requestQueue = Volley.newRequestQueue(this);
        loadingProg = (ProgressBar) findViewById(R.id.loading_prog);
        loadingProg.setVisibility(View.GONE);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(mCallBackManager, mCallBack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
