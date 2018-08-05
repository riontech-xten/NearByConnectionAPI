package com.xtensolution.connectionapi.model;


import com.xtensolution.connectionapi.service.ConnectionService;

public class MessageWrapper {
    private ConnectionService.Endpoint endpoint;
    private String message;
    private long timestamp;
    private String msgType;
    private boolean isMyChat;

    public ConnectionService.Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(ConnectionService.Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public boolean isMyChat() {
        return isMyChat;
    }

    public void setMyChat(boolean myChat) {
        isMyChat = myChat;
    }
}
