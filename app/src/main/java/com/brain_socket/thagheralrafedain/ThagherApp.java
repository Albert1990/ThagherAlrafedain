package com.brain_socket.thagheralrafedain;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.brain_socket.thagheralrafedain.data.DataCacheProvider;
import com.brain_socket.thagheralrafedain.data.DataStore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Albert on 11/24/16.
 */
public class ThagherApp extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public enum SUPPORTED_LANGUAGE{EN, AR}

    public static ThagherApp appContext;
    private static Gson sharedGsonParser;
    private static GoogleApiClient mGoogleApiClient;

    public static final int PERMISSIONS_REQUEST_LOCATION = 33;

    private static SUPPORTED_LANGUAGE currentLanguage = null;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        sharedGsonParser = new Gson();
        DataStore.getInstance().startScheduledUpdates();

        SUPPORTED_LANGUAGE userSelectedLang = SUPPORTED_LANGUAGE.values()[DataCacheProvider.getInstance().getStoredIntWithKey(DataCacheProvider.KEY_APP_LANG)];
        if(userSelectedLang == null) {
            //is user didnt inforce locale before then get default device locale
            Resources res = this.getResources();
            android.content.res.Configuration conf = res.getConfiguration();
            String langCode = conf.locale.getDisplayLanguage();
            if (langCode.equalsIgnoreCase("ar"))
                setLanguage(SUPPORTED_LANGUAGE.AR);
            else
                setLanguage(SUPPORTED_LANGUAGE.EN);
        }else{
            setLanguage(userSelectedLang);
        }
    }

    public static ThagherApp getAppContext() {
        return appContext;
    }

    public static Gson getSharedGsonParser() {
        return sharedGsonParser;
    }

    public static SUPPORTED_LANGUAGE getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(SUPPORTED_LANGUAGE newLang){
        try {
            if (newLang != null) {
                if (newLang != currentLanguage) {
                    ThagherApp.currentLanguage = newLang;
                    DataCacheProvider.getInstance().storeIntWithKey(DataCacheProvider.KEY_APP_LANG,newLang.ordinal());
                    Resources res = getAppContext().getResources();
                    // Change locale settings in the app.
                    DisplayMetrics dm = res.getDisplayMetrics();
                    android.content.res.Configuration conf = res.getConfiguration();
                    String locale = getLocale();
                    conf.locale = new Locale(locale.toLowerCase());
                    res.updateConfiguration(conf, dm);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getLocale(){
        String locale ;
        switch (currentLanguage) {
            case AR:
                locale = "ar";
                break;
            case EN:
                locale = "en";
                break;
            default:
                locale = "ar";
                break;
        }
        return locale;
    }

    public static long getTimestampNow() {
        long res = 0;
        try {
            res = Calendar.getInstance().getTimeInMillis();
        } catch (Exception ignored) {
        }
        return res;
    }

    public static String getFormatedDateForDisplay(long timestamp) {
        String res = null;
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            res = sdf.format(date);
        } catch (Exception ignored) {

        }
        return res;
    }

    private static final long oneDayMillies = 24 * 60 * 60 * 1000;
    private static final long oneHourMillies = 60 * 60 * 1000;
    private static final long oneMinuteMillies = 60 * 1000;

    public static String getDateString(long date) {

        String result = "";
        long now = Calendar.getInstance().getTimeInMillis();
        long timeLapsed = now - date;
        int days = (int) (timeLapsed / oneDayMillies);
        if (days == 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            result = sdf.format(date);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            result = sdf.format(date);
        }
        return result;
    }

    public static int getPXSize(int dp) {
        int px = dp;
        try {
            float density = getAppContext().getResources().getDisplayMetrics().density;
            px = Math.round((float) dp * density);
        } catch (Exception ignored) {
        }
        return px;
    }

    public static int getDPSize(int px) {
        int dp = px;
        try {
            float density = getAppContext().getResources().getDisplayMetrics().density;
            dp = Math.round((float) px / density);
        } catch (Exception ignored) {
        }
        return dp;
    }

    public static void displaySnackBar(int strRes) {
        if (strRes != 0) {
            displaySnackBar(appContext.getString(strRes));
        }
    }

    public static void displaySnackBar(String txt) {

        Snackbar.make(null, txt, Snackbar.LENGTH_SHORT).setAction("DISMISS", null).show();
    }

    public static void hideKeyboard(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    public static Dialog getNewLoadingDilaog(Context con) {
        Dialog dialogLoading = new Dialog(con);
        dialogLoading.setCancelable(false);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialogLoading.setContentView(com.brain_socket.thagheralrafedain.R.layout.layout_loading_diag);
        dialogLoading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialogLoading;
    }

    public static boolean checkPlayServices(final Activity activity) {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 300);//PLAY_SERVICES_RESOLUTION_REQUEST
                if (dialog != null) {
                    dialog.show();
                    return false;
                }
            }
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage("This device is not supported for required Google Play Services")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
            return false;
        }
        return true;
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000 * 60);
        locationRequest.setFastestInterval(10000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            DataStore.getInstance().setMeLastLocation(mLastLocation);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("Google Api connection", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            DataStore.getInstance().setMeLastLocation(location);
        }
    }

    public static void requestLastUserKnownLocation() {
        if (mGoogleApiClient == null) {
            // init google apis client
            mGoogleApiClient = new GoogleApiClient.Builder(ThagherApp.getAppContext())
                    .addConnectionCallbacks(ThagherApp.getAppContext())
                    .addOnConnectionFailedListener(ThagherApp.getAppContext())
                    .addApi(LocationServices.API)
                    .build();
        }
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    public static void disconnectGoogleApiClient() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /// -----
    /// ------ utils ----
    ///
    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static File getNewFile(){
        File f = new File(android.os.Environment.getExternalStorageDirectory(), "Thagher/DCIM_"+System.currentTimeMillis()+".jpg");
        File parentDir = f.getParentFile();
        if(parentDir != null && ! parentDir.exists() ) {
            parentDir.mkdirs();
        }
        return f;
    }

    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static boolean isNullOrEmpty(String str){
        if(str == null)
            return true;
        if(str.isEmpty())
            return true;
        return false;
    }

    public static void Toast(String msg){
        Toast.makeText(getAppContext(),msg,Toast.LENGTH_LONG).show();
    }

}