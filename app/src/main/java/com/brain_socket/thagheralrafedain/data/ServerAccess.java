package com.brain_socket.thagheralrafedain.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.brain_socket.thagheralrafedain.ThagherApp;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.CategoryModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;


public class ServerAccess {
    // GENERIC ERROR CODES

    public static final int ERROR_CODE_userExistsBefore = -2;
    public static final int ERROR_CODE_userNotExists = -3;
    public static final int ERROR_CODE_UNAUTHENTICATED = -4;
    public static final int ERROR_CODE_unknown = -1000;
    public static final int ERROR_CODE_appVersionInvalid= -121;
    public static final int ERROR_CODE_updateAvailable=  -122;

    public static final int RESPONCE_FORMAT_ERROR_CODE = 601 ;
    public static final int CONNECTION_ERROR_CODE = 600 ;
    public static final int REQUEST_SUCCESS_CODE = 0 ;

    // api
    static final String BASE_SERVICE_URL = "http://thageralrafedain.com/mobile_app/api";

    private static ServerAccess serverAccess = null;

    private ServerAccess() {

    }

    public static ServerAccess getInstance() {
        if (serverAccess == null) {
            serverAccess = new ServerAccess();
        }
        return serverAccess;
    }
    // API calls // ------------------------------------------------

    //////////////////
    // login
    /////////////////
    public ServerResult login(String email,String password) {
        ServerResult result = new ServerResult();
        AppUser me  = null ;
        boolean isRegistered = false;
        try {
            // parameters
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("userEmail", email);
            jsonPairs.put("userPassword",password);
            // url
            String url = BASE_SERVICE_URL + "/userLogin.php";
            // send request
            ApiRequestResult apiResult = httpRequest(url, jsonPairs, "post", null);
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            JSONArray jsonResponse = new JSONArray(apiResult.response);
            if(jsonResponse != null && jsonResponse.length() > 0)
            {
                JSONObject jsonObject = jsonResponse.getJSONObject(0);
                if(jsonObject.has("msg")) {
                    result.addPair("msg", jsonObject.get("msg"));
                }
                else{ // check if response is empty
                    me = AppUser.fromJson(jsonObject);
                    isRegistered = true;
                }
            }
        } catch (Exception e) {
            result.setStatusCode(RESPONCE_FORMAT_ERROR_CODE);
        }
        result.addPair("appUser", me);
        result.addPair("isRegistered",isRegistered);
        return result;
    }

    public ServerResult socialLogin(String fullName,String email,String providerName,String providerId) {
        ServerResult result = new ServerResult();
        AppUser me  = null ;
        boolean isRegistered = false;
        try {
            // parameters
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("userFullName", fullName);
            jsonPairs.put("userEmail",email);
            jsonPairs.put("providerName",providerName);
            jsonPairs.put("providerID",providerId);
            // url
            String url = BASE_SERVICE_URL + "/userSocialLogin.php";
            // send request
            ApiRequestResult apiResult = httpRequest(url, jsonPairs, "post", null);
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            JSONArray jsonResponse = new JSONArray(apiResult.response);
            if(jsonResponse != null && jsonResponse.length() > 0)
            {
                JSONObject jsonObject = jsonResponse.getJSONObject(0);
                if(jsonObject.has("msg")) {
                    result.addPair("msg", jsonObject.get("msg"));
                }
                else{ // check if response is empty
                    me = AppUser.fromJson(jsonObject);
                    isRegistered = true;
                }
            }
        } catch (Exception e) {
            result.setStatusCode(RESPONCE_FORMAT_ERROR_CODE);
        }
        result.addPair("appUser", me);
        result.addPair("isRegistered",isRegistered);
        return result;
    }

