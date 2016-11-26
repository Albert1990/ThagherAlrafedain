package com.brain_socket.thagheralrafedain.data;

import java.util.HashMap;

public class ServerResult {

    private static int[] errors = {
            ServerAccess.ERROR_CODE_userExistsBefore,
            ServerAccess.ERROR_CODE_userNotExists ,
            ServerAccess.ERROR_CODE_unknown ,
            ServerAccess.ERROR_CODE_userExistsBefore ,
            ServerAccess.ERROR_CODE_appVersionInvalid,
            ServerAccess.ERROR_CODE_updateAvailable
    };

    private int statusCode;
    private String apiError;
    private HashMap<String, Object> pairs;

//	public ServerResult(int statusCode, int flag) {
//		this.statusCode = statusCode;
//		this.flag = flag;
//		pairs = new HashMap<String, Object>();
//	}

    public ServerResult() {
        pairs = new HashMap<String, Object>();
    }

    public HashMap<String, Object> getPairs() {
        return pairs;
    }

    public Object get(String key){
        if(pairs != null && pairs.containsKey(key))
            return pairs.get(key);
        return  null;
    }

    public void setPairs(HashMap<String, Object> pairs) {
        this.pairs = pairs;
    }

//	public int getFlag() {
//		return flag;
//	}

    public int getRequestStatusCode(){
        return  statusCode;
    }

    public void setStatusCode(int flag) {
        this.statusCode = flag;
    }

    public void setApiError(String apiError) {
        this.apiError = apiError;
    }

    public String getApiError() {
        return apiError;
    }

    public void addPair(String key, Object value) {
        this.pairs.put(key, value);
    }

    public Object getValue(String key) {
        return this.pairs.get(key);
    }

    public boolean isValid() {
        for(int error : errors) {
            if(statusCode == error)
                return false;
        }
        return true;
    }

    public boolean connectionFailed (){
        return  apiError != null || statusCode >= 600 ;//(statusCode == ServerAccess.RESPONCE_FORMAT_ERROR_CODE || flag == ServerAccess.CONNECTION_ERROR_CODE );
    }
}