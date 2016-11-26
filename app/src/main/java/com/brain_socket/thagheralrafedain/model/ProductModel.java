package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

/**
 * Created by Albert on 11/24/16.
 */
public class ProductModel extends AppBaseModel{
    private String id;
    private String name;
    private String description;
    private String image;
    private String price;
    private String status;

    public static ProductModel fromJson(JSONObject json) {
        try {
            ProductModel productModel = ThagherApp.getSharedGsonParser().fromJson(json.toString(), ProductModel.class);
            return productModel;
        }catch (Exception ignored){}
        return new ProductModel();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public String getPriceWithUnit() {return price + "$"; }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JSONObject getJsonObject(){
        try {
            return new JSONObject(ThagherApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }

}