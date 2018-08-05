package com.xtensolution.nearbyconnection.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.location.nearby.apps.chat.listener.NearbyAdvertisingListener;
import com.google.location.nearby.apps.chat.service.ConnectionService;
import com.google.location.nearby.apps.chat.service.ConnectionService.Endpoint;

public class HostActivity extends EndpointActivity implements NearbyAdvertisingListener {

    private static final String TAG = HostActivity.class.getSimpleName();

    public static void startServiceActivity(Context context) {
        Intent intent = new Intent(context, HostActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Clients");
    }

    @Override
    public String getName() {
        return "Host";
    }

    @Override
    protected void onStart() {
        super.onStart();
        mConnectionService.setNearbyAdvertisingListener(this);
        mConnectionService.startAdvertising();
    }

    @Override
    public void onAdvertisingStarted() {
        mConnectionService.logD("onAdvertisingStarted");
        progressMsg.setText("Advertising Started");
    }

    @Override
    public void onAdvertisingFailed() {
        mConnectionService.logD("onAdvertisingFailed");
        progressMsg.setText("Advertising Failed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnectionService.stopAdvertising();
        mConnectionService.stopAllEndpoints();
    }

    @Override
    public void onConnectionInitiated(ConnectionService.Endpoint endpoint, ConnectionInfo connectionInfo) {
        mConnectionService.logD("onConnectionInitiated");
        mConnectionService.logD("Endpoint::" + endpoint.toString());
        progressMsg.setText("Connection Initiated with " + endpoint.getName() + "\n" + "request accepting..");
        mConnectionService.acceptConnection(endpoint);
    }

    public void startChatRoom(View view) {
        if (mConnectionService.getConnectedEndpoints().size() > 0)
            HostChatRoomActivity.startHostChatRoomActivity(HostActivity.this);
    }
}
