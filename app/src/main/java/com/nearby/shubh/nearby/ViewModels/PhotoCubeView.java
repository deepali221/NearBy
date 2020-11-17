package com.nearby.shubh.nearby.ViewModels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by sh on 4/11/2016.
 */
public class PhotoCubeView extends ImageView {
    public PhotoCubeView(Context context, Bitmap bitmap) {
        super(context);
        setImageBitmap(bitmap);
        setDefaults();
    }
    public PhotoCubeView(Context context, Drawable drawable){
        super(context);
        setBackground(drawable);
        setDefaults();
    }
    private void setDefaults(){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
        params.leftMargin =5;
        params.rightMargin=5;
        setLayoutParams(params);
    }
}
