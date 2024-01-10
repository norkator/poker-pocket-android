package com.nitramite.dynamic;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.CardVisualOnline;
import com.nitramite.pokerpocket.AudioPlayer;
import com.nitramite.pokerpocket.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.NumberFormat;

// This class contains player object
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "WeakerAccess"})
public class Player {

    // Logging
    private final static String TAG = "Player";

    // Player states
    public static int PLAYER_STATE_NONE = 0;
    public static int PLAYER_STATE_FOLD = 1;
    public static int PLAYER_STATE_CHECK = 2;
    public static int PLAYER_STATE_RAISE = 3;

    // Specifies player hand size
    public static final int TYPE_SEVEN = 0;
    public static final int TYPE_HOLDEM = 7; // 7 needed for hand + middle cards
    public static final int TYPE_FIVE_CARD_DRAW = 5;
    public static final int TYPE_BLACKJACK = 6; // 30.01.2018, added one more card to blackjack
    public static final int TYPE_HOLDEM_ONLINE = 2;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private AudioPlayer audioPlayer = new AudioPlayer();
    private Animation animFadeOut;
    private Animation animFadeInUserAction;

    // Game type
    private int gameType;

    // Online related
    public int playerId; // a.k.a. connectionId

    // Stack of variables
    public Seat playerSeat;
    public int playerMoney;
    public int totalBet = 0;
    public int tempBet = 0; // For actual user
    public boolean isBot = false;
    public boolean isRemoved = false;
    public int playerState = PLAYER_STATE_NONE;
    private boolean isFold = false;
    public boolean isPlayerTurn = false;
    private CardView playerCard;
    private String playerName;
    private TextView playerNameTV;
    private TextView playerMoneyTV;
    private TextView playerWinsCountTV;
    private ProgressBar playerTimeBar;
    public ImageView playerCard0;
    public ImageView playerCard1;
    public ImageView playerCard2;
    public ImageView playerCard3;
    public ImageView playerCard4;
    public ImageView playerCard5;
    public CardVisual cards[];
    public CardVisualOnline cardsOnline[];
    public double handValue = 0;
    public String handValueText = "";
    private FrameLayout seatBetFrame;
    private TextView totalBetTV;
    private TextView playerLastActionTV;
    private ImageView dealerChipIV;
    public Boolean isDealer;
    public boolean isAllIn = false;
    public boolean hasRaised = false;
    public int winsCount = 0; // Mainly for 5-card draw but also is used @ texas holdem for end results
    private TextView totalCardNumberTV;
    public int totalCardNumber = 0; // For blackjack
    public int cardsCount = 0; // For blackjack shows how many cards player has
    public boolean roundPlayed = false; // Used to check if player has interacted @ round

    // Constructor
    public Player(Context context, Seat playerSeat, int playerId_, String playerName_, int playerMoney_, Boolean isDealer_, int gameTypeHandSize) {
        this.playerSeat = playerSeat;
        this.playerId = playerId_;
        this.playerName = playerName_;
        this.playerMoney = playerMoney_;
        this.playerNameTV = playerSeat.seatNameTV;
        this.playerMoneyTV = playerSeat.seatMoneyTV;
        this.playerWinsCountTV = playerSeat.seatWinsCountTV;
        this.totalCardNumberTV = playerSeat.seatCardsNumberTV;
        this.playerTimeBar = playerSeat.seatTimeBar;
        this.playerCard0 = playerSeat.seatCard0;
        this.playerCard1 = playerSeat.seatCard1;
        this.playerCard2 = playerSeat.seatCard2;
        this.playerCard3 = playerSeat.seatCard3;
        this.playerCard4 = playerSeat.seatCard4;
        this.playerCard5 = playerSeat.seatCard5;
        this.setMoneyView();
        this.setName(playerName);
        this.seatBetFrame = playerSeat.seatBetFrame;
        this.totalBetTV = playerSeat.seatTotalBetTV;
        this.playerLastActionTV = playerSeat.seatLastUserActionTV;
        this.dealerChipIV = playerSeat.seatDealerChipIV;
        this.isDealer = isDealer_;
        if (gameTypeHandSize == TYPE_HOLDEM_ONLINE) { // Online holdem
            this.cardsOnline = new CardVisualOnline[gameTypeHandSize];
        } else {
            this.cards = new CardVisual[gameTypeHandSize];
        }
        this.gameType = gameTypeHandSize;
        this.animFadeOut = AnimationUtils.loadAnimation(context, R.anim.anim_fade_out_holdem);
        this.animFadeInUserAction = AnimationUtils.loadAnimation(context, R.anim.anim_fade_user_action);
        this.playerCard = playerSeat.seatCard;
        this.setDefaults();
    }


