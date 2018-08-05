package com.xtensolution.connectionapi.listener;


import com.xtensolution.connectionapi.service.ConnectionService;

public interface NearbyEndpointListener {
    void onEndpointDiscovered(ConnectionService.Endpoint endpoint);

    void onEndpointConnected(ConnectionService.Endpoint endpoint);

    void onEndpointDisconnected(ConnectionService.Endpoint endpoint);
}
