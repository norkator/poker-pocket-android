package com.nitramite.cardlogic;

import android.util.Log;

public class CardVisualOnline {


    /**
     * Returns card string for showing visual card when input comes from Node JS sever
     * In way like example: 7♥
     */

    private final static String TAG = "CardVisualOnline";

    private String cardStr = null;

    public CardVisualOnline(String cardStr_) {
        this.cardStr = cardStr_;
    }


    public String toWords() {
        return getSuit(this.cardStr) + "_" + getValue(this.cardStr);
    }


    // Used on bounce animation
    public String getCardStr() {
        return cardStr;
    }


    private static String getSuit(final String cardStr) {
        if (cardStr.contains("♠")) { // Spades
            return "spades";
        } else if (cardStr.contains("♥")) { // Hearts
            return "hearts";
        } else if (cardStr.contains("♣")) { // Clubs
            return "clubs";
        } else if (cardStr.contains("♦")) { // Diamonds
            return "diamonds";
        } else {
            return null;
        }
    }


    private static String getValue (final String cardStr) {
        if (cardStr.contains("2")) {
            return "two";
        } else if (cardStr.contains("3")) {
            return "three";
        } else if (cardStr.contains("4")) {
            return "four";
        } else if (cardStr.contains("5")) {
            return "five";
        } else if (cardStr.contains("6")) {
            return "six";
        } else if (cardStr.contains("7")) {
            return "seven";
        } else if (cardStr.contains("8")) {
            return "eight";
        } else if (cardStr.contains("9")) {
            return "nine";
        } else if (cardStr.contains("10")) {
            return "ten";
        } else if (cardStr.contains("J")) {
            return "eleven";
        } else if (cardStr.contains("Q")) {
            return "twelve";
        } else if (cardStr.contains("K")) {
            return "thirteen";
        } else if (cardStr.contains("A")) {
            return "fourteen";
        } else {
            return null;
        }
    }


} 