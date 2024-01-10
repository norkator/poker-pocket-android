package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nitramite.adapters.ClickListener;
import com.nitramite.adapters.RecyclerTouchListener;
import com.nitramite.adapters.RoomItem;
import com.nitramite.adapters.RoomItemsAdapter;
import com.nitramite.cardlogic.CardVisualOnline;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.dynamic.Player;
import com.nitramite.dynamic.Seat;
import com.nitramite.socket.OnWebSocketEvent;
import com.nitramite.socket.WebSocketClientHoldEm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings({"RedundantCast", "FieldCanBeLocal"})
public class TexasHoldem extends AppCompatActivity implements OnWebSocketEvent, Animation.AnimationListener {

    // Logging
    private final static String TAG = TexasHoldem.class.getSimpleName();

    // WebSocket
    private WebSocketClientHoldEm webSocketClient;
    private int CONNECTION_ID = 0;
    private String SOCKET_KEY = null;
    private int ROOM_ID = -1;
    private ProgressDialog progressDialog;

    // Activity features
    private Boolean activityRunning = true;
    private SharedPreferences sharedPreferences;
    private GameCardResource gameCardResource;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private AudioPlayer audioPlayer = new AudioPlayer();
    private String MY_PLAYER_NICKNAME = "";
    private Boolean IS_DEVELOPMENT_SERVER = false;
    private Boolean autoPlay = false;
    private Boolean autoPlayCommandRequested = false;

    // Rooms dialog related views
    private Dialog roomsDialog;
    private RecyclerTouchListener recyclerTouchListener = null;
    private TextView roomsListTitle;
    private RecyclerView roomsRecyclerView;
    private Button roomsPlayingRoomsBtn, roomsSpectateRoomsBtn, moreFundsBtn;
    private RadioButton roomsAllRB, roomsLowRB, roomsMediumRB, roomsHighRB;

    // Animation declarations
    private Animation animFadeIn, animFadeOut;

    // Buttons etc
    private CardView exitBtn, selectRoomBtn, spectateBtn;
    private Button foldBtn, checkBtn, raiseBtn;
    private Button tenBtn, twentyFiveBtn, oneHundredBtn, fiveHundredBtn, allInBtn;
    private boolean bettingEnabled = false; // Enabled on my turn
    private boolean allRBChecked = true, lowRBChecked = false, mediumRBChecked = false, highRBChecked = false;
    private CardView xpNeededForNextMedalCard;
    private TextView xpNeededForNextMedalTV;
    private TextView roomNameTV, spectatorsCountTV, appendPlayersCountTV, deckStatusTV, deckCardsBurnedTV;

    // Indicator views and variables
    private CardView hintTextCard;
    private TextView currentStatusTV;
    private TextView totalPotTV, minBetTV;

    // Threads
    private boolean killThreads = false;
    private ArrayList<Thread> threadList = new ArrayList<>();
    private Thread socketOpenCheckThread;
    private Thread holeCardsThread;
    private Thread theFlopThread;
    private Thread theTurnThread;
    private Thread theRiverThread;

    // Center cards
    private ImageView card1, card2, card3, card4, card5;
    private ArrayList<ImageView> middleCardIV = new ArrayList<>();
    private CardVisualOnline middleCards[] = new CardVisualOnline[5];

    // All available seats are in this array
    private ArrayList<Seat> seats = new ArrayList<>();

    // All players are in this array
    private ArrayList<Player> players = new ArrayList<>();


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        activityRunning = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Rooms dialog
        roomsDialog = new Dialog(this);
        roomsDialog.setContentView(R.layout.rooms_dialog);
        roomsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        roomsDialog.setTitle("");
        roomsDialog.setCanceledOnTouchOutside(false);
        roomsListTitle = (TextView) roomsDialog.findViewById(R.id.roomListTitle);
        roomsRecyclerView = (RecyclerView) roomsDialog.findViewById(R.id.recyclerView);
        roomsPlayingRoomsBtn = (Button) roomsDialog.findViewById(R.id.playingRoomsBtn);
        roomsSpectateRoomsBtn = (Button) roomsDialog.findViewById(R.id.spectateRoomsBtn);
        moreFundsBtn = (Button) roomsDialog.findViewById(R.id.moreFundsBtn);
        roomsAllRB = (RadioButton) roomsDialog.findViewById(R.id.allRB);
        roomsLowRB = (RadioButton) roomsDialog.findViewById(R.id.lowRB);
        roomsMediumRB = (RadioButton) roomsDialog.findViewById(R.id.mediumRB);
        roomsHighRB = (RadioButton) roomsDialog.findViewById(R.id.highRB);
        initRoomsDialogListeners();


        // Find button components
        exitBtn = (CardView) findViewById(R.id.exitBtn);
        selectRoomBtn = (CardView) findViewById(R.id.selectRoomBtn);
        spectateBtn = (CardView) findViewById(R.id.spectateBtn);
        foldBtn = (Button) findViewById(R.id.foldBtn);
        checkBtn = (Button) findViewById(R.id.checkBtn);
        raiseBtn = (Button) findViewById(R.id.raiseBtn);
        tenBtn = (Button) findViewById(R.id.tenBtn);
        twentyFiveBtn = (Button) findViewById(R.id.twentyFiveBtn);
        oneHundredBtn = (Button) findViewById(R.id.oneHundredBtn);
        fiveHundredBtn = (Button) findViewById(R.id.fiveHundredBtn);
        allInBtn = (Button) findViewById(R.id.allInBtn);
        xpNeededForNextMedalCard = (CardView) findViewById(R.id.xpNeededForNextMedalCard);
        xpNeededForNextMedalTV = (TextView) findViewById(R.id.xpNeededForNextMedalTV);
        roomNameTV = (TextView) findViewById(R.id.roomNameTV);
        spectatorsCountTV = (TextView) findViewById(R.id.spectatorsCountTV);
        appendPlayersCountTV = (TextView) findViewById(R.id.appendPlayersCountTV);
        deckStatusTV = (TextView) findViewById(R.id.deckStatusTV);
        deckCardsBurnedTV = (TextView) findViewById(R.id.deckCardsBurnedTV);

        // Find text views
        totalPotTV = (TextView) findViewById(R.id.totalPotTV);
        minBetTV = (TextView) findViewById(R.id.minBetTV);
        setMinBet(0);
        hintTextCard = (CardView) findViewById(R.id.hintTextCard);
        currentStatusTV = (TextView) findViewById(R.id.currentStatusTV);

        // Find card components
        card1 = (ImageView) findViewById(R.id.card1);
        middleCardIV.add(card1);
        card2 = (ImageView) findViewById(R.id.card2);
        middleCardIV.add(card2);
        card3 = (ImageView) findViewById(R.id.card3);
        middleCardIV.add(card3);
        card4 = (ImageView) findViewById(R.id.card4);
        middleCardIV.add(card4);
        card5 = (ImageView) findViewById(R.id.card5);
        middleCardIV.add(card5);

