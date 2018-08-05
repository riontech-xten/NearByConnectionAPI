package com.xtensolution.connectionapi.listener;

import com.google.android.gms.nearby.connection.Payload;
import com.xtensolution.connectionapi.service.ConnectionService;

public interface NearbyDataListener {
    void onReceive(ConnectionService.Endpoint endpoint, Payload payload);
}
