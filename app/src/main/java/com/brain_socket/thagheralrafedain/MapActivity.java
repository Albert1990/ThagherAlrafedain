package com.brain_socket.thagheralrafedain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.brain_socket.thagheralrafedain.fragments.FragMap;
import com.brain_socket.thagheralrafedain.model.BrandModel;

import org.json.JSONObject;

/**
 * Created by Molham on 2/12/16.
 */

public class MapActivity extends AppCompatActivity {


    Fragment fragment;
    FragmentManager fragmentManager;
    private static String TAG_MAIN_MAP_FRAG = "mainMapFrag";

    // temp Data Holder
    BrandModel brand;

    public static Bundle getLauncherBundle(FragMap.MAP_TYPE type, BrandModel brand){

        Bundle extras = new Bundle();
        if(brand != null)
            extras.putString("brand", brand.getJsonString());
        extras.putInt("type", type.ordinal());
        return extras;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        init();
        resolveIntentExtra(getIntent().getExtras());
        showBrandOnMap();
    }

    private void resolveIntentExtra(Bundle extras) {
        try{
            int type = extras.getInt("type");
            FragMap.MAP_TYPE mapType = FragMap.MAP_TYPE.values()[type];

            if(extras.containsKey("brand")) {
                String jsonStr = extras.getString("brand");
                JSONObject jsonObject = new JSONObject(jsonStr);
                brand = BrandModel.fromJson(jsonObject);
            }
        }catch (Exception e){}
    }

    private void init(){}

    private void showBrandOnMap(){
        fragmentManager = getSupportFragmentManager();
        FragMap fragMap = FragMap.newInstance(FragMap.MAP_TYPE.BRAND , brand);
        fragment = fragMap;
        fragmentManager.beginTransaction()
                .add(R.id.flMainFragmentContainer, fragment, TAG_MAIN_MAP_FRAG)
                .commit();
    }


}
