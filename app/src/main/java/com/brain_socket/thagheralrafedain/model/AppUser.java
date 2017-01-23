package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Albert on 11/24/16.
 */
public class AppUser extends AppBaseModel{

    public static enum USER_TYPE {USER, WORKSHOP, SHPWROOM}

    private String id ;
    private String token;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private String longitude;
    private String latitude;
    private String logo;
    private String type;
    private String active;
    private ArrayList<BrandModel> brands;


    public static AppUser fromJson(JSONObject json) {
        try {
            return ThagherApp.getSharedGsonParser().fromJson(json.toString(), AppUser.class);
        }catch (Exception ignored){}
        return new AppUser();
    }

    public AppUser() {}

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public ArrayList<BrandModel> getBrands() {
        return brands;
    }

    public ArrayList<String> getBrandsIds() {
        ArrayList<String> ids = new ArrayList<>();
        if(brands != null)
            for (BrandModel brand : brands) {
                ids.add(brand.getId());
            }
        return ids;
    }

    public USER_TYPE getUserType() {
        if ("I".equals(type)) {
            return USER_TYPE.USER;
        } else if ("W".equals(type)) {
            return USER_TYPE.WORKSHOP;
        } else if ("S".equals(type)) {
            return USER_TYPE.SHPWROOM;
        }
        return USER_TYPE.USER;
    }
}