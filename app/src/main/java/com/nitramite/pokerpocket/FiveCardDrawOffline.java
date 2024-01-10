package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.nitramite.bot.FiveCardDrawBot;
import com.nitramite.cardlogic.Card;
import com.nitramite.cardlogic.CardDeckGenerator;
import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.cardlogic.HandEvaluatorFive;
import com.nitramite.dynamic.Player;
import com.nitramite.dynamic.Seat;

import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("RedundantCast")
public class FiveCardDrawOffline extends AppCompatActivity {

    // Logging
    private final static String TAG = FiveCardDrawOffline.class.getSimpleName();

    // Player names
    private String MY_PLAYER_NICKNAME = "";
    private String[] dummyNames = {
            "Martin", "Bot 1", "Bot 2", "Bot 3", "Bot 4", "Bot 5"
    };

    // Game staging
    private static final int GAME_STAGE_ONE_GIVE_CARDS = 0;
    private static final int GAME_STAGE_TWO_CHANGE_CARDS = 1;
    private static final int GAME_STAGE_THREE_GIVE_MISSING_CARDS = 2;
    private static final int GAME_STAGE_FOUR_RESULTS = 3;

    // Activity features
    private Boolean activityRunning = true;
    private FiveCardDrawBot fiveCardDrawBot;
    private GameCardResource gameCardResource;
    private boolean AUTO_START_NEW_ROUND = true;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private int GAME_STAGE = GAME_STAGE_ONE_GIVE_CARDS;
    private int cardChangeTime = 0;
    private int deckCard = 0;

    // Threads
    private ArrayList<Thread> threadList = new ArrayList<>();
    private Thread giveCardsThread;
    private Thread changeCardsThread;
    private Thread giveMissingCardsThread;
    private Thread resultsThread;

    // Animation declarations
    private Animation animFadeIn, animFadeOut;

    // Shuffled card deck
    private Card[] cardDeck;

    // Buttons etc
    private CardView exitBtn, continueBtn, skipTimerBtn;
    private CardView hintTextCard;
    private TextView hintTextCardText;

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
        setContentView(R.layout.activity_five_card_draw_offline);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Find button components
        exitBtn = (CardView) findViewById(R.id.exitBtn);
        continueBtn = (CardView) findViewById(R.id.continueBtn);
        skipTimerBtn = (CardView) findViewById(R.id.skipTimerBtn);
        hintTextCard = (CardView) findViewById(R.id.hintTextCard);
        hintTextCardText = (TextView) findViewById(R.id.hintTextCardText);

        // Seat 1
        FrameLayout seat1Frame = (FrameLayout) findViewById(R.id.seat1Frame);
        CardView seat1Card = (CardView) findViewById(R.id.seat1Card);
        ImageView seat1Card0 = (ImageView) findViewById(R.id.seat1Card0);
        ImageView seat1Card1 = (ImageView) findViewById(R.id.seat1Card1);
        ImageView seat1Card2 = (ImageView) findViewById(R.id.seat1Card2);
        ImageView seat1Card3 = (ImageView) findViewById(R.id.seat1Card3);
        ImageView seat1Card4 = (ImageView) findViewById(R.id.seat1Card4);
        TextView seat1NameTV = (TextView) findViewById(R.id.seat1NameTV);
        ProgressBar seat1TimeBar = (ProgressBar) findViewById(R.id.seat1TimeBar);
        TextView seat1WinsCountTV = (TextView) findViewById(R.id.seat1WinsCountTV);
        seats.add(new Seat(seat1Frame, seat1Card, seat1Card0, seat1Card1, seat1Card2, seat1Card3, seat1Card4, null, seat1NameTV, null, seat1TimeBar, null, null, null, seat1WinsCountTV, null, null, null, null, null));

        // Seat 2
        FrameLayout seat2Frame = (FrameLayout) findViewById(R.id.seat2Frame);
        CardView seat2Card = (CardView) findViewById(R.id.seat2Card);
        ImageView seat2Card0 = (ImageView) findViewById(R.id.seat2Card0);
        ImageView seat2Card1 = (ImageView) findViewById(R.id.seat2Card1);
        ImageView seat2Card2 = (ImageView) findViewById(R.id.seat2Card2);
        ImageView seat2Card3 = (ImageView) findViewById(R.id.seat2Card3);
        ImageView seat2Card4 = (ImageView) findViewById(R.id.seat2Card4);
        TextView seat2NameTV = (TextView) findViewById(R.id.seat2NameTV);
        ProgressBar seat2TimeBar = (ProgressBar) findViewById(R.id.seat2TimeBar);
        TextView seat2WinsCountTV = (TextView) findViewById(R.id.seat2WinsCountTV);
        seats.add(new Seat(seat2Frame, seat2Card, seat2Card0, seat2Card1, seat2Card2, seat2Card3, seat2Card4, null, seat2NameTV, null, seat2TimeBar, null, null, null, seat2WinsCountTV, null, null, null, null, null));