        // Seat 1
        FrameLayout seat1Frame = (FrameLayout) findViewById(R.id.seat1Frame);
        CardView seat1Card = (CardView) findViewById(R.id.seat1Card);
        ImageView seat1Card0 = (ImageView) findViewById(R.id.seat1Card0);
        ImageView seat1Card1 = (ImageView) findViewById(R.id.seat1Card1);
        TextView seat1NameTV = (TextView) findViewById(R.id.seat1NameTV);
        TextView seat1MoneyTV = (TextView) findViewById(R.id.seat1MoneyTV);
        ProgressBar seat1TimeBar = (ProgressBar) findViewById(R.id.seat1TimeBar);
        FrameLayout seat1BetFrame = (FrameLayout) findViewById(R.id.seat1BetFrame);
        ImageView seat1ChipsIV = (ImageView) findViewById(R.id.seat1ChipsIV);
        TextView seat1TotalBetTV = (TextView) findViewById(R.id.seat1TotalBetTV);
        TextView seat1LastUserActionTV = (TextView) findViewById(R.id.seat1LastUserActionTV);
        ImageView seat1DealerChip = (ImageView) findViewById(R.id.seat1DealerChip);
        seats.add(new Seat(seat1Frame, seat1Card, seat1Card0, seat1Card1, null, null, null, null, seat1NameTV, seat1MoneyTV, seat1TimeBar,
                seat1BetFrame, seat1ChipsIV, seat1TotalBetTV, null, seat1LastUserActionTV, null, seat1DealerChip, 100f, -100f));


        // Seat 2
        FrameLayout seat2Frame = (FrameLayout) findViewById(R.id.seat2Frame);
        CardView seat2Card = (CardView) findViewById(R.id.seat2Card);
        ImageView seat2Card0 = (ImageView) findViewById(R.id.seat2Card0);
        ImageView seat2Card1 = (ImageView) findViewById(R.id.seat2Card1);
        TextView seat2NameTV = (TextView) findViewById(R.id.seat2NameTV);
        TextView seat2MoneyTV = (TextView) findViewById(R.id.seat2MoneyTV);
        ProgressBar seat2TimeBar = (ProgressBar) findViewById(R.id.seat2TimeBar);
        FrameLayout seat2BetFrame = (FrameLayout) findViewById(R.id.seat2BetFrame);
        ImageView seat2ChipsIV = (ImageView) findViewById(R.id.seat2ChipsIV);
        TextView seat2TotalBetTV = (TextView) findViewById(R.id.seat2TotalBetTV);
        TextView seat2LastUserActionTV = (TextView) findViewById(R.id.seat2LastUserActionTV);
        ImageView seat2DealerChip = (ImageView) findViewById(R.id.seat2DealerChip);
        seats.add(new Seat(seat2Frame, seat2Card, seat2Card0, seat2Card1, null, null, null, null, seat2NameTV, seat2MoneyTV, seat2TimeBar,
                seat2BetFrame, seat2ChipsIV, seat2TotalBetTV, null, seat2LastUserActionTV, null, seat2DealerChip, 240f, 110f));


        // Seat 3
        FrameLayout seat3Frame = (FrameLayout) findViewById(R.id.seat3Frame);
        CardView seat3Card = (CardView) findViewById(R.id.seat3Card);
        ImageView seat3Card0 = (ImageView) findViewById(R.id.seat3Card0);
        ImageView seat3Card1 = (ImageView) findViewById(R.id.seat3Card1);
        TextView seat3NameTV = (TextView) findViewById(R.id.seat3NameTV);
        TextView seat3MoneyTV = (TextView) findViewById(R.id.seat3MoneyTV);
        ProgressBar seat3TimeBar = (ProgressBar) findViewById(R.id.seat3TimeBar);
        FrameLayout seat3BetFrame = (FrameLayout) findViewById(R.id.seat3BetFrame);
        ImageView seat3ChipsIV = (ImageView) findViewById(R.id.seat3ChipsIV);
        TextView seat3TotalBetTV = (TextView) findViewById(R.id.seat3TotalBetTV);
        TextView seat3LastUserActionTV = (TextView) findViewById(R.id.seat3LastUserActionTV);
        ImageView seat3DealerChip = (ImageView) findViewById(R.id.seat3DealerChip);
        seats.add(new Seat(seat3Frame, seat3Card, seat3Card0, seat3Card1, null, null, null, null, seat3NameTV, seat3MoneyTV, seat3TimeBar,
                seat3BetFrame, seat3ChipsIV, seat3TotalBetTV, null, seat3LastUserActionTV, null, seat3DealerChip, 120f, 200f));


        // Seat 4
        FrameLayout seat4Frame = (FrameLayout) findViewById(R.id.seat4Frame);
        CardView seat4Card = (CardView) findViewById(R.id.seat4Card);
        ImageView seat4Card0 = (ImageView) findViewById(R.id.seat4Card0);
        ImageView seat4Card1 = (ImageView) findViewById(R.id.seat4Card1);
        TextView seat4NameTV = (TextView) findViewById(R.id.seat4NameTV);
        TextView seat4MoneyTV = (TextView) findViewById(R.id.seat4MoneyTV);
        ProgressBar seat4TimeBar = (ProgressBar) findViewById(R.id.seat4TimeBar);
        FrameLayout seat4BetFrame = (FrameLayout) findViewById(R.id.seat4BetFrame);
        ImageView seat4ChipsIV = (ImageView) findViewById(R.id.seat4ChipsIV);
        TextView seat4TotalBetTV = (TextView) findViewById(R.id.seat4TotalBetTV);
        TextView seat4LastUserActionTV = (TextView) findViewById(R.id.seat4LastUserActionTV);
        ImageView seat4DealerChip = (ImageView) findViewById(R.id.seat4DealerChip);
        seats.add(new Seat(seat4Frame, seat4Card, seat4Card0, seat4Card1, null, null, null, null, seat4NameTV, seat4MoneyTV, seat4TimeBar,
                seat4BetFrame, seat4ChipsIV, seat4TotalBetTV, null, seat4LastUserActionTV, null, seat4DealerChip, -120f, 200f));

        // Seat 5
        FrameLayout seat5Frame = (FrameLayout) findViewById(R.id.seat5Frame);
        CardView seat5Card = (CardView) findViewById(R.id.seat5Card);
        ImageView seat5Card0 = (ImageView) findViewById(R.id.seat5Card0);
        ImageView seat5Card1 = (ImageView) findViewById(R.id.seat5Card1);
        TextView seat5NameTV = (TextView) findViewById(R.id.seat5NameTV);
        TextView seat5MoneyTV = (TextView) findViewById(R.id.seat5MoneyTV);
        ProgressBar seat5TimeBar = (ProgressBar) findViewById(R.id.seat5TimeBar);
        FrameLayout seat5BetFrame = (FrameLayout) findViewById(R.id.seat5BetFrame);
        ImageView seat5ChipsIV = (ImageView) findViewById(R.id.seat5ChipsIV);
        TextView seat5TotalBetTV = (TextView) findViewById(R.id.seat5TotalBetTV);
        TextView seat5LastUserActionTV = (TextView) findViewById(R.id.seat5LastUserActionTV);
        ImageView seat5DealerChip = (ImageView) findViewById(R.id.seat5DealerChip);
        seats.add(new Seat(seat5Frame, seat5Card, seat5Card0, seat5Card1, null, null, null, null, seat5NameTV, seat5MoneyTV, seat5TimeBar,
                seat5BetFrame, seat5ChipsIV, seat5TotalBetTV, null, seat5LastUserActionTV, null, seat5DealerChip, -240f, 110f));

