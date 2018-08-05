package com.xtensolution.nearbyconnection.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.xtensolution.connectionapi.listener.NearbyDiscoveringListener;
import com.xtensolution.connectionapi.service.ConnectionService.Endpoint;

public class ClientActivity extends EndpointActivity implements NearbyDiscoveringListener {

    public static void startClientActivity(Context context) {
        Intent intent = new Intent(context, ClientActivity.class);
        context.startActivity(intent);
    }

    @Override
    public String getName() {
        return Build.MODEL;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Services");

    }

    @Override
    protected void onStart() {
        super.onStart();
        mConnectionService.setNearbyDiscoveringListener(this);
        mConnectionService.startDiscovering();
        mConnectionService.logD("onStart");
        fab.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
//        mConnectionService.stopDiscovering();
        super.onStop();
    }

    @Override
    public void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        mConnectionService.logD("onConnectionInitiated");
        mConnectionService.logD("Endpoint::" + endpoint.toString());
        progressMsg.setText("Connection Initiated with " + endpoint.getName() + "\n" + "request accepting..");
        mConnectionService.acceptConnection(endpoint);
    }

    @Override
    public void onDiscoveryStarted() {
        progressMsg.setText("Discovery Started ");
    }

    @Override
    public void onDiscoveryFailed() {
        progressMsg.setText("Discovery Failed ");
    }

    @Override
    public void onEndpointDiscovered(Endpoint endpoint) {
        progressMsg.setText("Endpoint Discovered=>" + endpoint.getName());
        mConnectionService.connectToEndpoint(endpoint);
    }

    public View.OnClickListener getItemClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Endpoint endpoint = (Endpoint) view.getTag();
                ClientChatRoomActivity.startChatActivity(ClientActivity.this, endpoint);
            }
        };
    }

    public void startChatRoom(View view){
    }
}