        // Seat 3
        FrameLayout seat3Frame = (FrameLayout) findViewById(R.id.seat3Frame);
        CardView seat3Card = (CardView) findViewById(R.id.seat3Card);
        ImageView seat3Card0 = (ImageView) findViewById(R.id.seat3Card0);
        ImageView seat3Card1 = (ImageView) findViewById(R.id.seat3Card1);
        ImageView seat3Card2 = (ImageView) findViewById(R.id.seat3Card2);
        ImageView seat3Card3 = (ImageView) findViewById(R.id.seat3Card3);
        ImageView seat3Card4 = (ImageView) findViewById(R.id.seat3Card4);
        TextView seat3NameTV = (TextView) findViewById(R.id.seat3NameTV);
        ProgressBar seat3TimeBar = (ProgressBar) findViewById(R.id.seat3TimeBar);
        TextView seat3WinsCountTV = (TextView) findViewById(R.id.seat3WinsCountTV);
        seats.add(new Seat(seat3Frame, seat3Card, seat3Card0, seat3Card1, seat3Card2, seat3Card3, seat3Card4, null, seat3NameTV, null, seat3TimeBar, null, null, null, seat3WinsCountTV, null, null, null, null, null));

        // Seat 4
        FrameLayout seat4Frame = (FrameLayout) findViewById(R.id.seat4Frame);
        CardView seat4Card = (CardView) findViewById(R.id.seat4Card);
        ImageView seat4Card0 = (ImageView) findViewById(R.id.seat4Card0);
        ImageView seat4Card1 = (ImageView) findViewById(R.id.seat4Card1);
        ImageView seat4Card2 = (ImageView) findViewById(R.id.seat4Card2);
        ImageView seat4Card3 = (ImageView) findViewById(R.id.seat4Card3);
        ImageView seat4Card4 = (ImageView) findViewById(R.id.seat4Card4);
        TextView seat4NameTV = (TextView) findViewById(R.id.seat4NameTV);
        ProgressBar seat4TimeBar = (ProgressBar) findViewById(R.id.seat4TimeBar);
        TextView seat4WinsCountTV = (TextView) findViewById(R.id.seat4WinsCountTV);
        seats.add(new Seat(seat4Frame, seat4Card, seat4Card0, seat4Card1, seat4Card2, seat4Card3, seat4Card4, null, seat4NameTV, null, seat4TimeBar, null, null, null, seat4WinsCountTV, null, null, null, null, null));

        // Seat 5
        FrameLayout seat5Frame = (FrameLayout) findViewById(R.id.seat5Frame);
        CardView seat5Card = (CardView) findViewById(R.id.seat5Card);
        ImageView seat5Card0 = (ImageView) findViewById(R.id.seat5Card0);
        ImageView seat5Card1 = (ImageView) findViewById(R.id.seat5Card1);
        ImageView seat5Card2 = (ImageView) findViewById(R.id.seat5Card2);
        ImageView seat5Card3 = (ImageView) findViewById(R.id.seat5Card3);
        ImageView seat5Card4 = (ImageView) findViewById(R.id.seat5Card4);
        TextView seat5NameTV = (TextView) findViewById(R.id.seat5NameTV);
        ProgressBar seat5TimeBar = (ProgressBar) findViewById(R.id.seat5TimeBar);
        TextView seat5WinsCountTV = (TextView) findViewById(R.id.seat5WinsCountTV);
        seats.add(new Seat(seat5Frame, seat5Card, seat5Card0, seat5Card1, seat5Card2, seat5Card3, seat5Card4, null, seat5NameTV, null, seat5TimeBar, null, null, null, seat5WinsCountTV, null, null, null, null, null));

