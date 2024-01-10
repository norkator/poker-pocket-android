package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nitramite.adapters.ClickListener;
import com.nitramite.adapters.RecyclerTouchListener;
import com.nitramite.adapters.RoomItem;
import com.nitramite.adapters.RoomItemsAdapter;
import com.nitramite.cardlogic.CardVisualOnline;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.dynamic.Player;
import com.nitramite.dynamic.Seat;
import com.nitramite.socket.OnWebSocketEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings({"FieldCanBeLocal", "RedundantCast"})
public class Sevens extends AppCompatActivity implements OnWebSocketEvent {

    // Logging
    private final static String TAG = Sevens.class.getSimpleName();

    // WebSocket
    // WebSocketClientSevens webSocketClient;
    private int CONNECTION_ID = 0;
    private String SOCKET_KEY = null;
    private int ROOM_ID = -1;
    private ProgressDialog progressDialog;

    // Activity features
    private SharedPreferences sharedPreferences;
    private GameCardResource gameCardResource;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private String MY_PLAYER_NICKNAME = "";
    private Boolean IS_DEVELOPMENT_SERVER = false;

    // Animation declarations
    Animation animFadeIn, animFadeOut;

    // Buttons etc
    private TextView currentStatusTV;
    private CardView exitBtn, selectRoomBtn, spectateBtn;
    private Button getCardBtn, skipBtn;
    private HorizontalScrollView scrollView;
    private int scrollViewLastXPosition = 0;
    private LinearLayout cardsScroller;


    // Threads
    private boolean killThreads = false;
    ArrayList<Thread> threadList = new ArrayList<>();
    Thread socketOpenCheckThread;
    Thread holeCardsThread;

    // Center cards
    private ImageView cardHeartsSevenIV, cardDiamondsSevenIV, cardClubsSevenIV, cardSpadesSevenIV;
    private ImageView cardHeartsUpperIV, cardDiamondsUpperIV, cardClubsUpperIV, cardSpadesUpperIV;
    private ImageView cardHeartsLowerIV, cardDiamondsLowerIV, cardClubsLowerIV, cardSpadesLowerIV;

    // Temporary last center cards for replacing logic
    private String cardHeartsSevenStr = "", cardDiamondsSevenStr = "", cardClubsSevenStr = "", cardSpadesSevenStr = "";
    private String cardHeartsUpperStr = "", cardDiamondsUpperStr = "", cardClubsUpperStr = "", cardSpadesUpperStr = "";
    private String cardHeartsLowerStr = "", cardDiamondsLowerStr = "", cardClubsLowerStr = "", cardSpadesLowerStr = "";

    // All available seats are in this array
    private ArrayList<Seat> seats = new ArrayList<>();

