package com.nearby.shubh.nearby.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nearby.shubh.nearby.Network.VolleySingleton;
import com.nearby.shubh.nearby.R;
import com.nearby.shubh.nearby.models.Review;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sh on 4/12/2016.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.MyViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    ImageLoader imageLoader;
    private ArrayList<Review> reviewList;

    public ReviewsAdapter(Context context){
        this.context = context;
        imageLoader = VolleySingleton.getsInstance().getmImageLoader();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.single_review,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.userName.setText(review.getName());
        holder.comment.setText(review.getComment());
        holder.commentDate.setText(review.getDate());
        if(review.getUrl() != "null"){
            imageLoader.get(review.getUrl(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.commentUserPhoto.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }
    public void setList(ArrayList<Review> list){
        this.reviewList = list;
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView commentUserPhoto;
        TextView comment,userName,commentDate;
        public MyViewHolder(View itemView) {
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            userName = (TextView) itemView.findViewById(R.id.comment_user_full_name);
            commentUserPhoto = (CircleImageView) itemView.findViewById(R.id.comment_user_dp);
            commentDate = (TextView) itemView.findViewById(R.id.comment_date);
        }
    }
}