    /**
     * register a new user with UserName and phoneNumber
     */
    public ServerResult registerUser(String email, String password,String fullName,String phone,String userType) {
        ServerResult result = new ServerResult();
        AppUser me  = null ;
        try {
            // parameters
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("userFullName", fullName);
            jsonPairs.put("userEmail", email);
            jsonPairs.put("userPhone",phone);
            jsonPairs.put("userPassword", password);
            jsonPairs.put("userType",userType);

            // url
            String url = BASE_SERVICE_URL + "/userRegister.php";
            // send request
            ApiRequestResult apiResult = httpRequest(url, jsonPairs, "post", null);
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            JSONArray jsonResponse = new JSONArray(apiResult.response);
            if(jsonResponse != null && jsonResponse.length() > 0)
            {
                JSONObject jsonObject = jsonResponse.getJSONObject(0);
                if(jsonObject.has("msg")) {
                    result.addPair("msg", jsonObject.get("msg"));
                }
                else{ // check if response is empty
                    me = AppUser.fromJson(jsonObject);
                    result.addPair("appUser",me);
                }
            }
        } catch (Exception e) {
            result.setStatusCode(RESPONCE_FORMAT_ERROR_CODE);
        }

        return result;
    }

    public ServerResult forgetPassword(String email) {
        ServerResult result = new ServerResult();
        AppUser me  = null ;
        try {
            // parameters
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("email", email);

            // url
            String url = BASE_SERVICE_URL + "/userForgetPwd.php";
            // send request
            ApiRequestResult apiResult = httpRequest(url, jsonPairs, "post", null);
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            JSONArray jsonResponse = new JSONArray(apiResult.response);
            if(jsonResponse != null && jsonResponse.length() > 0) {
                JSONObject jsonObject = jsonResponse.getJSONObject(0);
                if(jsonObject.has("msg"))
                    result.addPair("msg", jsonObject.get("msg"));
            }
        } catch (Exception e) {
            result.setStatusCode(RESPONCE_FORMAT_ERROR_CODE);
        }

        return result;
    }

    public ServerResult requestVerificationMsg(String accessToken) {
        ServerResult result = new ServerResult();
        boolean msgSent = false ;
        try {
            // parameters
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("access_token", accessToken);
            // url
            String url = BASE_SERVICE_URL + "/index.php/verification_messages_api/send_verification_code";
            // send request
            ApiRequestResult apiResult = httpRequest(url, jsonPairs,"post",null);
            JSONObject jsonResponse = apiResult.getResponseJsonObject();
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            if (jsonResponse != null) { // check if response is empty
                if(apiResult.getStatusCode() == REQUEST_SUCCESS_CODE){
                    msgSent = true ;
                }
            }
        } catch (Exception e) {
            result.setStatusCode(RESPONCE_FORMAT_ERROR_CODE);
        }
        result.addPair("msgSent", msgSent);

        return result;
    }

    public ServerResult verifyAccount(String accessToken, String verifMsg) {
        ServerResult result = new ServerResult();
        boolean verified = false ;
        try {
            // parameters
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("access_token", accessToken);
            jsonPairs.put("verification_code", verifMsg);
            // url
            String url = BASE_SERVICE_URL + "/index.php/verification_messages_api/accept_verification_code";
            // send request
            ApiRequestResult apiResult = httpRequest(url, jsonPairs,"post",null);
            JSONObject jsonResponse = apiResult.getResponseJsonObject();
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            if (jsonResponse != null) { // check if response is empty
                if(apiResult.getStatusCode() == REQUEST_SUCCESS_CODE){
                    verified = true ;
                }
            }
        } catch (Exception e) {
            result.setStatusCode(RESPONCE_FORMAT_ERROR_CODE);
        }
        result.addPair("verified", verified);
        return result;
    }

    public ServerResult getProductById(String productId){
        ServerResult result = new ServerResult();
        ProductModel product = new ProductModel();
        product.setName("selected product");
        product.setPrice("1200");
        result.addPair("product",product);
        return result;
    }

