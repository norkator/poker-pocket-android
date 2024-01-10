package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nitramite.cardlogic.Card;
import com.nitramite.cardlogic.CardDeckGenerator;
import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.CardVisualOnline;
import com.nitramite.cardlogic.CardVisualSquare;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.cardlogic.HandEvaluatorFiveSquare;

import java.util.ArrayList;

public class SquareSolitaire extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {

    // Logging
    private final static String TAG = SquareSolitaire.class.getSimpleName();

    // Activity features
    private ImageView deckCardIV; // Deck top card
    private GameCardResource gameCardResource;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private HandEvaluatorFiveSquare handEvaluatorFiveSquare;
    private ArrayList<ImageView> squareSlots = new ArrayList<>();
    private ArrayList<Integer> validatedLines = new ArrayList<>(); // Firstly horizontal and then vertically!
    private TextView lastResultTV;
    private TextView scoreTV, highScoreTV;
    private Integer score = 0;
    private Integer highScore = 0;

    // Shuffled card deck
    private Card[] cardDeck;
    private int deckCard = 0; // Current taken card


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_square_solitaire);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Find views
        scoreTV = (TextView) findViewById(R.id.scoreTV);
        highScoreTV = (TextView) findViewById(R.id.highScoreTV);
        lastResultTV = (TextView) findViewById(R.id.lastResultTV);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Get high score
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        highScore = sharedPreferences.getInt(Constants.SP_SQUARE_SOLITAIRE_HIGH_SCORE, 0);
        highScoreTV.setText(String.valueOf(highScore));

        generateDeck();
    } // End of onCreate()


    // Shuffles new deck
    private void generateDeck() {
        cardDeck = CardDeckGenerator.shuffledCardDeck();
        deckCard = 0;
        initCardSquare();
    }


    // Give next card from deck
    private void nextDeckCard() {
        final String card = CardVisualSquare.deckCardToString(cardDeck[deckCard].suite(), cardDeck[deckCard].rank());
        deckCard = deckCard + 1;
        deckCardIV.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, new CardVisualOnline(card).toWords())));
        deckCardIV.setTag(card);
        audioPlayer.playCardSlideSix(this);
    }


    // Initialized views and listeners
    private void initCardSquare() {
        // Deck
        deckCardIV = (ImageView) findViewById(R.id.deckCardIV);
        deckCardIV.setOnTouchListener(this);
        deckCardIV.setOnDragListener(this);

        // Dismiss cards views
        ImageView dismissCardOne = (ImageView) findViewById(R.id.dismissCardOne);
        dismissCardOne.setOnDragListener(this);
        ImageView dismissCardTwo = (ImageView) findViewById(R.id.dismissCardTwo);
        dismissCardTwo.setOnDragListener(this);
        ImageView dismissCardThree = (ImageView) findViewById(R.id.dismissCardThree);
        dismissCardThree.setOnDragListener(this);
        ImageView dismissCardFour = (ImageView) findViewById(R.id.dismissCardFour);
        dismissCardFour.setOnDragListener(this);

        // Square slots
        Integer[] imageViews = {
                R.id.rowOneCardOne, R.id.rowOneCardTwo, R.id.rowOneCardThree, R.id.rowOneCardFour, R.id.rowOneCardFive,
                R.id.rowTwoCardOne, R.id.rowTwoCardTwo, R.id.rowTwoCardThree, R.id.rowTwoCardFour, R.id.rowTwoCardFive,
                R.id.rowThreeCardOne, R.id.rowThreeCardTwo, R.id.rowThreeCardThree, R.id.rowThreeCardFour,
                R.id.rowThreeCardFive, R.id.rowFourCardOne, R.id.rowFourCardTwo, R.id.rowFourCardThree,
                R.id.rowFourCardFour, R.id.rowFourCardFive, R.id.rowFiveCardOne, R.id.rowFiveCardTwo,
                R.id.rowFiveCardThree, R.id.rowFiveCardFour, R.id.rowFiveCardFive
        };
        for (int i = 0; i < imageViews.length; i++) {
            squareSlots.add((ImageView) findViewById(imageViews[i]));
            squareSlots.get(i).setOnDragListener(this);
        }

        // Get first card
        nextDeckCard();
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDrag(View targetView, DragEvent dragEvent) {
        int action = dragEvent.getAction();
        View deckCardView = (View) dragEvent.getLocalState();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                break;
            case DragEvent.ACTION_DROP:
                ImageView slot = (ImageView) targetView;
                slot.setBackground(deckCardView.getBackground());
                slot.setTag(deckCardView.getTag());
                slot.setOnDragListener(null);
                deckCardView.setVisibility(View.VISIBLE);
                nextDeckCard();
                squareValidator();
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                if (dropEventNotHandled(dragEvent)) {
                    deckCardView.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean dropEventNotHandled(DragEvent dragEvent) {
        return !dragEvent.getResult();
    }


    // ---------------------------------------------------------------------------------------------


    // Validates available rows and cols, keeps track of them
    private void squareValidator() {
        // Horizontal lines from top to bottom
        score = score + validateSquareGroup(1, R.id.rowOne, new Integer[]{0, 1, 2, 3, 4});
        score = score + validateSquareGroup(2, R.id.rowTwo, new Integer[]{5, 6, 7, 8, 9});
        score = score + validateSquareGroup(3, R.id.rowThree, new Integer[]{10, 11, 12, 13, 14});
        score = score + validateSquareGroup(4, R.id.rowFour, new Integer[]{15, 16, 17, 18, 19});
        score = score + validateSquareGroup(5, R.id.rowFive, new Integer[]{20, 21, 22, 23, 24});
        // Vertical lines from left to right
        score = score + validateSquareGroup(6, R.id.rowSix, new Integer[]{0, 5, 10, 15, 20});
        score = score + validateSquareGroup(7, R.id.rowSeven, new Integer[]{1, 6, 11, 16, 21});
        score = score + validateSquareGroup(8, R.id.rowEight, new Integer[]{2, 7, 12, 17, 22});
        score = score + validateSquareGroup(9, R.id.rowNine, new Integer[]{3, 8, 13, 18, 23});
        score = score + validateSquareGroup(10, R.id.rowTen, new Integer[]{4, 9, 14, 19, 24});
        // Set score
        setScoreView();
        checkHighScore();
        // Check for end result
        if (validatedLines.size() == 10) {
            gameEndedDialog();
        }
    }


    // Validates wanted cards from grid
    private Integer validateSquareGroup(final Integer lineNumber, final int rowScoreResourceId, final Integer[] card_positions) {
        if (!validatedLines.contains(lineNumber)) {
            boolean valid = true;
            CardVisual gridLineCards[] = new CardVisual[5];
            for (int i = 0; i < card_positions.length; i++) {
                gridLineCards[i] = new CardVisual(
                        CardVisualSquare.suitToDeckNumber(String.valueOf(squareSlots.get(card_positions[i]).getTag())),
                        CardVisualSquare.valueToDeckNumber(String.valueOf(squareSlots.get(card_positions[i]).getTag()))
                );
                if (gridLineCards[i].getSuit() == -1 || gridLineCards[i].getRank() == -1) {
                    valid = false;
                }
            }
            if (valid) {
                handEvaluatorFiveSquare = new HandEvaluatorFiveSquare(gridLineCards);
                validatedLines.add(lineNumber);
                final int scoreValue = scoreForValue(handEvaluatorFiveSquare.getPokerHandAsString());
                final TextView lineScoreTV = (TextView) findViewById(rowScoreResourceId);
                lineScoreTV.setText(String.valueOf(scoreValue));
                return scoreValue;
            }
        }
        return 0;
    }


    // Get score for string valued cards
    private int scoreForValue(final String value) {
        if (value.equals("Pair")) {
            lastResultTV.setText(value);
            return 2;
        } else if (value.equals("Two Pair")) {
            lastResultTV.setText(value);
            return 5;
        } else if (value.equals("Three of a Kind")) {
            lastResultTV.setText(value);
            return 10;
        } else if (value.equals("Straight")) {
            lastResultTV.setText(value);
            return 15;
        } else if (value.equals("Flush")) {
            lastResultTV.setText(value);
            return 20;
        } else if (value.equals("Full House")) {
            lastResultTV.setText(value);
            return 25;
        } else if (value.equals("Four of a Kind")) {
            lastResultTV.setText(value);
            return 50;
        } else if (value.equals("Straight Flush")) {
            lastResultTV.setText(value);
            return 75;
        } else if (value.equals("Royal Flush")) {
            lastResultTV.setText(value);
            return 100;
        } else {
            return 0;
        }
    }


    // Set's score view score
    private void setScoreView() {
        scoreTV.setText(String.valueOf(score));
    }

    // Checks for high score and saves it if needed
    private void checkHighScore() {
        if (score > highScore) {
            highScore = score;
            highScoreTV.setText(String.valueOf(score));
            SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(SquareSolitaire.this.getBaseContext());
            SharedPreferences.Editor editor = setSharedPreferences.edit();
            editor.putInt(Constants.SP_SQUARE_SOLITAIRE_HIGH_SCORE, score);
            editor.apply();
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
        endGameDialog();
    }


    private void endGameDialog() {
        new AlertDialog.Builder(SquareSolitaire.this)
                .setTitle("Quit")
                .setMessage("Are you sure that you want to leave this game?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SquareSolitaire.this.finish();
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

    private void gameEndedDialog() {
        new AlertDialog.Builder(SquareSolitaire.this)
                .setTitle("End of game")
                .setMessage("Your final score was: " + String.valueOf(score) + "\n\n" + "Your highest score is: " + String.valueOf(highScore))
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SquareSolitaire.this.finish();
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setCancelable(false)
                .setIcon(R.mipmap.logo)
                .show();
    }

} 