package com.xtensolution.nearbyconnection.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;


import com.google.android.gms.nearby.connection.Payload;
import com.google.gson.Gson;
import com.xtensolution.connectionapi.listener.NearbyDataListener;
import com.xtensolution.connectionapi.model.MessageWrapper;
import com.xtensolution.connectionapi.service.ConnectionService;
import com.xtensolution.nearbyconnection.R;
import com.xtensolution.nearbyconnection.SampleApp;
import com.xtensolution.nearbyconnection.ui.adapter.ChatAdapter;

import java.util.ArrayList;

public class ClientChatRoomActivity extends AppCompatActivity implements NearbyDataListener {
    private ChatAdapter mAdapter;
    private static final String KEY_ENDPOINT = "endpoint";
    private static final String KEY_USER_NAME = "userName";
    private EditText etMessage;
    private ConnectionService.Endpoint endpoint;
    private ConnectionService connectionService;

    public static void startChatActivity(Context context, ConnectionService.Endpoint endpoint) {
        Intent intent = new Intent(context, ClientChatRoomActivity.class);
        intent.putExtra(KEY_ENDPOINT, endpoint);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        connectionService = SampleApp.getInstance().getConnectionService();
        connectionService.setNearbyDataListener(this);

        RecyclerView recyclerView = findViewById(R.id.chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ChatAdapter(this, new ArrayList<MessageWrapper>());
        recyclerView.setAdapter(mAdapter);

        if (getIntent().hasExtra(KEY_ENDPOINT)) {
            endpoint = (ConnectionService.Endpoint) getIntent().getSerializableExtra(KEY_ENDPOINT);
            setTitle(endpoint.getName());
        }

        etMessage = findViewById(R.id.etMessage);
//        findViewById(R.id.btnSend).setEnabled(false);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        connectToEndpoint(endpoint);
    }

    private void sendMessage() {
        if (etMessage.getText() != null) {
            String msg = etMessage.getText().toString();
            if (msg.length() > 0) {
                generateAndSendMessage(msg);
            }
        }
    }

    private void generateAndSendMessage(String msg) {
        MessageWrapper message = new MessageWrapper();
        message.setEndpoint(endpoint);
        message.setMessage(msg);
        message.setMyChat(true);
        message.setTimestamp(System.currentTimeMillis());
        String json = new Gson().toJson(message);
        int size = connectionService.getConnectedEndpoints().size();
        if (size > 0)
            connectionService.send(Payload.fromBytes(json.getBytes()));

        connectionService.logD("Connected Endpoint size =>" + size);
        mAdapter.addMessage(message);
        etMessage.setText("");
    }

    @Override
    public void onReceive(ConnectionService.Endpoint endpoint, Payload payload) {
        String json = new String(payload.asBytes());
        MessageWrapper messageWrapper = new Gson().fromJson(json, MessageWrapper.class);
        messageWrapper.setMyChat(false);
        messageWrapper.setEndpoint(endpoint);
        mAdapter.addMessage(messageWrapper);
    }
}
