package com.brain_socket.thagheralrafedain.model;

import com.brain_socket.thagheralrafedain.ThagherApp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Albert on 11/24/16.
 */
public abstract class AppBaseModel {

    public abstract String getId();
    public String getJsonString(){ return getJsonObject().toString();}

    public static JSONArray getJSONArray(ArrayList<? extends AppBaseModel> array){
        JSONArray jsnArray = new JSONArray();
        if(array == null)
            return null;
        for (int i = 0; i < array.size(); i++) {
            jsnArray.put(array.get(i).getJsonObject());
        }
        return jsnArray;
    }

    public JSONObject getJsonObject(){
        try {
            ThagherApp.getSharedGsonParser().toJson(this);
            return new JSONObject(ThagherApp.getSharedGsonParser().toJson(this));
        }catch (Exception e){}
        return  new JSONObject();
    }

    public static boolean removeFromListById(ArrayList<? extends  AppBaseModel> array, String id ){
        if(array == null || array.isEmpty() || id == null)
            return  false;
        for (AppBaseModel  item: array) {
            if(id.equals(item.getId())){
                array.remove(item);
                return true;
            }
        }
        return false;
    }

    public static boolean addToListById(ArrayList<AppBaseModel> array, AppBaseModel item ){
        if(array == null || array.isEmpty() || item == null)
            return false;
        for (AppBaseModel arrayItem: array) {
            if(item.getId().equals(arrayItem.getId())){
                return true;
            }
        }
        array.add(item);
        return false;
    }

    public static AppBaseModel getById(ArrayList<? extends  AppBaseModel> array, String id ){
        if(array == null || array.isEmpty() || id == null)
            return null;
        for (AppBaseModel item: array) {
            if(id.equals(item.getId())){
                return item;
            }
        }
        return null;
    }

    /**
     * @param jsonString String representation of JSONArray to convert to array
     * @param objectType to determine the type of the objects contained in the array we want to retrieve,
     *                   we can get the type using the following: new TypeToken<Class>() {}.getType();
     * @param <T> Generic return type of the array Contents
     * @return array object of the Type T
     */
    public static <T> ArrayList<T> getArrayFromJsonSting(String jsonString, Type objectType) {
        try {
            ArrayList<T> object = ThagherApp.getSharedGsonParser().fromJson(jsonString, objectType);
            return object;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}