package com.nitramite.bot;

import android.content.Context;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.nitramite.cardlogic.HandEvaluatorFive;
import com.nitramite.dynamic.Player;
import com.nitramite.pokerpocket.AudioPlayer;
import com.nitramite.pokerpocket.R;

import java.util.ArrayList;


public class FiveCardDrawBot {

    // Logging
    private final static String TAG = "FiveCardDrawBot";

    // Variables
    private Animation animFadeOut;
    private AudioPlayer audioPlayer = new AudioPlayer();


    // Constructor
    public FiveCardDrawBot(Context context) {
        animFadeOut = AnimationUtils.loadAnimation(context, R.anim.anim_fade_out_five_card_draw);
    }


    // Handles bot hand logic
    public void botHandHandler(Context context, Player player) {
        HandEvaluatorFive handEvaluator = new HandEvaluatorFive(player.cards);

        Log.i(TAG, handEvaluator.getPokerHandAsString());
        Log.i(TAG, String.valueOf(handEvaluator.getPokerHandAsValued()));
        Log.i(TAG, String.valueOf(handEvaluator.getUnSortedCardValues()));

        for (int i = 0; i < player.cards.length; i++) {
            if (handEvaluator.getUnSortedCardValues().get(i).intValue() < 5) {
                audioPlayer.playCardSlideOne(context);
                player.cards[i] = null;
                ArrayList<ImageView> playerCardImageViews = new ArrayList<>();
                playerCardImageViews.add(player.playerCard0);
                playerCardImageViews.add(player.playerCard1);
                playerCardImageViews.add(player.playerCard2);
                playerCardImageViews.add(player.playerCard3);
                playerCardImageViews.add(player.playerCard4);
                playerCardImageViews.get(i).startAnimation(animFadeOut);
                playerCardImageViews.get(i).setBackground(null);
            }
        }
    }


} 