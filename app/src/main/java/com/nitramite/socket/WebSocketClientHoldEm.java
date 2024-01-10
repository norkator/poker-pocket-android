package com.nitramite.socket;

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class WebSocketClientHoldEm {

    // Logging
    private final static String TAG = WebSocketClientHoldEm.class.getSimpleName();

    // Variables
    public WebSocketClient webSocketClient = null;
    private boolean connected = false;

    // Interface
    private final OnWebSocketEvent onWebSocketEvent;

    // Constructor
    public WebSocketClientHoldEm(OnWebSocketEvent listener, final String serverUrl_) {
        onWebSocketEvent = listener;

        URI uri;
        try {
            uri = new URI(serverUrl_);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                onWebSocketEvent.onConnectedEvent();
                connected = true;
            }

            @Override
            public void onTextReceived(String message) {
                onWebSocketEvent.onStringMessage(message);
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onCloseReceived(int reason, String description) {
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        // webSocketClient.addHeader("Origin", "http://developer.example.com");
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void disconnect(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId) {
        if (webSocketClient != null) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"key\": \"disconnect\", \"roomId\": " + roomId + "}");
            webSocketClient.close(1000, 200, "");
        }
    }

    // ---------------------------------------------------------------------------------------------


    public void getRooms(final int CONNECTION_ID, final String SOCKET_KEY, final String MY_PLAYER_NICKNAME, final int roomId, final String roomSortParam) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"getRooms\", \"playerName\": \"" + MY_PLAYER_NICKNAME + "\", \"roomId\": \"" + roomId + "\", \"roomSortParam\": \"" + roomSortParam + "\"}");
        }
    }

    public void getSpectateRooms(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"getSpectateRooms\", \"roomId\": " + roomId + "}");
        }
    }

    public void selectRoom(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"selectRoom\", \"roomId\": " + roomId + "}");
        }
    }

    public void selectSpectateRoom(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"selectSpectateRoom\", \"roomId\": " + roomId + "}");
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"getRoomParams\", \"roomId\": " + roomId + "}");
        }
    }

    public void setFold(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"setFold\", \"roomId\": " + roomId + "}");
        }
    }

    public void setCheck(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"setCheck\", \"roomId\": " + roomId + "}");
        }
    }

    public void setRaise(final int CONNECTION_ID, final String SOCKET_KEY, final int roomId, final int amount) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"setRaise\", \"roomId\": " + roomId + ", \"amount\": " + amount + "}");
        }
    }

    public void createAccount(final int CONNECTION_ID, final String SOCKET_KEY, final String username, final String password, final String email) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"createAccount\", \"name\": " + "\"" + username + "\"" + ", \"password\": " + "\"" + Crypto.sha3_512(password) + "\"" + ", \"email\": " + "\"" + email + "\"" + "}");
        }
    }

    public void userLogin(final int CONNECTION_ID, final String SOCKET_KEY, final String username, final String password) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"userLogin\", \"name\": " + "\"" + username + "\"" + ", \"password\": " + "\"" + Crypto.sha3_512(password) + "\"" + "}");
        }
    }

    public void setLoggedInUserParams(final int CONNECTION_ID, final String SOCKET_KEY, final String username, final String password) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"loggedInUserParams\", \"name\": " + "\"" + username + "\"" + ", \"password\": " + "\"" + password + "\"" + "}");
        }
    }

    public void getLoggedInUserStatistics(final int CONNECTION_ID, final String SOCKET_KEY) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"loggedInUserStatistics\"" + "}");
        }
    }

    public void rewardingAdShown(final int CONNECTION_ID, final String SOCKET_KEY) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"rewardingAdShown\"" + "}");
        }
    }

    public void getRankings(final int CONNECTION_ID, final String SOCKET_KEY) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"getRankings\"" + "}");
        }
    }

    public void getAutoPlayAction(final int CONNECTION_ID, final String SOCKET_KEY) {
        if (isConnected()) {
            webSocketClient.send("{\"connectionId\": " + CONNECTION_ID + ", \"socketKey\":" + "\"" + SOCKET_KEY + "\"" + ", \"key\": \"autoPlayAction\"}");
        }
    }

    // ---------------------------------------------------------------------------------------------
    /* Helpers */

    public boolean isConnected() {
        if (webSocketClient != null) {
            return connected;
        } else {
            return false;
        }
    }

}