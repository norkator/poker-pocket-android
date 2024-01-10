package com.nitramite.pokerpocket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.CardDeckGenerator;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.cardlogic.Card;
import com.nitramite.cardlogic.holdem.HandEvaluatorSeven;
import com.nitramite.dynamic.Player;
import com.nitramite.dynamic.Seat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings({"FieldCanBeLocal", "RedundantCast"})
public class TexasHoldemOffline extends Activity {

    // Logging
    private final static String TAG = TexasHoldemOffline.class.getSimpleName();

    // Player names
    private String MY_PLAYER_NICKNAME = "";
    private String[] dummyNames = {
            "", "Bot 1", "Bot 2", "Bot 3", "Bot 4", "Bot 5"
    };

    // Betting limits
    private static final int MIN_BET = 10;

    // Game staging
    private static final int GAME_STAGE_ONE_HOLE_CARDS = 0;
    private static final int GAME_STAGE_TWO_PRE_FLOP = 1; // Betting round
    private static final int GAME_STAGE_THREE_THE_FLOP = 2;
    private static final int GAME_STAGE_FOUR_POST_FLOP = 3; // Betting round
    private static final int GAME_STAGE_FIVE_THE_TURN = 4;
    private static final int GAME_STAGE_SIX_THE_POST_TURN = 5; // Betting round
    private static final int GAME_STAGE_SEVEN_THE_RIVER = 6;
    private static final int GAME_STAGE_EIGHT_THE_SHOW_DOWN = 7; // final betting round
    private static final int GAME_STAGE_NINE_RESULTS = 8;

    // Activity features
    private Boolean activityRunning = true;
    private SharedPreferences sharedPreferences;
    private GameCardResource gameCardResource;
    private boolean HOLDEM_SAVE_PROGRESS = false;
    private boolean AUTO_START_NEW_ROUND = true;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private AudioPlayer audioPlayer = new AudioPlayer();
    private int GAME_STAGE = GAME_STAGE_ONE_HOLE_CARDS;
    private int CURRENT_PLAYER_TURN = 0;
    private int TURN_TIME = 0;
    private int TOTAL_POT = 0;
    private boolean IS_CALL_SITUATION = false;
    private int CURRENT_HIGHEST_BET = 0; // Changing variables
    private int dealerPlayerArrayIndex = -1;
    private int smallBlindPlayerArrayIndex = -1;
    private boolean SMALL_BLIND_GIVEN = false;
    private boolean BIG_BLIND_GIVEN = false;

    // Threads
    private boolean KILL_THREADS = false;
    private ArrayList<Thread> threadList = new ArrayList<>();
    private Thread playerTurnThread;
    private Thread holeCardsThread;
    private Thread theFlopThread;
    private Thread theTurnThread;
    private Thread theRiverThread;
    private Thread resultsThread;
    private Thread moneyTransferThread;
    private Thread botThread;

    // Animation declarations
    private Animation animFadeIn, animFadeOut;

    // Shuffled card deck
    private Card[] cardDeck;

    // Windows
    private CardView debugWindow, tutorialWindow;
    private float dX, dY, tdX, tdY;

    // Buttons etc
    private CardView exitBtn, continueBtn;
    private Button foldBtn, checkBtn, raiseBtn;
    private Button tenBtn, twentyFiveBtn, oneHundredBtn, fiveHundredBtn, allInBtn;

    // Indicator views and variables
    private TextView tutorialTV;
    private TextView gameTotalMoneyTV;
    private TextView currentStageTV;
    private TextView totalPotTV;
    private TextView playersCountTV;

    // Center cards
    private ImageView card1, card2, card3, card4, card5;
    private CardVisual middleCards[] = new CardVisual[5];

    // All available seats are in this array
    private ArrayList<Seat> seats = new ArrayList<>();

