package com.xtensolution.connectionapi.listener;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public interface DataReceivedListener {
    void onDataReceived(String endpointId, Payload payload);
    void onDataTransfered(String endpointId, PayloadTransferUpdate update);
}
