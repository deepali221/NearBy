package com.nearby.shubh.nearby.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nearby.shubh.nearby.Database.DbManager;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.extras.Keys;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sh on 3/4/2016.
 */
public class AddPlaceActivity extends AppCompatActivity {

    private static final String URL_STRING = "http://nbplaces.in/api/places/AddPlaces.php";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_PLACE_ADDRESS = "place_address";
    private static final String KEY_PLACE_URL = "url_thumbnail";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_PHONE_NO = "phone_no";
    private static final String KEY_WEBSITE = "website";
    private static final String KEY_PLACE_AREA = "place_area";

    private static final String KEY_TAGS_STRING = "tags_string";

    private static final int CAM_REQUEST = 1313;
    private static final int GALLERY_REQUEST = 1414;

    private Button tags_btn, addPlace_btn,getLocaitonbtn,getMapButton;
    private ImageButton imgBtn;
    private ImageView placeThumbnail;
    private EditText longitude_et, latitude_et,place_name, place_address, phone_no, website,place_area;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private DbManager dbManager;
    private ProgressDialog locationProgress;
    private ArrayList<String> tags;
    private Map<String, Integer> reverseTags;

    private boolean[] is_checked;
    private StringBuilder mTagsString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        dbManager = new DbManager(getApplicationContext());

        phone_no = (EditText) findViewById(R.id.add_places_phone_no);
        place_address = (EditText) findViewById(R.id.add_place_address);
        addPlace_btn = (Button) findViewById(R.id.addPlace);
        longitude_et = (EditText) findViewById(R.id.longitude_et);
        latitude_et = (EditText) findViewById(R.id.latitude_et);
        imgBtn = (ImageButton) findViewById(R.id.add_places_thumbnail);
        tags_btn = (Button) findViewById(R.id.btntags);
        place_area = (EditText) findViewById(R.id.add_place_area);
        place_name = (EditText) findViewById(R.id.add_places_placename);
        placeThumbnail = (ImageView) findViewById(R.id.place_image);
        website = (EditText) findViewById(R.id.add_places_website);
        getLocaitonbtn = (Button) findViewById(R.id.btnGetLocation);

        reverseTags = new HashMap<>(dbManager.getReverseTags());
        tags = new ArrayList<>(reverseTags.keySet());

