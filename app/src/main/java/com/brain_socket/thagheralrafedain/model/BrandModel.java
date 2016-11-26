package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Albert on 11/24/16.
 */
public class BrandModel extends AppBaseModel{
    private String ID;
    private String ArabicName;
    private String EnglishName;
    private String Logo;
    private int Status;
    private ArrayList<ProductModel> Products;


    public static BrandModel fromJson(JSONObject json) {
        try {
            BrandModel brand = ThagherApp.getSharedGsonParser().fromJson(json.toString(), BrandModel.class);
            return brand;
        }catch (Exception ignored){}
        return new BrandModel();
    }

    @Override
    public String getId() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getArabicName() {
        return ArabicName;
    }

    public void setArabicName(String arabicName) {
        ArabicName = arabicName;
    }

    public String getEnglishName() {
        return EnglishName;
    }

    public void setEnglishName(String englishName) {
        EnglishName = englishName;
    }

    public String getLogo() {
        return Logo;
    }

    public void setLogo(String logo) {
        Logo = logo;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public ArrayList<ProductModel> getProducts() {
        return Products;
    }

    public void setProducts(ArrayList<ProductModel> products) {
        this.Products = products;
    }

    public JSONObject getJsonObject(){
        try {
            return new JSONObject(ThagherApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }


}