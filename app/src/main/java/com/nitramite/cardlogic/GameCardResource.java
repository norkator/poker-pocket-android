package com.nitramite.cardlogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nitramite.pokerpocket.Constants;

/**
 * Class return's playing card resource id
 */
public class GameCardResource {

    private boolean useBlackCards = false;

    public GameCardResource(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.useBlackCards = sharedPreferences.getBoolean(Constants.SP_USE_BLACK_CARDS, false);
    }


    // Checks for user selected card skin and then returns drawable id of corresponding card
    public int cardResourceId(Context context, String cardString) {
        if (useBlackCards && !cardString.equals("top_red")) {
            return getDrawable(context, "card_" + cardString + "_black");
        } else {
            return getDrawable(context, "card_" + cardString);
        }
    }


    // Returns drawable resource id with given drawable name
    private static int getDrawable(Context context, String cardDrawableName) {
        //Log.i("GameCardResource", cardDrawableName);
        return context.getResources().getIdentifier(cardDrawableName, "drawable", context.getPackageName());
    }

} 