package com.brain_socket.thagheralrafedain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.fragments.FragMap;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;

import org.json.JSONObject;

/**
 * Created by Molham on 2/12/16.
 */

public class MapActivity extends AppCompatActivity implements View.OnClickListener{


    Fragment fragment;
    FragmentManager fragmentManager;
    private static String TAG_MAIN_MAP_FRAG = "mainMapFrag";

    TextView tvTitle;

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
            showBrandOnMap();
        }catch (Exception e){}
    }

    private void init(){
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(this);
    }

    private void showBrandOnMap(){
        fragmentManager = getSupportFragmentManager();
        FragMap fragMap = FragMap.newInstance(FragMap.MAP_TYPE.BRAND , brand);
        fragment = fragMap;
        fragmentManager.beginTransaction()
                .add(R.id.flMainFragmentContainer, fragment, TAG_MAIN_MAP_FRAG)
                .commit();

        tvTitle.setText(brand.getName());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.tvTitle:
                finish();
                break;
        }

    }


}
