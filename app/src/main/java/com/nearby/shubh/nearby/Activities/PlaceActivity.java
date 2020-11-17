package com.nearby.shubh.nearby.Activities;

import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.ViewModels.PhotoCubeView;
import com.nearby.shubh.nearby.ViewModels.TagView;
import com.nearby.shubh.nearby.adapter.ReviewsAdapter;
import com.nearby.shubh.nearby.models.Place;
import com.nearby.shubh.nearby.models.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaceActivity extends AppCompatActivity {

    private static final String URL_STRING_PLACE = "http://nbplaces.in/api/places/getPlace.php?place_id=";
    private static final String URL_STRING_PHOTOS = "http://nbplaces.in/api/places/photos.php?place_id=";
    private static final String URL_STRING_REVIEWS = "http://nbplaces.in/api/places/reviews.php?place_id=";
    private static final String KEY_RESULT = "result";
    private static final String KEY_PHOTO_URL ="photo_url";
    private static final String KEY_PLACE_ID = "place_id";
    private static final String KEY_PLACE_AREA = "place_area";
    private static final String KEY_PLACE_ADDRESS = "place_address";
    private static final String KEY_PHONE_NO = "phone_no";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_URL_THUMBNAIL = "url_thumbnail";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_TAG = "place_tag";
    private static final String KEY_STATUS = "status";
    private static final String KEY_COMMENTS = "comments";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_DATE = "date";
    private static final String KEY_COMMENT_USER_URL = "user_url";
    private static final String KEY_USER_NAME = "full_name";

    private int lastTop =0;
    private TextView placeName, placeArea, placeAddress,placeLikes, placePhoneNo,commentsError;
    private Button addComment;
    private ProgressBar loadingProg,imgLoadingProg,commentsLoadingProg;
    private ScrollView placeView;
    private LinearLayout linearTags,linearPhotos;
    private RequestQueue requestQueue;
    private RecyclerView rcvComments;
    private ImageLoader imageLoader;
    ImageView placeImage;
    private ReviewsAdapter reviewsAdapter;
    private String mPlace_id;

    public PlaceActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        placeName = (TextView) findViewById(R.id.place_name);
        placeArea = (TextView) findViewById(R.id.place_area);
        placeImage = (ImageView) findViewById(R.id.place_image);
        placeAddress = (TextView) findViewById(R.id.place_address);
        placeLikes = (TextView) findViewById(R.id.place_likes);
        placePhoneNo = (TextView) findViewById(R.id.place_phone_no);
        addComment = (Button) findViewById(R.id.add_comment);
        loadingProg = (ProgressBar) findViewById(R.id.loading_prog);
        imgLoadingProg = (ProgressBar) findViewById(R.id.img_loading_prog);
        commentsLoadingProg = (ProgressBar) findViewById(R.id.comments_loading);
        commentsError = (TextView) findViewById(R.id.comments_error);
        placeView = (ScrollView) findViewById(R.id.place_view);
        reviewsAdapter = new ReviewsAdapter(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            placeView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    parallax(placeImage);
                }
            });
        }
        linearTags = (LinearLayout) findViewById(R.id.linear_for_tags);
        linearPhotos = (LinearLayout) findViewById(R.id.linear_for_photos);

        rcvComments = (RecyclerView) findViewById(R.id.rcv_comments);
        rcvComments.setLayoutManager(new LinearLayoutManager(this));
        rcvComments.setAdapter(reviewsAdapter);
        rcvComments.setVisibility(View.GONE);

        requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        imageLoader = VolleySingleton.getsInstance().getmImageLoader();

        placeView.setVisibility(View.GONE);
        imgLoadingProg.setVisibility(View.VISIBLE);
        loadingProg.setVisibility(View.VISIBLE);
        commentsLoadingProg.setVisibility(View.VISIBLE);

        addCommentButton();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mPlace_id = extras.getString("place_id");
            getPlace(mPlace_id);
            getPhotos(mPlace_id);
            getReviews(mPlace_id);
        }
    }

    private void addCommentButton(){
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CommentsActivity.class);
                intent.putExtra("place_id",mPlace_id);
                startActivity(intent);
            }
        });
    }

    private void getPlace(String placeId){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrl(placeId), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handleResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }

    private void getPhotos(String placeId){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrlPhotos(placeId), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                new getPhotos().execute(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }

    private void getReviews(String placeId){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrlReviews(placeId), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                new getReviews().execute(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }

    private String getUrl(String pId){
        return URL_STRING_PLACE+pId;
    }

    private String getUrlPhotos(String pId){
        return URL_STRING_PHOTOS+pId;
    }

    private String getUrlReviews(String pId){
        return URL_STRING_REVIEWS+pId;
    }

    private void handleResponse(JSONObject response){
        Place place=null;
        if (response != null || response.length() != 0) {

            try {
                int status = response.getInt(KEY_STATUS);
                if(status == 200) {
                    JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                    for (int i = 0; i < arrayResult.length(); i++) {
                        JSONObject currentPlace = arrayResult.getJSONObject(i);
                        String id = currentPlace.getString(KEY_PLACE_ID);
                        String name = currentPlace.getString(KEY_PLACE_NAME);
                        String area = currentPlace.getString(KEY_PLACE_AREA);
                        String address = currentPlace.getString(KEY_PLACE_ADDRESS);
                        String phoneNo = currentPlace.getString(KEY_PHONE_NO);
                        String likes = currentPlace.getString(KEY_LIKES);
                        String urlThumbnail = currentPlace.getString(KEY_URL_THUMBNAIL);
                        JSONArray tags = currentPlace.getJSONArray(KEY_PLACE_TAG);
                        String placeTags ="";
                        for (int j =0; j < tags.length();j++){
                            placeTags = placeTags+tags.getString(j);
                            if(j!= tags.length()-1){
                                placeTags = placeTags+",";
                            }
                        }
                        place = new Place(id, name, placeTags,area,address,phoneNo,urlThumbnail,likes);
                    }
                    showPlace(place);
                }
                else{
                    parsingError();
                }
            } catch (JSONException ex) {
                parsingError();
            }
        }
    }

    private void parsingError(){}

    private void showPlace(Place place){
        if(( linearTags).getChildCount() > 0)
            ( linearTags).removeAllViews();
        placeName.setText(place.getName());
        placeAddress.setText(place.getAddress());
        placePhoneNo.setText(place.getPhoneNo());
        placeLikes.setText(place.getLikes()+ " Likes");
        placeArea.setText(place.getArea());
        getPlaceImage(place.getUrl_thumbnail());
        List<String> tagList = Arrays.asList(place.getType().split(","));
        for (String s: tagList) {
            linearTags.addView(new TagView(this, s));
        }
        placeView.setVisibility(View.VISIBLE);
        loadingProg.setVisibility(View.GONE);
    }

    private void getPlaceImage(String url){
        String urlDp = url;
        if(urlDp!= null || urlDp !="null"){
            imageLoader.get(urlDp, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    placeImage.setImageBitmap(response.getBitmap());
                    imgLoadingProg.setVisibility(View.GONE);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Error While Retrieving the Profile pic", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void parallax(final View v){
        final Rect r = new Rect();
        v.getLocalVisibleRect(r);

        if(lastTop !=r.top){
            lastTop=r.top;
            v.post(new Runnable() {
                @Override
                public void run() {
                    v.setY((float) (r.top / 2.0));
                }
            });
        }
    }

    private void putPhotos(String url, String type) {
        if(type == "CAM"){
            linearPhotos.addView(new PhotoCubeView(getApplicationContext(), ResourcesCompat.getDrawable(getResources(), R.drawable.camera, null)));
        }else {
            VolleySingleton.getsInstance().getmImageLoader().get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    linearPhotos.addView(new PhotoCubeView(getApplicationContext(), response.getBitmap()));
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    class getPhotos extends AsyncTask<JSONObject,Void,ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(JSONObject... jsonObjects) {
            JSONObject response = jsonObjects[0];
            ArrayList<String> photoList = new ArrayList<>();
            if (response != null || response.length() != 0) {
                try {
                    int status = response.getInt(KEY_STATUS);
                    if(status == 200) {
                        JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                        JSONArray urls = (arrayResult.getJSONObject(0)).getJSONArray(KEY_PHOTO_URL);
                        for (int i = 0; i < urls.length(); i++) {
                            String url = urls.getString(i);
                            photoList.add(url);
                        }
                        photoList.add("CAM");
                    }
                    else{
                        photoList.add("CAM");
                    }
                } catch (JSONException ex) {
                    photoList.add("CAM");
                }
            }
            return photoList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> photoList) {
            for (String s: photoList) {
                if(s!="CAM"){
                    putPhotos(s,"IMG");
                }else{
                    putPhotos("","CAM");
                }
            }
        }
    }

    class getReviews extends AsyncTask<JSONObject,Void,Boolean>{

        @Override
        protected Boolean doInBackground(JSONObject... jsonObjects) {

            JSONObject response = jsonObjects[0];
            ArrayList<Review> list = new ArrayList<>();
            if(response!=null ||response.length()!=0){
                try{
                    int status = response.getInt(KEY_STATUS);
                    if(status==200){
                        JSONArray arrayResult = response.getJSONArray(KEY_RESULT);
                        JSONArray comments = (arrayResult.getJSONObject(0)).getJSONArray(KEY_COMMENTS);
                        if(comments.length()<=0){
                            return false;
                        }
                        for(int i=0;i<comments.length();i++){
                            JSONObject currentComment = comments.getJSONObject(i);
                            String user_id = currentComment.getString(KEY_USER_ID);
                            String comment = currentComment.getString(KEY_COMMENT);
                            String userName = currentComment.getString(KEY_USER_NAME);
                            String date = currentComment.getString(KEY_DATE);
                            String user_url = currentComment.getString(KEY_COMMENT_USER_URL);
                            if(user_url==null){
                                user_url = "null";
                            }
                            if(userName==null){
                                userName = "null";
                            }
                            list.add(new Review(userName,comment,user_id,user_url,date));
                        }
                        reviewsAdapter.setList(list);
                    }
                }
                catch (JSONException ex){
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                rcvComments.setVisibility(View.VISIBLE);
                commentsError.setVisibility(View.GONE);
                commentsLoadingProg.setVisibility(View.GONE);
            }else{
                rcvComments.setVisibility(View.GONE);
                commentsError.setVisibility(View.VISIBLE);
                commentsLoadingProg.setVisibility(View.GONE);
            }
        }
    }
}
