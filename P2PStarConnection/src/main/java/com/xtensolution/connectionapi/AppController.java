package com.xtensolution.connectionapi;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.xtensolution.connectionapi.service.ConnectionService;

public class AppController extends Application {
    /**
     * We'll talk to Nearby Connections through the GoogleApiClient.
     */
    private ConnectionService mConnectionService;

    @Override
    public void onCreate() {
        super.onCreate();
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
//
        mConnectionService = ConnectionService.getInstance(this);
        mConnectionService.setGoogleApiClient(googleApiClient);

    }

    public ConnectionService getConnectionService() {
        return mConnectionService;
    }
}
