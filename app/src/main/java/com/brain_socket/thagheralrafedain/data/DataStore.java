package com.brain_socket.thagheralrafedain.data;

import android.location.Location;
import android.os.Handler;

import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

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
    private ArrayList<WorkshopModel> workshops;


    // internal data
    private final int UPDATE_INTERVAL = 30000; // update Data each 30 sec
    private static boolean isUpdatingDataStore = false;


    private DataStore() {
        try {
            handler = new Handler();
            updateListeners = new ArrayList<DataStoreUpdateListener>();
            serverHandler = ServerAccess.getInstance();
            getLocalData();
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
        } catch (Exception ignored) {
        }
    }

    public void logout() {
        clearLocalData();
        broadcastloginStateChange();
    }

    public void getLocalData() {
        DataCacheProvider cache = DataCacheProvider.getInstance();

        brands = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_BRANDS, new TypeToken<ArrayList<BrandModel>>() {
        }.getType());
        workshops = cache.getStoredArrayWithKey(DataCacheProvider.KEY_APP_ARRAY_WORKSHOPS, new TypeToken<ArrayList<WorkshopModel>>() {
        }.getType());
        me = DataCacheProvider.getInstance().getStoredObjectWithKey(DataCacheProvider.KEY_APP_USER_ME, new TypeToken<AppUser>() {
        }.getType());
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

    //--------------------
    // Login
    //-------------------------------------------

    /**
     * @param FBID     : pass null if signing-up without facebook
     * @param callback
     */
    public void attemptSignUp(final String phoneNum, final String firstName, final String lastName, final String countryCode, final String versionId, final String FBID, final DataRequestCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.registerUser(firstName, lastName, phoneNum, countryCode, versionId, FBID);
                if (result.connectionFailed()) {
                    success = false;
                } else {
                    try {
                        AppUser me = (AppUser) result.getPairs().get("appUser");
                        apiAccessToken = me.getAccessToken();
                        setApiAccessToken(apiAccessToken);
                        setMe(me);
                        broadcastloginStateChange();
                    } catch (Exception e) {
                        success = false;
                    }
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    /**
     * attempting login using phone number
     *
     * @param phoneNumfinal if the phone number was found in the DB the user will be logged in otherwise error code will be returned
     */
    public void attemptLogin(final String phoneNumfinal, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.login(phoneNumfinal);
                if (result.getRequestStatusCode() >= 600) {
                    success = false;
                } else {
                    if (result.isValid()) {
                        me = (AppUser) result.getPairs().get("appUser");
                        apiAccessToken = me.getAccessToken();
                        setApiAccessToken(apiAccessToken);
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

    public void searchForKeyword(String keyWord, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getPickedForYouProducts();
                if (result.connectionFailed()) {
                    success = false;
                }
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    /**
     * get Product by Id this is used incase of opening product details activity through deeplinking
     *
     * @param prodId
     * @param callback
     */
    public void requestProduct(final String prodId, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerResult result = serverHandler.getProductById(prodId);
                broadcastDataStoreUpdate();
                invokeCallback(callback, !result.connectionFailed(), result);
            }
        }).start();
    }

    //--------------------
    // Brands
    //----------------------------------------------
    public void requestBrands(final String keyWord, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getBrands(keyWord);
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
                broadcastDataStoreUpdate();
                invokeCallback(callback, success, result); // invoking the callback
            }
        }).start();
    }

    public void requestBrandProducts(final String brandId, final DataRequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                ServerResult result = serverHandler.getBrandProducts(brandId);
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

    //--------------------
    // Getters
    //----------------------------------------------

    public ArrayList<BrandModel> getBrands() {
        return brands;
    }

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
        this.meLastLocation = meLastLocation;
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

        void onNewEventNotificationsAvailable();

        void onLoginStateChange();
    }

    public interface DataStoreErrorListener {
        void onError(GENERIC_ERROR_TYPE error);
    }
}