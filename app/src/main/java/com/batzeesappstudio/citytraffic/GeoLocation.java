package com.batzeesappstudio.citytraffic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adh on 7/20/2016.
 */
public class GeoLocation {
    private String userID;
    private String userName;
    private String type;
    private String longtitude;
    private String latitude;
    private String dateTime;


    public GeoLocation(){
        /*Blank default constructor essential for Firebase*/
    }

    public void setID(String id) {
        this.userID = id;
    }
    public String getID() {
        return userID;
    }

    public void setName(String name) {
        this.userName = name;
    }
    public String getName() {
        return userName;
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
        result.put("userid", userID);
        result.put("username", userName);
        result.put("type", type);
        result.put("longti", longtitude);
        result.put("lati", latitude);
        result.put("time", dateTime);
        return result;
    }

}