    // Set default player object variables
    private void setDefaults() {
        if (this.dealerChipIV != null) {
            if (this.isDealer) {
                dealerChipIV.setVisibility(View.VISIBLE);
            } else {
                dealerChipIV.setVisibility(View.GONE);
            }
        }
    }


    // ---------------------------------------------------------------------------------------------

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public void setPlayerTurn(boolean isPlayerTurn_) {
        isPlayerTurn = isPlayerTurn_;
    }


    public boolean isFold() {
        return isFold;
    }

    public void resetFold() {
        isFold = false;
    }


    public void setCheck(Context context) {
        this.roundPlayed = true;
        this.playerState = PLAYER_STATE_CHECK;
        audioPlayer.playCardPlaceChipsOne(context); // Sound
        setPlayerTimeBar(0);
        this.isPlayerTurn = false;
        this.setMoneyView();
        this.setTotalBetView();
    }


    public void setRaise(Context context) {
        this.roundPlayed = true;
        this.playerState = PLAYER_STATE_RAISE;
        audioPlayer.playCardPlaceChipsOne(context); // Sound
        this.setPlayerTimeBar(0);
        this.isPlayerTurn = false;
        this.setMoneyView();
        this.setTotalBetView();
    }


    // Must run actual raise logic too
    public void setGamePlayerRaiseHoldemOffline(Context context, int amount) {
        tempBet = tempBet + amount;
        this.totalBet = this.totalBet + amount;
        this.setTotalBetView();
        this.setMoneyView();
        audioPlayer.playCardPlaceChipsOne(context); // Sound
    }


    // Must run actual raise logic too
    public void setGamePlayerRaiseBlackJackOffline(Context context, int amount) {
        tempBet = amount;
        if (amount == this.playerMoney) {
            this.isAllIn = true;
        }
        if (this.playerMoney > amount - 1) {
            this.totalBet = this.totalBet + amount;
            this.playerMoney = this.playerMoney - amount;
            this.setTotalBetView();
            this.setMoneyView();
            audioPlayer.playCardPlaceChipsOne(context); // Sound
        }
    }

    public void setFold() {
        this.roundPlayed = true;
        this.playerState = PLAYER_STATE_FOLD;
        this.tempBet = 0;
        this.setPlayerTimeBar(0);
        this.setPlayerTurn(false);
        isFold = true;
        playerCard0.setBackground(null);
        playerCard1.setBackground(null);
    }


    // ---------------------------------------------------------------------------------------------


    public void removeCardDrawablesHoldem() {
        playerCard0.setBackground(null);
        playerCard1.setBackground(null);
    }

    public void removeCardDrawablesFiveCardDraw() {
        playerCard0.setBackground(null);
        playerCard1.setBackground(null);
        playerCard2.setBackground(null);
        playerCard3.setBackground(null);
        playerCard4.setBackground(null);
    }

    public void removeCardDrawablesBlackjack() {
        playerCard0.setBackground(null);
        playerCard1.setBackground(null);
        playerCard2.setBackground(null);
        playerCard3.setBackground(null);
        playerCard4.setBackground(null);
        playerCard5.setBackground(null);
    }


    public String getName() {
        return playerName;
    }

    public void setName(String name) {
        playerName = name;
        if (playerNameTV != null) {
            playerNameTV.setText(playerName);
        }
    }

    public void setMoneyView() {
        if (playerMoneyTV != null) {
            playerMoneyTV.setText(currencyFormat.format(playerMoney));
        }
    }

    // Using money view in seven of clubs game as a last action view
    public void setSevenPlayerActionTextView(final String actionStr) {
        if (playerMoneyTV != null) {
            playerMoneyTV.setText(actionStr);
        }
    }

    public void setTotalBetView() {
        if (this.totalBet > 0) {
            this.seatBetFrame.setVisibility(View.VISIBLE);
            final String totalBetStr = "+" + String.valueOf(totalBet);
            totalBetTV.setText(totalBetStr);
        } else {
            this.hideTotalBetFrame();
        }
    }

    public void setSevenCardsLeftView(final String cardsLeft) {
        if (this.totalBetTV != null) {
            final String cardsLeftStr = cardsLeft + " left";
            totalBetTV.setText(cardsLeftStr);
        }
    }

    public void setPlayerTimeBar(int progress) {
        if (playerTimeBar != null) {
            if (playerTimeBar.getVisibility() == View.INVISIBLE) {
                playerTimeBar.setVisibility(View.VISIBLE);
            }
            playerTimeBar.setProgress(progress);
        }
    }

