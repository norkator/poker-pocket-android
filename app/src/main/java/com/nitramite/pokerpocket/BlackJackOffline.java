package com.nitramite.pokerpocket;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.nitramite.cardlogic.Card;
import com.nitramite.cardlogic.CardDeckGenerator;
import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.dynamic.Player;
import com.nitramite.dynamic.Seat;
import com.nitramite.popups.RelativePopUpCard;
import com.nitramite.popups.RelativePopup;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings({"RedundantCast", "FieldCanBeLocal"})
public class BlackJackOffline extends AppCompatActivity {

    // Logging
    private final static String TAG = BlackJackOffline.class.getSimpleName();

    // Nickname
    private String MY_PLAYER_NICKNAME = "";

    // Windows
    private CardView tutorialWindow;
    private float tdX, tdY;

    // Game staging
    private static final int GAME_STAGE_ONE_BETTING = 0;
    private static final int GAME_STAGE_TWO_GIVE_CARDS = 1;
    private static final int GAME_STAGE_THREE_PLAYERS_TURN = 2;
    private static final int GAME_STAGE_FOUR_DEALERS_TURN = 3;
    private static final int GAME_STAGE_FIVE_THE_RESULTS = 4;

    // Activity features
    private Boolean activityRunning = true;
    private int GAME_MODE = 0;
    private SharedPreferences sharedPreferences;
    private GameCardResource gameCardResource;
    private boolean AUTO_START_NEW_ROUND = false;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private int GAME_STAGE = GAME_STAGE_ONE_BETTING;
    private int turnTime = 0;
    private int deckCard = 0; // Last picked card from deck
    private int lastDeckCount = 0;

    // Indicator views and variables
    private TextView tutorialTV;
    private TextView currentStageTV;
    private TextView handCountingTV;
    RelativePopUpCard popUpCard;

    // Store pop ups in array for closing
    ArrayList<RelativePopup> relativePopupArrayList = new ArrayList<>();

    // Threads
    private boolean KILL_THREADS = false;
    ArrayList<Thread> threadList = new ArrayList<>();
    Thread playerBettingThread;
    Thread giveCardsThread;
    Thread dealersTurnThread;

    // Animation declarations
    Animation animFadeIn, animFadeOut;

    // Shuffled card deck
    private Card[] cardDeck;

    // Buttons etc
    private CardView exitBtn, continueBtn, skipTimerBtn;
    private Button hitBtn, standBtn, surrenderBtn;
    private Button tenBtn, twentyFiveBtn, oneHundredBtn, fiveHundredBtn, allInBtn;

    // Players are in this array (0 = dealer, 1 = player)
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
        setContentView(R.layout.activity_black_jack_offline);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get game mode
        Intent intent = getIntent();
        GAME_MODE = intent.getIntExtra("mode", 0);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Find indicator views
        tutorialWindow = (CardView)findViewById(R.id.tutorialWindow);
        tutorialTV = (TextView)findViewById(R.id.tutorialTV);
        currentStageTV = (TextView)findViewById(R.id.currentStageTV);
        handCountingTV = (TextView)findViewById(R.id.handCountingTV);
        handCountingTV.setVisibility(View.GONE);

        // Find button components
        exitBtn = (CardView)findViewById(R.id.exitBtn);
        continueBtn = (CardView)findViewById(R.id.continueBtn);
        skipTimerBtn = (CardView)findViewById(R.id.skipTimerBtn);
        hitBtn = (Button)findViewById(R.id.hitBtn);
        standBtn = (Button)findViewById(R.id.standBtn);
        surrenderBtn = (Button)findViewById(R.id.surrenderBtn);
        tenBtn = (Button)findViewById(R.id.tenBtn);
        twentyFiveBtn = (Button)findViewById(R.id.twentyFiveBtn);
        oneHundredBtn = (Button)findViewById(R.id.oneHundredBtn);
        fiveHundredBtn = (Button)findViewById(R.id.fiveHundredBtn);
        allInBtn = (Button)findViewById(R.id.allInBtn);

