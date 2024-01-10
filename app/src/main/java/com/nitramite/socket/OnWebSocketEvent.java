package com.nitramite.socket;

public interface OnWebSocketEvent {

    void onConnectedEvent();

    void onStringMessage(String msg);

}