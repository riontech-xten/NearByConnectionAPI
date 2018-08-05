package com.xtensolution.connectionapi.listener;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.xtensolution.connectionapi.service.ConnectionService;

public interface NearbyConnectionListener {
    void onConnectionInitiated(ConnectionService.Endpoint endpoint, ConnectionInfo connectionInfo);
    void onConnectionFailed(ConnectionService.Endpoint endpoint);
}
