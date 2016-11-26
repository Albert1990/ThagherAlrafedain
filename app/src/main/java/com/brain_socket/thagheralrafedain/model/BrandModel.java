package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Albert on 11/24/16.
 */
public class BrandModel extends AppBaseModel{
    private String id;
    private String name;
    private String logo;
    private String status;
    private ArrayList<ProductModel> products;


    public static BrandModel fromJson(JSONObject json) {
        try {
            BrandModel brand = ThagherApp.getSharedGsonParser().fromJson(json.toString(), BrandModel.class);
            return brand;
        }catch (Exception ignored){}
        return new BrandModel();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<ProductModel> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<ProductModel> products) {
        this.products = products;
    }

    public JSONObject getJsonObject(){
        try {
            return new JSONObject(ThagherApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }


}