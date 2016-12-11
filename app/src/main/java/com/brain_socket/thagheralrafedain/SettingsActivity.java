package com.brain_socket.thagheralrafedain;


import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.ThagherApp.SUPPORTED_LANGUAGE;
import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.DataStore.DataStoreUpdateListener;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.AppUser.USER_TYPE;

public class SettingsActivity extends AppCompatActivity implements OnClickListener, DataStoreUpdateListener {

    TextView tvCheckLangAr, tvCheckLangEn;
    ImageView ivCheckLangAr, ivCheckLangEn;

    TextView txtWorkshop;
    TextView txtChangePsw;
    TextView txtLogout;
    View vLogedInOptions;
    View btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        updateView();
        DataStore.getInstance().addUpdateBroadcastListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataStore.getInstance().removeUpdateBroadcastListener(this);
    }

    private void init(){
        tvCheckLangAr = (TextView) findViewById(R.id.tvCheckArabic);
        tvCheckLangEn = (TextView) findViewById(R.id.tvCheckEnglish);
        ivCheckLangAr = (ImageView) findViewById(R.id.ivCheckArabic);
        ivCheckLangEn = (ImageView) findViewById(R.id.ivCheckEnglish);
        TextView txtLang = (TextView) findViewById(R.id.txtLang);
        txtWorkshop = (TextView) findViewById(R.id.txtWorskhop);
        txtChangePsw = (TextView) findViewById(R.id.txtChangePsw);
        TextView txtTitle = (TextView) findViewById(R.id.tvTitle);
        txtLogout = (TextView) findViewById(R.id.txtLogout);
        vLogedInOptions =findViewById(R.id.vLogedInOptions);
        btnLogin = findViewById(R.id.btnLogin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        tvCheckLangAr.setOnClickListener(this);
        tvCheckLangEn.setOnClickListener(this);
        ivCheckLangAr.setOnClickListener(this);
        ivCheckLangEn.setOnClickListener(this);
        txtWorkshop.setOnClickListener(this);
        txtChangePsw.setOnClickListener(this);
        txtLogout.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //set text
        txtLang.setText(R.string.settings_lang);
        txtChangePsw.setText(R.string.settings_change_psw);
        txtLogout.setText(R.string.settings_logout);

        AppUser me= DataStore.getInstance().getMe();
        if(me != null) {
            vLogedInOptions.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            txtTitle.setText(me.getFullname());
            if (me.getUserType() == USER_TYPE.USER) {
                txtWorkshop.setText(R.string.settings_upgrade_to_workshop);
            } else {
                txtWorkshop.setText(R.string.settings_edit_workshop);
            }
        }else{ // not logged in
            vLogedInOptions.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateView(){
        SUPPORTED_LANGUAGE newLang = ThagherApp.getCurrentLanguage();
        switch (newLang){
            case AR:
                ivCheckLangAr.setImageResource(R.drawable.ic_radio_active);
                ivCheckLangEn.setImageResource(R.drawable.ic_radio);
                txtChangePsw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_has_details, 0, 0, 0);
                txtWorkshop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_has_details,0,0,0);
                break;
            case EN:
                ivCheckLangEn.setImageResource(R.drawable.ic_radio_active);
                ivCheckLangAr.setImageResource(R.drawable.ic_radio);
                txtChangePsw.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_has_details, 0);
                txtWorkshop.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_has_details, 0);
                break;
        }

        AppUser me = DataStore.getInstance().getMe();
        if(me != null) {
            vLogedInOptions.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            if (me.getUserType() == USER_TYPE.USER) {
                txtWorkshop.setText(R.string.settings_upgrade_to_workshop);
            } else {
                txtWorkshop.setText(R.string.settings_edit_workshop);
            }
        }else{ // not logged in
            vLogedInOptions.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }
    }

    private void changeLanguage(SUPPORTED_LANGUAGE newLang){
        try {
            if (newLang != null) {
                if (newLang != ThagherApp.getCurrentLanguage()) {
                    ThagherApp.setLanguage(newLang);
                    init();
                    updateView();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDataStoreUpdate() {

    }

    @Override
    public void onLoginStateChange() {
        updateView();
    }

    @Override
    public void onNewEventNotificationsAvailable() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivCheckArabic:
            case R.id.tvCheckArabic:
                changeLanguage(SUPPORTED_LANGUAGE.AR);
                break;
            case R.id.ivCheckEnglish:
            case R.id.tvCheckEnglish:
                changeLanguage(SUPPORTED_LANGUAGE.EN);
                break;
            case R.id.txtChangePsw:

                break;
            case R.id.txtLogout:
                DataStore.getInstance().logout();
                finish();
                break;
            case R.id.txtWorskhop:
                Intent i = new Intent(this, WorkshopDetails.class);
                startActivity(i);
                break;
            case R.id.btnLogin:
                Intent intrent = new Intent(this, LoginActivity.class);
                startActivity(intrent);
                break;
        }
    }

}
