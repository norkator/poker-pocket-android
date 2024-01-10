package com.nitramite.pokerpocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nitramite.socket.OnWebSocketEvent;
import com.nitramite.socket.WebSocketClientHoldEm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class TexasHoldemStats extends AppCompatActivity implements OnWebSocketEvent {

    // Logging
    private final static String TAG = TexasHoldemStats.class.getSimpleName();

    // WebSocket
    WebSocketClientHoldEm webSocketClient;
    private int CONNECTION_ID = 0;
    private String SOCKET_KEY = null;

    // Features
    private SharedPreferences sharedPreferences;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    // Components
    private TextView moneyTV, winCountTV, loseCountTV;
    private TextView xpTV;
    private TextView gainedMedalsTitleTV;
    private LinearLayout medalsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem_stats);

        // Find components
        moneyTV = (TextView) findViewById(R.id.moneyTV);
        winCountTV = (TextView) findViewById(R.id.winCountTV);
        loseCountTV = (TextView) findViewById(R.id.loseCountTV);
        xpTV = (TextView) findViewById(R.id.xpTV);
        gainedMedalsTitleTV = (TextView) findViewById(R.id.gainedMedalsTitleTV);
        medalsLayout = (LinearLayout) findViewById(R.id.medalsLayout);

        initWebSocketClient();
    } // End of onCreate();


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
                Toast.makeText(TexasHoldemStats.this, "Error creating socket connection!", Toast.LENGTH_LONG).show();
                TexasHoldemStats.this.finish();
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
                    setLoggedInUserParams();
                    break;
                case "loggedInUserParamsResult":
                    loggedInUserParamsResult(jsonObject.getJSONObject("data"));
                    break;
                case "loggedInUserStatisticsResults":
                    loggedInUserStatisticsResults(jsonObject.getJSONObject("data"));
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

    // Result from set logged in user parameters
    private void loggedInUserParamsResult(final JSONObject jsonObject) {
        try {
            if (!jsonObject.getBoolean("result")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TexasHoldemStats.this, "You are logged in from another instance (Web or Android), which is forbidden! This may be error case when old instance is left open.", Toast.LENGTH_LONG).show();
                        TexasHoldemStats.this.finish();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webSocketClient.getLoggedInUserStatistics(CONNECTION_ID, SOCKET_KEY);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loggedInUserStatisticsResults(final JSONObject resultData) {
        try {
            final int money = resultData.getInt("money");
            final String winCount = resultData.getString("winCount");
            final String loseCount = resultData.getString("loseCount");
            final String xp = "Total of " + resultData.getString("xp") + " of XP";
            final int xpLevel = resultData.getInt("xp");
            final JSONArray havingMedalsJsonArray = resultData.getJSONArray("havingMedals");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    moneyTV.setText(currencyFormat.format(money));
                    winCountTV.setText(winCount);
                    loseCountTV.setText(loseCount);
                    xpTV.setText(xp);
                    addGainedMedals(xpLevel, havingMedalsJsonArray);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Draw gained medals
    private void addGainedMedals(final int xpLevel, final JSONArray havingMedalsJsonArray) {
        if (xpLevel < 1000) {
            gainedMedalsTitleTV.setText("You don't have medals yet");
        }
        try {
            for (int i = 0; i < havingMedalsJsonArray.length(); i++) {
                JSONObject havingMedalObject = havingMedalsJsonArray.getJSONObject(i);
                appendGainedMedal(
                        getDrawable(TexasHoldemStats.this, havingMedalObject.getString("image")),
                        havingMedalObject.getString("title")
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Returns drawable resource id with given drawable name
    private static int getDrawable(Context context, String cardDrawableName) {
        return context.getResources().getIdentifier(cardDrawableName, "drawable", context.getPackageName());
    }


    private void appendGainedMedal(final int resourceId, final String level) {
        int width = 0, height = 0;

        // Create inner layout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, this.getResources().getDisplayMetrics());
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, this.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        linearLayout.setLayoutParams(layoutParams);

        // Create image
        ImageView imageView = new ImageView(this);
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, this.getResources().getDisplayMetrics());
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, this.getResources().getDisplayMetrics());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        imageView.setBackground(ContextCompat.getDrawable(this, resourceId));

        // Create text view
        TextView textView = new TextView(this);
        textView.setText(level);
        textView.setGravity(Gravity.CENTER);

        // Add views
        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        medalsLayout.addView(linearLayout);
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
        TexasHoldemStats.this.finish();
    }

    // ---------------------------------------------------------------------------------------------


} 