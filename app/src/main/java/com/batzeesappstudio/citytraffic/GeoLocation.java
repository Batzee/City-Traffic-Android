package com.batzeesappstudio.citytraffic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adh on 7/20/2016.
 */
public class GeoLocation {
    private String userid;
    private String username;
    private String type;
    private String longtitude;
    private String latitude;
    private String dateTime;


    public GeoLocation(){
        /*Blank default constructor essential for Firebase*/
    }


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDisplayName() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setID(String id) {
        this.userid = id;
    }
    public String getID() {
        return userid;
    }

    public void setName(String name) {
        this.username = name;
    }
    public String getName() {
        return username;
    }

    public void setType(String locType) {
        this.type = locType;
    }
    public String getType() {
        return type;
    }

    public void setLongti(String longti) {
        this.longtitude = longti;
    }
    public String getLongti() {
        return longtitude;
    }

    public void setLati(String lati) {
        this.latitude = lati;
    }
    public String getLati() {
        return latitude;
    }

    public void setTime(String time) {
        this.dateTime = time;
    }
    public String getTime() {
        return dateTime;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userid", userid);
        result.put("username", username);
        result.put("type", type);
        result.put("longti", longtitude);
        result.put("lati", latitude);
        result.put("time", dateTime);
        return result;
    }

}