    // All players are in this array
    private ArrayList<Player> players = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sevens);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Find button components
        exitBtn = (CardView) findViewById(R.id.exitBtn);
        selectRoomBtn = (CardView) findViewById(R.id.selectRoomBtn);
        spectateBtn = (CardView) findViewById(R.id.spectateBtn);
        getCardBtn = (Button) findViewById(R.id.getCardBtn);
        skipBtn = (Button) findViewById(R.id.skipBtn);
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        cardsScroller = (LinearLayout) findViewById(R.id.cardsScroller);

        // Status view
        currentStatusTV = (TextView) findViewById(R.id.currentStatusTV);


        // Center cards (top, middle sevens, bottom)
        cardHeartsSevenIV = (ImageView) findViewById(R.id.cardHeartsSevenIV);
        cardDiamondsSevenIV = (ImageView) findViewById(R.id.cardDiamondsSevenIV);
        cardClubsSevenIV = (ImageView) findViewById(R.id.cardClubsSevenIV);
        cardSpadesSevenIV = (ImageView) findViewById(R.id.cardSpadesSevenIV);
        cardHeartsUpperIV = (ImageView) findViewById(R.id.cardHeartsUpperIV);
        cardDiamondsUpperIV = (ImageView) findViewById(R.id.cardDiamondsUpperIV);
        cardClubsUpperIV = (ImageView) findViewById(R.id.cardClubsUpperIV);
        cardSpadesUpperIV = (ImageView) findViewById(R.id.cardSpadesUpperIV);
        cardHeartsLowerIV = (ImageView) findViewById(R.id.cardHeartsLowerIV);
        cardDiamondsLowerIV = (ImageView) findViewById(R.id.cardDiamondsLowerIV);
        cardClubsLowerIV = (ImageView) findViewById(R.id.cardClubsLowerIV);
        cardSpadesLowerIV = (ImageView) findViewById(R.id.cardSpadesLowerIV);


        // Seat 1
        FrameLayout seat1Frame = (FrameLayout) findViewById(R.id.seat1Frame);
        CardView seat1Card = (CardView) findViewById(R.id.seat1Card);
        ImageView seat1Card0 = (ImageView) findViewById(R.id.seat1Card0);
        ImageView seat1Card1 = (ImageView) findViewById(R.id.seat1Card1);
        TextView seat1NameTV = (TextView) findViewById(R.id.seat1NameTV);
        TextView seat1MoneyTV = (TextView) findViewById(R.id.seat1MoneyTV);
        ProgressBar seat1TimeBar = (ProgressBar) findViewById(R.id.seat1TimeBar);
        TextView seat1CardsLeftTV = (TextView) findViewById(R.id.seat1CardsLeftTV);
        seats.add(new Seat(seat1Frame, seat1Card, seat1Card0, seat1Card1, null, null, null, null, seat1NameTV, seat1MoneyTV, seat1TimeBar, null, null, seat1CardsLeftTV, null, null, null, null, null, null));

        // Seat 2
        FrameLayout seat2Frame = (FrameLayout) findViewById(R.id.seat2Frame);
        CardView seat2Card = (CardView) findViewById(R.id.seat2Card);
        ImageView seat2Card0 = (ImageView) findViewById(R.id.seat2Card0);
        ImageView seat2Card1 = (ImageView) findViewById(R.id.seat2Card1);
        TextView seat2NameTV = (TextView) findViewById(R.id.seat2NameTV);
        TextView seat2MoneyTV = (TextView) findViewById(R.id.seat2MoneyTV);
        ProgressBar seat2TimeBar = (ProgressBar) findViewById(R.id.seat2TimeBar);
        TextView seat2CardsLeftTV = (TextView) findViewById(R.id.seat2CardsLeftTV);
        seats.add(new Seat(seat2Frame, seat2Card, seat2Card0, seat2Card1, null, null, null, null, seat2NameTV, seat2MoneyTV, seat2TimeBar, null, null, seat2CardsLeftTV, null, null, null, null, null, null));

        // Seat 3
        FrameLayout seat3Frame = (FrameLayout) findViewById(R.id.seat3Frame);
        CardView seat3Card = (CardView) findViewById(R.id.seat3Card);
        ImageView seat3Card0 = (ImageView) findViewById(R.id.seat3Card0);
        ImageView seat3Card1 = (ImageView) findViewById(R.id.seat3Card1);
        TextView seat3NameTV = (TextView) findViewById(R.id.seat3NameTV);
        TextView seat3MoneyTV = (TextView) findViewById(R.id.seat3MoneyTV);
        ProgressBar seat3TimeBar = (ProgressBar) findViewById(R.id.seat3TimeBar);
        TextView seat3CardsLeftTV = (TextView) findViewById(R.id.seat3CardsLeftTV);
        seats.add(new Seat(seat3Frame, seat3Card, seat3Card0, seat3Card1, null, null, null, null, seat3NameTV, seat3MoneyTV, seat3TimeBar, null, null, seat3CardsLeftTV, null, null, null, null, null, null));

        // Seat 4
        FrameLayout seat4Frame = (FrameLayout) findViewById(R.id.seat4Frame);
        CardView seat4Card = (CardView) findViewById(R.id.seat4Card);
        ImageView seat4Card0 = (ImageView) findViewById(R.id.seat4Card0);
        ImageView seat4Card1 = (ImageView) findViewById(R.id.seat4Card1);
        TextView seat4NameTV = (TextView) findViewById(R.id.seat4NameTV);
        TextView seat4MoneyTV = (TextView) findViewById(R.id.seat4MoneyTV);
        ProgressBar seat4TimeBar = (ProgressBar) findViewById(R.id.seat4TimeBar);
        TextView seat4CardsLeftTV = (TextView) findViewById(R.id.seat4CardsLeftTV);
        seats.add(new Seat(seat4Frame, seat4Card, seat4Card0, seat4Card1, null, null, null, null, seat4NameTV, seat4MoneyTV, seat4TimeBar, null, null, seat4CardsLeftTV, null, null, null, null, null, null));

        // Seat 5
        FrameLayout seat5Frame = (FrameLayout) findViewById(R.id.seat5Frame);
        CardView seat5Card = (CardView) findViewById(R.id.seat5Card);
        ImageView seat5Card0 = (ImageView) findViewById(R.id.seat5Card0);
        ImageView seat5Card1 = (ImageView) findViewById(R.id.seat5Card1);
        TextView seat5NameTV = (TextView) findViewById(R.id.seat5NameTV);
        TextView seat5MoneyTV = (TextView) findViewById(R.id.seat5MoneyTV);
        ProgressBar seat5TimeBar = (ProgressBar) findViewById(R.id.seat5TimeBar);
        TextView seat5CardsLeftTV = (TextView) findViewById(R.id.seat5CardsLeftTV);
        seats.add(new Seat(seat5Frame, seat5Card, seat5Card0, seat5Card1, null, null, null, null, seat5NameTV, seat5MoneyTV, seat5TimeBar, null, null, seat5CardsLeftTV, null, null, null, null, null, null));

        // Seat 6
        FrameLayout seat6Frame = (FrameLayout) findViewById(R.id.seat6Frame);
        CardView seat6Card = (CardView) findViewById(R.id.seat6Card);
        ImageView seat6Card0 = (ImageView) findViewById(R.id.seat6Card0);
        ImageView seat6Card1 = (ImageView) findViewById(R.id.seat6Card1);
        TextView seat6NameTV = (TextView) findViewById(R.id.seat6NameTV);
        TextView seat6MoneyTV = (TextView) findViewById(R.id.seat6MoneyTV);
        ProgressBar seat6TimeBar = (ProgressBar) findViewById(R.id.seat6TimeBar);
        TextView seat6CardsLeftTV = (TextView) findViewById(R.id.seat6CardsLeftTV);
        seats.add(new Seat(seat6Frame, seat6Card, seat6Card0, seat6Card1, null, null, null, null, seat6NameTV, seat6MoneyTV, seat6TimeBar, null, null, seat6CardsLeftTV, null, null, null, null, null, null));

        // Get settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        MY_PLAYER_NICKNAME = sharedPreferences.getString(Constants.SP_PLAYER_NICKNAME, "Anon" + new Random().nextInt(1000) + 1);
        IS_DEVELOPMENT_SERVER = sharedPreferences.getBoolean(Constants.SP_DEVELOPMENT_SERVER, false);
        ImageView pokerTableIV = (ImageView) findViewById(R.id.pokerTableIV);
        if (sharedPreferences.getBoolean(Constants.SP_USE_PURPLE_HOLDEM_TABLE, false)) {
            pokerTableIV.setBackground(ContextCompat.getDrawable(this, R.drawable.poker_table_purple));
        }


        // Init threads
        socketOpenCheckThread = new Thread(socketOpenCheckRunnable);
        //holeCardsThread = new Thread(holeCardsRunnable);

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_holdem);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_holdem);

        // Init exit button
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGameDialog();
            }
        });

        // Select room to play in btn
        selectRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadConnectionDialog();
            }
        });

        // Enter into spectating mode btn
        spectateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ROOM_ID == -1) {
//                     webSocketClient.getSpectateRooms(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
                } else {
                    spectatingErrorDialog();
                }
            }
        });

        // Init webSocket listener and get rooms
        initWebSocketClient();

        // Connection progress dialog
        progressDialog = new ProgressDialog(Sevens.this);
        progressDialog.setMessage("Connecting to server...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    } // End of onCreate();


    // ---------------------------------------------------------------------------------------------

    private void initWebSocketClient() {
    }

    // ---------------------------------------------------------------------------------------------


    // Starting point before new game starts
    private void roomParameters(final JSONObject jsonData) {
        Log.i(TAG, jsonData.toString());
        try {
            // Example:
            players.clear();
            final int playerCount = jsonData.getInt("playerCount");
            final ArrayList<Integer> playerIds = new ArrayList<>();
            final ArrayList<String> playerNames = new ArrayList<>();

            JSONArray playersArray = jsonData.getJSONArray("playersData");
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject playerObject = playersArray.getJSONObject(i);
                playerIds.add(playerObject.getInt("playerId"));
                playerNames.add(playerObject.getString("playerName"));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playerSeats(playerCount, playerIds, playerNames);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void playerSeats(int playerCount, ArrayList<Integer> playerIds, ArrayList<String> playerNames) {
        players.clear();
        switch (playerCount) {
            case 1:
                giveSeats(playerCount, new int[]{0}, playerIds, playerNames);
                break;
            case 2:
                giveSeats(playerCount, new int[]{0, 3}, playerIds, playerNames);
                break;
            case 3:
                giveSeats(playerCount, new int[]{0, 2, 3}, playerIds, playerNames);
                break;
            case 4:
                giveSeats(playerCount, new int[]{0, 2, 3, 5}, playerIds, playerNames);
                break;
            case 5:
                giveSeats(playerCount, new int[]{0, 1, 2, 3, 5}, playerIds, playerNames);
                break;
            case 6:
                giveSeats(playerCount, new int[]{0, 1, 2, 3, 4, 5}, playerIds, playerNames);
                break;
        }
    }

    private void giveSeats(int playerCount, int[] seatNum, ArrayList<Integer> playerIds, ArrayList<String> playerNames) {
        initializeSeats();
        for (int i = 0; i < playerCount; i++) {
            players.add(
                    new Player(
                            this,
                            seats.get(seatNum[i]),
                            playerIds.get(i),
                            playerNames.get(i),
                            0,
                            false,
                            Player.TYPE_SEVEN
                    )
            );
            seats.get(seatNum[i]).activateSeat(); // Sets visibility to visible
        }
        initializeViews();
    }

    private void initializeSeats() {
        for (int i = 0; i < seats.size(); i++) {
            seats.get(i).clearSeat();
        }
    }

    // Hides all cards
    private void initializeViews() {
        this.clearMiddleCards();
        this.initOnClickListeners();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setPlayerTimeBar(0);
            players.get(i).setSevenPlayerActionTextView("-");
            players.get(i).clearGlowAnimation();
        }
        skipBtn.setVisibility(View.VISIBLE);
    }


    // ---------------------------------------------------------------------------------------------

    private void statusUpdate(JSONObject jsonData) {
        // Example: {"currentStatus":"Giving cards and waiting a while...","playersData":[{"playerId":0,"playerName":"Anon7571","isPlayerTurn":true,"timeBar":90}],"roundWinnerPlayerId":-1}
        Log.i(TAG, jsonData.toString());
        try {
            final String currentStatus = jsonData.getString("currentStatus");
            final int roundWinnerPlayerId = jsonData.getInt("roundWinnerPlayerId");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentStatusTV.setText(currentStatus);
                }
            });
            JSONArray playersArray = jsonData.getJSONArray("playersData");
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject playerObject = playersArray.getJSONObject(i);
                final int _i = i;
                final boolean isPlayerTurn = playerObject.getBoolean("isPlayerTurn");
                final int playerTimeBar = playerObject.getInt("timeBar");
                final String playerCardsCount = playerObject.getString("cardsCount");
                final int playerId = playerObject.getInt("playerId");
                try {
                    players.get(i).isPlayerTurn = isPlayerTurn;
                } catch (IndexOutOfBoundsException ignored) {
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            players.get(_i).setPlayerTimeBar(playerTimeBar);
                            players.get(_i).setSevenCardsLeftView(playerCardsCount);
                        } catch (IndexOutOfBoundsException ignored) {
                            // Ignore exception for now
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void cardsStatusUpdate(JSONObject jsonData) {
        // Example: {"middleCards":[["","","","","","","","","","","",""],["","","","","","","","","","","",""],["","","","","","","","","","","",""],["","","","","","","","","","","",""]]
        // ,"playerId":0,"cards":["5♠","4♣","J♠","5♦","J♣","2♥","6♠","7♦","2♣","K♦","A♥","2♠","A♣","6♦","K♥","8♠","Q♦","3♦","9♣","2♦","4♥","Q♠","10♥","6♣","10♣","6♥","7♣","7♥","10♦","5♣","Q♥","A♠","4♠","7♠","4♦","8♥","A♦","10♠","8♣","5♥","K♠","Q♣","9♠","9♥","9♦","3♠","K♣","J♥","3♥","3♣","J♦","8♦"]}
        Log.i(TAG, jsonData.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardsScroller.removeAllViews();
            }
        });
        try {
            // Parse player's cards
            final Integer playerId = jsonData.getInt("playerId");
            for (int i = 0; i < this.players.size(); i++) {
                if (this.players.get(i).playerId == playerId) {
                    final JSONArray myCards = jsonData.getJSONArray("cards");
                    this.scrollViewLastXPosition = scrollView.getScrollX();
                    for (int c = 0; c < myCards.length(); c++) {
                        final String card = myCards.getString(c);
                        if (!card.equals("null")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView cardIV = new ImageView(Sevens.this);
                                    cardIV.setBackground(ContextCompat.getDrawable(Sevens.this, gameCardResource.cardResourceId(Sevens.this, new CardVisualOnline(card).toWords())));
                                    cardIV.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            for (int i = 0; i < players.size(); i++) {
                                                if (players.get(i).playerId == CONNECTION_ID) {
                                                    if (players.get(i).isPlayerTurn) {
                                                        // webSocketClient.playerSetCard(CONNECTION_ID, SOCKET_KEY, ROOM_ID, card);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(toDpi(40), toDpi(50));
                                    layoutParams.setMargins(toDpi(2), 0, toDpi(2), 0);
                                    cardIV.setLayoutParams(layoutParams);
                                    cardsScroller.addView(cardIV);
                                }
                            });
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.setScrollX(scrollViewLastXPosition);
                        }
                    });
                }
            }
            // Parse middle cards
            final String cardHeartsUpperStr = jsonData.getString("cardHeartsUpperStr");
            final String cardDiamondsUpperStr = jsonData.getString("cardDiamondsUpperStr");
            final String cardClubsUpperStr = jsonData.getString("cardClubsUpperStr");
            final String cardSpadesUpperStr = jsonData.getString("cardSpadesUpperStr");
            final String cardHeartsSevenStr = jsonData.getString("cardHeartsSevenStr");
            final String cardDiamondsSevenStr = jsonData.getString("cardDiamondsSevenStr");
            final String cardClubsSevenStr = jsonData.getString("cardClubsSevenStr");
            final String cardSpadesSevenStr = jsonData.getString("cardSpadesSevenStr");
            final String cardHeartsLowerStr = jsonData.getString("cardHeartsLowerStr");
            final String cardDiamondsLowerStr = jsonData.getString("cardDiamondsLowerStr");
            final String cardClubsLowerStr = jsonData.getString("cardClubsLowerStr");
            final String cardSpadesLowerStr = jsonData.getString("cardSpadesLowerStr");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUpperCardDrawable(0, cardHeartsUpperStr);
                    setUpperCardDrawable(1, cardDiamondsUpperStr);
                    setUpperCardDrawable(2, cardClubsUpperStr);
                    setUpperCardDrawable(3, cardSpadesUpperStr);
                    setSevenCardDrawable(0, cardHeartsSevenStr);
                    setSevenCardDrawable(1, cardDiamondsSevenStr);
                    setSevenCardDrawable(2, cardClubsSevenStr);
                    setSevenCardDrawable(3, cardSpadesSevenStr);
                    setLowerCardDrawable(0, cardHeartsLowerStr);
                    setLowerCardDrawable(1, cardDiamondsLowerStr);
                    setLowerCardDrawable(2, cardClubsLowerStr);
                    setLowerCardDrawable(3, cardSpadesLowerStr);
                }
            });
            // Parse other parameters
            final Boolean allSevensSet = jsonData.getBoolean("allSevensSet");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (allSevensSet && skipBtn.getVisibility() == View.VISIBLE) {
                        skipBtn.setVisibility(View.GONE);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------


    private void turnErrorMessage(JSONObject jsonData) {
        // Example:
        Log.i(TAG, jsonData.toString());
        try {
            final String playerId = jsonData.getString("playerId");
            final String warningMessage = jsonData.getString("message");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Sevens.this, warningMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------

    // We have a winner, show animation
    private void roundWinner(JSONObject jsonData) {
        try {
            final int roundWinnerPlayerId = jsonData.getInt("roundWinnerPlayerId");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < players.size(); i++) {
                        if (roundWinnerPlayerId == players.get(i).playerId) {
                            players.get(i).startGlowAnimation();
                            players.get(i).setSevenPlayerActionTextView("! WINNER !");
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // WebSocket communications and methods

    @Override
    public void onConnectedEvent() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        // if (webSocketClient.webSocket != null) {
        //     socketOpenCheckThread = new Thread(socketOpenCheckRunnable);
        //     threadList.add(socketOpenCheckThread);
        //     if (!socketOpenCheckThread.isAlive()) {
        //         socketOpenCheckThread.start();
        //     }
        //     // webSocketMessageListener();
        // } else {
        //     runOnUiThread(new Runnable() {
        //         @Override
        //         public void run() {
        //             Toast.makeText(Sevens.this, "Error creating socket connection!", Toast.LENGTH_LONG).show();
        //             Sevens.this.finish();
        //         }
        //     });
        // }
    }

    @Override
    public void onStringMessage(String msg) {
    }


    // private void webSocketMessageListener() {
    //     webSocketClient.webSocket.setStringCallback(new WebSocket.StringCallback() {
    //         @Override
    //         public void onStringAvailable(String s) {
    //             try {
    //                 JSONObject jsonObject = new JSONObject(s);
    //                 switch (jsonObject.getString("key")) {
    //                     case "connectionId":
    //                         CONNECTION_ID = jsonObject.getInt("connectionId");
    //                         SOCKET_KEY = jsonObject.getString("socketKey");
    //                         webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID);
    //                         break;
    //                     case "getRooms":
    //                         parseRooms(jsonObject.getJSONArray("data"), false);
    //                         break;
    //                     case "getSpectateRooms":
    //                         parseRooms(jsonObject.getJSONArray("data"), true);
    //                         break;
    //                     case "roomParams":
    //                         roomParameters(jsonObject.getJSONObject("data"));
    //                         break;
    //                     case "statusUpdate":
    //                         statusUpdate(jsonObject.getJSONObject("data"));
    //                         break;
    //                     case "cardsStatusUpdate":
    //                         cardsStatusUpdate(jsonObject.getJSONObject("data"));
    //                         break;
    //                     case "turnErrorMessage":
    //                         turnErrorMessage(jsonObject.getJSONObject("data"));
    //                         break;
    //                     case "lastUserAction":
    //                         playerLastActionHandler(jsonObject.getJSONObject("data"));
    //                         break;
    //                     case "audioCommand":
    //                         audioCommand(jsonObject.getJSONObject("data"));
    //                         break;
    //                     case "roundWinner":
    //                         roundWinner(jsonObject.getJSONObject("data"));
    //                         break;
    //                 }
    //             } catch (JSONException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     });
    // }

    // ---------------------------------------------------------------------------------------------


    private void parseRooms(JSONArray roomData, final Boolean isSpectateMode) {
        Log.i(TAG, "Parse rooms data: " + roomData.toString());
        final ArrayList<RoomItem> roomsList = new ArrayList<>();
        try {
            final JSONArray roomsArray = roomData;
            for (int i = 0; i < roomsArray.length(); i++) {
                JSONObject roomObject = roomsArray.getJSONObject(i);
                RoomItem roomItem = new RoomItem(
                        this, R.drawable.room_icon, roomObject.getInt("roomId"), roomObject.getString("roomName") + " ➟ (" + roomObject.getString("playerCount") + "/" + roomObject.getString("maxPlayers") + ")", null
                );
                roomsList.add(roomItem);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roomsDialog(roomsList, isSpectateMode);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void roomsDialog(final ArrayList<RoomItem> roomsList, final Boolean isSpectateMode) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.rooms_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        TextView roomListTitle = (TextView) dialog.findViewById(R.id.roomListTitle);
        if (isSpectateMode) {
            roomListTitle.setText("Select room to spectate");
        } else {
            roomListTitle.setText("Select room");
        }
        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerView);
        RoomItemsAdapter roomItemsAdapter = new RoomItemsAdapter(roomsList);
        recyclerView.setAdapter(roomItemsAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                audioPlayer.playCardChipLayOne(Sevens.this);
                ROOM_ID = roomsList.get(position).getRoomId();
                // if (isSpectateMode) {
                //     webSocketClient.selectSpectateRoom(CONNECTION_ID, SOCKET_KEY, roomsList.get(position).getRoomId());
                // } else {
                //     webSocketClient.selectRoom(CONNECTION_ID, SOCKET_KEY, roomsList.get(position).getRoomId());
                // }
                dialog.dismiss();
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        final Button playingRoomsBtn = (Button) dialog.findViewById(R.id.playingRoomsBtn);
        playingRoomsBtn.setVisibility(View.GONE);
        final Button spectateRoomsBtn = (Button) dialog.findViewById(R.id.spectateRoomsBtn);
        spectateRoomsBtn.setVisibility(View.GONE);
        final RadioButton allRB = (RadioButton) dialog.findViewById(R.id.allRB);
        allRB.setVisibility(View.GONE);
        final RadioButton lowRB = (RadioButton) dialog.findViewById(R.id.lowRB);
        lowRB.setVisibility(View.GONE);
        final RadioButton mediumRB = (RadioButton) dialog.findViewById(R.id.mediumRB);
        mediumRB.setVisibility(View.GONE);
        final RadioButton highRB = (RadioButton) dialog.findViewById(R.id.highRB);
        highRB.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------------------------------------
    /* Helpers */


    // Audio commands to play other players action sounds
    private void audioCommand(final JSONObject jsonData) {
        try {
            final String audioCommand = jsonData.getString("command");
            switch (audioCommand) {
                case "placeCard":
                    audioPlayer.playCardPlaceOne(this);
                    break;
                case "getCard":
                    audioPlayer.playCardShoveTwo(this);
                    break;
                case "skipCard":
                    audioPlayer.playCardChipLayOne(this);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Handles last player animation text's
    private void playerLastActionHandler(final JSONObject jsonData) {
        try {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).playerId == jsonData.getInt("playerId")) {
                    final String actionText = jsonData.getString("actionText");
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            players.get(finalI).setSevenPlayerActionTextView(actionText);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Clear all middle cards drawables
    private void clearMiddleCards() {
        this.cardHeartsSevenIV.setBackground(null);
        this.cardDiamondsSevenIV.setBackground(null);
        this.cardClubsSevenIV.setBackground(null);
        this.cardSpadesSevenIV.setBackground(null);

        cardHeartsUpperIV.setBackground(null);
        cardDiamondsUpperIV.setBackground(null);
        cardClubsUpperIV.setBackground(null);
        cardSpadesUpperIV.setBackground(null);

        cardHeartsLowerIV.setBackground(null);
        cardDiamondsLowerIV.setBackground(null);
        cardClubsLowerIV.setBackground(null);
        cardSpadesLowerIV.setBackground(null);
    }


    private void setSevenCardDrawable(int card, String cardStr) {
        if (!cardStr.equals("") && !cardStr.equals("null")) {
            switch (card) {
                case 0:
                    if (!this.cardHeartsSevenStr.equals(cardStr)) {
                        this.cardHeartsSevenIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        this.cardHeartsSevenIV.startAnimation(animFadeIn);
                        this.cardHeartsSevenStr = cardStr;
                    }
                    break;
                case 1:
                    if (!this.cardDiamondsSevenStr.equals(cardStr)) {
                        this.cardDiamondsSevenIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        this.cardDiamondsSevenIV.startAnimation(animFadeIn);
                        this.cardDiamondsSevenStr = cardStr;
                    }
                    break;
                case 2:
                    if (!this.cardClubsSevenStr.equals(cardStr)) {
                        this.cardClubsSevenIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        this.cardClubsSevenIV.startAnimation(animFadeIn);
                        this.cardClubsSevenStr = cardStr;
                    }
                    break;
                case 3:
                    if (!this.cardSpadesSevenStr.equals(cardStr)) {
                        this.cardSpadesSevenIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        this.cardSpadesSevenIV.startAnimation(animFadeIn);
                        this.cardSpadesSevenStr = cardStr;
                    }
                    break;
            }
        }
    }


    private void setUpperCardDrawable(int card, String cardStr) {
        if (!cardStr.equals("") && !cardStr.equals("null")) {
            switch (card) {
                case 0:
                    if (!this.cardHeartsUpperStr.equals(cardStr)) {
                        if (cardStr.contains("K")) {
                            this.cardHeartsUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardHeartsUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardHeartsUpperIV.startAnimation(animFadeIn);
                        this.cardHeartsUpperStr = cardStr;
                    }
                    break;
                case 1:
                    if (!this.cardDiamondsUpperStr.equals(cardStr)) {
                        if (cardStr.contains("K")) {
                            this.cardDiamondsUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardDiamondsUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardDiamondsUpperIV.startAnimation(animFadeIn);
                        this.cardDiamondsUpperStr = cardStr;
                    }
                    break;
                case 2:
                    if (!this.cardClubsUpperStr.equals(cardStr)) {
                        if (cardStr.contains("K")) {
                            this.cardClubsUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardClubsUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardClubsUpperIV.startAnimation(animFadeIn);
                        this.cardClubsUpperStr = cardStr;
                    }
                    break;
                case 3:
                    if (!this.cardSpadesUpperStr.equals(cardStr)) {
                        if (cardStr.contains("K")) {
                            this.cardSpadesUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardSpadesUpperIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardSpadesUpperIV.startAnimation(animFadeIn);
                        this.cardSpadesUpperStr = cardStr;
                    }
                    break;
            }
        }
    }


    private void setLowerCardDrawable(int card, String cardStr) {
        if (!cardStr.equals("") && !cardStr.equals("null")) {
            switch (card) {
                case 0:
                    if (!this.cardHeartsLowerStr.equals(cardStr)) {
                        if (cardStr.contains("A")) {
                            this.cardHeartsLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardHeartsLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardHeartsLowerIV.startAnimation(animFadeIn);
                        this.cardHeartsLowerStr = cardStr;
                    }
                    break;
                case 1:
                    if (!this.cardDiamondsLowerStr.equals(cardStr)) {
                        if (cardStr.contains("A")) {
                            this.cardDiamondsLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardDiamondsLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardDiamondsLowerIV.startAnimation(animFadeIn);
                        this.cardDiamondsLowerStr = cardStr;
                    }
                    break;
                case 2:
                    if (!this.cardClubsLowerStr.equals(cardStr)) {
                        if (cardStr.contains("A")) {
                            this.cardClubsLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {
                            this.cardClubsLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardClubsLowerIV.startAnimation(animFadeIn);
                        this.cardClubsLowerStr = cardStr;
                    }
                    break;
                case 3:
                    if (!this.cardSpadesLowerStr.equals(cardStr)) {
                        if (cardStr.contains("A")) {
                            this.cardSpadesLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        } else {

                            this.cardSpadesLowerIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(cardStr).toWords())));
                        }
                        this.cardSpadesLowerIV.startAnimation(animFadeIn);
                        this.cardSpadesLowerStr = cardStr;
                    }
                    break;
            }
        }
    }


    // Initialize onClick listener's
    private void initOnClickListeners() {
        getCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).playerId == CONNECTION_ID) {
                        if (players.get(i).isPlayerTurn) {
                            // webSocketClient.playerGetCard(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
                        }
                    }
                }
            }
        });
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).playerId == CONNECTION_ID) {
                        if (players.get(i).isPlayerTurn) {
                            // webSocketClient.playerSkipTurn(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
                        }
                    }
                }
            }
        });
    }


    // Card dpi
    private int toDpi(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    private void killThreads(boolean finish) {
        killThreads = true;
        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).interrupt();
        }
        if (finish) {
            Sevens.this.finish();
        }
    }


    Runnable socketOpenCheckRunnable = new Runnable() {
        @Override
        public void run() {
        }
    };


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
        endGameDialog();
    }


    private void reloadConnectionDialog() {
        new AlertDialog.Builder(Sevens.this)
                .setTitle("Reload rooms")
                .setMessage("This action closes current connection and does reload, continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        initWebSocketClient();
                    }
                })
                .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Return
                    }
                })
                .setIcon(R.mipmap.logo)
                .show();
    }


    private void spectatingErrorDialog() {
        new AlertDialog.Builder(Sevens.this)
                .setTitle("Spectate mode")
                .setMessage("Spectating is only possible if there's no existing room connection. Re-open Hold'em online, close room selection dialog and press \"S\" again.")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Return
                    }
                })
                .setIcon(R.mipmap.logo)
                .show();
    }


    private void endGameDialog() {
        new AlertDialog.Builder(Sevens.this)
                .setTitle("Quit")
                .setMessage("Are you sure that you want to leave this online game?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // webSocketClient.disconnect(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
                        killThreads(true);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Return
                    }
                })
                .setIcon(R.mipmap.logo)
                .show();
    }


    private void socketClosedErrorDialog() {
        new AlertDialog.Builder(Sevens.this)
                .setTitle("Socket error")
                .setMessage("Socket closed unexpectedly, do you want to stay looking around or quit?")
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Sevens.this.finish();
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Return
                    }
                })
                .setIcon(R.mipmap.logo)
                .show();
    }


} 