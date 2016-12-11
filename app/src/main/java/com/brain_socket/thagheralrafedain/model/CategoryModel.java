package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONObject;

/**
 * Created by Albert on 12/10/16.
 */
public class CategoryModel extends AppBaseModel{
    private String id;
    private String name;
    private String icon;
    private String status;

    public static CategoryModel fromJson(JSONObject json) {
        try {
            CategoryModel category = ThagherApp.getSharedGsonParser().fromJson(json.toString(), CategoryModel.class);
            return category;
        }catch (Exception ignored){}
        return new CategoryModel();
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
