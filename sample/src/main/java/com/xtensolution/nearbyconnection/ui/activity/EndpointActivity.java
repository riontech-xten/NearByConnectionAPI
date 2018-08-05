package com.xtensolution.nearbyconnection.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.xtensolution.connectionapi.listener.NearByServiceListener;
import com.xtensolution.connectionapi.listener.NearbyConnectionListener;
import com.xtensolution.connectionapi.listener.NearbyDataListener;
import com.xtensolution.connectionapi.listener.NearbyEndpointListener;
import com.xtensolution.connectionapi.service.ConnectionService;
import com.xtensolution.nearbyconnection.R;
import com.xtensolution.nearbyconnection.SampleApp;
import com.xtensolution.nearbyconnection.ui.adapter.EndpointAdapter;

import java.util.ArrayList;


public abstract class EndpointActivity extends AppCompatActivity implements
        NearbyEndpointListener, NearbyConnectionListener,
        NearByServiceListener, NearbyDataListener {

    private RecyclerView deviceList;
    protected EndpointAdapter adapter;
    private LayoutInflater inflater;
    protected TextView progressMsg;
    protected ConnectionService mConnectionService;
    protected FloatingActionButton fab;


    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initialize() {
        try {
            mConnectionService = SampleApp.getInstance().getConnectionService();
            fab = findViewById(R.id.fab);
            inflater = LayoutInflater.from(this);
            progressMsg = findViewById(R.id.txtConnecting);
            deviceList = findViewById(R.id.clientList);
            adapter = new EndpointAdapter(this, new ArrayList<ConnectionService.Endpoint>());
            adapter.setOnClickListener(getItemClickListener());
            deviceList.setLayoutManager(new LinearLayoutManager(this));
            deviceList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
            deviceList.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected View.OnClickListener getItemClickListener() {
        return null;
    }

    @Override
    public String getServiceId() {
        return getPackageName();
    }

    @Override
    public Strategy getStrategy() {
        return Strategy.P2P_STAR;
    }

    @Override
    public void onEndpointDisconnected(ConnectionService.Endpoint endpoint) {
        mConnectionService.logD("onEndpointDisconnected");
        mConnectionService.logD("Endpoint::" + endpoint.toString());
        adapter.remove(endpoint);

    }

    @Override
    public void onEndpointConnected(ConnectionService.Endpoint endpoint) {
        mConnectionService.logD("onEndpointConnected");
        mConnectionService.logD("Endpoint::" + endpoint.toString());
        progressMsg.setText("Connected");
        progressMsg.setVisibility(View.GONE);
//        addEndpoint(endpoint);
        adapter.addEndpoint(endpoint);
    }

    @Override
    public void onEndpointDiscovered(ConnectionService.Endpoint endpoint) {

    }

    @Override
    public void onConnectionFailed(ConnectionService.Endpoint endpoint) {
        mConnectionService.logD("onConnectionFailed");
        mConnectionService.logD("Endpoint::" + endpoint.toString());
        progressMsg.setText("Connection Failed to :: " + endpoint.getName());
//        findViewById(R.id.txtConnecting).setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        mConnectionService.setNearbyConnectionListener(this);
        mConnectionService.setNearbyEndpointListener(this);
        mConnectionService.setNearByServiceListener(this);
        mConnectionService.setNearbyDataListener(this);

        super.onStart();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    /**
     * Returns {@code true} if the app was granted all the permissions. Otherwise, returns {@code
     * false}.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the user has accepted (or denied) our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onReceive(ConnectionService.Endpoint endpoint, Payload payload) {
        adapter.notifyUnreadCount(endpoint);
    }
}
