package com.xtensolution.nearbyconnection;

import android.app.Application;

import com.xtensolution.connectionapi.AppController;

public class SampleApp extends AppController {
    private static SampleApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static SampleApp getInstance() {
        return instance;
    }
}
