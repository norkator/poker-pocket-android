package com.nitramite.adapters;

import android.content.Context;

public class RankingItem {

    private Context context;
    private String playerName;
    private Integer playerXP;
    private String medalIcon;
    private String winCount;
    private String loseCount;

    public RankingItem(Context context, String playerName, Integer playerXP, String medalIcon, String winCount, String loseCount) {
        this.context = context;
        this.playerName = playerName;
        this.playerXP = playerXP;
        this.medalIcon = medalIcon;
        this.winCount = winCount;
        this.loseCount = loseCount;
    }

    public Context getContext() {
        return this.context;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerXP() {
        return String.valueOf(playerXP) + "XP";
    }

    public Integer getMedalImageResource() {
        return getDrawable(context, this.medalIcon);
    }

    public String getWinCount() {
        return winCount;
    }

    public String getLoseCount() {
        return loseCount;
    }

    // Returns drawable resource id with given drawable name
    private static int getDrawable(Context context, String cardDrawableName) {
        return context.getResources().getIdentifier(cardDrawableName, "drawable", context.getPackageName());
    }

}