package com.nearby.shubh.nearby.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.nearby.shubh.nearby.Database.SharedDataClass;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sh on 4/15/2016.
 */
public class CommentsActivity extends AppCompatActivity {

    private static final String ADD_COMMENT_URL = "http://nbplaces.in/api/places/addReview.php";
    private Toolbar toolbar;
    private String userId,placeId;
    private Button addComment;
    private ProgressBar loadingProg;
    private EditText comment_et;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            placeId = extras.getString("place_id");
        }
        toolbar.setTitle("Comment");
        addComment = (Button) findViewById(R.id.add_comment);
        requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        comment_et = (EditText) findViewById(R.id.comment);
        userId = SharedDataClass.getUserId(getApplicationContext());
        loadingProg = (ProgressBar) findViewById(R.id.loading_prog);
        Toast.makeText(this,userId + placeId,Toast.LENGTH_LONG).show();

        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String comment = comment_et.getText().toString();
                if(comment.trim()!="") {
                    loadingProg.setVisibility(View.VISIBLE);
                    StringRequest request = new StringRequest(Request.Method.POST, ADD_COMMENT_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();

                            params.put("user_id",userId);
                            params.put("place_id",placeId);
                            params.put("comment",comment);

                            return params;
                        }
                    };
                    requestQueue.add(request);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please Comment",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
