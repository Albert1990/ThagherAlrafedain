package com.brain_socket.thagheralrafedain.data;

import android.location.Location;
import android.os.Handler;

import com.brain_socket.thagheralrafedain.ThagherApp;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.CategoryModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;
import com.google.android.gms.location.places.Place;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will be responsible for requesting new data from the data providers
 * like  and invoking the callback when ready plus handling multithreading when required
 *
 * @author MolhamStein
 */
@SuppressWarnings({"unchecked", "UnusedAssignment"})
public class DataStore {

    public static String VERSIOIN_ID = "0.1";

    public enum GENERIC_ERROR_TYPE {NO_ERROR, UNDEFINED_ERROR, NO_CONNECTION, NOT_LOGGED_IN, NO_MORE_PAGES}

    public enum App_ACCESS_MODE {NOT_LOGGED_IN, NOT_VERIFIED, VERIFIED}

    ;
    private static DataStore instance = null;

    private Handler handler;
    private ArrayList<DataStoreUpdateListener> updateListeners;
    private ServerAccess serverHandler;

    private AppUser me;
    private String apiAccessToken;
    private App_ACCESS_MODE accessMode;

    // user location
    private float myLocationLatitude;
    private float myLocationLongitude;
    protected Location meLastLocation;

    // Home screen data
    private ArrayList<BrandModel> brands;
    private ArrayList<BrandModel> allBrands;
    private ArrayList<CategoryModel> categories;
    private ArrayList<WorkshopModel> workshops;


    // internal data
    private final int UPDATE_INTERVAL = 60000; // update Data each 60 sec
    private static boolean isUpdatingDataStore = false;


    private DataStore() {
        try {
            handler = new Handler();
            updateListeners = new ArrayList<DataStoreUpdateListener>();
            serverHandler = ServerAccess.getInstance();
            getLocalData();
            requestCategories();
            requestAllBrands(null);
            requestBrands(null);
        } catch (Exception ignored) {
        }
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }


