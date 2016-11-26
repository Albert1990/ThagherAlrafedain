package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

/**
 * Created by Albert on 11/24/16.
 */
public class ProductModel extends AppBaseModel{
    private String ID;
    private String ArabicName;
    private String EnglishName;
    private String ArabicDescription;
    private String EnglishDescription;
    private String Image;
    private float Price;
    private int Status;
    private BrandModel Brand;

    public static ProductModel fromJson(JSONObject json) {
        try {
            ProductModel productModel = ThagherApp.getSharedGsonParser().fromJson(json.toString(), ProductModel.class);
            return productModel;
        }catch (Exception ignored){}
        return new ProductModel();
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

    public String getArabicDescription() {
        return ArabicDescription;
    }

    public void setArabicDescription(String arabicDescription) {
        ArabicDescription = arabicDescription;
    }

    public String getEnglishDescription() {
        return EnglishDescription;
    }

    public void setEnglishDescription(String englishDescription) {
        EnglishDescription = englishDescription;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public float getPrice() {
        return Price;
    }

    public void setPrice(float price) {
        Price = price;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public BrandModel getBrand() {
        return Brand;
    }

    public void setBrand(BrandModel brand) {
        Brand = brand;
    }

    @Override
    public String getId() {
        return ID;
    }

    public JSONObject getJsonObject(){
        try {
            return new JSONObject(ThagherApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }

}