        // Get settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (sharedPreferences.getBoolean(Constants.SP_OFFLINE_BLACKJACK_SHOW_TUTORIAL_WINDOW, false)) {
            tutorialWindow.setVisibility(View.VISIBLE);
        } else {
            tutorialWindow.setVisibility(View.INVISIBLE);
        }
        MY_PLAYER_NICKNAME = sharedPreferences.getString(Constants.SP_PLAYER_NICKNAME, "Anon" + new Random().nextInt(1000) + 1);

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_holdem);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_holdem);

        // Init threads
        playerBettingThread = new Thread(playerBettingRunnable);
        giveCardsThread = new Thread(giveCardsRunnable);
        dealersTurnThread = new Thread(dealersTurnRunnable);

        // Add dealer
        TextView dealerMoneyTV = (TextView)findViewById(R.id.dealerMoneyTV);
        CardView dealerCard = (CardView)findViewById(R.id.dealerCard);
        ImageView dCard0 = (ImageView)findViewById(R.id.dCard0);
        ImageView dCard1 = (ImageView)findViewById(R.id.dCard1);
        ImageView dCard2 = (ImageView)findViewById(R.id.dCard2);
        ImageView dCard3 = (ImageView)findViewById(R.id.dCard3);
        ImageView dCard4 = (ImageView)findViewById(R.id.dCard4);
        ImageView dCard5 = (ImageView)findViewById(R.id.dCard5);
        TextView dealerCardsNumberTV = (TextView)findViewById(R.id.dealerCardsNumberTV);
        Seat dealerSeat = new Seat(null, dealerCard, dCard0, dCard1, dCard2, dCard3, dCard4, dCard5, null, dealerMoneyTV, null, null, null, null, null, null, dealerCardsNumberTV, null, null, null);
        players.add(new Player(this, dealerSeat, 0, "Dealer", 0, false, Player.TYPE_BLACKJACK));

        // Add player
        CardView playerCard = (CardView)findViewById(R.id.playerCard);
        TextView playerNameTV = (TextView)findViewById(R.id.playerNameTV);
        TextView playerMoneyTV = (TextView)findViewById(R.id.playerMoneyTV);
        ImageView playerCard0 = (ImageView)findViewById(R.id.playerCard0);
        ImageView playerCard1 = (ImageView)findViewById(R.id.playerCard1);
        ImageView playerCard2 = (ImageView)findViewById(R.id.playerCard2);
        ImageView playerCard3 = (ImageView)findViewById(R.id.playerCard3);
        ImageView playerCard4 = (ImageView)findViewById(R.id.playerCard4);
        ImageView playerCard5 = (ImageView)findViewById(R.id.playerCard5);
        FrameLayout playerBetFrame = (FrameLayout)findViewById(R.id.playerBetFrame);
        TextView playerTotalBetTV = (TextView)findViewById(R.id.playerTotalBetTV);
        TextView playerTotalNumberTV = (TextView)findViewById(R.id.playerTotalNumberTV);
        ProgressBar playerTimeBar = (ProgressBar)findViewById(R.id.playerTimeBar);
        Seat playerSeat = new Seat(null, playerCard, playerCard0, playerCard1, playerCard2, playerCard3, playerCard4, playerCard5, playerNameTV, playerMoneyTV, playerTimeBar, playerBetFrame, null, playerTotalBetTV, null, null, playerTotalNumberTV, null, null, null);
        players.add(new Player(this, playerSeat, 0, MY_PLAYER_NICKNAME, 10000, false, Player.TYPE_BLACKJACK));

        initializeViews();
    } // End of onCreate();


    // ---------------------------------------------------------------------------------------------

    // Hides all cards
    private void initializeViews() {
        dismissPopups();
        continueBtn.setVisibility(View.GONE);
        skipTimerBtn.setVisibility(View.GONE);
        hideActionButtons(false);
        killThreads(false);
        disableUserButtons();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).removeCardDrawablesBlackjack();
            players.get(i).resetFold(); // For blackjack too
            players.get(i).setPlayerTimeBar(0);
            players.get(i).totalBet = 0;
            players.get(i).hideTotalBetFrame();
            players.get(i).setCardTotalNumber(0);
            players.get(i).cardsCount = 0;
            players.get(i).cards[0] = null;
            players.get(i).cards[1] = null;
            players.get(i).cards[2] = null;
            players.get(i).cards[3] = null;
            players.get(i).cards[4] = null;
            players.get(i).cards[5] = null;
            players.get(i).clearGlowAnimation();
        }
        if (players.get(1).playerMoney <= 0) {
            Toast.makeText(this, GAME_MODE == 0 ? "You run out of money... thank you for playing in our casino!" : "Got caught of calculating cards on casino and got banned (joke)... you run out of money.", Toast.LENGTH_LONG).show();
            BlackJackOffline.this.finish();
        } else {
            initOnClickListeners();
            GAME_STAGE = GAME_STAGE_ONE_BETTING;
            startGame();
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Start method
    private void startGame() {
        shuffleCardDeck();
    }

    // ---------------------------------------------------------------------------------------------

    // Create new shuffled card deck
    private void shuffleCardDeck() {
        Log.i(TAG, String.valueOf(GAME_MODE));
        if (GAME_MODE == 0) { // Normal game
            cardDeck = CardDeckGenerator.shuffledCardDeck();
            deckCard = 0;
        } else {
            if (cardDeck == null) {
                cardDeck = CardDeckGenerator.shuffledCardDeck();
                deckCard = 0;
                lastDeckCount = 0;
            } else {
                if (deckCard > (cardDeck.length-10)) {
                    cardDeck = CardDeckGenerator.shuffledCardDeck();
                    deckCard = 0;
                    lastDeckCount = 0;
                }
            }
        }
        staging();
    }

    // ---------------------------------------------------------------------------------------------


    private void staging() {
        if (!KILL_THREADS) {
            switch (GAME_STAGE) {
                case GAME_STAGE_ONE_BETTING:
                    GAME_STAGE = GAME_STAGE + 1;
                    setCurrentStageText("Stage: Betting");
                    setTutorialWindowText("On betting, player must set bets to be able to continue game.");
                    skipTimerBtn.setVisibility(View.VISIBLE);
                    showBetButtons();
                    bettingRound();
                    break;
                case GAME_STAGE_TWO_GIVE_CARDS:
                    GAME_STAGE = GAME_STAGE + 1;
                    setCurrentStageText("Stage: Give cards");
                    setTutorialWindowText("You get first two cards.");
                    skipTimerBtn.setVisibility(View.GONE);
                    hideBetButtons();
                    disableUserButtons();
                    giveCards();
                    break;
                case GAME_STAGE_THREE_PLAYERS_TURN:
                    GAME_STAGE = GAME_STAGE + 1;
                    setCurrentStageText("Stage: Players turn");
                    setTutorialWindowText("Time for decisions. Following are the possible outcomes and rules:\n" + "• 21 means blackjack and you win right away, with 3:2 payoff.\n" + "• If you go over 21, you lose.\n" + "• Hit as much as you feel comfortable to get your total as close as 21 or 21 blackjack.\n" + "• If you lose with two cards, you get half of your bet back.\n" + "• If dealer gets blackjack, you lose.\n" + "• Dealer must hit if total under 16 and stand on 17\n" + "• Stand if and when you feel comfortable to continue.\n" + "• Surrender with two cards and you get half of your bet back.");
                    showActionButtons();
                    initOnClickListeners();
                    calculateCardsCounts(true); // Does the hand verify process too
                    break;
                case GAME_STAGE_FOUR_DEALERS_TURN:
                    GAME_STAGE = GAME_STAGE + 1;
                    setCurrentStageText("Stage: Dealers turn");
                    setTutorialWindowText("Dealer will hit as long as dealer has total under of 16. Dealer will stand on 17.");
                    hideActionButtons(true);
                    dealersTurn();
                    break;
                case GAME_STAGE_FIVE_THE_RESULTS:
                    setTutorialWindowText("Results...");
                    roundEnd();
                    break;
            }
        }
    }


    // ---------------------------------------------------------------------------------------------


    // Player can set bet here
    private void bettingRound() {
        enableUserButtons();
        turnTime = 0;
        if (playerBettingThread.isAlive()) {
            playerBettingThread.interrupt();
            playerBettingThread = null;
        }
        playerBettingThread = new Thread(playerBettingRunnable);
        threadList.add(playerBettingThread);
        if (!playerBettingThread.isAlive()) {
            playerBettingThread.start();
        }
    }
    Runnable playerBettingRunnable = new Runnable() {
        final int timeout = 10 * 1000;
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    while (turnTime < timeout && !KILL_THREADS) {
                        Thread.sleep(500);
                        players.get(1).setPlayerTimeBar((timeout - turnTime) / 100);
                        turnTime = turnTime + 500;
                    }
                    players.get(1).setPlayerTimeBar(0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (players.get(1).totalBet > 0) {
                                staging();
                            } else {
                                Toast.makeText(BlackJackOffline.this, "No bet was set!", Toast.LENGTH_SHORT).show();
                                initializeViews();
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


    // ---------------------------------------------------------------------------------------------


    // Gives first two cards for player and dealer
    private void giveCards() {
        for (int i = 0; i < players.size(); i++) { // Two cards for both players
            players.get(i).cards[0] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cardsCount = players.get(i).cardsCount + 1;
            players.get(i).cards[1] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(i).cardsCount = players.get(i).cardsCount + 1;
        }
        if (giveCardsThread.isAlive()) {
            giveCardsThread.interrupt();
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
                    final int sleepDuringCardPlace = GAME_MODE == 0 ? 200 : 1000; // ms
                    // ---------------------------------------------------
                    for (int r = 0; r < 2; r++) { // Card
                        for (int c = 0; c < players.size(); c++) { // Player
                            if (!players.get(c).isFold()) {
                                audioPlayer.playCardSlideSix(BlackJackOffline.this);
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
                }
                Thread.currentThread().interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };



    // ---------------------------------------------------------------------------------------------
    // Players turn logic

    private void playerHit() {
        boolean cardGiven = false;
        if (players.get(1).cards[2] == null) {
            players.get(1).cards[2] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            cardGiven = true;
            deckCard = deckCard + 1;
            players.get(1).cardsCount = players.get(1).cardsCount + 1;
            audioPlayer.playCardSlideSix(BlackJackOffline.this);
            setPlayerCardDrawable(1, 2);
        }
        if (players.get(1).cards[3] == null && !cardGiven) {
            players.get(1).cards[3] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            cardGiven = true;
            deckCard = deckCard + 1;
            players.get(1).cardsCount = players.get(1).cardsCount + 1;
            audioPlayer.playCardSlideSix(BlackJackOffline.this);
            setPlayerCardDrawable(1, 3);
        }
        if (players.get(1).cards[4] == null && !cardGiven) {
            players.get(1).cards[4] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
            deckCard = deckCard + 1;
            players.get(1).cardsCount = players.get(1).cardsCount + 1;
            audioPlayer.playCardSlideSix(BlackJackOffline.this);
            setPlayerCardDrawable(1, 4);
        }
        calculateCardsCounts(true);
    }

    private void playerStand() {
        staging();
    }

    private void playerSurrender() {
        if (players.get(1).cardsCount == 2) { // Return half of the money back
            players.get(0).playerMoney = players.get(0).playerMoney + (players.get(1).totalBet / 2);
            players.get(1).playerMoney = players.get(1).playerMoney + (players.get(1).totalBet / 2);
            players.get(1).totalBet = 0;
            players.get(0).setMoneyView();
            players.get(1).setMoneyView();
            players.get(1).setTotalBetView();
            players.get(0).startGlowAnimation();
            roundEnd();
        } else { // Dealer takes it all
            players.get(0).playerMoney = players.get(0).playerMoney + players.get(1).totalBet;
            players.get(1).totalBet = 0;
            players.get(0).setMoneyView();
            players.get(1).setMoneyView();
            players.get(1).setTotalBetView();
            roundEnd();
        }
    }

    // ---------------------------------------------------------------------------------------------

    // If player has 21 = Blackjack, give bet back with 3:2 logic
    private void verifyPlayerHand() {
        if (players.get(1).totalCardNumber > 21) {
            Toast.makeText(this, "You lost with: " + String.valueOf(players.get(1).totalCardNumber), Toast.LENGTH_SHORT).show();
            playerSurrender(); // Player lost
        } else if (players.get(1).totalCardNumber == 21) {
            // Blackjack win!
            int winAmount = players.get(1).totalBet + ((players.get(1).totalBet / 3) * 2);
            players.get(1).playerMoney = players.get(1).playerMoney + players.get(1).totalBet;
            players.get(1).playerMoney = players.get(1).playerMoney + winAmount;
            players.get(1).totalBet = 0;
            players.get(1).setMoneyView();
            players.get(1).setTotalBetView();
            players.get(0).playerMoney = players.get(0).playerMoney - winAmount;
            players.get(0).setMoneyView();
            players.get(0).startGlowAnimation();
            setCurrentStageText("Blackjack!");
            roundEnd();
        }
    }


    // ---------------------------------------------------------------------------------------------

    /* DEALER'S LOGIC RELIES HERE */


    // Gives first two cards for player and dealer
    private void dealersTurn() {
        if (dealersTurnThread.isAlive()) {
            dealersTurnThread.interrupt();
            dealersTurnThread = null;
        }
        dealersTurnThread = new Thread(dealersTurnRunnable);
        threadList.add(dealersTurnThread);
        if (!dealersTurnThread.isAlive()) {
            dealersTurnThread.start();
        }
    }
    Runnable dealersTurnRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (activityRunning) {
                    final int sleepDuringHits = 2000;
                    Thread.sleep(sleepDuringHits);
                    // ---------------------------------------------------
                    audioPlayer.playCardSlideSix(BlackJackOffline.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setPlayerCardDrawable(0, 1); // Show one dealers upside down card
                            verifyDealerHand();
                        }
                    });
                    Thread.sleep(sleepDuringHits);
                    int tookCard = 2; // Important
                    while (players.get(0).totalCardNumber < 16) { // While dealer card count is under 16
                        players.get(0).cards[tookCard] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
                        deckCard = deckCard + 1;
                        audioPlayer.playCardSlideSix(BlackJackOffline.this);
                        final int finalTookCard = tookCard;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setPlayerCardDrawable(0, finalTookCard); // Show third card
                                verifyDealerHand();
                            }
                        });
                        tookCard = tookCard + 1;
                        Thread.sleep(sleepDuringHits);
                    }
                    if (players.get(0).totalCardNumber == 17) { // Dealer got 17, must stay here
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                verifyDealerHand();
                            }
                        });
                    } else {
                        while (players.get(0).totalCardNumber <= players.get(1).totalCardNumber) { // While dealer has shittier hand than player, hit more cards
                            players.get(0).cards[tookCard] = new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
                            deckCard = deckCard + 1;
                            audioPlayer.playCardSlideSix(BlackJackOffline.this);
                            final int finalTookCard = tookCard;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setPlayerCardDrawable(0, finalTookCard); // Show third card
                                    verifyDealerHand();
                                }
                            });
                            tookCard = tookCard + 1;
                            Thread.sleep(sleepDuringHits);
                        }
                    }
                    if (players.get(0).totalCardNumber > players.get(1).totalCardNumber && players.get(0).totalCardNumber < 21) { // Dealer won
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dealerWon();
                            }
                        });
                    }
                    Thread.sleep(sleepDuringHits); // final wait time
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


    private void verifyDealerHand() {
        calculateCardsCounts(false); // Calc new card values
        if (players.get(0).totalCardNumber > 21) { // Dealer lost 1:1 win for player
            Toast.makeText(this, "Dealer lost with " + String.valueOf(players.get(0).totalCardNumber), Toast.LENGTH_SHORT).show();
            dealerSurrender();
        } else if (players.get(1).totalCardNumber == 21) { // Blackjack win!
            Toast.makeText(this, "Dealer got blackjack", Toast.LENGTH_SHORT).show();
            setCurrentStageText("Dealer got blackjack!");
            players.get(0).playerMoney = players.get(0).playerMoney + players.get(1).totalBet;
            players.get(1).totalBet = 0;
            players.get(1).setMoneyView();
            players.get(1).setTotalBetView();
            players.get(0).setMoneyView();
            players.get(0).startGlowAnimation();
        } else if (players.get(0).totalCardNumber == 17) {
            if (players.get(0).totalCardNumber > players.get(1).totalCardNumber) { // Player lost
                playerSurrender();
            } else if(players.get(0).totalCardNumber == players.get(1).totalCardNumber) { // Even, share money
                players.get(0).playerMoney = players.get(0).playerMoney + (players.get(1).totalBet / 2);
                players.get(1).playerMoney = players.get(1).playerMoney + (players.get(1).totalBet / 2);
                players.get(1).totalBet = 0;
                players.get(0).setMoneyView();
                players.get(1).setMoneyView();
                players.get(1).setTotalBetView();
            } else { // Dealer lost
                dealerSurrender();
            }
        }
    }


    private void dealerSurrender() {
        final int pWinAmount = players.get(1).totalBet * 2;
        final int dLoseAmount = pWinAmount / 2;
        players.get(1).playerMoney = players.get(1).playerMoney + pWinAmount;
        players.get(0).playerMoney = players.get(0).playerMoney - dLoseAmount;
        players.get(1).totalBet = 0;
        players.get(0).setMoneyView();
        players.get(1).setMoneyView();
        players.get(1).setTotalBetView();
        players.get(1).startGlowAnimation();
        setCurrentStageText("Player won!");
        roundEnd();
    }


    private void dealerWon() {
        players.get(0).playerMoney = players.get(0).playerMoney + players.get(1).totalBet;
        players.get(1).totalBet = 0;
        players.get(1).setMoneyView();
        players.get(1).setTotalBetView();
        players.get(0).setMoneyView();
        players.get(0).startGlowAnimation();
        setCurrentStageText("Dealer won!");
        roundEnd();
    }


    // ---------------------------------------------------------------------------------------------


    // Handle continuing process
    private void roundEnd() {
        hideActionButtons(false);
        if (AUTO_START_NEW_ROUND) {
            initializeViews();
        } else {
            continueBtn.setVisibility(View.VISIBLE);
        }
    }


    // ---------------------------------------------------------------------------------------------


    // *********************************************************************************************
    // Helpers basically

    private void killThreads(boolean finish) {
        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).interrupt();
        }
        if (finish) {
            KILL_THREADS = true;
            BlackJackOffline.this.finish();
        }
    }


    // Calculates dealers and players cards total count
    private void calculateCardsCounts(boolean isPlayersTurn) {
        for (int i = 0; i < players.size(); i++) {
            int worth = 0;
            if (i == 0) {
                if (isPlayersTurn) {
                    worth = worth + players.get(i).cards[0].getValue();
                } else {
                    worth = worth + players.get(i).cards[0].getValue();
                    worth = worth + players.get(i).cards[1].getValue();
                    if (players.get(i).cards[2] != null) {
                        worth = worth + players.get(i).cards[2].getValue();
                    }
                    if (players.get(i).cards[3] != null) {
                        worth = worth + players.get(i).cards[3].getValue();
                    }
                    if (players.get(i).cards[4] != null) {
                        worth = worth + players.get(i).cards[4].getValue();
                    }
                }
            } else {
                worth = worth + players.get(i).cards[0].getValue();
                worth = worth + players.get(i).cards[1].getValue();
                if (players.get(i).cards[2] != null) {
                    worth = worth + players.get(i).cards[2].getValue();
                }
                if (players.get(i).cards[3] != null) {
                    worth = worth + players.get(i).cards[3].getValue();
                }
                if (players.get(i).cards[4] != null) {
                    worth = worth + players.get(i).cards[4].getValue();
                }
            }
            players.get(i).setCardTotalNumber(worth);
        }
        verifyPlayerHand();
    }


    private void disableUserButtons() {
        tenBtn.setEnabled(false);
        twentyFiveBtn.setEnabled(false);
        oneHundredBtn.setEnabled(false);
        fiveHundredBtn.setEnabled(false);
        allInBtn.setEnabled(false);
    }

    private void enableUserButtons() {
        tenBtn.setEnabled(true);
        twentyFiveBtn.setEnabled(true);
        oneHundredBtn.setEnabled(true);
        fiveHundredBtn.setEnabled(true);
        allInBtn.setEnabled(true);
    }


    private void showBetButtons() {
        //audioPlayer.playCardTakeOutFromPackageOne(this);
        tenBtn.startAnimation(animFadeIn);
        tenBtn.setVisibility(View.VISIBLE);
        twentyFiveBtn.startAnimation(animFadeIn);
        twentyFiveBtn.setVisibility(View.VISIBLE);
        oneHundredBtn.startAnimation(animFadeIn);
        oneHundredBtn.setVisibility(View.VISIBLE);
        fiveHundredBtn.startAnimation(animFadeIn);
        fiveHundredBtn.setVisibility(View.VISIBLE);
        allInBtn.startAnimation(animFadeIn);
        allInBtn.setVisibility(View.VISIBLE);
    }

    private void hideBetButtons() {
        audioPlayer.playCardTakeOutFromPackageOne(this);
        tenBtn.startAnimation(animFadeOut);
        tenBtn.setVisibility(View.INVISIBLE);
        twentyFiveBtn.startAnimation(animFadeOut);
        twentyFiveBtn.setVisibility(View.INVISIBLE);
        oneHundredBtn.startAnimation(animFadeOut);
        oneHundredBtn.setVisibility(View.INVISIBLE);
        fiveHundredBtn.startAnimation(animFadeOut);
        fiveHundredBtn.setVisibility(View.INVISIBLE);
        allInBtn.startAnimation(animFadeOut);
        allInBtn.setVisibility(View.INVISIBLE);
    }

    private void showActionButtons() {
        audioPlayer.playCardTakeOutFromPackageOne(this);
        hitBtn.startAnimation(animFadeIn);
        hitBtn.setVisibility(View.VISIBLE);
        standBtn.startAnimation(animFadeIn);
        standBtn.setVisibility(View.VISIBLE);
        surrenderBtn.startAnimation(animFadeIn);
        surrenderBtn.setVisibility(View.VISIBLE);
    }

    private void hideActionButtons(boolean animate) {
        if (animate) {
            hitBtn.startAnimation(animFadeOut);
            standBtn.startAnimation(animFadeOut);
            surrenderBtn.startAnimation(animFadeOut);
        }
        hitBtn.setVisibility(View.INVISIBLE);
        standBtn.setVisibility(View.INVISIBLE);
        surrenderBtn.setVisibility(View.INVISIBLE);
    }


    // Initialize onClick listener's
    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        // Normal buttons
        hitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerHit();
            }
        });
        standBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerStand();
            }
        });
        surrenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerSurrender();
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
                turnTime = 10 * 1000;
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
                players.get(1).setGamePlayerRaiseBlackJackOffline(BlackJackOffline.this, 10);
            }
        });
        twentyFiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(1).setGamePlayerRaiseBlackJackOffline(BlackJackOffline.this, 25);
            }
        });
        oneHundredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(1).setGamePlayerRaiseBlackJackOffline(BlackJackOffline.this, 100);
            }
        });
        fiveHundredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(1).setGamePlayerRaiseBlackJackOffline(BlackJackOffline.this, 500);
            }
        });
        allInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                players.get(1).setGamePlayerRaiseBlackJackOffline(BlackJackOffline.this, players.get(1).playerMoney);
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


    private void setPlayerCardDrawable(int p, int card) {
        switch (p) {
            case 0: // Dealer
                switch (card) {
                    case 0:
                        players.get(p).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[0].toWords())));
                        players.get(p).playerCard0.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard0 ,players.get(p).cards[0].getValue());
                        setHandCount(players.get(p).cards[0].getValue());
                        break;
                    case 1:
                        players.get(p).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this,
                                GAME_STAGE == GAME_STAGE_TWO_GIVE_CARDS+1 ? "top_red" : players.get(p).cards[1].toWords())));
                        players.get(p).playerCard1.startAnimation(animFadeIn);
                        if (GAME_STAGE > GAME_STAGE_TWO_GIVE_CARDS+1) {
                            showPopup(players.get(p).playerCard1, players.get(p).cards[1].getValue());
                            setHandCount(players.get(p).cards[1].getValue());
                        }
                        break;
                    case 2:
                        players.get(p).playerCard2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[2].toWords())));
                        players.get(p).playerCard2.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard2 ,players.get(p).cards[2].getValue());
                        setHandCount(players.get(p).cards[2].getValue());
                        break;
                    case 3:
                        players.get(p).playerCard3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[3].toWords())));
                        players.get(p).playerCard3.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard3 ,players.get(p).cards[3].getValue());
                        setHandCount(players.get(p).cards[3].getValue());
                        break;
                    case 4:
                        players.get(p).playerCard4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[4].toWords())));
                        players.get(p).playerCard4.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard4 ,players.get(p).cards[4].getValue());
                        setHandCount(players.get(p).cards[4].getValue());
                        break;
                }
                break;
            case 1: // Player
                switch (card) {
                    case 0:
                        players.get(p).playerCard0.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[0].toWords())));
                        players.get(p).playerCard0.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard0 ,players.get(p).cards[0].getValue());
                        setHandCount(players.get(p).cards[0].getValue());
                        break;
                    case 1:
                        players.get(p).playerCard1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[1].toWords())));
                        players.get(p).playerCard1.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard1 ,players.get(p).cards[1].getValue());
                        setHandCount(players.get(p).cards[1].getValue());
                        break;
                    case 2:
                        players.get(p).playerCard2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[2].toWords())));
                        players.get(p).playerCard2.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard2 ,players.get(p).cards[2].getValue());
                        setHandCount(players.get(p).cards[2].getValue());
                        break;
                    case 3:
                        players.get(p).playerCard3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[3].toWords())));
                        players.get(p).playerCard3.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard3 ,players.get(p).cards[3].getValue());
                        setHandCount(players.get(p).cards[3].getValue());
                        break;
                    case 4:
                        players.get(p).playerCard4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, players.get(p).cards[4].toWords())));
                        players.get(p).playerCard4.startAnimation(animFadeIn);
                        showPopup(players.get(p).playerCard4 ,players.get(p).cards[4].getValue());
                        setHandCount(players.get(p).cards[4].getValue());
                        break;
                }
                break;
        }
    }


    private void setCurrentStageText(String stage) {
        currentStageTV.setText(stage);
    }


    private void setTutorialWindowText(String text) {
        tutorialTV.setText(text);
    }


    private void setHandCount(int cardValue) {
        Log.i(TAG, "-----------------  " + String.valueOf(cardValue));
        final int count = handCountLogic(cardValue);
        lastDeckCount = lastDeckCount + count;
        if (GAME_MODE == 1) { // Counting cards mode
            handCountingTV.setVisibility(View.VISIBLE);
            handCountingTV.setText(lastDeckCount < 0 ? "" : "+" + String.valueOf(lastDeckCount));
        }
    }
    private static int handCountLogic(int cardValue) {
        if (cardValue <= 6) {
            return  1;
        } else if (cardValue <= 7 && cardValue <= 9) {
            return 0;
        } else if (cardValue > 9) {
            return -1;
        }
        return  0;
    }


    // Show +1 +0 or -1 pop up (card calculating)
    private void showPopup(ImageView imageView, int handValue) {
        if (GAME_MODE == 1) {
            popUpCard = new RelativePopUpCard(this, R.layout.count_popup);
            relativePopupArrayList.add(popUpCard);
            popUpCard.showOnAnchor(imageView, RelativePopup.VerticalPosition.CENTER, RelativePopup.HorizontalPosition.CENTER, 0, 0, handCountLogic(handValue));
        }
    }

    private void dismissPopups() {
        for (int i = 0; i < relativePopupArrayList.size(); i++) {
            relativePopupArrayList.get(i).dismiss();
        }
    }


    // *********************************************************************************************

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
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
        new AlertDialog.Builder(BlackJackOffline.this)
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

    // *********************************************************************************************

} 