    /**
     * used to invoke the DataRequestCallback on the main thread
     */
    private void invokeCallback(final DataRequestCallback callback, final boolean success, final ServerResult result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback == null)
                    return;
                callback.onDataReady(result, success);
            }
        });
    }

    public void clearLocalData() {
        try {
            DataCacheProvider.getInstance().clearCache();
            me = null;
            brands = null;
            allBrands = null;
        } catch (Exception ignored) {
        }
    }

    public void logout() {
        clearLocalData();
        broadcastloginStateChange();
    }

    public void getLocalData() {
        DataCacheProvider cache = DataCacheProvider.getInstance();

        categories = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_CATEGORIES, new TypeToken<ArrayList<CategoryModel>>() {
        }.getType());
        brands = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_BRANDS, new TypeToken<ArrayList<BrandModel>>() {
        }.getType());
        allBrands = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_ALL_BRANDS, new TypeToken<ArrayList<BrandModel>>() {
        }.getType());
        workshops = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_WORKSHOPS, new TypeToken<ArrayList<WorkshopModel>>() {
        }.getType());
        me = DataCacheProvider.getInstance().getStoredObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, new TypeToken<AppUser>() {
        }.getType());

        myLocationLatitude = DataCacheProvider.getInstance().getStoredFloatWithKey(DataCacheProvider.KEY_APP_LAST_KNOWN_LAT);
        myLocationLongitude = DataCacheProvider.getInstance().getStoredFloatWithKey(DataCacheProvider.KEY_APP_LAST_KNOWN_LON);
    }

    //--------------------
    // DataStore Update
    //-------------------------------------------

    public void startScheduledUpdates() {
        try {
            // start schedule timer
            handler.post(runnableUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopScheduledUpdates() {
        try {
            handler.removeCallbacks(runnableUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Runnable runnableUpdate = new Runnable() {
        @Override
        public void run() {
            requestBrandsWithProducts(null);
            requestWorkshops("", null);
            if (isUserLoggedIn()) {

            }
            handler.postDelayed(runnableUpdate, UPDATE_INTERVAL);
        }
    };

    public void triggerDataUpdate() {
        requestCategories();
        requestAllBrands(null);
        requestBrands(null);
        // get Following list and cache it for later use
        if (isUserLoggedIn()) {

        }
    }

    private void broadcastDataStoreUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DataStoreUpdateListener listener : updateListeners) {
                    listener.onDataStoreUpdate();
                }
            }
        });
    }

    public void removeUpdateBroadcastListener(DataStoreUpdateListener listener) {
        if (updateListeners != null && updateListeners.contains(listener))
            updateListeners.remove(listener);
    }

    public void addUpdateBroadcastListener(DataStoreUpdateListener listener) {
        if (updateListeners == null)
            updateListeners = new ArrayList();
        if (!updateListeners.contains(listener))
            updateListeners.add(listener);
    }

    private void broadcastloginStateChange() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DataStoreUpdateListener listener : updateListeners) {
                    listener.onLoginStateChange();
                }
            }
        });
    }

    public void broadcastLanguageChange() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DataStoreUpdateListener listener : updateListeners) {
                    listener.onLanguageChanged();
                }
            }
        });
    }

    //--------------------
    // Login
    //--------------------
    public void attemptSignUp(final String email, final String password, final String fullName,
                              final String phone, final String userType, final DataRequestCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                String md5Password = ThagherApp.MD5(password);
                ServerResult result = serverHandler.registerUser(email, md5Password, fullName, phone, userType);
                if (result.isValid() && !result.containsKey("msg") && result.containsKey("appUser")) {
                    me = (AppUser) result.getPairs().get("appUser");
                    setMe(me);
                    broadcastloginStateChange();
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void attemptLogin(final String email,final String password, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                String md5Password = ThagherApp.MD5(password);
                ServerResult result = serverHandler.login(email, md5Password);
                    if (result.isValid() && !result.containsKey("msg")) {
                        me = (AppUser) result.getPairs().get("appUser");
                        setMe(me);
                        broadcastloginStateChange();
                    }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void attemptSocialLogin(final String fullName, final String email,final String providerName,final String providerId, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.socialLogin(fullName, email, providerName, providerId);
                if (result.isValid() && !result.containsKey("msg")) {
                    me = (AppUser) result.getPairs().get("appUser");
                    //apiAccessToken = me.getAccessToken();
                    //setApiAccessToken(apiAccessToken);
                    setMe(me);
                    broadcastloginStateChange();
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void requestForgetPassword(final String email, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.forgetPassword(email);
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void attemptChangePsw(final String oldPsw,final String newPsw, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                String md5OldPassword = ThagherApp.MD5(oldPsw);
                String md5NewPassword = ThagherApp.MD5(newPsw);
                ServerResult result = serverHandler.changePassword(getMe().getId(),md5OldPassword, md5NewPassword);
                if (!result.isValid()) {
                    success = false;
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }


    public void attemptUpdateUser(final String fullName,final String phone,final String type, final float lat, final float lng, final String address, final String imagePath, final ArrayList<String> selectedBrandsIds, final DataRequestCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                AppUser currentUser = getMe();
                ServerResult result = serverHandler.updateUser(currentUser.getId(),fullName,currentUser.getEmail(),
                        phone,address,String.valueOf(lat), String.valueOf(lng), imagePath, type, selectedBrandsIds);
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid() && !result.containsKey("msg") && result.containsKey("appUser")) {
                        me = (AppUser) result.getPairs().get("appUser");
                        setMe(me);
                        broadcastloginStateChange();
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void logoutUser() {
        try {
            stopScheduledUpdates();
            clearLocalData();
        } catch (Exception e) {
        }
    }

    public void requestVerificationMsg(final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.requestVerificationMsg(apiAccessToken);
                if (result.connectionFailed()) {
                    success = false;
                }
                if (callback != null)
                    invokeCallback(callback, success, result);
            }
        }).start();
    }

    public void verifyAccount(final String verifMsg, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.verifyAccount(apiAccessToken, verifMsg);
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    //boolean loginSuccess = (Boolean) data.get("verified");
                    if (!result.isValid()) {
                        setAccessMode(App_ACCESS_MODE.NOT_VERIFIED);
                    } else {
                        setAccessMode(App_ACCESS_MODE.VERIFIED);
                    }
                }
                if (callback != null)
                    invokeCallback(callback, success, result);
            }
        }).start();
    }


    //--------------------
    // Brands
    //----------------------------------------------
    public void requestBrands(final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getBrands();
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        ArrayList<BrandModel> arrayRecieved = (ArrayList<BrandModel>) result.get("brands");
                        if (arrayRecieved != null && !arrayRecieved.isEmpty()) {
                            brands = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_BRANDS, arrayRecieved);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void requestAllBrands(final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getAllBrands();
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        ArrayList<BrandModel> arrayRecieved = (ArrayList<BrandModel>) result.get("brands");
                        if (arrayRecieved != null && !arrayRecieved.isEmpty()) {
                            allBrands = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_ALL_BRANDS, arrayRecieved);
                        }
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    //--------------------
    // Brands with products
    //----------------------------------------------
    public void requestBrandsWithProducts(final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getBrandsWithProducts();
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        ArrayList<BrandModel> arrayRecieved = (ArrayList<BrandModel>) result.get("brands");
                        if (arrayRecieved != null && !arrayRecieved.isEmpty()) {
                            brands = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_BRANDS, arrayRecieved);
                        }
                    }
                }
                //broadcastDataStoreUpdate();
                if(callback != null)
                    invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void requestProducts(final String brandId,final String lang,final ArrayList<String> categoriesIds, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getProducts(brandId,lang,categoriesIds);
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        ArrayList<ProductModel> brandProducts = (ArrayList<ProductModel>) result.get("products");
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void requestCategories() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getCategories();
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        categories = (ArrayList<CategoryModel>) result.get("categories");
                        DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_CATEGORIES, categories);
                    }
                }
            }
        }).start();
    }

    //--------------------
    // Workshops
    //----------------------------------------------
    public void requestWorkshops(final String keyWord, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getWorkshops(keyWord);
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        ArrayList<WorkshopModel> arrayRecieved = (ArrayList<WorkshopModel>) result.get("workshops");
                        if (arrayRecieved != null && !arrayRecieved.isEmpty()) {
                            workshops = arrayRecieved;
                            DataCacheProvider.getInstance().storeArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_WORKSHOPS, arrayRecieved);
                        }
                    }
                }
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void requestNearbyWorkshops(final float centerLat, final float centerLng, final float radius, final ArrayList<BrandModel> brands,final boolean workshopsEnabled, final boolean showRoomsEnabled, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getNearbyWorkshops(centerLat, centerLng, radius, brands, workshopsEnabled, showRoomsEnabled);
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        ArrayList<WorkshopModel> arrayRecieved = (ArrayList<WorkshopModel>) result.get("workshops");
                        if (arrayRecieved != null && !arrayRecieved.isEmpty()) {
                            workshops = arrayRecieved;
                        }
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    //--------------------
    // Getters
    //----------------------------------------------

    public ArrayList<BrandModel> getBrands()
    {
        return brands;
    }

    public ArrayList<BrandModel> getAllBrands()
    {
        return allBrands;
    }

    public ArrayList<CategoryModel> getCategories() { return categories; }

    public ArrayList<WorkshopModel> getWorkshops() {
        return workshops;
    }

    public float getMyLocationLatitude() {
        return myLocationLatitude;
    }

    public float getMyLocationLongitude() {
        return myLocationLongitude;
    }

    public Location getMeLastLocation() {
        return meLastLocation;
    }

    public void setMeLastLocation(Location meLastLocation) {
        if(meLastLocation != null) {
            this.meLastLocation = meLastLocation;
            myLocationLatitude = (float) meLastLocation.getLatitude();
            myLocationLongitude = (float) meLastLocation.getLongitude();

            DataCacheProvider.getInstance().storeFloatWithKey(DataCacheProvider.KEY_APP_LAST_KNOWN_LAT, myLocationLatitude);
            DataCacheProvider.getInstance().storeFloatWithKey(DataCacheProvider.KEY_APP_LAST_KNOWN_LON, myLocationLongitude);
            broadcastDataStoreUpdate();
        }
    }

    public boolean isUserLoggedIn() {
        return me != null;
    }

    public AppUser getMe() {
        if (me == null)
            me = DataCacheProvider.getInstance().getStoredObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, new TypeToken<AppUser>() {
            }.getType());
        return me;
    }

    public void setMe(AppUser newUser) {
        if (isUserLoggedIn())
            this.me = newUser;
        DataCacheProvider.getInstance().storeObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, newUser);
    }

    public void setApiAccessToken(String apiAccessToken) {
        this.apiAccessToken = apiAccessToken;
        DataCacheProvider.getInstance().storeStringWithKey(DataCacheProvider.KEY_ACCESS_TOKEN, apiAccessToken);

    }

    public void setAccessMode(App_ACCESS_MODE accessMode) {
        this.accessMode = accessMode;
        DataCacheProvider.getInstance().storeIntWithKey(DataCacheProvider.KEY_APP_ACCESS_MODE, accessMode.ordinal());
    }

    public App_ACCESS_MODE getAccessMode() {
        return accessMode;
    }

    public boolean isLoggedIn() {
        return apiAccessToken != null && !apiAccessToken.isEmpty() && accessMode == App_ACCESS_MODE.VERIFIED;
    }

    // interfaces
    public interface DataRequestCallback {
        void onDataReady(ServerResult result, boolean success);
    }

    public interface DataStoreUpdateListener {
        void onDataStoreUpdate();
        void onLanguageChanged();
        void onLoginStateChange();
    }

    public interface DataStoreErrorListener {
        void onError(GENERIC_ERROR_TYPE error);
    }
}