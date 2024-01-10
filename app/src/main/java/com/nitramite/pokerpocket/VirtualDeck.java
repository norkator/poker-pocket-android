package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nitramite.cardlogic.Card;
import com.nitramite.cardlogic.CardDeckGenerator;
import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.GameCardResource;

import java.util.ArrayList;

public class VirtualDeck extends AppCompatActivity {

    // https://stackoverflow.com/questions/14173012/android-layout-views-rotated-and-spaced-around-a-circle

    // Logging
    private static final String TAG = VirtualDeck.class.getSimpleName();

    // Variables
    private int cardsOnView = 0;
    private int playersCount = 4;
    private int gameHandSize = 5;
    private int cardWidth = 30;
    private int cardHeight = 40;

    // Activity components
    private AudioPlayer audioPlayer = new AudioPlayer();
    private FrameLayout deckFrame;
    private LinearLayout dealBtn, hideCardsBtn, showAllBtn, startOverBtn;

    // Card deck related
    private Card[] cardDeck;
    private int deckCard = 0;
    private GameCardResource gameCardResource;

    // Animations
    private Animation animFadeIn, animFadeOut;

    // Cards to be turned
    private ArrayList<Cards> cards = new ArrayList<>();

    // Commons cards
    private LinearLayout dealCommonBtn;
    private ImageView commonCard1, commonCard2, commonCard3, commonCard4, commonCard5;
    private ArrayList<ImageView> commonCards = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_deck);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Find components
        deckFrame = (FrameLayout) findViewById(R.id.deckFrame);
        dealBtn = (LinearLayout) findViewById(R.id.dealBtn);
        hideCardsBtn = (LinearLayout) findViewById(R.id.hideCardsBtn);
        showAllBtn = (LinearLayout) findViewById(R.id.showAllBtn);
        startOverBtn = (LinearLayout) findViewById(R.id.startOverBtn);
        dealCommonBtn = (LinearLayout) findViewById(R.id.dealCommonBtn);
        commonCard1 = (ImageView) findViewById(R.id.commonCard1);
        commonCards.add(commonCard1);
        commonCard2 = (ImageView) findViewById(R.id.commonCard2);
        commonCards.add(commonCard2);
        commonCard3 = (ImageView) findViewById(R.id.commonCard3);
        commonCards.add(commonCard3);
        commonCard4 = (ImageView) findViewById(R.id.commonCard4);
        commonCards.add(commonCard4);
        commonCard5 = (ImageView) findViewById(R.id.commonCard5);
        commonCards.add(commonCard5);

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_virtual_deck);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_virtual_deck);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Create new shuffled card deck
        cardDeck = CardDeckGenerator.shuffledCardDeck();

        // Get settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        playersCount = sharedPreferences.getInt(Constants.SP_VIRTUAL_DECK_PLAYERS_COUNT, 4);
        gameHandSize = sharedPreferences.getInt(Constants.SP_VIRTUAL_DECK_PLAYER_HAND_SIZE, 5);


        dealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCard();
            }
        });
        hideCardsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideCardsBtn.setVisibility(View.GONE);
                dealBtn.setVisibility(View.VISIBLE);
                dealBtn.startAnimation(animFadeIn);
                addCard();
            }
        });
        showAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < cards.size(); i++) {
                    cards.get(i).showCard();
                }
                audioPlayer.playCardChipLayOne(VirtualDeck.this);
                hideCardsBtn.setVisibility(View.GONE);
                dealBtn.setVisibility(View.GONE);
                showAllBtn.setVisibility(View.GONE);
                startOverBtn.setVisibility(View.VISIBLE);
                startOverBtn.startAnimation(animFadeIn);
            }
        });
        startOverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tuningStartOverRunnable();
            }
        });
        dealCommonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyNextCommonCardClick();
            }
        });
    } // End of onCreate();


    // Initializes everything to start over
    private void initialize() {
        cardsOnView = 0;
        cards = new ArrayList<>();
        dealBtn.setVisibility(View.VISIBLE);
        hideCardsBtn.setVisibility(View.GONE);
        showAllBtn.setVisibility(View.GONE);
        startOverBtn.setVisibility(View.GONE);
        clearCommonCards();
    }


    // This uses view height as reference to calculate cards sizes
    private void calculateCardSize() {
        final int frameHeight = deckFrame.getHeight();
        final int centerBtnHeight = dealBtn.getHeight();
        cardHeight = (((toDpi(frameHeight) - toDpi(centerBtnHeight)) / toDpi(2)) - toDpi(10));
        cardWidth = cardHeight - toDpi(10);
    }


    // Add card into circle
    private void addCard() {
        calculateCardSize();
        if (cardsOnView != (playersCount * gameHandSize) + (playersCount - 1)) {
            final int cWidthToDpi = cardWidth;//toDpi(cardWidth);
            final int cHeightToDpi = cardHeight;//toDpi(cardHeight);
            cards.add(new Cards(false));
            cards.get(cardsOnView).originalCard = new ImageView(this);
            cards.get(cardsOnView).currentCard = new ImageView(this);
            int x = cardsOnView + 1;
            if (x % (gameHandSize + 1) != 0) {
                cards.get(cardsOnView).setBackGrounds(ContextCompat.getDrawable(this,
                        gameCardResource.cardResourceId(this, new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank()).toWords())
                ));
                cards.get(cardsOnView).currentCard.startAnimation(animFadeIn);
                audioPlayer.playCardSlideSix(this);
                deckCard = deckCard + 1;
            } else {
                for (int i = 0; i < cards.size(); i++) {
                    cards.get(i).hideCard();
                }
                audioPlayer.playCardTakeOutFromPackageOne(this);
            }
            if (x % (gameHandSize + 1) == gameHandSize) {
                hideCardsBtn.setVisibility(View.VISIBLE);
                hideCardsBtn.startAnimation(animFadeIn);
                dealBtn.setVisibility(View.GONE);
            }

            Log.i(TAG, String.valueOf(cardsOnView % gameHandSize));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(cWidthToDpi, cHeightToDpi);
            lp.gravity = Gravity.CENTER;
            cards.get(cardsOnView).setLayoutParams(lp);

            // Calculate the angle of the current view. Adjust by 90 degrees to
            // get View 0 at the top. We need the angle in degrees and radians.
            float angleDeg = cardsOnView * 360.0f / ((playersCount * gameHandSize) + playersCount) - getAngleOffset();
            float angleRad = (float) (angleDeg * Math.PI / 180.0f);

            // Calculate the position of the view, offset from center (300 px from
            // center). Again, this should be done in a display size independent way.
            final int cRadius = dealBtn.getWidth() / 2;
            final int cHeightRadius = cHeightToDpi / 2;
            cards.get(cardsOnView).setTranslationX((cRadius + cHeightRadius + 5) * (float) Math.cos(angleRad));
            cards.get(cardsOnView).setTranslationY((cRadius + cHeightRadius + 5) * (float) Math.sin(angleRad));
            // Set the rotation of the view.
            cards.get(cardsOnView).setRotation(angleDeg + 90.0f);
            deckFrame.addView(cards.get(cardsOnView).currentCard);
            cardsOnView = cardsOnView + 1;
        } else {
            for (int i = 0; i < cards.size(); i++) {
                cards.get(i).hideCard();
            }
            audioPlayer.playCardTakeOutFromPackageOne(this);
            showAllBtn.setVisibility(View.VISIBLE);
            hideCardsBtn.setVisibility(View.GONE);
            dealBtn.setVisibility(View.GONE);
        }
    }


    // Starting offset
    private float getAngleOffset() {
        switch (gameHandSize) {
            case 5:
                return 120.0f;
            case 7:
                return 130.0f;
            default:
                return 90.0f;
        }
    }


    // Card dpi
    private int toDpi(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // ---------------------------------------------------------------------------------------------


    private class Cards {

        boolean hidden = false;
        boolean isSeparator = true;
        ImageView originalCard = null;
        ImageView currentCard = null;

        Cards(final boolean hidden_) {
            this.hidden = hidden_;
        }

        void setBackGrounds(final Drawable drawable) {
            this.originalCard.setBackground(drawable);
            this.currentCard.setBackground(drawable);
            this.isSeparator = false;
        }

        void setLayoutParams(final FrameLayout.LayoutParams lp) {
            this.originalCard.setLayoutParams(lp);
            this.currentCard.setLayoutParams(lp);
        }

        void setTranslationX(final float X) {
            this.originalCard.setTranslationX(X);
            this.currentCard.setTranslationX(X);
        }

        void setTranslationY(final float Y) {
            this.originalCard.setTranslationY(Y);
            this.currentCard.setTranslationY(Y);
        }

        void setRotation(final float rotation) {
            this.originalCard.setRotation(rotation);
            this.currentCard.setRotation(rotation);
        }

        void hideCard() {
            if (!this.isSeparator) {
                this.currentCard.setBackground(ContextCompat.getDrawable(VirtualDeck.this, gameCardResource.cardResourceId(VirtualDeck.this, "top_red")));
            }
        }

        void showCard() {
            this.currentCard.setBackground(this.originalCard.getBackground());
        }

        void clearCard() {
            this.currentCard.setBackground(null);
            this.originalCard.setBackground(null);
        }

    }

    // ---------------------------------------------------------------------------------------------

    private void clearCommonCards() {
        for (int i = 0; i < this.commonCards.size(); i++) {
            this.commonCards.get(i).setBackground(null);
        }
    }

    private void verifyNextCommonCardClick() {
        new AlertDialog.Builder(VirtualDeck.this)
                .setTitle("Common card")
                .setMessage("Show next common card?")
                .setPositiveButton("Yes", (dialog, which) -> setNextCommonCard())
                .setNegativeButton("Return", (dialog, which) -> {
                    // Return
                })
                .setIcon(R.mipmap.logo)
                .show();
    }

    private void setNextCommonCard() {
        for (int i = 0; i < this.commonCards.size(); i++) {
            if (this.commonCards.get(i).getBackground() == null) {
                this.commonCards.get(i).setBackground(ContextCompat.getDrawable(this,
                        gameCardResource.cardResourceId(this, new CardVisual(cardDeck[deckCard].suite(), cardDeck[deckCard].rank()).toWords())
                ));
                this.commonCards.get(i).startAnimation(animFadeIn);
                audioPlayer.playCardSlideSix(this);
                deckCard = deckCard + 1;
                i = this.commonCards.size();
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void tuningStartOverRunnable() {
        /*
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < cards.size(); i++) {
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!cards.get(finalI).isSeparator) {
                                    audioPlayer.playCardPlaceChipsOne(VirtualDeck.this);
                                    cards.get(finalI).originalCard.startAnimation(animFadeIn);
                                    cards.get(finalI).currentCard.startAnimation(animFadeIn);
                                }
                            }
                        });
                        Thread.sleep(200);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cards.get(finalI).clearCard();
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initialize();
                        }
                    });
                    Thread.currentThread().interrupt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
        */
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).clearCard();
        }
        initialize();
        audioPlayer.playCardChipLayOne(VirtualDeck.this);
    }


    // ---------------------------------------------------------------------------------------------

} 