        // Seat 6
        FrameLayout seat6Frame = (FrameLayout) findViewById(R.id.seat6Frame);
        CardView seat6Card = (CardView) findViewById(R.id.seat6Card);
        ImageView seat6Card0 = (ImageView) findViewById(R.id.seat6Card0);
        ImageView seat6Card1 = (ImageView) findViewById(R.id.seat6Card1);
        ImageView seat6Card2 = (ImageView) findViewById(R.id.seat6Card2);
        ImageView seat6Card3 = (ImageView) findViewById(R.id.seat6Card3);
        ImageView seat6Card4 = (ImageView) findViewById(R.id.seat6Card4);
        TextView seat6NameTV = (TextView) findViewById(R.id.seat6NameTV);
        ProgressBar seat6TimeBar = (ProgressBar) findViewById(R.id.seat6TimeBar);
        TextView seat6WinsCountTV = (TextView) findViewById(R.id.seat6WinsCountTV);
        seats.add(new Seat(seat6Frame, seat6Card, seat6Card0, seat6Card1, seat6Card2, seat6Card3, seat6Card4, null, seat6NameTV, null, seat6TimeBar, null, null, null, seat6WinsCountTV, null, null, null, null, null));

        // Get settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final int playerCount = sharedPreferences.getInt(Constants.SP_OFFLINE_FIVE_CARD_DRAW_BOT_COUNT, 3);
        AUTO_START_NEW_ROUND = sharedPreferences.getBoolean(Constants.SP_OFFLINE_FIVE_CARD_DRAW_AUTO_NEXT_ROUND, false);
        MY_PLAYER_NICKNAME = sharedPreferences.getString(Constants.SP_PLAYER_NICKNAME, "Anon" + new Random().nextInt(1000) + 1);
        dummyNames[0] = MY_PLAYER_NICKNAME;

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_five_card_draw);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_five_card_draw);

        // Init bot logic
        fiveCardDrawBot = new FiveCardDrawBot(this);

        // Init threads
        giveCardsThread = new Thread(giveCardsRunnable);
        changeCardsThread = new Thread(changeCardsRunnable);
        giveMissingCardsThread = new Thread(giveMissingCardsRunnable);
        resultsThread = new Thread(resultsRunnable);

        initOnClickListeners();
        playerSeats(playerCount);
    } // End of onCreate()


    // Gives players seat according to player count (makes view look better)
    private void playerSeats(int playerCount) {
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
                            0,
                            false,
                            Player.TYPE_FIVE_CARD_DRAW
                    )
            );
            seats.get(seatNum[i]).activateSeat();
        }
        // Play starting sound
        audioPlayer.playCardOpenPackage(this);
        initializeViews();
    }


    // Init game
    private void initializeViews() {
        deckCard = 0;
        hintTextCard.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        skipTimerBtn.setVisibility(View.GONE);
        killThreads();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).removeCardDrawablesFiveCardDraw();
            players.get(i).setPlayerTimeBar(0);
            players.get(i).clearGlowAnimation();
        }
        GAME_STAGE = GAME_STAGE_ONE_GIVE_CARDS;
        startGame();
    }


    // Start method
    private void startGame() {
        shuffleCardDeck();
    }


    // Create new shuffled card deck
    private void shuffleCardDeck() {
        cardDeck = CardDeckGenerator.shuffledCardDeck();
        staging();
    }

    // ---------------------------------------------------------------------------------------------


    private void staging() {
        switch (GAME_STAGE) {
            case GAME_STAGE_ONE_GIVE_CARDS: // Cards for players
                GAME_STAGE = GAME_STAGE_TWO_CHANGE_CARDS;
                giveCards();
                break;
            case GAME_STAGE_TWO_CHANGE_CARDS: // First betting round
                GAME_STAGE = GAME_STAGE_THREE_GIVE_MISSING_CARDS;
                cardChangeTime = 0; // Set to zero
                hintTextCard.setVisibility(View.VISIBLE);
                hintTextCardText.setText("Hint: Click cards which you want to change");
                initPlayerCardClickListeners();
                skipTimerBtn.setVisibility(View.VISIBLE);
                changeCards();
                break;
            case GAME_STAGE_THREE_GIVE_MISSING_CARDS:
                GAME_STAGE = GAME_STAGE_FOUR_RESULTS;
                giveMissingCards();
                break;
            case GAME_STAGE_FOUR_RESULTS: // Show first three cards
                results();
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Give all players two cards
    private void giveCards() {
        // Set player's cards
        for (int i = 0; i < players.size(); i++) {
            players.get(i).cards[0] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cards[1] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cards[2] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cards[3] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cards[4] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
        }
        // Card images, sounds and animations
        if (giveCardsThread.isAlive()) {
            giveCardsThread = null;
        }
        giveCardsThread = new Thread(giveCardsRunnable);
        threadList.add(giveCardsThread);
        if (!giveCardsThread.isAlive()) {
            giveCardsThread.start();
        }
    }

    Runnable giveCardsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(1700);
                    final int sleepDuringCardPlace = 200; // ms
                    // ---------------------------------------------------
                    for (int r = 0; r < 5; r++) { // Card
                        for (int c = 0; c < players.size(); c++) { // Player
                            if (!players.get(c).isFold()) {
                                audioPlayer.playCardSlideSix(FiveCardDrawOffline.this);
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

    // Change cards
    private void changeCards() {
        if (changeCardsThread.isAlive()) {
            changeCardsThread.interrupt();
            changeCardsThread = null;
        }
        changeCardsThread = new Thread(changeCardsRunnable);
        threadList.add(changeCardsThread);
        if (!changeCardsThread.isAlive()) {
            changeCardsThread.start();
        }
    }

    /* Most important thread, which handles player turns in logical way */
    Runnable changeCardsRunnable = new Runnable() {
        final int timeout = 10 * 1000;

        @Override
        public void run() {
            try {
                if (activityRunning) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            botsChangeCards();
                        }
                    });
                    while (cardChangeTime < timeout) {
                        Thread.sleep(500);
                        cardChangeTime = cardChangeTime + 500;
                        for (int i = 0; i < players.size(); i++) {
                            players.get(i).setPlayerTimeBar((timeout - cardChangeTime) / 100);
                        }
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

    private void botsChangeCards() {
        for (int i = 1; i < players.size(); i++) {
            fiveCardDrawBot.botHandHandler(this, players.get(i));
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Give new cards for swapped cards
    private void giveMissingCards() {
        // Card images, sounds and animations
        if (giveMissingCardsThread.isAlive()) {
            giveMissingCardsThread = null;
        }
        giveMissingCardsThread = new Thread(giveMissingCardsRunnable);
        threadList.add(giveMissingCardsThread);
        if (!giveMissingCardsThread.isAlive()) {
            giveMissingCardsThread.start();
        }
    }

    Runnable giveMissingCardsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    Thread.sleep(1700);
                    final int sleepDuringCardPlace = 200; // ms
                    // ---------------------------------------------------
                    // Give new card for swapped cards
                    for (int i = 0; i < players.size(); i++) {
                        for (int a = 0; a < 5; a++) {
                            if (players.get(i).cards[a] == null) {
                                audioPlayer.playCardSlideSix(FiveCardDrawOffline.this);
                                players.get(i).cards[a] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
                                final int finalI = i;
                                final int finalA = a;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setPlayerCardDrawable(finalI, finalA);
                                    }
                                });
                                Thread.sleep(sleepDuringCardPlace);
                                deckCard = deckCard + 1;
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

    // Show all players cards and results
    private void results() {
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
                    audioPlayer.playCardPlaceOne(FiveCardDrawOffline.this);
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
        HandEvaluatorFive handEvaluatorFive;
        for (int i = 0; i < players.size(); i++) {
            handEvaluatorFive = new HandEvaluatorFive(players.get(i).cards);
            players.get(i).handValue = handEvaluatorFive.getPokerHandAsValued();
            players.get(i).handValueText = handEvaluatorFive.getPokerHandAsString();
        }
        double temp = 0;
        int winnerPlayer = 0;
        for (int a = 0; a < players.size(); a++) {
            if (players.get(a).handValue > temp) {
                temp = players.get(a).handValue;
                winnerPlayer = a;
            }
        }
        Toast.makeText(this, "Winner: " + dummyNames[winnerPlayer], Toast.LENGTH_LONG).show();
        hintTextCardText.setText("Winner: " + dummyNames[winnerPlayer] + " ➞ Hand: " + players.get(winnerPlayer).handValueText);
        players.get(winnerPlayer).incrementWinsCount(); // Increment wins count
        players.get(winnerPlayer).startGlowAnimation();
        if (winnerPlayer != 1) {
            /* would normally show ad here */
        }
        if (AUTO_START_NEW_ROUND) {
            initializeViews();
        } else {
            continueBtn.setVisibility(View.VISIBLE);
        }
    }

    // ---------------------------------------------------------------------------------------------


    // *********************************************************************************************
    /* Helpers */

    private void killThreads() {
        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).interrupt();
        }
    }


    private void showAllPlayersCards() {
        for (int i = 1; i < players.size(); i++) {
            players.get(i).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[0].toWords())));
            players.get(i).playerCard0.startAnimation(animFadeIn);
            players.get(i).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[1].toWords())));
            players.get(i).playerCard1.startAnimation(animFadeIn);
            players.get(i).playerCard2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[2].toWords())));
            players.get(i).playerCard2.startAnimation(animFadeIn);
            players.get(i).playerCard3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[3].toWords())));
            players.get(i).playerCard3.startAnimation(animFadeIn);
            players.get(i).playerCard4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(i).cards[4].toWords())));
            players.get(i).playerCard4.startAnimation(animFadeIn);
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
                        case 2:
                            players.get(p).playerCard2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[2].toWords())));
                            players.get(p).playerCard2.startAnimation(animFadeIn);
                            break;
                        case 3:
                            players.get(p).playerCard3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[3].toWords())));
                            players.get(p).playerCard3.startAnimation(animFadeIn);
                            break;
                        case 4:
                            players.get(p).playerCard4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[4].toWords())));
                            players.get(p).playerCard4.startAnimation(animFadeIn);
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
                        case 2:
                            players.get(p).playerCard2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                            players.get(p).playerCard2.startAnimation(animFadeIn);
                            break;
                        case 3:
                            players.get(p).playerCard3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                            players.get(p).playerCard3.startAnimation(animFadeIn);
                            break;
                        case 4:
                            players.get(p).playerCard4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, "top_red")));
                            players.get(p).playerCard4.startAnimation(animFadeIn);
                            break;
                    }
                }
                break;
        }
    }


    private void initPlayerCardClickListeners() {
        // Player card swap listeners
        players.get(0).playerCard0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(GAME_STAGE));
                if (GAME_STAGE == GAME_STAGE_THREE_GIVE_MISSING_CARDS) {
                    audioPlayer.playCardSlideOne(FiveCardDrawOffline.this);
                    players.get(0).cards[0] = null;
                    players.get(0).playerCard0.startAnimation(animFadeOut);
                    players.get(0).playerCard0.setBackground(null);
                    players.get(0).playerCard0.setOnClickListener(null);
                }
            }
        });
        players.get(0).playerCard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(GAME_STAGE));
                if (GAME_STAGE == GAME_STAGE_THREE_GIVE_MISSING_CARDS) {
                    audioPlayer.playCardSlideOne(FiveCardDrawOffline.this);
                    players.get(0).cards[1] = null;
                    players.get(0).playerCard1.startAnimation(animFadeOut);
                    players.get(0).playerCard1.setBackground(null);
                    players.get(0).playerCard1.setOnClickListener(null);
                }
            }
        });
        players.get(0).playerCard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(GAME_STAGE));
                if (GAME_STAGE == GAME_STAGE_THREE_GIVE_MISSING_CARDS) {
                    audioPlayer.playCardSlideOne(FiveCardDrawOffline.this);
                    players.get(0).cards[2] = null;
                    players.get(0).playerCard2.startAnimation(animFadeOut);
                    players.get(0).playerCard2.setBackground(null);
                    players.get(0).playerCard2.setOnClickListener(null);
                }
            }
        });
        players.get(0).playerCard3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(GAME_STAGE));
                if (GAME_STAGE == GAME_STAGE_THREE_GIVE_MISSING_CARDS) {
                    audioPlayer.playCardSlideOne(FiveCardDrawOffline.this);
                    players.get(0).cards[3] = null;
                    players.get(0).playerCard3.startAnimation(animFadeOut);
                    players.get(0).playerCard3.setBackground(null);
                    players.get(0).playerCard3.setOnClickListener(null);
                }
            }
        });
        players.get(0).playerCard4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(GAME_STAGE));
                if (GAME_STAGE == GAME_STAGE_THREE_GIVE_MISSING_CARDS) {
                    audioPlayer.playCardSlideOne(FiveCardDrawOffline.this);
                    players.get(0).cards[4] = null;
                    players.get(0).playerCard4.startAnimation(animFadeOut);
                    players.get(0).playerCard4.setBackground(null);
                    players.get(0).playerCard4.setOnClickListener(null);
                }
            }
        });
    }


    private void initOnClickListeners() {
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeViews();
            }
        });
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGameDialog();
            }
        });
        skipTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardChangeTime = 10 * 1000; // Set time as gone
                skipTimerBtn.setVisibility(View.GONE);
            }
        });
    }

    // *********************************************************************************************

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
        new AlertDialog.Builder(FiveCardDrawOffline.this)
                .setTitle("Quit")
                .setMessage("Are you sure that you want to quit your current game?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        killThreads();
                        FiveCardDrawOffline.this.finish();
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


    // *********************************************************************************************


} 