package com.nitramite.pokerpocket;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nitramite.adapters.RankingItem;
import com.nitramite.adapters.RankingItemsAdapter;
import com.nitramite.socket.OnWebSocketEvent;
import com.nitramite.socket.WebSocketClientHoldEm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TexasHoldemRankings extends AppCompatActivity implements OnWebSocketEvent {

    // Logging
    private final static String TAG = TexasHoldemRankings.class.getSimpleName();

    // WebSocket
    WebSocketClientHoldEm webSocketClient;
    private int CONNECTION_ID = 0;
    private String SOCKET_KEY = null;

    // Features
    private SharedPreferences sharedPreferences;

    // Components
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem_rankings);

        // Find components
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        initWebSocketClient();
    } // End of onCreate()


    private void initWebSocketClient() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final boolean isDevelopmentServer = sharedPreferences.getBoolean(Constants.SP_DEVELOPMENT_SERVER, false);
        if (isDevelopmentServer) {
            webSocketClient = new WebSocketClientHoldEm(this, Constants.DEVELOPMENT_SERVER);
        } else {
            webSocketClient = new WebSocketClientHoldEm(this, Constants.PRODUCTION_SERVER);
        }
    }


    @Override
    public void onConnectedEvent() {
        if (webSocketClient.webSocketClient == null) {
            runOnUiThread(() -> {
                Toast.makeText(TexasHoldemRankings.this, "Error creating socket connection!", Toast.LENGTH_LONG).show();
                TexasHoldemRankings.this.finish();
            });
        }
    }

    @Override
    public void onStringMessage(String msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg);
            switch (jsonObject.getString("key")) {
                case "connectionId":
                    CONNECTION_ID = jsonObject.getInt("connectionId");
                    SOCKET_KEY = jsonObject.getString("socketKey");
                    webSocketClient.getRankings(CONNECTION_ID, SOCKET_KEY);
                    break;
                case "getRankingsResult":
                    rankingsResult(jsonObject.getInt("code"), jsonObject.getJSONArray("data"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------


    // Set's logged in user parameters if user is logged in
    private void setLoggedInUserParams() {
        if (sharedPreferences.getBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false)) {
            webSocketClient.setLoggedInUserParams(CONNECTION_ID, SOCKET_KEY, sharedPreferences.getString(Constants.SP_ONLINE_HOLDEM_USERNAME, ""), sharedPreferences.getString(Constants.SP_ONLINE_HOLDEM_PASSWORD, ""));
        }
    }

    // ---------------------------------------------------------------------------------------------


    // Draw rankings
    private void rankingsResult(final int responseCode, final JSONArray rankingsArray) {
        Log.i(TAG, String.valueOf(responseCode));
        Log.i(TAG, rankingsArray.toString());
        final ArrayList<RankingItem> rankingItems = new ArrayList<>();
        try {
            for (int i = 0; i < rankingsArray.length(); i++) {
                JSONObject rankingObject = rankingsArray.getJSONObject(i);
                RankingItem rankingItem = new RankingItem(
                        this,
                        rankingObject.getString("name"),
                        rankingObject.getInt("xp"),
                        rankingObject.getString("icon"),
                        rankingObject.getString("win_count"),
                        rankingObject.getString("lose_count")
                );
                rankingItems.add(rankingItem);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RankingItemsAdapter rankingItemsAdapter = new RankingItemsAdapter(rankingItems);
                    recyclerView.setAdapter(rankingItemsAdapter);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TexasHoldemRankings.this);
                    recyclerView.setLayoutManager(layoutManager);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        webSocketClient.disconnect(CONNECTION_ID, SOCKET_KEY, -1);
        TexasHoldemRankings.this.finish();
    }

    // ---------------------------------------------------------------------------------------------

} 