    public ServerResult getProducts(String brandId,String lang,ArrayList<String> categoriesIds){
        ServerResult serverResult = new ServerResult();
        ArrayList<ProductModel> products = null;
        try{
            String url = BASE_SERVICE_URL+"/getProducts.php";
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("brandID",brandId);
            jsonPairs.put("lang",lang);
            if(categoriesIds != null && categoriesIds.size() > 0)
                jsonPairs.put("categories",categoriesIds);
            ApiRequestResult apiResult = httpRequest(url,jsonPairs,"post",null);
            JSONArray jsonResponse = apiResult.getResponseJsonArray();
            if(jsonResponse != null){
                products = new ArrayList<>();
                for(int i=0;i<jsonResponse.length();i++){
                    products.add(ProductModel.fromJson(jsonResponse.getJSONObject(i)));
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        serverResult.addPair("products",products);
        return serverResult;
    }

    //////////////////
    // Brands
    /////////////////
    public ServerResult getBrands(String keyWord) {
        ServerResult result = new ServerResult();
        ArrayList<BrandModel> brands = null;
        try{
            String url = BASE_SERVICE_URL+"/getBrands.php";
            ApiRequestResult apiResult = httpRequest(url,null,"get",null);
            JSONArray jsonResponse = apiResult.getResponseJsonArray();
            if(jsonResponse != null){
                brands = new ArrayList<>();
                for(int i=0;i<jsonResponse.length();i++){
                    brands.add(BrandModel.fromJson(jsonResponse.getJSONObject(i)));
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        result.addPair("brands",brands);
        return result;
    }

    //////////////////
    // Brands with products
    /////////////////
    public ServerResult getBrandsWithProducts() {
        ServerResult result = new ServerResult();
        ArrayList<BrandModel> brands = null;
        try{
            String url = BASE_SERVICE_URL+"/getBrandsWithProducts.php";
            ApiRequestResult apiResult = httpRequest(url,null,"post",null);
            JSONArray jsonResponse = apiResult.getResponseJsonArray();
            if(jsonResponse != null){
                brands = new ArrayList<>();
                for(int i=0;i<jsonResponse.length();i++){
                    JSONObject ob = jsonResponse.getJSONObject(i);
                    brands.add(BrandModel.fromJson(ob));
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        result.addPair("brands",brands);
        return result;
    }

    public ServerResult getCategories() {
        ServerResult result = new ServerResult();
        ArrayList<CategoryModel> categories = null;
        try{
            String url = BASE_SERVICE_URL+"/getCategories.php";
            ApiRequestResult apiResult = httpRequest(url,null,"get",null);
            JSONArray jsonResponse = apiResult.getResponseJsonArray();
            if(jsonResponse != null){
                categories = new ArrayList<>();
                for(int i=0;i<jsonResponse.length();i++){
                    categories.add(CategoryModel.fromJson(jsonResponse.getJSONObject(i)));
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        result.addPair("categories",categories);
        return result;
    }

    public ServerResult updateUser(String userId,String fullName,String email,String phone,String address,String lon,String lat, String imagePath, String type){
        ServerResult result = new ServerResult();
        AppUser me = null;
        try{
            String url = BASE_SERVICE_URL+"/userUpdate.php";
            JSONObject jsonPairs = new JSONObject();
            jsonPairs.put("userFullName",fullName);
            jsonPairs.put("userEmail",email);
            jsonPairs.put("userPhone",phone);
            jsonPairs.put("userAddress",address);
            jsonPairs.put("userLon",lon);
            jsonPairs.put("userLat",lat);
            jsonPairs.put("userType",type);
            jsonPairs.put("userID",userId);

            // image
            try{
                if(imagePath != null) {
                    Bitmap bm = decodeFile(new File(imagePath), 400, 400);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 80, baos); //bm is the bitmap object
                    byte[] byteArrayImage = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    jsonPairs.put("userLogo", encodedImage);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            ApiRequestResult apiResult = httpRequest(url,jsonPairs,"post",null);
            result.setStatusCode(apiResult.getStatusCode());
            result.setApiError(apiResult.getApiError());
            JSONArray jsonResponse = new JSONArray(apiResult.response);
            if(jsonResponse != null && jsonResponse.length() > 0)
            {
                JSONObject jsonObject = jsonResponse.getJSONObject(0);
                if(jsonObject.has("msg")) {
                    result.addPair("msg", jsonObject.get("msg"));
                }
                else{ // check if response is empty
                    me = AppUser.fromJson(jsonObject);
                    result.addPair("appUser", me);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    //////////////////
    // Workshops
    /////////////////
    public ServerResult getWorkshops(String keyWord) {
        ServerResult result = new ServerResult();
        ArrayList<WorkshopModel> workshops = new ArrayList<>();
        for(int i=0;i<10;i++) {
            WorkshopModel w = new WorkshopModel();
            w.setName("workshop" + i);
            w.setAddress("addressxxx" + i);
            w.setEmail("samer" + i + ".shatta@gmail.com");
            workshops.add(w);
        }
        result.addPair("workshops",workshops);
        return result;
    }

    public ServerResult getNearbyWorkshops(float centerLat, float centerLng, float radius, ArrayList<BrandModel> brands) {
        ServerResult result = new ServerResult();
        ArrayList<WorkshopModel> workshops = null;
        try{
            JSONObject params = new JSONObject();

            params.put("lat",centerLat);
            params.put("lon",centerLng);
            params.put("dist",radius);

            if(brands != null) {
                ArrayList<String> brandsIdsArray = new ArrayList<>();
                for (BrandModel brand : brands) {
                    brandsIdsArray.add(brand.getId());
                }
                String commaSeperatedArray = brandsIdsArray.toString();
                commaSeperatedArray = commaSeperatedArray.replace("[", "").replace("]", "").replaceAll("\\s", "").trim();
                params.put("brands",commaSeperatedArray);
            }

            String url = BASE_SERVICE_URL+"/getNearby.php";
            ApiRequestResult apiResult = httpRequest(url,params,"post",null);
            JSONArray jsonResponse = apiResult.getResponseJsonArray();
            if(jsonResponse != null){
                workshops = new ArrayList<>();
                for(int i=0;i<jsonResponse.length();i++){
                    JSONObject ob = jsonResponse.getJSONObject(i);
                    workshops.add(WorkshopModel.fromJson(ob));
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        result.addPair("workshops",workshops);
        return result;
    }

    /**
     * send Https request through connection
     * @param method  get or post
     */
    public ApiRequestResult httpRequest(String url, JSONObject jsonPairs, String method, JSONObject headers) {
        String result = null;
        int responseCode;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //configure connection
            con.setUseCaches(false);
            // add headers
            if (headers != null) {
                Iterator keys = headers.keys();
                while (keys.hasNext()) {
                    // loop to get the dynamic key
                    String key = (String) keys.next();
                    String value = headers.getString(key);
                    con.setRequestProperty(key, value);
                }
            }
            //con.setRequestProperty("Content-Type","application/json");
            //con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //application/x-www-form-urlencoded
            if (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("delete")) {
                //String urlParameters = jsonPairs.toString();
                StringBuilder postData = new StringBuilder();
                if (jsonPairs != null) {
                    Iterator<String> keys = jsonPairs.keys();
                    while (keys.hasNext()) {
                        if (postData.length() != 0) postData.append('&');
                        String key = keys.next();
                        postData.append(URLEncoder.encode(key, "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(String.valueOf(jsonPairs.get(key)), "UTF-8"));
                    }
                }
                con.setDoOutput(true); // implicitly set to POST
                con.setRequestMethod(method.toUpperCase());
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postData.toString());
                wr.flush();
                wr.close();
            }

            responseCode = con.getResponseCode();
            if (responseCode >= 400) {
                try {
                    // try to read returned error response
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    result = response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                result = response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseCode = 600; // indicates connection failure
        }
        ApiRequestResult requestResult = new ApiRequestResult();
        requestResult.statusCode = responseCode;
        requestResult.response = result;
        return requestResult;
    }

    public static class ApiRequestResult{
        String response;
        int statusCode;
        int apiErrorCode;
        JSONObject jsonResponse;

        public boolean connectionSuccess(){
            return  statusCode <400;
        }

        public JSONObject getResponseJsonObject() throws JSONException {
            if (response != null && !response.equals("")) { // check if response is empty
                if(jsonResponse == null )
                    jsonResponse = new JSONObject(response);
                if(!jsonResponse.isNull("object"))
                    return jsonResponse.getJSONObject("object");
            }
            return null;
        }

        public JSONArray getResponseJsonArray() throws JSONException{
            JSONArray res = null;
            if (response != null && !response.equals("")) { // check if response is empty
                res = new JSONArray(response);
            }
            return res;
        }

        public int getStatusCode(){
            return statusCode;
        }

        public String getApiError() {
            try {
                if(jsonResponse == null )
                    jsonResponse = new JSONObject(response);
                if(jsonResponse.has("error"))
                    return jsonResponse.getString("error");
                else
                    return null;
            }catch (Exception e){}
            return "";
        }
    }


    private static Bitmap decodeFile(File f,int WIDTH,int HIGHT){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH=WIDTH;
            final int REQUIRED_HIGHT=HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

}