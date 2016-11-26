package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Albert on 11/24/16.
 */
public class WorkshopModel extends AppBaseModel {
    private int id;
    private String name;
    private String coverImage;
    private String address;
    private String email;
    private String lat;
    private String lon;
    private AppUser user;
    private ArrayList<BrandModel> brands;

    public static WorkshopModel fromJson(JSONObject json) {
        try {
            WorkshopModel workshopModel = ThagherApp.getSharedGsonParser().fromJson(json.toString(), WorkshopModel.class);
            return workshopModel;
        }catch (Exception ignored){}
        return new WorkshopModel();
    }

    public JSONObject getJsonObject(){
        try {
            return new JSONObject(ThagherApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }

    @Override
    public String getId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public ArrayList<BrandModel> getBrands() {
        return brands;
    }

    public void setBrands(ArrayList<BrandModel> brands) {
        this.brands = brands;
    }
}