        getMapButton = (Button) findViewById(R.id.get_map_button);
        getMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapBoxActivity.class);
                startActivity(intent);
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String tag = extras.getString("Tag");
            switch (tag){
                case Keys.TAG_FOOD:
                    placeThumbnail.setImageResource(R.drawable.food_default);
                    break;
                case Keys.TAG_DAILY_NEEDS:
                    placeThumbnail.setImageResource(R.drawable.daily_needs_default);
                    break;
                case Keys.TAG_HANGOUT:
                    placeThumbnail.setImageResource(R.drawable.hangout_default);
                    break;
                case Keys.TAG_LODGING:
                    placeThumbnail.setImageResource(R.drawable.lodging_default);
                    break;
                case Keys.TAG_MEDICAL:
                    placeThumbnail.setImageResource(R.drawable.medical_default);
                    break;
                case Keys.TAG_MONEY:
                    placeThumbnail.setImageResource(R.drawable.money_default);
                    break;
                case Keys.TAG_SPORTS:
                    placeThumbnail.setImageResource(R.drawable.sports_default);
                    break;
                case Keys.TAG_TRANSPORTATION:
                    placeThumbnail.setImageResource(R.drawable.transport_default);
                    break;
                case Keys.TAG_UTILITIES:
                    placeThumbnail.setImageResource(R.drawable.utilities_default);
                    break;
                case Keys.TAG_WORSHIP:
                    placeThumbnail.setImageResource(R.drawable.worship_default);
                    break;
            }
        }
        else{
            Toast.makeText(this,"Tag not Selected on the Previous Screen",Toast.LENGTH_LONG).show();
            finish();
        }

        locationProgress = new ProgressDialog(this);
        locationProgress.setMessage("Getting Location");
        locationProgress.setCanceledOnTouchOutside(false);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                latitude_et.setText("" + location.getLatitude());
                longitude_et.setText("" + location.getLongitude());
                Log.e("in", "received");
                try {
                    locationProgress.dismiss();
                } catch (Exception ex) {
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configureImgBtn();
        configureTagsDialogBox();
        configureAddPlacesbtn();

        getLocaitonbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);

                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, locationListener);
                }
                locationProgress.show();
            }
        });
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tags = savedInstanceState.getStringArrayList("tags_list");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("tags_list", tags);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== CAM_REQUEST) {
            if (data == null) {
                return;
            }
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            placeThumbnail.setImageBitmap(thumbnail);
        }else if(requestCode == GALLERY_REQUEST){
            if (data == null) {
                return;
            }
            Uri pickedImage = data.getData();
            Log.e("shubh",""+pickedImage);
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
            placeThumbnail.setImageBitmap(scaled);
        }
    }

    private void configureImgBtn(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(AddPlaceActivity.this);
        builder.setMessage("Take Image From");
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAM_REQUEST);
            }

        });
        builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST);
            }
        });
        final AlertDialog alert = builder.create();

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });
    }

    private void configureTagsDialogBox() {

        tags_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Intialize  readable sequence of char values
                final CharSequence[] dialogList=  tags.toArray(new CharSequence[tags.size()]);
                final AlertDialog.Builder builderDialog = new AlertDialog.Builder(AddPlaceActivity.this);
                builderDialog.setTitle("Select Tags");
                int count = dialogList.length;
                if (is_checked == null) {
                    is_checked = new boolean[count]; // set is_checked boolean false;
                }

                if (mTagsString == null) {
                    mTagsString = new StringBuilder();
                }
                // Creating multiple selection by using setMutliChoiceItem method
                builderDialog.setMultiChoiceItems(dialogList, is_checked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton, boolean isChecked) {
                            }
                        });

                builderDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ListView list = ((AlertDialog) dialog).getListView();
                                // make selected item in the comma seprated string
                                StringBuilder stringBuilder = new StringBuilder();
                                mTagsString.setLength(0);
                                for (int i = 0; i < list.getCount(); i++) {
                                    boolean checked = list.isItemChecked(i);

                                    if (checked) {
                                        if (stringBuilder.length() > 0) stringBuilder.append(",");
                                        stringBuilder.append(list.getItemAtPosition(i));
                                        if (mTagsString.length() > 0) mTagsString.append(",");
                                        mTagsString.append(reverseTags.get(list.getItemAtPosition(i)));
                                        Log.e("tags", mTagsString.toString());
                                    }
                                }

                        /*Check string builder is empty or not. If string builder is not empty.
                          It will display on the screen.
                         */
                                if (stringBuilder.toString().trim().equals("")) {

                                    ((TextView) findViewById(R.id.add_places_tags)).setText("");
                                    stringBuilder.setLength(0);
                                    mTagsString.setLength(0);

                                } else {

                                    ((TextView) findViewById(R.id.add_places_tags)).setText(stringBuilder);
                                }
                            }
                        });

                builderDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((TextView) findViewById(R.id.add_places_tags)).setText("");
                                mTagsString = null;
                                is_checked = null;
                            }
                        });
                AlertDialog alert = builderDialog.create();
                alert.show();
            }
        });

    }

    private void configureAddPlacesbtn(){
        addPlace_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPlace();
            }
        });
    }

    private boolean valid(){
        AlertDialog.Builder alert = new AlertDialog.Builder(AddPlaceActivity.this);
        alert.setTitle("Error");
        alert.setPositiveButton("OK",null);
        if (mTagsString == null || mTagsString.length() ==0){
            alert.setMessage("Please Select At Least Three Tags For Your Place.");
            alert.show();
            return false;
        }
        if(placeThumbnail.getDrawable()== null){
            alert.setMessage("Please Submit A Place Picture.");
            alert.show();
            return false;
        }
        if (place_name.getText().toString().trim().equals("")){
            alert.setMessage("Please Fill Completely.");
            alert.show();
            return false;
        }
        if (place_address.getText().toString().trim().equals("")){
            alert.setMessage("Please Fill Completely.");
            alert.show();
            return false;
        }
        if (place_area.getText().toString().trim().equals("")){
            alert.setMessage("Please Fill Completely.");
            alert.show();
            return false;
        }
        if (latitude_et.getText().toString().trim().equals("")){
            alert.setMessage("Please Give The Location.");
            alert.show();
            return false;
        }
        if (longitude_et.getText().toString().trim().equals("")){
            alert.setMessage("Please Give The Location.");
            alert.show();
            return false;
        }
        return true;
    }

    private void registerPlace(){

        if(!valid()){
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(AddPlaceActivity.this);
        dialog.setMessage("Adding Your Place...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final String placeName = place_name.getText().toString();
        final String placeArea = place_area.getText().toString();
        final String placeAddress = place_address.getText().toString();
        final String placeLatitude = latitude_et.getText().toString();
        final String placeLongitude = longitude_et.getText().toString();
        final Bitmap thumbnail = ((BitmapDrawable)placeThumbnail.getDrawable()).getBitmap();
        final String placePhoneNo;
        final String placeWebsite;
        if(phone_no.getText().toString().trim().equals("")){
            placePhoneNo = "0";
        }
        else{placePhoneNo = phone_no.getText().toString();}
        if(website.getText().toString().trim().equals("")){
             placeWebsite = "null";
        }
        else{placeWebsite = website.getText().toString();}

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_STRING,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(AddPlaceActivity.this,response, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddPlaceActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();

                String image = getStringImage(thumbnail);

                params.put(KEY_PLACE_NAME,placeName);
                params.put(KEY_PLACE_AREA,placeArea);
                params.put(KEY_PLACE_ADDRESS,placeAddress);
                params.put(KEY_PLACE_URL,image);
                params.put(KEY_LATITUDE,placeLatitude);
                params.put(KEY_LONGITUDE,placeLongitude);
                params.put(KEY_TAGS_STRING, mTagsString.toString());
                params.put(KEY_PHONE_NO,placePhoneNo);
                params.put(KEY_WEBSITE,placeWebsite);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);

    }

    private String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