        // Seat 6
        FrameLayout seat6Frame = (FrameLayout) findViewById(R.id.seat6Frame);
        CardView seat6Card = (CardView) findViewById(R.id.seat6Card);
        ImageView seat6Card0 = (ImageView) findViewById(R.id.seat6Card0);
        ImageView seat6Card1 = (ImageView) findViewById(R.id.seat6Card1);
        TextView seat6NameTV = (TextView) findViewById(R.id.seat6NameTV);
        TextView seat6MoneyTV = (TextView) findViewById(R.id.seat6MoneyTV);
        ProgressBar seat6TimeBar = (ProgressBar) findViewById(R.id.seat6TimeBar);
        FrameLayout seat6BetFrame = (FrameLayout) findViewById(R.id.seat6BetFrame);
        ImageView seat6ChipsIV = (ImageView) findViewById(R.id.seat6ChipsIV);
        TextView seat6TotalBetTV = (TextView) findViewById(R.id.seat6TotalBetTV);
        TextView seat6LastUserActionTV = (TextView) findViewById(R.id.seat6LastUserActionTV);
        ImageView seat6DealerChip = (ImageView) findViewById(R.id.seat6DealerChip);
        seats.add(new Seat(seat6Frame, seat6Card, seat6Card0, seat6Card1, null, null, null, null, seat6NameTV, seat6MoneyTV, seat6TimeBar,
                seat6BetFrame, seat6ChipsIV, seat6TotalBetTV, null, seat6LastUserActionTV, null, seat6DealerChip, -100f, -100f));

