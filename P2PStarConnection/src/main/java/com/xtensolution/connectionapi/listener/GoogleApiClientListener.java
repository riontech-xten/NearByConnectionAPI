package com.xtensolution.connectionapi.listener;

public interface GoogleApiClientListener {
    void onConnected();
    void onConnectionFailed();
    void onConnectionSuspended(int reason);
}
