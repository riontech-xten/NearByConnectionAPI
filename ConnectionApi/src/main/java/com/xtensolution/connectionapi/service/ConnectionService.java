package com.xtensolution.connectionapi.service;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.xtensolution.connectionapi.listener.NearByServiceListener;
import com.xtensolution.connectionapi.listener.NearbyAdvertisingListener;
import com.xtensolution.connectionapi.listener.NearbyConnectionListener;
import com.xtensolution.connectionapi.listener.NearbyDataListener;
import com.xtensolution.connectionapi.listener.NearbyDiscoveringListener;
import com.xtensolution.connectionapi.listener.NearbyEndpointListener;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class ConnectionService {

    private static final String TAG = ConnectionService.class.getSimpleName();
    private static ConnectionService ourInstance;
    private GoogleApiClient googleApiClient;
    /**
     * Our handler to Nearby Connections.
     */
    private ConnectionsClient mConnectionsClient;

    /**
     * The devices we've discovered near us.
     */
    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();

    /**
     * The devices we have pending connections to. They will stay pending until we call {@link
     * #acceptConnection(Endpoint)} or {@link #rejectConnection(Endpoint)}.
     */
    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();

    /**
     * The devices we are currently connected to. For advertisers, this may be large. For discoverers,
     * there will only be one entry in this map.
     */
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();

    /**
     * True if we are asking a discovered device to connect to us. While we ask, we cannot ask another
     * device.
     */
    private boolean mIsConnecting = false;

    /**
     * True if we are discovering.
     */
    private boolean mIsDiscovering = false;

    /**
     * True if we are advertising.
     */
    private boolean mIsAdvertising = false;

    //create reference for all the listeneres that you have created
    private NearbyAdvertisingListener mNearbyAdvertisingListener;
    private NearbyDiscoveringListener mNearbyDiscoveringListener;
    private NearbyDataListener mNearbyDataListener;
    private NearbyEndpointListener mNearbyEndpointListener;
    private NearbyConnectionListener mNearbyConnectionListener;
    private NearByServiceListener mNearByServiceListener;

    public static ConnectionService getInstance(Context context) {
        synchronized (ConnectionService.class) {
            ourInstance = new ConnectionService(context);
        }
        return ourInstance;
    }

    private ConnectionService(Context context) {
        mConnectionsClient = Nearby.getConnectionsClient(context);
    }

    /**
     * Callbacks for connections to other devices.
     */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            logD(String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)",
                    endpointId, connectionInfo.getEndpointName()));
            Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
            mPendingConnections.put(endpointId, endpoint);
            mNearbyConnectionListener.onConnectionInitiated(endpoint, connectionInfo);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            logD(String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));

            // We're no longer connecting
            mIsConnecting = false;

            if (!result.getStatus().isSuccess()) {
                logW(
                        String.format(
                                "Connection failed. Received status %s.",
                                ConnectionService.toString(result.getStatus())));
                mNearbyConnectionListener.onConnectionFailed(mPendingConnections.remove(endpointId));
                return;
            }
            connectedToEndpoint(mPendingConnections.remove(endpointId));
        }

        @Override
        public void onDisconnected(String endpointId) {
            if (!mEstablishedConnections.containsKey(endpointId)) {
                logW("Unexpected disconnection from endpoint " + endpointId);
                return;
            }
            disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));
        }
    };

    /**
     * Callbacks for payloads (bytes of data) sent from another device to us.
     */
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            logD(String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
            mNearbyDataListener.onReceive(mEstablishedConnections.get(endpointId), payload);
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            logD(String.format("onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update));
        }
    };

    /**
     * Sets the device to advertising mode. It will broadcast to other devices in discovery mode.
     * Either {@link NearbyAdvertisingListener#onAdvertisingStarted()} or {@link NearbyAdvertisingListener#onAdvertisingFailed()} will be called once
     * we've found out if we successfully entered this mode.
     */
    public void startAdvertising() {
        mIsAdvertising = true;
        final String localEndpointName = mNearByServiceListener.getName();

        Task<Void> voidTask = mConnectionsClient.startAdvertising(localEndpointName, mNearByServiceListener.getServiceId(), mConnectionLifecycleCallback,
                new AdvertisingOptions(mNearByServiceListener.getStrategy()));

        voidTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unusedResult) {
                logV("Now advertising endpoint " + localEndpointName);
                mNearbyAdvertisingListener.onAdvertisingStarted();
            }
        });

        voidTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mIsAdvertising = false;
                logW("startAdvertising() failed.", e);
                mNearbyAdvertisingListener.onAdvertisingFailed();
            }
        });
    }

    /**
     * Stops advertising.
     */
    public void stopAdvertising() {
        mIsAdvertising = false;
        mConnectionsClient.stopAdvertising();
    }

    /**
     * Accepts a connection request.
     */
    public void acceptConnection(final Endpoint endpoint) {
        Task<Void> voidTask = mConnectionsClient.acceptConnection(endpoint.getId(), mPayloadCallback);
        voidTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logW("acceptConnection() failed.", e);
            }
        });
    }

    /**
     * Rejects a connection request.
     */
    public void rejectConnection(Endpoint endpoint) {
        Task<Void> voidTask = mConnectionsClient.rejectConnection(endpoint.getId());
        voidTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logW("rejectConnection() failed.", e);
            }
        });
    }

    /**
     * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
     * {@link NearbyDiscoveringListener#onDiscoveryStarted()} or {@link NearbyDiscoveringListener#onDiscoveryFailed()} will be called once we've found
     * out if we successfully entered this mode.
     */
    public void startDiscovering() {
        mIsDiscovering = true;
        mDiscoveredEndpoints.clear();

        Task<Void> voidTask = mConnectionsClient.startDiscovery(mNearByServiceListener.getServiceId(), mEndpointDiscoveryCallback,
                new DiscoveryOptions(mNearByServiceListener.getStrategy()));

        voidTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unusedResult) {
                mNearbyDiscoveringListener.onDiscoveryStarted();
            }
        });

        voidTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mIsDiscovering = false;
                logW("startDiscovering() failed.", e);
                mNearbyDiscoveringListener.onDiscoveryFailed();
            }
        });
    }


    private EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            logD(String.format("onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                    endpointId, info.getServiceId(), info.getEndpointName()));

            if (mNearByServiceListener.getServiceId().equals(info.getServiceId())) {
                Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
                mDiscoveredEndpoints.put(endpointId, endpoint);
                mNearbyEndpointListener.onEndpointDiscovered(endpoint);
            }
        }

        @Override
        public void onEndpointLost(String endpointId) {
            logD(String.format("onEndpointLost(endpointId=%s)", endpointId));
        }
    };

    /**
     * Stops discovery.
     */
    public void stopDiscovering() {
        mIsDiscovering = false;
        mConnectionsClient.stopDiscovery();
    }

    /**
     * Disconnects from the given endpoint.
     */
    public void disconnect(Endpoint endpoint) {
        mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
        mEstablishedConnections.remove(endpoint.getId());
    }

    /**
     * Disconnects from all currently connected endpoints.
     */
    public void disconnectFromAllEndpoints() {
        for (Endpoint endpoint : mEstablishedConnections.values()) {
            mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
        }
        mEstablishedConnections.clear();
    }

    /**
     * Resets and clears all state in Nearby Connections.
     */
    public void stopAllEndpoints() {
        mConnectionsClient.stopAllEndpoints();
        mIsAdvertising = false;
        mIsDiscovering = false;
        mIsConnecting = false;
        mDiscoveredEndpoints.clear();
        mPendingConnections.clear();
        mEstablishedConnections.clear();
    }

    /**
     * Sends a connection request to the endpoint. Either {@link NearbyConnectionListener#onConnectionInitiated(Endpoint,
     * ConnectionInfo)} or {@link NearbyConnectionListener#onConnectionFailed(Endpoint)} will be called once we've found out
     * if we successfully reached the device.
     */
    public void connectToEndpoint(final Endpoint endpoint) {
        logV("Sending a connection request to endpoint " + endpoint);
        // Mark ourselves as connecting so we don't connect multiple times
        mIsConnecting = true;

        // Ask to connect
        Task<Void> voidTask = mConnectionsClient.requestConnection(mNearByServiceListener.getName(), endpoint.getId(), mConnectionLifecycleCallback);
        voidTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logW("requestConnection() failed.", e);
                mIsConnecting = false;
                mNearbyConnectionListener.onConnectionFailed(endpoint);
            }
        });
    }

    /**
     * Returns {@code true} if we're currently attempting to connect to another device.
     */
    public final boolean isConnecting() {
        return mIsConnecting;
    }

    private void connectedToEndpoint(Endpoint endpoint) {
        logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.put(endpoint.getId(), endpoint);
        mNearbyEndpointListener.onEndpointConnected(endpoint);
    }

    private void disconnectedFromEndpoint(Endpoint endpoint) {
        logD(String.format("disconnectedFromEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.remove(endpoint.getId());
        mNearbyEndpointListener.onEndpointDisconnected(endpoint);
    }

    /**
     * Returns a list of currently connected endpoints.
     */
    public Set<Endpoint> getDiscoveredEndpoints() {
        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.addAll(mDiscoveredEndpoints.values());
        return endpoints;
    }

    /**
     * Returns a list of currently connected endpoints.
     */
    public Set<Endpoint> getConnectedEndpoints() {
        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.addAll(mEstablishedConnections.values());
        return endpoints;
    }

    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     *
     * @param payload The data you want to send.
     */
    public void send(Payload payload) {
        send(payload, mEstablishedConnections.keySet());
    }

    private void send(Payload payload, Set<String> endpoints) {
        Task<Void> voidTask = mConnectionsClient.sendPayload(new ArrayList<>(endpoints), payload);
        voidTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logW("sendPayload() failed.", e);
            }
        });
    }


    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setNearbyAdvertisingListener(NearbyAdvertisingListener nearbyAdvertisingListener) {
        mNearbyAdvertisingListener = nearbyAdvertisingListener;
    }

    public NearbyAdvertisingListener getNearbyAdvertisingListener() {
        return mNearbyAdvertisingListener;
    }

    public NearbyDiscoveringListener getNearbyDiscoveringListener() {
        return mNearbyDiscoveringListener;
    }

    public void setNearbyDiscoveringListener(NearbyDiscoveringListener mNearbyDiscoveringListener) {
        this.mNearbyDiscoveringListener = mNearbyDiscoveringListener;
    }

    public NearbyDataListener getNearbyDataListener() {
        return mNearbyDataListener;
    }

    public void setNearbyDataListener(NearbyDataListener mNearbyDataListener) {
        this.mNearbyDataListener = mNearbyDataListener;
    }

    public NearbyEndpointListener getNearbyEndpointListener() {
        return mNearbyEndpointListener;
    }

    public void setNearbyEndpointListener(NearbyEndpointListener mNearbyEndpointListener) {
        this.mNearbyEndpointListener = mNearbyEndpointListener;
    }

    public NearbyConnectionListener getNearbyConnectionListener() {
        return mNearbyConnectionListener;
    }

    public void setNearbyConnectionListener(NearbyConnectionListener mNearbyConnectionListener) {
        this.mNearbyConnectionListener = mNearbyConnectionListener;
    }

    public void setNearByServiceListener(NearByServiceListener mNearByServiceListener) {
        this.mNearByServiceListener = mNearByServiceListener;
    }

    public NearByServiceListener getNearByServiceListener() {
        return mNearByServiceListener;
    }

    /**
     * Transforms a {@link Status} into a English-readable message for logging.
     *
     * @param status The current status
     * @return A readable String. eg. [404]File not found.
     */
    private static String toString(Status status) {
        return String.format(
                Locale.US,
                "[%d]%s",
                status.getStatusCode(),
                status.getStatusMessage() != null
                        ? status.getStatusMessage()
                        : ConnectionsStatusCodes.getStatusCodeString(status.getStatusCode()));
    }

    @CallSuper
    public void logV(String msg) {
        Log.v(TAG, msg);
    }

    @CallSuper
    public void logD(String msg) {
        Log.d(TAG, msg);
    }

    @CallSuper
    public void logW(String msg) {
        Log.w(TAG, msg);
    }

    @CallSuper
    public void logW(String msg, Throwable e) {
        Log.w(TAG, msg, e);
    }

    @CallSuper
    public void logE(String msg, Throwable e) {
        Log.e(TAG, msg, e);
    }

    /**
     * Represents a device we can talk to.
     */
    public static class Endpoint implements Serializable {
        @NonNull
        private final String id;
        @NonNull
        private final String name;
        private int unreadCount;

        private Endpoint(@NonNull String id, @NonNull String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        public String getId() {
            return id;
        }

        @NonNull
        public String getName() {
            return name;
        }

        public int getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof ConnectionService.Endpoint) {
                ConnectionService.Endpoint other = (ConnectionService.Endpoint) obj;
                return id.equals(other.id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Endpoint{id=%s, name=%s}", id, name);
        }
    }
}