        // Get settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        MY_PLAYER_NICKNAME = sharedPreferences.getString(Constants.SP_PLAYER_NICKNAME, "Anon" + new Random().nextInt(1000) + 1);
        IS_DEVELOPMENT_SERVER = sharedPreferences.getBoolean(Constants.SP_DEVELOPMENT_SERVER, false);
        autoPlay = sharedPreferences.getBoolean(Constants.SP_HOLDEM_AUTO_PLAY, false);
        ImageView pokerTableIV = (ImageView) findViewById(R.id.pokerTableIV);
        if (sharedPreferences.getBoolean(Constants.SP_USE_PURPLE_HOLDEM_TABLE, false)) {
            pokerTableIV.setBackground(ContextCompat.getDrawable(this, R.drawable.poker_table_purple));
        }
        if (!sharedPreferences.getBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false)) {
            xpNeededForNextMedalCard.setVisibility(View.GONE);
        }


        // Init threads
        socketOpenCheckThread = new Thread(socketOpenCheckRunnable);
        holeCardsThread = new Thread(holeCardsRunnable);
        theFlopThread = new Thread(theFlopRunnable);
        theTurnThread = new Thread(theTurnRunnable);
        theRiverThread = new Thread(theRiverRunnable);

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_holdem);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_holdem);

        // Init exit button
        exitBtn.setOnClickListener(view -> endGameDialog());

        // Select room to play in btn
        selectRoomBtn.setOnClickListener(view -> {
            if (ROOM_ID == -1) {
                showRoomsDialog();
                webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "all");
            } else {
                reloadConnectionDialog();
            }
        });

        // Enter into spectating mode btn
        spectateBtn.setOnClickListener(view -> {
            if (ROOM_ID == -1) {
                showRoomsDialog();
                webSocketClient.getSpectateRooms(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
            } else {
                reloadConnectionDialog();
            }
        });

        // Init webSocket listener and get rooms
        initWebSocketClient(); // enable this back

        // Connection progress dialog
        progressDialog = new ProgressDialog(TexasHoldem.this);
        progressDialog.setMessage("Connecting to server...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    } // End of onCreate()


    private void initWebSocketClient() {
        if (webSocketClient != null) {
            webSocketClient.disconnect(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
            this.ROOM_ID = -1;
            this.CONNECTION_ID = -1;
        }
        if (IS_DEVELOPMENT_SERVER) {
            webSocketClient = new WebSocketClientHoldEm(this, Constants.DEVELOPMENT_SERVER);
        } else {
            webSocketClient = new WebSocketClientHoldEm(this, Constants.PRODUCTION_SERVER);
        }
    }


    // Starting point before new game starts
    private void roomParameters(final JSONObject jsonData) {
        Log.i(TAG, jsonData.toString());
        try {
            // Example: {"playerCount":3,"roomMinBet":10,"middleCards":["3♣","6♥","Q♥"],"playersData":[{"playerId":0,"playerName":"Bot704","playerMoney":9990,"isDealer":true},{"playerId":1,"playerName":"Bot112","playerMoney":9990,"isDealer":false},{"playerId":2,"playerName":"Bot513","playerMoney":9990,"isDealer":false}]}
            players.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < middleCardIV.size(); i++) {
                        middleCardIV.get(i).setBackground(null);
                    }
                }
            });
            final boolean gameStarted = jsonData.getBoolean("gameStarted");
            final int playerCount = jsonData.getInt("playerCount");
            final int roomMinBet = jsonData.getInt("roomMinBet");
            final ArrayList<Integer> playerIds = new ArrayList<>();
            final ArrayList<String> playerNames = new ArrayList<>();
            final ArrayList<Integer> playerMoney = new ArrayList<>();
            final ArrayList<Boolean> playerIsDealer = new ArrayList<>();
            // If there is middle cards then player is coming here middle of the game
            JSONArray middleCardsArray = jsonData.getJSONArray("middleCards");
            if (middleCardsArray.length() > 0) {
                for (int i = 0; i < middleCardsArray.length(); i++) {
                    middleCards[i] = new CardVisualOnline(middleCardsArray.getString(i));
                    final int finalI = (i + 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setMiddleCardDrawable(finalI);
                        }
                    });
                }
            }
            JSONArray playersArray = jsonData.getJSONArray("playersData");
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject playerObject = playersArray.getJSONObject(i);
                playerIds.add(playerObject.getInt("playerId"));
                playerNames.add(playerObject.getString("playerName"));
                playerMoney.add(playerObject.getInt("playerMoney"));
                playerIsDealer.add(playerObject.getBoolean("isDealer"));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setMinBet(roomMinBet);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playerSeats(playerCount, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void playerSeats(int playerCount, ArrayList<Integer> playerIds, ArrayList<String> playerNames, ArrayList<Integer> playerMoney, ArrayList<Boolean> playerIsDealer, Boolean gameStarted) {
        players.clear();
        switch (playerCount) {
            case 1:
                giveSeats(playerCount, new int[]{0}, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                break;
            case 2:
                giveSeats(playerCount, new int[]{0, 3}, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                break;
            case 3:
                giveSeats(playerCount, new int[]{0, 2, 3}, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                break;
            case 4:
                giveSeats(playerCount, new int[]{0, 2, 3, 5}, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                break;
            case 5:
                giveSeats(playerCount, new int[]{0, 1, 2, 3, 5}, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                break;
            case 6:
                giveSeats(playerCount, new int[]{0, 1, 2, 3, 4, 5}, playerIds, playerNames, playerMoney, playerIsDealer, gameStarted);
                break;
        }
    }

    private void giveSeats(int playerCount, int[] seatNum, ArrayList<Integer> playerIds, ArrayList<String> playerNames, ArrayList<Integer> playerMoney, ArrayList<Boolean> playerIsDealer, Boolean gameStarted) {
        initializeSeats();
        for (int i = 0; i < playerCount; i++) {
            players.add(
                    new Player(
                            this,
                            seats.get(seatNum[i]),
                            playerIds.get(i),
                            playerNames.get(i),
                            playerMoney.get(i),
                            playerIsDealer.get(i),
                            Player.TYPE_HOLDEM_ONLINE
                    )
            );
            seats.get(seatNum[i]).activateSeat(); // Sets visibility to visible
        }
        initializeViews(gameStarted);
    }

    private void initializeSeats() {
        for (int i = 0; i < seats.size(); i++) {
            seats.get(i).clearSeat();
        }
    }

    // Hides all cards
    private void initializeViews(Boolean gameStarted) {
        hideActionButtons(false);
        initOnClickListeners();
        setTotalPot(0);
        clearMiddleCardAnimation();
        for (int i = 0; i < players.size(); i++) {
            if (gameStarted) {
                setPlayerCardDrawable(i, 0);
                setPlayerCardDrawable(i, 1);
            } else {
                players.get(i).removeCardDrawablesHoldem();
            }
            players.get(i).setPlayerTimeBar(0);
            players.get(i).totalBet = 0;
            players.get(i).tempBet = 0;
            players.get(i).hideTotalBetFrame();
            players.get(i).isAllIn = false;
            players.get(i).handValue = 0;
            players.get(i).handValueText = "";
            players.get(i).hidePlayerLastActionTV();
            players.get(i).clearGlowAnimation();
            players.get(i).clearWinnerCardsBounceAnimation();
        }
        if (sharedPreferences.getBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false)) { // To load next medal xp again
            webSocketClient.getLoggedInUserStatistics(CONNECTION_ID, SOCKET_KEY);
        }
    }


    // ---------------------------------------------------------------------------------------------


    private void holeCards(final JSONObject jsonData) {
        // Example: {"players":[{"playerId":0,"cards":["7♥","5♦"]}]}
        Log.i(TAG, jsonData.toString());
        try {
            JSONArray playersArray = jsonData.getJSONArray("players");
            for (int p = 0; p < playersArray.length(); p++) {
                JSONObject playerObject = playersArray.getJSONObject(p);
                final int playerId = playerObject.getInt("playerId");
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).playerId == playerId) {
                        JSONArray cardsArray = playerObject.getJSONArray("cards");
                        if (cardsArray.length() == 2) {
                            players.get(p).cardsOnline[0] = new CardVisualOnline(cardsArray.getString(0));
                            players.get(p).cardsOnline[1] = new CardVisualOnline(cardsArray.getString(1));
                        }
                    }
                }

            }
            runOnUiThread(new Runnable() { // Run after player cards are set
                @Override
                public void run() {
                    // Card images, sounds and animations
                    if (holeCardsThread.isAlive()) {
                        holeCardsThread = null;
                    }
                    holeCardsThread = new Thread(holeCardsRunnable);
                    threadList.add(holeCardsThread);
                    if (!holeCardsThread.isAlive()) {
                        holeCardsThread.start();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Runnable holeCardsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    final int sleepDuringCardPlace = 200; // ms
                    for (int r = 0; r < 2; r++) { // Card
                        for (int c = 0; c < players.size(); c++) { // Player
                            if (!players.get(c).isFold()) {
                                audioPlayer.playCardSlideSix(TexasHoldem.this);
                                final int finalR = r;
                                final int finalC = c;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setPlayerCardDrawable(finalC, finalR);
                                    }
                                });
                                Thread.sleep(sleepDuringCardPlace);
                            }
                        }
                    }
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    private void theFlop(final JSONObject jsonData) {
        // Example: {"middleCards":["4♣","3♠","3♥"]}
        Log.i(TAG, jsonData.toString());
        try {
            JSONArray cardsArray = jsonData.getJSONArray("middleCards");
            middleCards[0] = new CardVisualOnline(cardsArray.getString(0));
            middleCards[1] = new CardVisualOnline(cardsArray.getString(1));
            middleCards[2] = new CardVisualOnline(cardsArray.getString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Card images, sounds and animations
        if (theFlopThread.isAlive()) {
            theFlopThread = null;
        }
        theFlopThread = new Thread(theFlopRunnable);
        threadList.add(theFlopThread);
        if (!theFlopThread.isAlive()) {
            theFlopThread.start();
        }
    }

    Runnable theFlopRunnable = new Runnable() {
        final int sleepDuringCardPlace = 200; // ms

        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(500);
                    for (int f = 0; f < 3; f++) {
                        audioPlayer.playCardSlideSix(TexasHoldem.this);
                        final int finalF = f;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setMiddleCardDrawable(finalF + 1);
                            }
                        });
                        Thread.sleep(sleepDuringCardPlace);
                    }
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    private void theTurn(final JSONObject jsonData) {
        // Example: {"middleCards":["8♠","J♥","10♣","6♠"]}
        Log.i(TAG, jsonData.toString());
        try {
            JSONArray cardsArray = jsonData.getJSONArray("middleCards");
            middleCards[3] = new CardVisualOnline(cardsArray.getString(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Card images, sounds and animations
        if (theTurnThread.isAlive()) {
            theTurnThread = null;
        }
        theTurnThread = new Thread(theTurnRunnable);
        threadList.add(theTurnThread);
        if (!theTurnThread.isAlive()) {
            theTurnThread.start();
        }
    }

    Runnable theTurnRunnable = new Runnable() {
        final int sleepDuringCardPlace = 200; // ms

        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(500);
                    audioPlayer.playCardSlideSix(TexasHoldem.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setMiddleCardDrawable(4);
                        }
                    });
                    Thread.sleep(sleepDuringCardPlace);
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    private void theRiver(final JSONObject jsonData) {
        // Example: {"middleCards":["8♠","J♥","10♣","6♠","3♠"]}
        Log.i(TAG, jsonData.toString());
        try {
            JSONArray cardsArray = jsonData.getJSONArray("middleCards");
            middleCards[4] = new CardVisualOnline(cardsArray.getString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Card images, sounds and animations
        if (theRiverThread.isAlive()) {
            theRiverThread = null;
        }
        theRiverThread = new Thread(theRiverRunnable);
        threadList.add(theRiverThread);
        if (!theRiverThread.isAlive()) {
            theRiverThread.start();
        }
    }

    Runnable theRiverRunnable = new Runnable() {
        final int sleepDuringCardPlace = 200; // ms

        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(500);
                    audioPlayer.playCardSlideSix(TexasHoldem.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setMiddleCardDrawable(5);
                        }
                    });
                    Thread.sleep(sleepDuringCardPlace);
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    // ---------------------------------------------------------------------------------------------


    // Receive all players cards before results for showing them
    private void allPlayersCards(final JSONObject jsonData) {
        // Example: {"players":[{"playerId":0,"cards":["6♦","A♦"]},{"playerId":1,"cards":["7♣","7♠"]}]}
        Log.i(TAG, jsonData.toString());
        try {
            JSONArray playersArray = jsonData.getJSONArray("players");
            for (int p = 0; p < playersArray.length(); p++) {
                JSONObject playerObject = playersArray.getJSONObject(p);
                final int playerId = playerObject.getInt("playerId");
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).playerId == playerId) {
                        JSONArray cardsArray = playerObject.getJSONArray("cards");
                        players.get(i).cardsOnline[0] = new CardVisualOnline(cardsArray.getString(0));
                        players.get(i).cardsOnline[1] = new CardVisualOnline(cardsArray.getString(1));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------

    private void statusUpdate(JSONObject jsonData) {
        // Example: {"totalPot":0,"middleCards":[],"playersData":[{"playerMoney":10000,"totalBet":0,"isPlayerTurn":false,"isFold":false}]}
        try {
            final int totalPot = jsonData.getInt("totalPot");
            final String currentStatus = jsonData.getString("currentStatus");
            final boolean isCallSituation = jsonData.getBoolean("isCallSituation");

            final String roomName = "♦ " + jsonData.getString("roomName");
            final String spectatorsCount = "♦ Spectating: " + jsonData.getString("spectatorsCount");
            final String appendPlayersCount = "♦ Waiting: " + jsonData.getString("appendPlayersCount");
            final String deckStatus = "♦ Deck: " + jsonData.getString("deckStatus");
            final String deckCardsBurned = "♦ Burned: " + jsonData.getString("deckCardsBurned");

            final Boolean collectingPot = jsonData.getBoolean("collectingPot");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTotalPot(totalPot);
                    currentStatusTV.setText(currentStatus);
                    roomNameTV.setText(roomName);
                    spectatorsCountTV.setText(spectatorsCount);
                    appendPlayersCountTV.setText(appendPlayersCount);
                    deckStatusTV.setText(deckStatus);
                    deckCardsBurnedTV.setText(deckCardsBurned);
                    if (isCallSituation) {
                        checkBtn.setText("CALL");
                    } else {
                        checkBtn.setText("CHECK");
                    }
                }
            });


            final JSONArray winnerPlayerIds = jsonData.getJSONArray("roundWinnerPlayerIds");
            final JSONArray roundWinnerPlayerCards = jsonData.getJSONArray("roundWinnerPlayerCards");

            final boolean isResultsCall = jsonData.getBoolean("isResultsCall");
            if (isResultsCall) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startMiddleCardAnimation(roundWinnerPlayerCards); // Middle card animations
                        try {
                            for (int i = 0; i < players.size(); i++) {
                                players.get(i).transferMoneyAnimation(TexasHoldem.this);
                                players.get(i).totalBet = 0;
                                players.get(i).tempBet = 0;
                                players.get(i).setMoneyView();
                                showAllPlayersCards();
                                for (int w = 0; w < winnerPlayerIds.length(); w++) {
                                    if (winnerPlayerIds.getInt(w) == players.get(i).playerId) {
                                        players.get(i).startGlowAnimation();
                                        for (int c = 0; c < roundWinnerPlayerCards.length(); c++) {
                                            players.get(i).startWinnerCardsBounceAnimation(roundWinnerPlayerCards.getJSONArray(c));
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            JSONArray playersArray = jsonData.getJSONArray("playersData");
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject playerObject = playersArray.getJSONObject(i);
                final int _i = i;
                final int pMoney = playerObject.getInt("playerMoney");
                final int pTotalBet = playerObject.getInt("totalBet");
                final boolean pTurn = playerObject.getBoolean("isPlayerTurn");
                final boolean pIsFold = playerObject.getBoolean("isFold");
                final int pTimeBar = playerObject.getInt("timeBar");
                final int pId = playerObject.getInt("playerId");
                try {
                    if (pId == CONNECTION_ID && players.get(i).tempBet > 0) {
                        // Do nothing
                    } else {
                        players.get(i).playerMoney = pMoney;
                        if (!collectingPot) {
                            players.get(i).totalBet = pTotalBet;
                        }
                    }
                    if (pTurn) {
                        players.get(i).setPlayerTurn(true);

                        /* Auto play action implementation */
                        if (autoPlay && !autoPlayCommandRequested) {
                            autoPlayCommandRequested = true;
                            webSocketClient.getAutoPlayAction(CONNECTION_ID, SOCKET_KEY);
                        }

                    } else {
                        players.get(i).setPlayerTurn(false);
                    }
                    if (pIsFold) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!players.get(_i).isFold()) {
                                    players.get(_i).setFold();
                                }
                            }
                        });
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            players.get(_i).setPlayerTimeBar(pTimeBar);
                            if (pId == CONNECTION_ID && players.get(_i).tempBet > 0) {
                                // Do nothing
                            } else {
                                players.get(_i).setMoneyView();
                                players.get(_i).setTotalBetView();
                            }
                            if (pId == CONNECTION_ID) {
                                if (players.get(_i).isPlayerTurn()) {
                                    showActionButtons();
                                    enableActionButtons();
                                } else {
                                    hideActionButtons(true);
                                }
                            }
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
    // WebSocket communications and methods

    @Override
    public void onConnectedEvent() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (webSocketClient.webSocketClient != null) {
            socketOpenCheckThread = new Thread(socketOpenCheckRunnable);
            threadList.add(socketOpenCheckThread);
            if (!socketOpenCheckThread.isAlive()) {
                socketOpenCheckThread.start();
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(TexasHoldem.this, "Error creating socket connection!", Toast.LENGTH_LONG).show();
                TexasHoldem.this.finish();
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
                    showRoomsDialog();
                    webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "all");
                    setLoggedInUserParams();
                    break;
                case "getRooms":
                    parseRooms(jsonObject.getJSONArray("data"), false);
                    break;
                case "getSpectateRooms":
                    parseRooms(jsonObject.getJSONArray("data"), true);
                    break;
                case "roomParams":
                    roomParameters(jsonObject.getJSONObject("data"));
                    break;
                case "holeCards":
                    holeCards(jsonObject.getJSONObject("data"));
                    break;
                case "statusUpdate":
                    statusUpdate(jsonObject.getJSONObject("data"));
                    break;
                case "theFlop":
                    theFlop(jsonObject.getJSONObject("data"));
                    break;
                case "theTurn":
                    theTurn(jsonObject.getJSONObject("data"));
                    break;
                case "theRiver":
                    theRiver(jsonObject.getJSONObject("data"));
                    break;
                case "allPlayersCards":
                    allPlayersCards(jsonObject.getJSONObject("data"));
                    break;
                case "audioCommand":
                    audioCommand(jsonObject.getJSONObject("data"));
                    break;
                case "lastUserAction":
                    playerLastActionHandler(jsonObject.getJSONObject("data"));
                    break;
                case "loggedInUserParamsResult":
                    loggedInUserParamsResult(jsonObject.getJSONObject("data"));
                    break;
                case "loggedInUserStatisticsResults":
                    loggedInUserStatisticsResults(jsonObject.getJSONObject("data"));
                    break;
                case "rewardingAdShownServerResult":
                    break;
                case "onXPGained":
                    onXPGained(jsonObject.getInt("code"), jsonObject.getJSONObject("data"));
                    break;
                case "clientMessage":
                    onClientMessage(jsonObject.getJSONObject("data"));
                    break;
                case "collectChipsToPot":
                    collectChipsToPotAction(jsonObject.getJSONObject("data"));
                    break;
                case "autoPlayActionResult":
                    autoPlayActionResult(jsonObject.getJSONObject("data"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------


    private void parseRooms(JSONArray roomData, final Boolean isSpectateMode) {
        final ArrayList<RoomItem> roomsList = new ArrayList<>();
        try {
            final JSONArray roomsArray = roomData;
            for (int i = 0; i < roomsArray.length(); i++) {
                JSONObject roomObject = roomsArray.getJSONObject(i);
                RoomItem roomItem = new RoomItem(
                        this,
                        isSpectateMode ? R.drawable.spectate_icon : R.drawable.room_icon,
                        roomObject.getInt("roomId"), roomObject.getString("roomName") + " ➟ (" + roomObject.getString("playerCount") + "/" + roomObject.getString("maxSeats") + ")", currencyFormat.format(roomObject.getInt("roomMinBet"))
                );
                roomsList.add(roomItem);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRoomsDialogData(roomsList, isSpectateMode);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Initialize rooms dialog listeners
    private void initRoomsDialogListeners() {
        roomsAllRB.setChecked(allRBChecked);
        roomsLowRB.setChecked(lowRBChecked);
        roomsMediumRB.setChecked(mediumRBChecked);
        roomsHighRB.setChecked(highRBChecked);
        roomsPlayingRoomsBtn.setOnClickListener(view -> webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "all"));
        roomsSpectateRoomsBtn.setOnClickListener(view -> webSocketClient.getSpectateRooms(CONNECTION_ID, SOCKET_KEY, ROOM_ID));
        moreFundsBtn.setOnClickListener(view -> {
            webSocketClient.rewardingAdShown(CONNECTION_ID, SOCKET_KEY);
            Toast.makeText(TexasHoldem.this, "More funds transferred successfully", Toast.LENGTH_SHORT).show();
        });
        roomsAllRB.setOnCheckedChangeListener((compoundButton, b) -> {
            allRBChecked = b;
            if (b) {
                webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "all");
            }
        });
        roomsLowRB.setOnCheckedChangeListener((compoundButton, b) -> {
            lowRBChecked = b;
            if (b) {
                webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "lowBets");
            }
        });
        roomsMediumRB.setOnCheckedChangeListener((compoundButton, b) -> {
            mediumRBChecked = b;
            if (b) {
                webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "mediumBets");
            }
        });
        roomsHighRB.setOnCheckedChangeListener((compoundButton, b) -> {
            highRBChecked = b;
            if (b) {
                webSocketClient.getRooms(CONNECTION_ID, SOCKET_KEY, MY_PLAYER_NICKNAME, ROOM_ID, "highBets");
            }
        });
    }


    // Set rooms dialog data
    private void setRoomsDialogData(final ArrayList<RoomItem> roomsList, final Boolean isSpectateMode) {
        if (recyclerTouchListener != null) {
            roomsRecyclerView.removeOnItemTouchListener(recyclerTouchListener);
            recyclerTouchListener = null;
        }
        if (isSpectateMode) {
            roomsListTitle.setText("Select room to spectate");
            roomsAllRB.setEnabled(false);
            roomsLowRB.setEnabled(false);
            roomsMediumRB.setEnabled(false);
            roomsHighRB.setEnabled(false);
        } else {
            roomsListTitle.setText("Select room");
            roomsAllRB.setEnabled(true);
            roomsLowRB.setEnabled(true);
            roomsMediumRB.setEnabled(true);
            roomsHighRB.setEnabled(true);
        }
        RoomItemsAdapter roomItemsAdapter = new RoomItemsAdapter(roomsList);
        roomsRecyclerView.setAdapter(roomItemsAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        roomsRecyclerView.setLayoutManager(layoutManager);
        recyclerTouchListener = new RecyclerTouchListener(this, roomsRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                audioPlayer.playCardChipLayOne(TexasHoldem.this);
                ROOM_ID = roomsList.get(position).getRoomId();
                if (isSpectateMode) {
                    webSocketClient.selectSpectateRoom(CONNECTION_ID, SOCKET_KEY, roomsList.get(position).getRoomId());
                    currentStatusTV.setText("Waiting for players...");
                } else {
                    webSocketClient.selectRoom(CONNECTION_ID, SOCKET_KEY, roomsList.get(position).getRoomId());
                    currentStatusTV.setText("Waiting for players...");
                }
                roomsDialog.dismiss();
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        });
        roomsRecyclerView.addOnItemTouchListener(recyclerTouchListener);
    }


    // Show rooms dialog
    private void showRoomsDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomsDialog.show();
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    /* Helpers */


    // On xp gained message handler
    private void onXPGained(final Integer responseCode, final JSONObject xpData) {
        if (responseCode == 200) {
            try {
                final String xpGainedAmount = xpData.getString("xpGainedAmount");
                final String xpGainedMessage = xpData.getString("xpMessage");
                Log.i(TAG, xpGainedAmount);
                Log.i(TAG, xpGainedMessage);
                runOnUiThread(() -> {
                    final Snackbar snackBar = Snackbar.make(
                            findViewById(android.R.id.content),
                            "+" + xpGainedAmount + "XP gained due " + xpGainedMessage,
                            Snackbar.LENGTH_LONG);
                    View view = snackBar.getView();
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    view.setLayoutParams(params);
                    snackBar.setAction("Close", v -> snackBar.dismiss());
                    snackBar.show();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // Show incoming message straight on UI
    private void onClientMessage(final JSONObject cData) {
        try {
            final String message = cData.getString("message");
            runOnUiThread(() -> Toast.makeText(TexasHoldem.this, message, Toast.LENGTH_LONG).show());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Show incoming message straight on UI
    private void collectChipsToPotAction(final JSONObject cData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean playSound = false;
                        for (int i = 0; i < players.size(); i++) {
                            if (players.get(i).totalBet > 0) {
                                playSound = true;
                                startCollectAnimationForView(
                                        players.get(i).playerSeat.seatBetFrame,
                                        players.get(i).playerSeat.collectAnimToX,
                                        players.get(i).playerSeat.collectAnimToY
                                );
                            }
                        }
                        if (playSound) {
                            audioPlayer.playCollectChipsToPot(TexasHoldem.this);
                        }
                    }
                }, 100);
            }
        });
    }


    // Start collection animation for view
    private void startCollectAnimationForView(final FrameLayout frameLayout, final float toX, final float toY) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            frameLayout.setVisibility(View.VISIBLE); // Remove this
            frameLayout.bringToFront(); // Bring to top of view stack
            TranslateAnimation translateAnimation = new TranslateAnimation(0, toX, 0, toY);
            translateAnimation.setDuration(1000);
            translateAnimation.setFillAfter(false);
            translateAnimation.setAnimationListener(TexasHoldem.this);
            frameLayout.startAnimation(translateAnimation);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    frameLayout.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }


    // Audio commands to play other players action sounds
    private void audioCommand(final JSONObject jsonData) {
        boolean play = true;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId == CONNECTION_ID && players.get(i).isPlayerTurn()) {
                play = false;
            }
        }
        if (play) {
            try {
                final String audioCommand = jsonData.getString("command");
                switch (audioCommand) {
                    case "fold":
                        audioPlayer.playCardFoldOne(this);
                        break;
                    case "check":
                        audioPlayer.playCheckSound(this);
                        break;
                    case "call":
                        audioPlayer.playCardPlaceChipsOne(this);
                        break;
                    case "raise":
                        audioPlayer.playCardPlaceChipsOne(this);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // Set's logged in user parameters if user is logged in
    private void setLoggedInUserParams() {
        if (sharedPreferences.getBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false)) {
            webSocketClient.setLoggedInUserParams(CONNECTION_ID, SOCKET_KEY, sharedPreferences.getString(Constants.SP_ONLINE_HOLDEM_USERNAME, ""), sharedPreferences.getString(Constants.SP_ONLINE_HOLDEM_PASSWORD, ""));
        }
    }


    // Result from set logged in user parameters
    private void loggedInUserParamsResult(final JSONObject jsonObject) {
        try {
            if (!jsonObject.getBoolean("result")) { // Error case
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TexasHoldem.this, "You are logged in from another instance (Web or Android), which is forbidden! This may be error case when old instance is left open.", Toast.LENGTH_LONG).show();
                        TexasHoldem.this.finish();
                    }
                });
            } else { // Check player's money left
                final int moneyLeft = jsonObject.getInt("moneyLeft");
                if (moneyLeft <= 2000) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runningOutOfMoneyDialog(moneyLeft);
                        }
                    });
                }
                webSocketClient.getLoggedInUserStatistics(CONNECTION_ID, SOCKET_KEY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // User statistics, get needed amount of xp for next medal from here
    private void loggedInUserStatisticsResults(final JSONObject resultData) {
        try {
            final String xpNeededForNextMedal = "Next medal +" + resultData.getString("xpNeededForNextMedal") + "xp";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    xpNeededForNextMedalTV.setText(xpNeededForNextMedal);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Handles last player action animation
    private void playerLastActionHandler(final JSONObject jsonData) {
        try {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).playerId == jsonData.getInt("playerId")) {
                    final String actionText = jsonData.getString("actionText");
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            players.get(finalI).setPlayerLastActionText(actionText);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showActionButtons() {
        bettingEnabled = true;
        if (foldBtn.getVisibility() == View.INVISIBLE) {
            audioPlayer.playCardTakeOutFromPackageOne(this);
            foldBtn.startAnimation(animFadeIn);
            foldBtn.setVisibility(View.VISIBLE);
            checkBtn.startAnimation(animFadeIn);
            checkBtn.setVisibility(View.VISIBLE);
            raiseBtn.startAnimation(animFadeIn);
            raiseBtn.setVisibility(View.VISIBLE);
        }
    }

    private void hideActionButtons(boolean animate) {
        bettingEnabled = false;
        if (foldBtn.getVisibility() == View.VISIBLE) {
            if (animate) {
                foldBtn.startAnimation(animFadeOut);
                checkBtn.startAnimation(animFadeOut);
                raiseBtn.startAnimation(animFadeOut);
            }
            foldBtn.setVisibility(View.INVISIBLE);
            checkBtn.setVisibility(View.INVISIBLE);
            raiseBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void enableActionButtons() {
        foldBtn.setEnabled(true);
        checkBtn.setEnabled(true);
        raiseBtn.setEnabled(true);
    }

    private void disableActionButtons() {
        foldBtn.setEnabled(false);
        checkBtn.setEnabled(false);
        raiseBtn.setEnabled(false);
    }


    private void setTotalPot(final int amount) {
        totalPotTV.setText(getString(R.string.pot) + " " + currencyFormat.format(amount));
    }

    private void setMinBet(final int amount) {
        minBetTV.setText("MB " + currencyFormat.format(amount));
    }


    private void setPlayerCardDrawable(int p, int card) {
        if (players.get(p).playerId == CONNECTION_ID) {
            if (!players.get(p).isFold()) {
                if (players.get(p).cardsOnline != null) {
                    try {
                        switch (card) {
                            case 0:
                                players.get(p).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cardsOnline[card].toWords())));
                                players.get(p).playerCard0.startAnimation(animFadeIn);
                                break;
                            case 1:
                                players.get(p).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cardsOnline[card].toWords())));
                                players.get(p).playerCard1.startAnimation(animFadeIn);
                                break;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (!players.get(p).isFold()) {
                switch (card) {
                    case 0:
                        players.get(p).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        players.get(p).playerCard0.startAnimation(animFadeIn);
                        break;
                    case 1:
                        players.get(p).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                        players.get(p).playerCard1.startAnimation(animFadeIn);
                        break;
                }
            }
        }
    }


    // Shows all players cards
    private void showAllPlayersCards() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId != CONNECTION_ID) {
                if (!players.get(i).isFold()) {
                    try {
                        players.get(i).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cardsOnline[0].toWords())));
                        //players.get(i).playerCard0.startAnimation(animFadeIn); // Removed due winner bounce animation not working if this is playing same time
                        players.get(i).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cardsOnline[1].toWords())));
                        //players.get(i).playerCard1.startAnimation(animFadeIn); // Removed due winner bounce animation not working if this is playing same time
                    } catch (Exception ignored) {
                        // Just ignore exception
                    }
                }
            }
        }
    }


    // Sets card drawable for image view
    private void setMiddleCardDrawable(int card) {
        try {
            switch (card) {
                case 1:
                    middleCardIV.get(0).setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[0].toWords())));
                    middleCardIV.get(0).startAnimation(animFadeIn);
                    break;
                case 2:
                    middleCardIV.get(1).setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[1].toWords())));
                    middleCardIV.get(1).startAnimation(animFadeIn);
                    break;
                case 3:
                    middleCardIV.get(2).setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[2].toWords())));
                    middleCardIV.get(2).startAnimation(animFadeIn);
                    break;
                case 4:
                    middleCardIV.get(3).setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[3].toWords())));
                    middleCardIV.get(3).startAnimation(animFadeIn);
                    break;
                case 5:
                    middleCardIV.get(4).setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[4].toWords())));
                    middleCardIV.get(4).startAnimation(animFadeIn);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
    }


    // Start middle card animation for all round winner player related cards
    private void startMiddleCardAnimation(final JSONArray roundWinnerPlayerCards) {
        for (int c = 0; c < roundWinnerPlayerCards.length(); c++) {
            for (int i = 0; i < middleCards.length; i++) {
                try {
                    final JSONArray roundWinnerPlayerCardsInner = roundWinnerPlayerCards.getJSONArray(c);
                    for (int w = 0; w < roundWinnerPlayerCardsInner.length(); w++) {
                        if (middleCards[i].getCardStr().equals(roundWinnerPlayerCardsInner.getString(w))) {
                            TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, -20.0f);
                            animation.setDuration(1500);
                            animation.setFillAfter(false);
                            animation.setRepeatCount(20);
                            animation.setRepeatMode(Animation.REVERSE);
                            switch (i) {
                                case 0:
                                    card1.startAnimation(animation);
                                    break;
                                case 1:
                                    card2.startAnimation(animation);
                                    break;
                                case 2:
                                    card3.startAnimation(animation);
                                    break;
                                case 3:
                                    card4.startAnimation(animation);
                                    break;
                                case 4:
                                    card5.startAnimation(animation);
                                    break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void clearMiddleCardAnimation() {
        for (int i = 0; i < this.middleCardIV.size(); i++) {
            if (this.middleCardIV.get(i) != null) {
                this.middleCardIV.get(i).clearAnimation();
            }
        }
    }


    private void fold() {
        webSocketClient.setFold(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
        disableActionButtons();
    }


    private void check() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId == CONNECTION_ID) {
                if (players.get(i).tempBet > 0) {
                    Toast.makeText(TexasHoldem.this, "You have already thrown chips in... raising...", Toast.LENGTH_LONG).show();
                    webSocketClient.setRaise(CONNECTION_ID, SOCKET_KEY, ROOM_ID, myRaiseHelper());
                } else {
                    webSocketClient.setCheck(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
                }
                audioPlayer.playCardPlaceChipsOne(TexasHoldem.this); // Sound
                disableActionButtons();
            }
        }
    }

    private void raise() {
        final int raiseAmount = myRaiseHelper();
        if (raiseAmount > 0) {
            audioPlayer.playCardPlaceChipsOne(TexasHoldem.this); // Sound
            webSocketClient.setRaise(CONNECTION_ID, SOCKET_KEY, ROOM_ID, raiseAmount);
            disableActionButtons();
        } else {
            Toast.makeText(TexasHoldem.this, "Throw some chips in first...", Toast.LENGTH_SHORT).show();
        }
    }


    // Initialize onClick listener's
    private void initOnClickListeners() {
        // Normal buttons
        foldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fold();
            }
        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });
        raiseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raise();
            }
        });
        // Betting buttons
        tenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseHelper(10, false);
            }
        });
        twentyFiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseHelper(25, false);
            }
        });
        oneHundredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseHelper(100, false);
            }
        });
        fiveHundredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseHelper(500, false);
            }
        });
        allInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseHelper(0, true);
            }
        });
    }


    private void raiseHelper(int amount, boolean allIn) {
        if (bettingEnabled) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).playerId == CONNECTION_ID && players.get(i).playerMoney > 0) {
                    if (!allIn) {
                        if ((players.get(i).playerMoney + players.get(i).tempBet) > amount) {
                            players.get(i).totalBet = players.get(i).totalBet + amount;
                            players.get(i).playerMoney = players.get(i).playerMoney - amount;
                            players.get(i).tempBet = players.get(i).tempBet + amount;
                            players.get(i).setMoneyView();
                            players.get(i).setTotalBetView();
                            audioPlayer.playCardPlaceChipsOne(TexasHoldem.this); // Sound
                        } else {
                            Toast.makeText(this, "Not enough money...", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        audioPlayer.playCardPlaceChipsOne(TexasHoldem.this); // Sound
                        players.get(i).totalBet = players.get(i).playerMoney + players.get(i).tempBet;
                        players.get(i).tempBet = players.get(i).playerMoney + players.get(i).tempBet;
                        players.get(i).playerMoney = 0;
                        players.get(i).setMoneyView();
                        players.get(i).setTotalBetView();
                    }
                }
            }
        }
    }


    private int myRaiseHelper() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId == CONNECTION_ID) {
                final int tempBet = players.get(i).tempBet;
                players.get(i).tempBet = 0;
                return tempBet;
            }
        }
        return 0;
    }


    private void killThreads(boolean finish) {
        killThreads = true;
        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).interrupt();
        }
        if (finish) {
            TexasHoldem.this.finish();
        }
    }


    Runnable socketOpenCheckRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    while (webSocketClient.isConnected()) {
                        Thread.sleep(2000);
                    }
                    if (!killThreads) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                socketClosedErrorDialog();
                            }
                        });
                    }
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // Handle auto play action result
    private void autoPlayActionResult(final JSONObject aData) {
        if (autoPlayCommandRequested) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String action = aData.optString("action");
                    Log.i(TAG, "AutoPlay action: " + action);
                    switch (action) {
                        case "bot_fold":
                            fold();
                            break;
                        case "bot_check":
                            check();
                            break;
                        case "bot_call":
                            check();
                            break;
                        case "bot_raise":
                            final int amount = aData.optInt("amount");
                            raiseHelper(amount, false);
                            raise();
                            break;
                        case "remove_bot":
                            TexasHoldem.this.finish();
                            break;
                        default:
                            check();
                            break;
                    }
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        autoPlayCommandRequested = false;
                    }
                }, 2000);
            }
        });
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
        endGameDialog();
    }


    private void runningOutOfMoneyDialog(final int currentMoney) {
        new AlertDialog.Builder(TexasHoldem.this)
                .setTitle("Money")
                .setMessage("Looks like you are running out of money. You have " + String.valueOf(currentMoney) + "$ left. Click below to get additional 2000.00$ of funds?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    webSocketClient.rewardingAdShown(CONNECTION_ID, SOCKET_KEY);
                    Toast.makeText(TexasHoldem.this, "More funds transferred successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // Return
                })
                .setIcon(R.mipmap.logo)
                .show();
    }


    private void reloadConnectionDialog() {
        new AlertDialog.Builder(TexasHoldem.this)
                .setTitle("Reload")
                .setMessage("This action closes current connection and does reload. After reload you are able to chose room or spectate. Do you want to continue?")
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


    private void endGameDialog() {
        new AlertDialog.Builder(TexasHoldem.this)
                .setTitle("Quit")
                .setMessage("Are you sure that you want to leave this online game?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        webSocketClient.disconnect(CONNECTION_ID, SOCKET_KEY, ROOM_ID);
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
        try {
            new AlertDialog.Builder(TexasHoldem.this)
                    .setTitle("Socket error")
                    .setMessage("Socket closed unexpectedly, do you want to stay looking around or quit?")
                    .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            TexasHoldem.this.finish();
                        }
                    })
                    .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Return
                        }
                    })
                    .setIcon(R.mipmap.logo)
                    .show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        FrameLayout frameLayout = findViewById(R.id.seat1BetFrame);
        frameLayout.clearAnimation();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


}