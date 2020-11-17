package com.nearby.shubh.nearby.models;

/**
 * Created by sh on 4/4/2016.
 */
public class Place {
private String id,name,type,area,address,phoneNo,url_thumbnail,likes,latitude,longitude,distance;



    public Place(String id, String name, String type,String url_thumbnail){
        this.name = name;
        this.id =id;
        this.url_thumbnail = url_thumbnail;
        this.type = type;
    }

    public Place( String id, String name, String type,String area,String address,String phoneNo,String url_thumbnail,String likes){
        this.id =id;
        this.name = name;
        this.type = type;
        this.area = area;
        this.address= address;
        this.url_thumbnail = url_thumbnail;
        this.likes = likes;
        this.phoneNo = phoneNo;
    }

    public Place(String id,String name,String type,String area, String address,String phoneNo,String url_thumbnail,String likes,String latitude,String longitude){
        this.id =id;
        this.name = name;
        this.type = type;
        this.area = area;
        this.address= address;
        this.url_thumbnail = url_thumbnail;
        this.likes = likes;
        this.phoneNo = phoneNo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getArea() {
        return area;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getUrl_thumbnail() {
        return url_thumbnail;
    }

    public String getLikes() {
        return likes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }
}
