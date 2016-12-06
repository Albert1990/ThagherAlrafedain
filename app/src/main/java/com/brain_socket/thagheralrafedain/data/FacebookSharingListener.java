package com.brain_socket.thagheralrafedain.data;

public interface FacebookSharingListener
{
    void onShareResult(boolean success);

    void onShareError(String error);

    void onShareCancelled();
}