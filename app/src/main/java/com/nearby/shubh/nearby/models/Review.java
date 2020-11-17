package com.nearby.shubh.nearby.models;

/**
 * Created by sh on 4/12/2016.
 */
public class Review {
    private String comment,url,user_id,date,name;

    public Review(String name,String comment,String user_id,String url, String date){
        this.comment = comment;
        this.user_id = user_id;
        this.url = url;
        this.date = date;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getUrl() {
        return url;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getDate() {
        return date;
    }
}
