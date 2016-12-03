package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Albert on 11/24/16.
 */
public class WorkshopModel extends AppBaseModel {

    public static enum WORKSHOP_TYPE {WORKSHOP, SHOW_ROOM}

    private int id;
    private String fullname;
    private String logo;
    private String address;
    private String type;
    private String phone;
    private String email;
    private String latitude;
    private String longitude;
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
        return fullname;
    }

    public void setName(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public String getLogo() {
        return logo;
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
        return longitude;
    }

    public void setLon(String lon) {
        this.longitude = lon;
    }

    public String getLat() {
        return latitude;
    }

    public void setLat(String lat) {
        this.latitude = lat;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public WORKSHOP_TYPE getType() {
        if (type == null)
            return WORKSHOP_TYPE.WORKSHOP;
        if(type.equals("S"))
            return WORKSHOP_TYPE.SHOW_ROOM;
        return WORKSHOP_TYPE.WORKSHOP;
    }

    public ArrayList<BrandModel> getBrands() {
        return brands;
    }

    public void setBrands(ArrayList<BrandModel> brands) {
        this.brands = brands;
    }

    public LatLng getCoords(){
        try {
            LatLng coords = new LatLng(Float.valueOf(latitude), Float.valueOf(longitude));
            return coords;
        }catch (Exception e){
            return new LatLng(0, 0);
        }
    }
}