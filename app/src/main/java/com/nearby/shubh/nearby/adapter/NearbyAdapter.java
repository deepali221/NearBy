package com.nearby.shubh.nearby.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nearby.shubh.nearby.Activities.PlaceActivity;
import com.nearby.shubh.nearby.Activities.SearchActivity;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.ViewModels.TagView;
import com.nearby.shubh.nearby.models.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sh on 4/3/2016.
 */
public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.MyViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    ImageLoader imageLoader;
    private ArrayList<Place> list;

    public NearbyAdapter(Context context){
        this.context = context;
        imageLoader = VolleySingleton.getsInstance().getmImageLoader();

        inflater = LayoutInflater.from(context);
    }
    public NearbyAdapter(Context context, ArrayList<Place> list){
        this.context = context;
        imageLoader = VolleySingleton.getsInstance().getmImageLoader();
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.single_place_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        if(( holder.linearLayout).getChildCount() > 0)
            ( holder.linearLayout).removeAllViews();
        Place current = list.get(position);
        holder.place_name.setText(current.getName());
        Log.e("shubh", current.getId());
        if(current.getUrl_thumbnail()!="null"){
            imageLoader.get(current.getUrl_thumbnail(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.placeImage.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    //make a default nearby bitmap
                }
            });
        }
        holder.container.setTag(current.getId());
        List<String> tagList = Arrays.asList(current.getType().split(","));
        for (String s: tagList) {
            holder.linearLayout.addView(new TagView(context,s));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(ArrayList<Place> list){
        this.list = list;
    }
    public void removeList(){
        list.clear();
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView placeImage;
        CardView container;
        TextView place_name;
        LinearLayout linearLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            container = (CardView) itemView.findViewById(R.id.place_card);
            container.setOnClickListener(this);
            placeImage = (ImageView) itemView.findViewById(R.id.place_image);
            place_name = (TextView) itemView.findViewById(R.id.place_name);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linear_for_tags);
        }

        @Override
        public void onClick(View view) {
            String tag = view.getTag().toString();
            Log.e("shubh",tag);
            Intent intent = new Intent(context,PlaceActivity.class);
            intent.putExtra("place_id",tag);
            context.startActivity(intent);
        }
    }
}
