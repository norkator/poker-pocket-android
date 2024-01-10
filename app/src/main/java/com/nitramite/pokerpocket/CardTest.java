package com.nitramite.pokerpocket;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.nitramite.cardlogic.Card;
import com.nitramite.cardlogic.CardDeckGenerator;
import com.nitramite.cardlogic.CardVisual;
import com.nitramite.cardlogic.GameCardResource;
import com.nitramite.cardlogic.holdem.HandEvaluatorSeven;

public class CardTest extends AppCompatActivity {

    // Logging
    private final static String TAG = CardTest.class.getSimpleName();

    // Animation declarations
    Animation animFadeIn, animFadeOut;

    // Buttons etc
    private TextView valueTV;
    private AudioPlayer audioPlayer = new AudioPlayer();

    // Center cards
    private ImageView card1, card2, card3, card4, card5, card6, card7;
    private CardVisual cards[] = new CardVisual[7];

    // Cards
    private GameCardResource gameCardResource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_test);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Cards
        gameCardResource = new GameCardResource(this);

        // Find button components
        valueTV = (TextView) findViewById(R.id.valueTV);
        CardView exitBtn = (CardView) findViewById(R.id.exitBtn);
        CardView generateBtn = (CardView) findViewById(R.id.generateBtn);

        // Find card components
        card1 = (ImageView) findViewById(R.id.card1);
        card2 = (ImageView) findViewById(R.id.card2);
        card3 = (ImageView) findViewById(R.id.card3);
        card4 = (ImageView) findViewById(R.id.card4);
        card5 = (ImageView) findViewById(R.id.card5);
        card6 = (ImageView) findViewById(R.id.card6);
        card7 = (ImageView) findViewById(R.id.card7);

        // Init animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in_holdem);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out_holdem);

        // Exit
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardTest.this.finish();
            }
        });

        // Generate
        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardOpenPackage(CardTest.this);
                process();
            }
        });

        Toast.makeText(this, "Welcome to extra feature (hand generator / validator)", Toast.LENGTH_LONG).show();
    } // End of onCreate();


    private void process() {
        Card[] cardDeck = CardDeckGenerator.shuffledCardDeck();
        cards[0] = new CardVisual(cardDeck[0].suite(), cardDeck[0].rank());
        cards[1] = new CardVisual(cardDeck[1].suite(), cardDeck[1].rank());
        cards[2] = new CardVisual(cardDeck[2].suite(), cardDeck[2].rank());
        cards[3] = new CardVisual(cardDeck[3].suite(), cardDeck[3].rank());
        cards[4] = new CardVisual(cardDeck[4].suite(), cardDeck[4].rank());
        cards[5] = new CardVisual(cardDeck[5].suite(), cardDeck[5].rank());
        cards[6] = new CardVisual(cardDeck[6].suite(), cardDeck[6].rank());
        card1.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[0].toWords())));
        card1.startAnimation(animFadeIn);
        card2.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[1].toWords())));
        card2.startAnimation(animFadeIn);
        card3.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[2].toWords())));
        card3.startAnimation(animFadeIn);
        card4.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[3].toWords())));
        card4.startAnimation(animFadeIn);
        card5.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[4].toWords())));
        card5.startAnimation(animFadeIn);
        card6.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[5].toWords())));
        card6.startAnimation(animFadeIn);
        card7.setBackground(ContextCompat.getDrawable(this, gameCardResource.cardResourceId(this, cards[6].toWords())));
        card7.startAnimation(animFadeIn);
        evaluate();
    }


    private void evaluate() {
        //HandEvaluatorFive handEvaluatorFive;
        //handEvaluatorFive = new HandEvaluatorFive(cards);
        //setValue(handEvaluatorFive.getPokerHandAsString(), handEvaluatorFive.getPokerHandAsValued());
        HandEvaluatorSeven handEvaluatorSeven = new HandEvaluatorSeven(cards);
        setValue(String.valueOf(handEvaluatorSeven.getType()), (double) handEvaluatorSeven.getValue());
    }


    private void setValue(String asText, double value) {
        valueTV.setText("Value: " + asText + "\n" + String.valueOf(value));
    }


} 