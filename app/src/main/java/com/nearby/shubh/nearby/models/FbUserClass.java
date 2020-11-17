package com.nearby.shubh.nearby.models;

/**
 * Created by sh on 3/29/2016.
 */
public class FbUserClass {

    private String name, email , url;

    public FbUserClass(String name, String email, String url){
        this .name = name;
        this.email = email;
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {

        return name;
    }
}
