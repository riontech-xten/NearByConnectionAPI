package com.xtensolution.nearbyconnection.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xtensolution.nearbyconnection.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void startService(View view) {
        HostActivity.startServiceActivity(this);
    }

    public void startClient(View view) {
        ClientActivity.startClientActivity(this);
    }
}