    // All players are in this array
    private ArrayList<Player> players = new ArrayList<>();


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityRunning = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem_offline);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always

        // Cards
        gameCardResource = new GameCardResource(this);

        // Find button components
        tutorialWindow = (CardView) findViewById(R.id.tutorialWindow);
        debugWindow = (CardView) findViewById(R.id.debugWindow);
        exitBtn = (CardView) findViewById(R.id.exitBtn);
        continueBtn = (CardView) findViewById(R.id.continueBtn);
        foldBtn = (Button) findViewById(R.id.foldBtn);
        checkBtn = (Button) findViewById(R.id.checkBtn);
        raiseBtn = (Button) findViewById(R.id.raiseBtn);
        tenBtn = (Button) findViewById(R.id.tenBtn);
        twentyFiveBtn = (Button) findViewById(R.id.twentyFiveBtn);
        oneHundredBtn = (Button) findViewById(R.id.oneHundredBtn);
        fiveHundredBtn = (Button) findViewById(R.id.fiveHundredBtn);
        allInBtn = (Button) findViewById(R.id.allInBtn);

        // Find text views
        gameTotalMoneyTV = (TextView) findViewById(R.id.gameTotalMoneyTV);
        currentStageTV = (TextView) findViewById(R.id.currentStageTV);
        totalPotTV = (TextView) findViewById(R.id.totalPotTV);
        tutorialTV = (TextView) findViewById(R.id.tutorialTV);
        playersCountTV = (TextView) findViewById(R.id.playersCountTV);

        // Find card components
        card1 = (ImageView) findViewById(R.id.card1);
        card2 = (ImageView) findViewById(R.id.card2);
        card3 = (ImageView) findViewById(R.id.card3);
        card4 = (ImageView) findViewById(R.id.card4);
        card5 = (ImageView) findViewById(R.id.card5);

        // Seat 1
        FrameLayout seat1Frame = (FrameLayout) findViewById(R.id.seat1Frame);
        CardView seat1Card = (CardView) findViewById(R.id.seat1Card);
        ImageView seat1Card0 = (ImageView) findViewById(R.id.seat1Card0);
        ImageView seat1Card1 = (ImageView) findViewById(R.id.seat1Card1);
        TextView seat1NameTV = (TextView) findViewById(R.id.seat1NameTV);
        TextView seat1MoneyTV = (TextView) findViewById(R.id.seat1MoneyTV);
        ProgressBar seat1TimeBar = (ProgressBar) findViewById(R.id.seat1TimeBar);
        FrameLayout seat1BetFrame = (FrameLayout) findViewById(R.id.seat1BetFrame);
        TextView seat1TotalBetTV = (TextView) findViewById(R.id.seat1TotalBetTV);
        TextView seat1LastUserActionTV = (TextView) findViewById(R.id.seat1LastUserActionTV);
        ImageView seat1DealerChip = (ImageView) findViewById(R.id.seat1DealerChip);
        seats.add(new Seat(seat1Frame, seat1Card, seat1Card0, seat1Card1, null, null, null, null, seat1NameTV, seat1MoneyTV, seat1TimeBar,
                seat1BetFrame, null, seat1TotalBetTV, null, seat1LastUserActionTV, null, seat1DealerChip, null, null));

        // Seat 2
        FrameLayout seat2Frame = (FrameLayout) findViewById(R.id.seat2Frame);
        CardView seat2Card = (CardView) findViewById(R.id.seat2Card);
        ImageView seat2Card0 = (ImageView) findViewById(R.id.seat2Card0);
        ImageView seat2Card1 = (ImageView) findViewById(R.id.seat2Card1);
        TextView seat2NameTV = (TextView) findViewById(R.id.seat2NameTV);
        TextView seat2MoneyTV = (TextView) findViewById(R.id.seat2MoneyTV);
        ProgressBar seat2TimeBar = (ProgressBar) findViewById(R.id.seat2TimeBar);
        FrameLayout seat2BetFrame = (FrameLayout) findViewById(R.id.seat2BetFrame);
        TextView seat2TotalBetTV = (TextView) findViewById(R.id.seat2TotalBetTV);
        TextView seat2LastUserActionTV = (TextView) findViewById(R.id.seat2LastUserActionTV);
        ImageView seat2DealerChip = (ImageView) findViewById(R.id.seat2DealerChip);
        seats.add(new Seat(seat2Frame, seat2Card, seat2Card0, seat2Card1, null, null, null, null, seat2NameTV, seat2MoneyTV, seat2TimeBar,
                seat2BetFrame, null, seat2TotalBetTV, null, seat2LastUserActionTV, null, seat2DealerChip, null, null));

        // Seat 3
        FrameLayout seat3Frame = (FrameLayout) findViewById(R.id.seat3Frame);
        CardView seat3Card = (CardView) findViewById(R.id.seat3Card);
        ImageView seat3Card0 = (ImageView) findViewById(R.id.seat3Card0);
        ImageView seat3Card1 = (ImageView) findViewById(R.id.seat3Card1);
        TextView seat3NameTV = (TextView) findViewById(R.id.seat3NameTV);
        TextView seat3MoneyTV = (TextView) findViewById(R.id.seat3MoneyTV);
        ProgressBar seat3TimeBar = (ProgressBar) findViewById(R.id.seat3TimeBar);
        FrameLayout seat3BetFrame = (FrameLayout) findViewById(R.id.seat3BetFrame);
        TextView seat3TotalBetTV = (TextView) findViewById(R.id.seat3TotalBetTV);
        TextView seat3LastUserActionTV = (TextView) findViewById(R.id.seat3LastUserActionTV);
        ImageView seat3DealerChip = (ImageView) findViewById(R.id.seat3DealerChip);
        seats.add(new Seat(seat3Frame, seat3Card, seat3Card0, seat3Card1, null, null, null, null, seat3NameTV, seat3MoneyTV, seat3TimeBar,
                seat3BetFrame, null, seat3TotalBetTV, null, seat3LastUserActionTV, null, seat3DealerChip, null, null));

        // Seat 4
        FrameLayout seat4Frame = (FrameLayout) findViewById(R.id.seat4Frame);
        CardView seat4Card = (CardView) findViewById(R.id.seat4Card);
        ImageView seat4Card0 = (ImageView) findViewById(R.id.seat4Card0);
        ImageView seat4Card1 = (ImageView) findViewById(R.id.seat4Card1);
        TextView seat4NameTV = (TextView) findViewById(R.id.seat4NameTV);
        TextView seat4MoneyTV = (TextView) findViewById(R.id.seat4MoneyTV);
        ProgressBar seat4TimeBar = (ProgressBar) findViewById(R.id.seat4TimeBar);
        FrameLayout seat4BetFrame = (FrameLayout) findViewById(R.id.seat4BetFrame);
        TextView seat4TotalBetTV = (TextView) findViewById(R.id.seat4TotalBetTV);
        TextView seat4LastUserActionTV = (TextView) findViewById(R.id.seat4LastUserActionTV);
        ImageView seat4DealerChip = (ImageView) findViewById(R.id.seat4DealerChip);
        seats.add(new Seat(seat4Frame, seat4Card, seat4Card0, seat4Card1, null, null, null, null, seat4NameTV, seat4MoneyTV, seat4TimeBar,
                seat4BetFrame, null, seat4TotalBetTV, null, seat4LastUserActionTV, null, seat4DealerChip, null, null));

        // Seat 5
        FrameLayout seat5Frame = (FrameLayout) findViewById(R.id.seat5Frame);
        CardView seat5Card = (CardView) findViewById(R.id.seat5Card);
        ImageView seat5Card0 = (ImageView) findViewById(R.id.seat5Card0);
        ImageView seat5Card1 = (ImageView) findViewById(R.id.seat5Card1);
        TextView seat5NameTV = (TextView) findViewById(R.id.seat5NameTV);
        TextView seat5MoneyTV = (TextView) findViewById(R.id.seat5MoneyTV);
        ProgressBar seat5TimeBar = (ProgressBar) findViewById(R.id.seat5TimeBar);
        FrameLayout seat5BetFrame = (FrameLayout) findViewById(R.id.seat5BetFrame);
        TextView seat5TotalBetTV = (TextView) findViewById(R.id.seat5TotalBetTV);
        TextView seat5LastUserActionTV = (TextView) findViewById(R.id.seat5LastUserActionTV);
        ImageView seat5DealerChip = (ImageView) findViewById(R.id.seat5DealerChip);
        seats.add(new Seat(seat5Frame, seat5Card, seat5Card0, seat5Card1, null, null, null, null, seat5NameTV, seat5MoneyTV, seat5TimeBar,
                seat5BetFrame, null, seat5TotalBetTV, null, seat5LastUserActionTV, null, seat5DealerChip, null, null));

        // Seat 6
        FrameLayout seat6Frame = (FrameLayout) findViewById(R.id.seat6Frame);
        CardView seat6Card = (CardView) findViewById(R.id.seat6Card);
        ImageView seat6Card0 = (ImageView) findViewById(R.id.seat6Card0);
        ImageView seat6Card1 = (ImageView) findViewById(R.id.seat6Card1);
        TextView seat6NameTV = (TextView) findViewById(R.id.seat6NameTV);
        TextView seat6MoneyTV = (TextView) findViewById(R.id.seat6MoneyTV);
        ProgressBar seat6TimeBar = (ProgressBar) findViewById(R.id.seat6TimeBar);
        FrameLayout seat6BetFrame = (FrameLayout) findViewById(R.id.seat6BetFrame);
        TextView seat6TotalBetTV = (TextView) findViewById(R.id.seat6TotalBetTV);
        TextView seat6LastUserActionTV = (TextView) findViewById(R.id.seat6LastUserActionTV);
        ImageView seat6DealerChip = (ImageView) findViewById(R.id.seat6DealerChip);
        seats.add(new Seat(seat6Frame, seat6Card, seat6Card0, seat6Card1, null, null, null, null, seat6NameTV, seat6MoneyTV, seat6TimeBar,
                seat6BetFrame, null, seat6TotalBetTV, null, seat6LastUserActionTV, null, seat6DealerChip, null, null));

        // Get settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final int playerCount = sharedPreferences.getInt(Constants.SP_OFFLINE_HOLDEM_BOT_COUNT, 3);
        AUTO_START_NEW_ROUND = sharedPreferences.getBoolean(Constants.SP_OFFLINE_HOLDEM_AUTO_NEXT_ROUND, true);
        HOLDEM_SAVE_PROGRESS = sharedPreferences.getBoolean(Constants.SP_OFFLINE_HOLDEM_SAVE_PROGRESS, false);
        if (sharedPreferences.getBoolean(Constants.SP_OFFLINE_HOLDEM_SHOW_DEBUG_WINDOW, false)) {
            debugWindow.setVisibility(View.VISIBLE);
        } else {
            debugWindow.setVisibility(View.INVISIBLE);
        }
        if (sharedPreferences.getBoolean(Constants.SP_OFFLINE_HOLDEM_SHOW_TUTORIAL_WINDOW, false)) {
            tutorialWindow.setVisibility(View.VISIBLE);
        } else {
            tutorialWindow.setVisibility(View.INVISIBLE);
        }
        MY_PLAYER_NICKNAME = sharedPreferences.getString(Constants.SP_PLAYER_NICKNAME, "Anon" + new Random().nextInt(1000) + 1);
        dummyNames[0] = MY_PLAYER_NICKNAME;
        ImageView pokerTableIV = (ImageView) findViewById(R.id.pokerTableIV);
        if (sharedPreferences.getBoolean(Constants.SP_USE_PURPLE_HOLDEM_TABLE, false)) {
            pokerTableIV.setBackground(ContextCompat.getDrawable(this, R.drawable.poker_table_purple));
        }

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_holdem);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_holdem);

        // Init threads
        playerTurnThread = new Thread(playerTurnRunnable);
        holeCardsThread = new Thread(holeCardsRunnable);
        theFlopThread = new Thread(theFlopRunnable);
        theTurnThread = new Thread(theTurnRunnable);
        theRiverThread = new Thread(theRiverRunnable);
        resultsThread = new Thread(resultsRunnable);
        moneyTransferThread = new Thread(moneyTransferRunnable);
        botThread = new Thread(botRunnable);

        playerSeats(playerCount);
    } // End of onCreate();


    // Gives players seat according to player count (makes view look better)
    private void playerSeats(int playerCount) {
        playersCountTV.setText("♦ Players: " + String.valueOf(playerCount));
        players.clear();
        switch (playerCount) {
            case 2:
                giveSeats(playerCount, new int[]{0, 3});
                break;
            case 3:
                giveSeats(playerCount, new int[]{0, 2, 3});
                break;
            case 4:
                giveSeats(playerCount, new int[]{0, 2, 3, 5});
                break;
            case 5:
                giveSeats(playerCount, new int[]{0, 1, 2, 3, 5});
                break;
            case 6:
                giveSeats(playerCount, new int[]{0, 1, 2, 3, 4, 5});
                break;
        }
    }

    private void giveSeats(int playerCount, int[] seatNum) {
        for (int i = 0; i < playerCount; i++) {
            players.add(
                    new Player(
                            this,
                            seats.get(seatNum[i]),
                            0,
                            dummyNames[i],
                            10000,
                            false,
                            Player.TYPE_HOLDEM
                    )
            );
            seats.get(seatNum[i]).activateSeat(); // Sets visibility to visible
        }
        // Play starting sound
        audioPlayer.playCardOpenPackage(this);
        initializeViews();
    }


    // Hides all cards
    private void initializeViews() {
        continueBtn.setVisibility(View.GONE);
        killThreads(false);
        disableActionButtons(false);
        calculateTotalPot();
        card1.setBackground(null);
        card2.setBackground(null);
        card3.setBackground(null);
        card4.setBackground(null);
        card5.setBackground(null);
        for (int i = 0; i < players.size(); i++) {
            if (i == 0) {
                this.players.get(i).isBot = false;
            } else {
                this.players.get(i).isBot = true;
            }
            ;
            players.get(i).removeCardDrawablesHoldem();
            players.get(i).resetFold();
            if (players.get(i).playerMoney <= 100 || players.get(i).isRemoved) {
                players.get(i).setFold();
            }
            players.get(i).totalBet = 0;
            players.get(i).tempBet = 0;
            players.get(i).hideTotalBetFrame();
            players.get(i).isAllIn = false;
            players.get(i).handValue = 0;
            players.get(i).handValueText = "";
            players.get(i).clearGlowAnimation();
            players.get(i).playerState = Player.PLAYER_STATE_NONE;
            players.get(i).setPlayerTimeBar(0);
            players.get(i).isDealer = false;
            players.get(i).unsetPlayerAsDealer(); // Sets invisible if dealer set false
        }
        CURRENT_HIGHEST_BET = 0;
        initOnClickListeners();
        CURRENT_PLAYER_TURN = 0;
        SMALL_BLIND_GIVEN = false;
        BIG_BLIND_GIVEN = false;
        GAME_STAGE = GAME_STAGE_ONE_HOLE_CARDS;
        startGame();
    }


    // Start method
    private void startGame() {
        this.setNextDealerPlayer(); // Get next dealer player
        this.players.get(this.dealerPlayerArrayIndex).setPlayerAsDealer();
        this.getNextSmallBlindPlayer(); // Get small blind player
        this.shuffleCardDeck();
    }


    // Create new shuffled card deck
    private void shuffleCardDeck() {
        cardDeck = CardDeckGenerator.shuffledCardDeck();
        staging();
    }

    // ---------------------------------------------------------------------------------------------

    private void staging() {
        if (!KILL_THREADS) {
            switch (GAME_STAGE) {
                case GAME_STAGE_ONE_HOLE_CARDS: // Cards for players
                    GAME_STAGE = GAME_STAGE_TWO_PRE_FLOP;
                    setCurrentStageText("Hole Cards");
                    setTutorialWindowText("Hole cards stage gives every player two cards.");
                    holeCards();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_TWO_PRE_FLOP: // First betting round
                    GAME_STAGE = GAME_STAGE_THREE_THE_FLOP;
                    setCurrentStageText("Pre Flop");
                    IS_CALL_SITUATION = false;
                    CURRENT_PLAYER_TURN = this.smallBlindPlayerArrayIndex; // Round starting player is always small blind player
                    this.resetPlayerStates();
                    setTutorialWindowText("Pre Flop is first betting round after each player has been given two cards. You can check, make raise or fold.");
                    players.get(0).tempBet = 0;
                    resetPlayerHasRaised(); // No raises done yet
                    this.resetRoundParameters();
                    bettingRound();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_THREE_THE_FLOP: // Show first three cards
                    GAME_STAGE = GAME_STAGE_FOUR_POST_FLOP;
                    setCurrentStageText("The Flop");
                    setTutorialWindowText("The Flop shows first three middle cards and is followed by next betting round.");
                    theFlop();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_FOUR_POST_FLOP: // Second betting round
                    GAME_STAGE = GAME_STAGE_FIVE_THE_TURN;
                    setCurrentStageText("Post Flop");
                    IS_CALL_SITUATION = false;
                    CURRENT_PLAYER_TURN = this.smallBlindPlayerArrayIndex; // Round starting player is always small blind player
                    this.resetPlayerStates();
                    setTutorialWindowText("Post Flop is the second betting round. You can check, make raise or fold. Do decision based on your two cards + three middle cards.");
                    players.get(0).tempBet = 0;
                    resetPlayerHasRaised(); // No raises done yet
                    this.resetRoundParameters();
                    bettingRound();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_FIVE_THE_TURN: // Show fourth card
                    GAME_STAGE = GAME_STAGE_SIX_THE_POST_TURN;
                    setCurrentStageText("The Turn");
                    setTutorialWindowText("The Turn shows fourth card on the middle of the table.");
                    theTurn();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_SIX_THE_POST_TURN: // Third betting round
                    GAME_STAGE = GAME_STAGE_SEVEN_THE_RIVER;
                    setCurrentStageText("Post Turn");
                    IS_CALL_SITUATION = false;
                    CURRENT_PLAYER_TURN = this.smallBlindPlayerArrayIndex; // Round starting player is always small blind player
                    this.resetPlayerStates();
                    setTutorialWindowText("Post Turn is the third betting round. You can check, make raise or fold.");
                    players.get(0).tempBet = 0;
                    resetPlayerHasRaised(); // No raises done yet
                    this.resetRoundParameters();
                    bettingRound();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_SEVEN_THE_RIVER: // Show fifth card
                    GAME_STAGE = GAME_STAGE_EIGHT_THE_SHOW_DOWN;
                    setCurrentStageText("The River");
                    setTutorialWindowText("The River shows fifth a.k.a. final card of the middle cards.");
                    theRiver();
                    break;
                case GAME_STAGE_EIGHT_THE_SHOW_DOWN: // Final betting round (fourth)
                    GAME_STAGE = GAME_STAGE_NINE_RESULTS;
                    setCurrentStageText("Show Down");
                    IS_CALL_SITUATION = false;
                    CURRENT_PLAYER_TURN = this.smallBlindPlayerArrayIndex; // Round starting player is always small blind player
                    this.resetPlayerStates();
                    setTutorialWindowText("Show Down is the final and toughest betting round, following by results.");
                    players.get(0).tempBet = 0;
                    resetPlayerHasRaised(); // No raises done yet
                    this.resetRoundParameters();
                    bettingRound();
                    debugWindowFunctions();
                    break;
                case GAME_STAGE_NINE_RESULTS: // Show all cards and check for winner
                    setTutorialWindowText("Results...");
                    results();
                    debugWindowFunctions();
                    break;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Give all players two cards
    private void holeCards() {
        // Set player's cards
        int deckCard = 0;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).cards[0] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cards[1] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
        }
        // Set middle card's
        middleCards[0] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
        middleCards[1] = new CardVisual(cardDeck[deckCard + 1].suite(), cardDeck[deckCard + 1].rank());
        middleCards[2] = new CardVisual(cardDeck[deckCard + 2].suite(), cardDeck[deckCard + 2].rank());
        middleCards[3] = new CardVisual(cardDeck[deckCard + 3].suite(), cardDeck[deckCard + 3].rank());
        middleCards[4] = new CardVisual(cardDeck[deckCard + 4].suite(), cardDeck[deckCard + 4].rank());
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

    Runnable holeCardsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(1700);
                    final int sleepDuringCardPlace = 200; // ms
                    // ---------------------------------------------------
                    for (int r = 0; r < 2; r++) { // Card
                        for (int c = 0; c < players.size(); c++) { // Player
                            if (!players.get(c).isFold()) {
                                //audioPlayer.playCardPlaceOne(TexasHoldemOffline.this);
                                audioPlayer.playCardSlideSix(TexasHoldemOffline.this);
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
                    // ---------------------------------------------------
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staging();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    // ---------------------------------------------------------------------------------------------

    // Let's show first three cards after first betting round
    private void theFlop() {
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
                        audioPlayer.playCardSlideSix(TexasHoldemOffline.this);
                        final int finalF = f;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setMiddleCardDrawable(finalF + 1);
                            }
                        });
                        Thread.sleep(sleepDuringCardPlace);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staging();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    // Let's show fourth card
    private void theTurn() {
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
                    audioPlayer.playCardSlideSix(TexasHoldemOffline.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setMiddleCardDrawable(4);
                        }
                    });
                    Thread.sleep(sleepDuringCardPlace);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staging();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    // Let's show fifth card
    private void theRiver() {
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
                    audioPlayer.playCardSlideSix(TexasHoldemOffline.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setMiddleCardDrawable(5);
                        }
                    });
                    Thread.sleep(sleepDuringCardPlace);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staging();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    // All betting rounds goes thru this section
    private void bettingRound() {
        if (getActivePlayers()) {
            final int verify = verifyPlayersBets();
            final int noRoundPlayer = this.getNotRoundPlayedPlayer();
            Log.i(TAG, String.valueOf("Verify bit: " + String.valueOf(verify) + " / Round bit: " + String.valueOf(noRoundPlayer)));
            if (CURRENT_PLAYER_TURN >= players.size() || IS_CALL_SITUATION && verify == -1 || verify == -1 && noRoundPlayer == -1) {
                this.resetPlayerStates();
                if (verify == -1 && this.SMALL_BLIND_GIVEN) {
                    if (noRoundPlayer == -1) {
                        this.staging();
                    } else {
                        this.CURRENT_PLAYER_TURN = noRoundPlayer;
                        this.bettingRound();
                    }
                } else {
                    IS_CALL_SITUATION = true;
                    CURRENT_PLAYER_TURN = verify;
                    this.bettingRound();
                }
            } else {
                if (players.get(CURRENT_PLAYER_TURN) != null || IS_CALL_SITUATION && verify == -1 || !this.SMALL_BLIND_GIVEN || !this.BIG_BLIND_GIVEN) {
                    if (!players.get(CURRENT_PLAYER_TURN).isFold() && !players.get(CURRENT_PLAYER_TURN).isAllIn
                            || players.get(CURRENT_PLAYER_TURN).isAllIn && players.get(CURRENT_PLAYER_TURN).isBot && verify != -1) {
                        if (verify != -1 || !this.SMALL_BLIND_GIVEN || !this.BIG_BLIND_GIVEN) {
                            IS_CALL_SITUATION = true;
                        }
                        players.get(CURRENT_PLAYER_TURN).setPlayerTurn(true);
                        if (players.get(CURRENT_PLAYER_TURN).isBot) {
                            disableActionButtons(false);
                            if (botThread.isAlive()) {
                                botThread = null;
                            }
                            botThread = new Thread(botRunnable);
                            threadList.add(botThread);
                            if (!botThread.isAlive()) {
                                botThread.start();
                            }
                        } else {
                            enableActionButtons();
                        }
                        if (playerTurnThread.isAlive()) {
                            playerTurnThread = null;
                        }
                        playerTurnThread = new Thread(playerTurnRunnable);
                        threadList.add(playerTurnThread);
                        if (!playerTurnThread.isAlive()) {
                            playerTurnThread.start();
                        }
                    } else {
                        CURRENT_PLAYER_TURN = CURRENT_PLAYER_TURN + 1;
                        this.bettingRound();
                    }
                } else {
                    if (verify == -1) {
                        CURRENT_PLAYER_TURN = CURRENT_PLAYER_TURN + 1;
                        this.bettingRound();
                    } else {
                        CURRENT_PLAYER_TURN = verify;
                        this.bettingRound();
                    }
                }
            }
        } else {
            results();
        }
    }

    Runnable playerTurnRunnable = new Runnable() {
        final int timeout = 10 * 1000;

        @Override
        public void run() {
            try {
                if (activityRunning) {
                    final int current_player_turn = CURRENT_PLAYER_TURN;
                    players.get(current_player_turn).setPlayerTurn(true);
                    while (players.get(current_player_turn).playerState == Player.PLAYER_STATE_NONE && players.get(current_player_turn).isPlayerTurn()) {
                        if (TURN_TIME < timeout) {
                            Thread.sleep(1000);
                            TURN_TIME = TURN_TIME + 1000;
                            if (players.get(current_player_turn).isPlayerTurn()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        players.get(current_player_turn).setPlayerTimeBar((timeout - TURN_TIME) / 100);
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    players.get(current_player_turn).setPlayerTurn(false);
                                    playerFold(current_player_turn);
                                }
                            });
                        }
                    }
                    TURN_TIME = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            players.get(current_player_turn).setPlayerTurn(false);
                            for (int i = 0; i < players.size(); i++) {
                                if (i != current_player_turn) {
                                    players.get(i).hidePlayerTimeBar();
                                }
                            }
                            CURRENT_PLAYER_TURN = CURRENT_PLAYER_TURN + 1;
                            bettingRound();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    // ---------------------------------------------------------------------------------------------


    // Player folds
    private void playerFold(final int current_player_turn) {
        if (!this.SMALL_BLIND_GIVEN || !this.BIG_BLIND_GIVEN) {
            int blind_amount = 0;
            if (!this.SMALL_BLIND_GIVEN && !this.BIG_BLIND_GIVEN) {
                blind_amount = (MIN_BET / 2);
                this.SMALL_BLIND_GIVEN = true;
            } else if (this.SMALL_BLIND_GIVEN && !this.BIG_BLIND_GIVEN) {
                blind_amount = MIN_BET;
                this.BIG_BLIND_GIVEN = true;
            }
            if (blind_amount <= this.players.get(current_player_turn).playerMoney) {
                if (blind_amount == this.players.get(current_player_turn).playerMoney || this.someOneHasAllIn()) {
                    this.players.get(current_player_turn).isAllIn = true;
                }
                this.players.get(current_player_turn).totalBet = this.players.get(current_player_turn).totalBet + blind_amount;
                this.players.get(current_player_turn).playerMoney = this.players.get(current_player_turn).playerMoney - blind_amount;
                this.players.get(current_player_turn).setTotalBetView();
                this.players.get(current_player_turn).setMoneyView();
            }
        }
        this.players.get(current_player_turn).setFold();
        this.players.get(current_player_turn).setPlayerLastActionText("FOLD");
        checkHighestBet();
        calculateTotalPot();
    }


    // Player checks or calls
    private void playerCheck(final int current_player_turn) {
        if (IS_CALL_SITUATION || TOTAL_POT == 0 || !this.SMALL_BLIND_GIVEN || !this.BIG_BLIND_GIVEN) {
            int check_amount = 0;
            if (this.SMALL_BLIND_GIVEN && this.BIG_BLIND_GIVEN) {
                check_amount = CURRENT_HIGHEST_BET == 0 ? MIN_BET : (CURRENT_HIGHEST_BET - players.get(current_player_turn).totalBet);
            } else {
                if (this.SMALL_BLIND_GIVEN && !this.BIG_BLIND_GIVEN) {
                    check_amount = MIN_BET;
                    this.BIG_BLIND_GIVEN = true;
                } else {
                    check_amount = MIN_BET / 2;
                    this.SMALL_BLIND_GIVEN = true;
                }
            }
            if (check_amount <= players.get(current_player_turn).playerMoney) {
                if (check_amount == this.players.get(current_player_turn).playerMoney || someOneHasAllIn()) {
                    this.players.get(current_player_turn).isAllIn = true;
                }
                this.players.get(current_player_turn).totalBet = this.players.get(current_player_turn).totalBet + check_amount;
                this.players.get(current_player_turn).playerMoney = this.players.get(current_player_turn).playerMoney - check_amount;
                players.get(current_player_turn).setCheck(this);
            }
            this.players.get(current_player_turn).setPlayerLastActionText("CALL");
        } else {
            this.players.get(current_player_turn).setCheck(this);
            this.players.get(current_player_turn).setPlayerLastActionText("CHECK");
        }
        this.checkHighestBet();
        this.calculateTotalPot();
    }


    // Player raises
    private void playerRaise(final int current_player_turn, int amount) {
        if (amount == 0) {
            amount = (CURRENT_HIGHEST_BET - this.players.get(current_player_turn).totalBet);
        }
        if (amount <= this.players.get(current_player_turn).playerMoney) {
            if (amount == this.players.get(current_player_turn).playerMoney || this.someOneHasAllIn()) {
                this.players.get(current_player_turn).isAllIn = true;
            }
            if (current_player_turn > 0) {
                this.players.get(current_player_turn).totalBet = this.players.get(current_player_turn).totalBet + amount;
            }
            this.players.get(current_player_turn).playerMoney = this.players.get(current_player_turn).playerMoney - amount;
            this.players.get(current_player_turn).setRaise(this);
            IS_CALL_SITUATION = true;
            if (!this.SMALL_BLIND_GIVEN || !this.BIG_BLIND_GIVEN) {
                if (amount >= (MIN_BET / 2)) {
                    this.SMALL_BLIND_GIVEN = true;
                }
                if (amount >= MIN_BET) {
                    this.BIG_BLIND_GIVEN = true;
                }
            }
        }
        this.players.get(current_player_turn).setPlayerLastActionText("RAISE");
        this.checkHighestBet();
        this.calculateTotalPot();
    }


    // ---------------------------------------------------------------------------------------------


    // Show all players cards
    private void results() {
        // Calculate final pot
        calculateTotalPot();
        // Card images, sounds and animations
        if (resultsThread.isAlive()) {
            resultsThread = null;
        }
        resultsThread = new Thread(resultsRunnable);
        threadList.add(resultsThread);
        if (!resultsThread.isAlive()) {
            resultsThread.start();
        }
    }

    Runnable resultsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(500);
                    audioPlayer.playCardPlaceOne(TexasHoldemOffline.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAllPlayersCards();
                        }
                    });
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            calculateWinner();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void calculateWinner() {
        HandEvaluatorSeven handEvaluatorSeven;
        for (int i = 0; i < players.size(); i++) {
            if (!players.get(i).isFold()) {
                players.get(i).cards[2] = middleCards[0];
                players.get(i).cards[3] = middleCards[1];
                players.get(i).cards[4] = middleCards[2];
                players.get(i).cards[5] = middleCards[3];
                players.get(i).cards[6] = middleCards[4];
                handEvaluatorSeven = new HandEvaluatorSeven(players.get(i).cards);
                players.get(i).handValue = handEvaluatorSeven.getValue();
                players.get(i).handValueText = handEvaluatorSeven.getType().toString();
            }
        }

        debugWindowFunctions();

        double temp = 0;
        int winnerPlayer = 0;
        for (int a = 0; a < players.size(); a++) {
            if (players.get(a).handValue > temp) {
                temp = players.get(a).handValue;
                winnerPlayer = a;
            }
        }
        transferMoney(winnerPlayer);
    }


    // Transfer money to player and zero total pot
    private void transferMoney(int p) {
        Toast.makeText(this, "Winner: " + dummyNames[p], Toast.LENGTH_LONG).show();
        players.get(p).winsCount = players.get(p).winsCount + 1;
        players.get(p).playerMoney = players.get(p).playerMoney + TOTAL_POT;
        players.get(p).setMoneyView();
        players.get(p).startGlowAnimation();
        TOTAL_POT = 0;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).transferMoneyAnimation(this);
            players.get(i).totalBet = 0;
            players.get(i).tempBet = 0;
            players.get(i).setMoneyView();
            players.get(i).setPlayerTimeBar(0);
        }
        calculateTotalPot();
        if (moneyTransferThread.isAlive()) {
            moneyTransferThread = null;
        }
        moneyTransferThread = new Thread(moneyTransferRunnable);
        threadList.add(moneyTransferThread);
        if (!moneyTransferThread.isAlive()) {
            moneyTransferThread.start();
        }
    }

    // Basically starts new game
    Runnable moneyTransferRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            verifyPlayers();
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    // Check's that there's players that have money
    private void verifyPlayers() {
        int count = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerMoney > 0) {
                count = count + 1;
            }
        }
        if (count >= 2) {
            if (AUTO_START_NEW_ROUND) {
                initializeViews();
            } else {
                disableActionButtons(true);
                continueBtn.setVisibility(View.VISIBLE);
            }
        } else {
            noMorePlayersDialog();
        }
    }


    // Game saving is currently disabled
    /*
    // Save current player money amounts as stringSet
    private void saveCurrentGame() {
        if (GAME_STAGE == GAME_STAGE_NINE_RESULTS) {
            ArrayList<String> scoreList = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                scoreList.add(String.valueOf(players.get(i).playerMoney));
            }
            Log.i(TAG, scoreList.toString());
            SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(TexasHoldemOffline.this.getBaseContext());
            SharedPreferences.Editor editor = setSharedPreferences.edit();
            editor.putString(Constants.SP_OFFLINE_HOLDEM_SAVE_PARAMS, scoreList.toString());
            editor.apply();
            Toast.makeText(this, "Game saved.", Toast.LENGTH_SHORT).show();
            killThreads(true);
        } else {
            Toast.makeText(this, "Play current round to the end first.", Toast.LENGTH_SHORT).show();
        }
    }
    */

    /*
    private void loadSavedGame(int playerCount) {
        ArrayList<String> scoreList = new ArrayList<>(Arrays.asList(sharedPreferences.getString(Constants.SP_OFFLINE_HOLDEM_SAVE_PARAMS, null)
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "")
                .split(","))
        );
        if (scoreList.size() > 0) {
            if (scoreList.size() == playerCount) {
                for (int i = 0; i < players.size(); i++) {
                    players.get(i).playerMoney = Integer.valueOf(scoreList.get(i));
                    players.get(i).setMoneyView();
                }
            } else {
                Toast.makeText(this, "Players count is altered so saved game parameters are not matching current setup. Save game cleared.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Saved game params are corrupted", Toast.LENGTH_SHORT).show();
        }
        HOLDEM_SAVE_PROGRESS = false;
        SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(TexasHoldemOffline.this.getBaseContext());
        SharedPreferences.Editor editor = setSharedPreferences.edit();
        editor.putBoolean(Constants.SP_OFFLINE_HOLDEM_SAVE_PROGRESS, false);
        editor.apply();
    }
    */

    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    // Helpers basically


    // Set next dealer player
    private void setNextDealerPlayer() {
        this.dealerPlayerArrayIndex = this.dealerPlayerArrayIndex + 1;
        if (this.dealerPlayerArrayIndex >= this.players.size()) {
            this.dealerPlayerArrayIndex = 0;
        }
        this.players.get(this.dealerPlayerArrayIndex).isDealer = true;
    }


    // Get next small blind player
    private void getNextSmallBlindPlayer() {
        if (this.players.size() > 2) {
            this.smallBlindPlayerArrayIndex = this.dealerPlayerArrayIndex + 1;
            if (this.smallBlindPlayerArrayIndex >= this.players.size()) {
                this.smallBlindPlayerArrayIndex = 0;
            }
        } else {
            this.smallBlindPlayerArrayIndex = this.dealerPlayerArrayIndex;
        }
    }


    // Get next big blind player
    private int getNextBigBlindPlayer() {
        int bigBlindPlayerIndex = this.dealerPlayerArrayIndex + 2;
        if (bigBlindPlayerIndex >= this.players.size()) {
            bigBlindPlayerIndex = 0;
        }
        return bigBlindPlayerIndex;
    }


    // Get player which has not played round
    private int getNotRoundPlayedPlayer() {
        for (int i = 0; i < this.players.size(); i++) {
            if (!players.get(i).isFold() && !players.get(i).roundPlayed) {
                return i;
            }
        }
        return -1;
    }


    // Reset player round related parameters
    private void resetRoundParameters() {
        for (int i = 0; i < this.players.size(); i++) {
            players.get(i).roundPlayed = false;
        }
    }


    // Calculates total pot
    private void calculateTotalPot() {
        TOTAL_POT = 0;
        for (int i = 0; i < players.size(); i++) {
            TOTAL_POT = TOTAL_POT + players.get(i).totalBet;
        }
        totalPotTV.setText(getString(R.string.pot) + " " + currencyFormat.format(TOTAL_POT));
    }


    // Sets variable value to current highest bet
    private void checkHighestBet() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).totalBet > CURRENT_HIGHEST_BET) {
                CURRENT_HIGHEST_BET = players.get(i).totalBet;
            }
        }
        if (CURRENT_HIGHEST_BET < MIN_BET) {
            CURRENT_HIGHEST_BET = MIN_BET;
        }
    }


    // Some player has all in
    private boolean someOneHasAllIn() {
        int count = 0;
        for (int i = 0; i < this.players.size(); i++) {
            if (this.players.get(i).isAllIn) {
                count = count + 1;
            }
        }
        return count > 0;
    }


    // Check's if someone has all in
    private void resetPlayerHasRaised() {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).hasRaised = false;
        }
    }


    private void enableActionButtons() {
        if (foldBtn.getVisibility() == View.INVISIBLE) {
            audioPlayer.playCardTakeOutFromPackageOne(this);
            foldBtn.startAnimation(animFadeIn);
            foldBtn.setVisibility(View.VISIBLE);
            checkBtn.startAnimation(animFadeIn);
            checkBtn.setVisibility(View.VISIBLE);
            raiseBtn.startAnimation(animFadeIn);
            raiseBtn.setVisibility(View.VISIBLE);
            tenBtn.setEnabled(true);
            twentyFiveBtn.setEnabled(true);
            oneHundredBtn.setEnabled(true);
            fiveHundredBtn.setEnabled(true);
            allInBtn.setEnabled(true);
            allInBtn.setVisibility(View.VISIBLE);
            if (IS_CALL_SITUATION) {
                checkBtn.setText("CALL");
            } else {
                checkBtn.setText("CHECK");
            }
        }
    }


    private void disableActionButtons(boolean animate) {
        if (foldBtn.getVisibility() == View.VISIBLE) {
            if (animate) {
                foldBtn.startAnimation(animFadeOut);
                checkBtn.startAnimation(animFadeOut);
                raiseBtn.startAnimation(animFadeOut);
            }
            foldBtn.setVisibility(View.INVISIBLE);
            checkBtn.setVisibility(View.INVISIBLE);
            raiseBtn.setVisibility(View.INVISIBLE);
            tenBtn.setEnabled(false);
            twentyFiveBtn.setEnabled(false);
            oneHundredBtn.setEnabled(false);
            fiveHundredBtn.setEnabled(false);
            allInBtn.setVisibility(View.VISIBLE);
            allInBtn.setEnabled(false);
        }
    }


    // Initialize onClick listener's
    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        // Normal buttons
        foldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (players.get(0).tempBet == 0) {
                    playerFold(0);
                } else {
                    Toast.makeText(TexasHoldemOffline.this, "You have already thrown chips in game. Please raise.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerCheck(0);
            }
        });
        raiseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (players.get(0).tempBet > 0) {
                    playerRaise(0, players.get(0).tempBet);
                } else {
                    Toast.makeText(TexasHoldemOffline.this, "Select amount to raise first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGameDialog();
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeViews();
            }
        });
        // Betting buttons
        tenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(0).setGamePlayerRaiseHoldemOffline(TexasHoldemOffline.this, 10);
            }
        });
        twentyFiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(0).setGamePlayerRaiseHoldemOffline(TexasHoldemOffline.this, 25);
            }
        });
        oneHundredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(0).setGamePlayerRaiseHoldemOffline(TexasHoldemOffline.this, 100);
            }
        });
        fiveHundredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(0).setGamePlayerRaiseHoldemOffline(TexasHoldemOffline.this, 500);
            }
        });
        allInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (players.get(0).tempBet == 0) {
                    players.get(0).setGamePlayerRaiseHoldemOffline(TexasHoldemOffline.this, players.get(0).playerMoney);
                    playerRaise(0, players.get(0).tempBet);
                } else {
                    Toast.makeText(TexasHoldemOffline.this, "No all in accepted in middle of incremental raise. Press raise.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        debugWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - motionEvent.getRawX();
                        dY = view.getY() - motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.setX(motionEvent.getRawX() + dX);
                        view.setY(motionEvent.getRawY() + dY);
                        break;
                }
                return true;
            }
        });
        tutorialWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tdX = view.getX() - motionEvent.getRawX();
                        tdY = view.getY() - motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.setX(motionEvent.getRawX() + tdX);
                        view.setY(motionEvent.getRawY() + tdY);
                        break;
                }
                return true;
            }
        });
    }


    // Returns boolean is there more than one active player
    private boolean getActivePlayers() {
        int count = 0;
        for (int i = 0; i < players.size(); i++) {
            if (!players.get(i).isFold()) {
                count = count + 1;
            }
        }
        return count > 1;
    }


    // Returns player with not enough money in bet to continue
    private int verifyPlayersBets() {
        int i = 0;
        int highestBet = 0;
        for (i = 0; i < players.size(); i++) { // Get highest bet
            if (!players.get(i).isFold()) {
                if (highestBet == 0) {
                    highestBet = players.get(i).totalBet;
                }
                if (players.get(i).totalBet > highestBet) {
                    highestBet = players.get(i).totalBet;
                }
            }

        }
        for (i = 0; i < players.size(); i++) { // Find some one with lower bet
            if (!players.get(i).isFold()) {
                if (players.get(i).totalBet < highestBet) {
                    return i;
                }
            }
        }
        return !this.SMALL_BLIND_GIVEN || !this.BIG_BLIND_GIVEN ? 0 : -1;
    }


    // Resets player states
    private void resetPlayerStates() {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).playerState = Player.PLAYER_STATE_NONE;
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Bot logic

    Runnable botRunnable = new Runnable() {
        int[] botTurnTimes = {1500, 2000, 2500, 3000, 4000};

        @Override
        public void run() {
            try {
                if (activityRunning) {
                    final String[] result = botPerformAction(CURRENT_PLAYER_TURN, IS_CALL_SITUATION);
                    Thread.sleep(botTurnTimes[new Random().nextInt(4)]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "_______________________________________________________");
                            Log.i(TAG, players.get(CURRENT_PLAYER_TURN).getName());
                            Log.i(TAG, result[0]);
                            Log.i(TAG, result[1]);
                            Log.i(TAG, "_______________________________________________________");
                            switch (result[0]) {
                                case "bot_fold":
                                    playerFold(CURRENT_PLAYER_TURN);
                                    break;
                                case "bot_check":
                                    playerCheck(CURRENT_PLAYER_TURN);
                                    break;
                                case "bot_call":
                                    playerCheck(CURRENT_PLAYER_TURN);
                                    break;
                                case "bot_raise":
                                    playerRaise(CURRENT_PLAYER_TURN, Integer.parseInt(result[1]));
                                    break;
                                case "remove_bot":
                                    playerFold(CURRENT_PLAYER_TURN);
                                    players.get(CURRENT_PLAYER_TURN).isRemoved = true;
                                    break;
                            }
                        }
                    });
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private String[] botPerformAction(final int currentPlayer, final boolean isCallSituation) {
        final int checkAmount = CURRENT_HIGHEST_BET == 0 ? MIN_BET : (CURRENT_HIGHEST_BET - players.get(currentPlayer).totalBet);
        String[] resultSet = new String[2];
        final int randomInt = new Random().nextInt(1000) + 1;
        boolean hasSameCardHand = false;
        if (players.get(currentPlayer).cards[0].toWords().equals(players.get(currentPlayer).cards[1].toWords())) {
            hasSameCardHand = true;
        }
        if (players.get(currentPlayer).playerMoney < MIN_BET) {
            resultSet[0] = "remove_bot";
            resultSet[1] = "0";
        } else {
            if (players.get(currentPlayer).playerMoney < checkAmount) {
                resultSet[0] = "bot_fold";
                resultSet[1] = "0";
            } else {
                if (randomInt >= 980 && !hasSameCardHand) {
                    resultSet[0] = "bot_fold";
                    resultSet[1] = "0";
                } else if (randomInt > 700 && randomInt < 980 && isCallSituation || hasSameCardHand && isCallSituation) {
                    resultSet[0] = "bot_raise";
                    resultSet[1] = botGetRandomRaiseAmount(currentPlayer);
                } else {
                    if (isCallSituation) {
                        resultSet[0] = "bot_call";
                        resultSet[1] = "0";
                    } else {
                        resultSet[0] = "bot_check";
                        resultSet[1] = "0";
                    }
                }
            }
        }
        return resultSet;
    }

    private String botGetRandomRaiseAmount(final int currentPlayer) {
        final int[] betAmounts = {25, 35, 100, 500};
        final int raise = betAmounts[new Random().nextInt(3)];
        if (raise > players.get(currentPlayer).playerMoney) {
            return botGetRandomRaiseAmount(currentPlayer);
        } else {
            return String.valueOf(raise);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void setMiddleCardDrawable(int card) {
        switch (card) {
            case 1:
                card1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[0].toWords())));
                card1.startAnimation(animFadeIn);
                break;
            case 2:
                card2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[1].toWords())));
                card2.startAnimation(animFadeIn);
                break;
            case 3:
                card3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[2].toWords())));
                card3.startAnimation(animFadeIn);
                break;
            case 4:
                card4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[3].toWords())));
                card4.startAnimation(animFadeIn);
                break;
            case 5:
                card5.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, middleCards[4].toWords())));
                card5.startAnimation(animFadeIn);
                break;
        }
    }

    private void setPlayerCardDrawable(int p, int card) {
        switch (p) {
            case 0:
                if (!players.get(p).isFold()) {
                    switch (card) {
                        case 0:
                            players.get(p).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[0].toWords())));
                            players.get(p).playerCard0.startAnimation(animFadeIn);
                            break;
                        case 1:
                            players.get(p).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[1].toWords())));
                            players.get(p).playerCard1.startAnimation(animFadeIn);
                            break;
                    }
                }
                break;
            default:
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
                break;
        }
    }

    private void showAllPlayersCards() {
        for (int i = 1; i < players.size(); i++) {
            if (!players.get(i).isFold()) {
                players.get(i).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[0].toWords())));
                players.get(i).playerCard0.startAnimation(animFadeIn);
                players.get(i).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[1].toWords())));
                players.get(i).playerCard1.startAnimation(animFadeIn);
            }
        }
    }

    private void killThreads(boolean finish) {
        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).interrupt();
        }
        if (finish) {
            KILL_THREADS = true;
            TexasHoldemOffline.this.finish();
        }
    }


    private void setCurrentStageText(String stage) {
        currentStageTV.setText("Stage: " + stage);
    }


    private void setTutorialWindowText(String text) {
        tutorialTV.setText(text);
    }


    // Debug function
    private void debugWindowFunctions() {
        int totalMoney = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            totalMoney = totalMoney + players.get(i).playerMoney;
            totalMoney = totalMoney + players.get(i).totalBet;
            sb.append(dummyNames[i] + " value: " + String.valueOf(players.get(i).handValue) + " - " + players.get(i).handValueText + "\n");
        }
        gameTotalMoneyTV.setText("Game total money" + "\n" + String.valueOf(totalMoney) + "\n\n" + sb.toString());
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

    private void endGameDialog() {
        new AlertDialog.Builder(TexasHoldemOffline.this)
                .setTitle("Quit")
                .setMessage("Are you sure that you want to quit your current game?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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

    private void noMorePlayersDialog() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            sb.append(players.get(i).getName()).append("\n");
            sb.append("➥ ").append(String.valueOf(players.get(i).winsCount)).append(" wins.\n\n");
        }
        new AlertDialog.Builder(TexasHoldemOffline.this)
                .setTitle("End of game")
                .setMessage(sb.toString())
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TexasHoldemOffline.this.finish();
                    }
                })
                .setCancelable(false)
                .setIcon(R.mipmap.logo)
                .show();
    }


    // ---------------------------------------------------------------------------------------------

} 