    public void hidePlayerTimeBar() {
        if (playerTimeBar != null) {
            playerTimeBar.setVisibility(View.INVISIBLE);
        }
    }

    public void hideTotalBetFrame() {
        if (this.seatBetFrame != null) {
            this.seatBetFrame.setVisibility(View.GONE);
        }
    }

    public void transferMoneyAnimation(Context context) {
        if (!this.isFold()) {
            this.seatBetFrame.startAnimation(animFadeOut);
            this.audioPlayer.playChipsHandleFive(context);
            this.seatBetFrame.setVisibility(View.GONE);
        }
    }

    public void incrementWinsCount() {
        if (playerWinsCountTV != null) {
            winsCount = winsCount + 1;
            final String winsCountStr = "Wins: " + String.valueOf(winsCount);
            playerWinsCountTV.setText(winsCountStr);
        }
    }

    public void setCardTotalNumber(int totalNumber) {
        if (totalCardNumberTV != null) {
            totalCardNumber = totalNumber;
            totalCardNumberTV.setText(String.valueOf(totalCardNumber));
        }
    }

    public void startGlowAnimation() {
        if (playerCard != null) {
            AlphaAnimation blinkAnimation = new AlphaAnimation(1, 0);
            blinkAnimation.setDuration(300);
            blinkAnimation.setInterpolator(new LinearInterpolator());
            blinkAnimation.setRepeatCount(20);
            blinkAnimation.setRepeatMode(Animation.REVERSE);
            playerCard.startAnimation(blinkAnimation);
        }
    }

    public void clearGlowAnimation() {
        if (playerCard != null) {
            playerCard.clearAnimation();
        }
    }


    public void hidePlayerLastActionTV() {
        if (this.playerLastActionTV != null) {
            playerLastActionTV.setVisibility(View.GONE);
        }
    }

    public void setPlayerLastActionText(final String actionStr) {
        if (this.playerLastActionTV != null && this.animFadeInUserAction != null) {
            if (actionStr.equals("CHECK") && this.gameType == TYPE_HOLDEM_ONLINE) {
                if (this.playerSeat.seatChipsIV != null) {
                    this.playerSeat.seatChipsIV.setVisibility(View.INVISIBLE);
                }
                this.totalBetTV.setVisibility(View.GONE);
                this.seatBetFrame.setVisibility(View.VISIBLE);
            }
            playerLastActionTV.setVisibility(View.VISIBLE);
            playerLastActionTV.setText(actionStr);
            playerLastActionTV.startAnimation(animFadeInUserAction);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(2000);
                        playerLastActionTV.post(new Runnable() {
                            @Override
                            public void run() {
                                playerLastActionTV.setVisibility(View.GONE);
                                if (actionStr.equals("CHECK") && gameType == TYPE_HOLDEM_ONLINE) {
                                    seatBetFrame.setVisibility(View.GONE);
                                    totalBetTV.setVisibility(View.VISIBLE);
                                    if (playerSeat.seatChipsIV != null) {
                                        playerSeat.seatChipsIV.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    // Set's player as dealer player
    public void setPlayerAsDealer() {
        if (this.dealerChipIV != null) {
            if (this.isDealer) {
                dealerChipIV.setVisibility(View.VISIBLE);
            }
        }
    }


    // Player is not dealer, unset
    public void unsetPlayerAsDealer() {
        if (this.dealerChipIV != null) {
            if (!this.isDealer) {
                dealerChipIV.setVisibility(View.GONE);
            }
        }
    }


    public void startWinnerCardsBounceAnimation(JSONArray winnerCardsArray) throws JSONException {
        for (int c = 0; c < winnerCardsArray.length(); c++) {
            for (int i = 0; i < this.cardsOnline.length; i++) {
                try {
                    if (this.cardsOnline[i].getCardStr().equals(winnerCardsArray.getString(c))) {
                        TranslateAnimation bounceAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, -20.0f);
                        bounceAnimation.setDuration(1500);
                        bounceAnimation.setFillAfter(false);
                        bounceAnimation.setRepeatCount(20);
                        bounceAnimation.setRepeatMode(Animation.REVERSE);
                        switch (i) {
                            case 0:
                                this.playerCard0.startAnimation(bounceAnimation);
                                break;
                            case 1:
                                this.playerCard1.startAnimation(bounceAnimation);
                                break;
                        }
                    }
                } catch (NullPointerException e) {
                    Log.i(TAG, e.toString());
                }
            }
        }
    }

    public void clearWinnerCardsBounceAnimation() {
        if (playerCard0 != null) {
            playerCard0.clearAnimation();
        }
        if (playerCard1 != null) {
            playerCard1.clearAnimation();
        }
    }

    // ---------------------------------------------------------------------------------------------

} 