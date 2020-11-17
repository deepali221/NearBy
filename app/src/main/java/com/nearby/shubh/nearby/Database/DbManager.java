package com.nearby.shubh.nearby.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sh on 3/13/2016.
 */
public class DbManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Nearby.db";
    public static final String TABLE_TAGS_NAME = "Tags_table";
    public static final String TAG_ID_COLUMN = "tag_id";
    public static final String TAG_NAME_COLUMN = "tag_name";


    public DbManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS " + TABLE_TAGS_NAME + "(" + TAG_ID_COLUMN + " Integer," + TAG_NAME_COLUMN + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("shubh","database changed");
        if(oldVersion != DATABASE_VERSION) {
            db.execSQL("drop table " + TABLE_TAGS_NAME);
            onCreate(db);
        }
    }
    public boolean insertTags(Map<Integer,String> tagsMap){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        long res = -1;
        for (int id : tagsMap.keySet()) {
            cv.put(TAG_ID_COLUMN,id);
            cv.put(TAG_NAME_COLUMN,tagsMap.get(id));
            res = db.insert(TABLE_TAGS_NAME, null,cv);
            if (res == -1){return false;}
        }
        return true;
    }
    public Map<String,Integer> getReverseTags(){
        SQLiteDatabase db = getWritableDatabase();
        Map<String,Integer>map = new HashMap<>();
        Cursor res = db.rawQuery("select * from "+TABLE_TAGS_NAME,null);
        if(res.getCount() != 0){
            while(res.moveToNext()){
                int id = res.getInt(0);
                String tag = res.getString(1);
                map.put(tag,id);
            }
        }
        return map;
    }
    public ArrayList<String> getTags(){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<String> list = new ArrayList<>();
        Cursor res = db.rawQuery("select * from "+TABLE_TAGS_NAME,null);
        if(res.getCount() != 0){
            while(res.moveToNext()){
                String tag = res.getString(1);
                list.add(tag);
            }
        }
        return list;
    }
    public int getTagsDbCount(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cur = db.rawQuery("select count(*) from " + TABLE_TAGS_NAME, null);
        int res;
        cur.moveToFirst();
        res = cur.getInt(0);
        return res;
    }
}
