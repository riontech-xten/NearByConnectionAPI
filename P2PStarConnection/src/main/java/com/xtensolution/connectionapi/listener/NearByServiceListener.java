package com.xtensolution.connectionapi.listener;

import com.google.android.gms.nearby.connection.Strategy;

public interface NearByServiceListener {
    String getName();
    Strategy getStrategy();
    String getServiceId();
}
