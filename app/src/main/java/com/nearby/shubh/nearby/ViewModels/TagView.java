package com.nearby.shubh.nearby.ViewModels;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nearby.shubh.nearby.Activities.HomeActivity;
import com.nearby.shubh.nearby.Activities.SearchActivity;
import com.nearby.shubh.nearby.R;

/**
 * Created by sh on 4/7/2016.
 */
public class TagView extends TextView implements View.OnClickListener{

    HomeActivity activity;
    Context context;
    public TagView(Context context,String text) {
        super(context);
        this.context = context;
        //activity = (HomeActivity)context;
        setText(text);
        setTag("tag");
        setOnClickListener(this);
        this.setBackgroundResource(R.drawable.tags_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.gravity = (Gravity.END & Gravity.BOTTOM);
        params.leftMargin =5;
        params.rightMargin=5;
        this.setLayoutParams(params);
    }
    public TagView(Context context,int rsid,String text){
        super(context);
        this.context = context;
        //activity = (HomeActivity)context;
        setText(text);
        setTextColor(ContextCompat.getColor(context,R.color.colorTextPrimary));
        setTag("tag");
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        setOnClickListener(this);
        this.setBackgroundResource(rsid);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.gravity = (Gravity.END & Gravity.BOTTOM);
        params.topMargin =10;
        this.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        if(view.getTag() == "tag"){
            String search = (String) ((TextView)view).getText();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setTransitionName("start_search");
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, view, view.getTransitionName());
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra("search_query",search);
                context.startActivity(intent, optionsCompat.toBundle());
            }else {
                Intent intent = new Intent(context, SearchActivity.class);
                intent.putExtra("search_query", search);
                context.startActivity(intent);
            }
        }
    }
}
