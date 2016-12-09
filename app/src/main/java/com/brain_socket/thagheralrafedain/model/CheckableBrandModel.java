package com.brain_socket.thagheralrafedain.model;

import java.util.ArrayList;

/**
 * Created by Albert on 12/8/16.
 */
public class CheckableBrandModel {
    private String id;
    private String name;
    private String logo;
    private boolean checked;

    public CheckableBrandModel(BrandModel b){
        id = b.getId();
        name = b.getName();
        logo = b.getLogo();
        checked = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean check){
        checked = check;
    }
}
