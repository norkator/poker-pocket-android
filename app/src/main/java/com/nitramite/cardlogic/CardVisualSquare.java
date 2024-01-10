package com.nitramite.cardlogic;

public class CardVisualSquare {


    public static String deckCardToString(int suit, int value) {
        return numberToSuit(suit) + numberToValue(value);
    }

    private static String numberToSuit(final int suit_) {
        String suit = "";
        if (suit_ == 0) {
            suit = "♠";
        } else if (suit_ == 1) {
            suit = "♣";
        } else if (suit_ == 2) {
            suit = "♥";
        } else if (suit_ == 3) {
            suit = "♦";
        }
        return suit;
    }

    private static String numberToValue(final int value_) {
        String value = "";
        if (value_ == 0) {
            value = "A"; // Ace
        } else if (value_ == 1) {
            value = "2";
        } else if (value_ == 2) {
            value = "3";
        } else if (value_ == 3) {
            value = "4";
        } else if (value_ == 4) {
            value = "5";
        } else if (value_ == 5) {
            value = "6";
        } else if (value_ == 6) {
            value = "7";
        } else if (value_ == 7) {
            value = "8";
        } else if (value_ == 8) {
            value = "9";
        } else if (value_ == 9) {
            value = "10";
        } else if (value_ == 10) {
            value = "J"; // Jack
        } else if (value_ == 11) {
            value = "Q"; // Queen
        } else if (value_ == 12) {
            value = "K"; // King
        }
        return value;
    }

    // ---------------------------------------------------------------------------------------------

    public static Integer suitToDeckNumber(final String suitStr) {
        if (suitStr.contains("♠")) {
            return 0;
        } else if (suitStr.contains("♣")) {
            return 1;
        } else if (suitStr.contains("♥")) {
            return 2;
        } else if (suitStr.contains("♦")) {
            return 3;
        } else {
            return -1;
        }
    }

    public static Integer valueToDeckNumber(final String suitStr) {
        if (suitStr.contains("A")) {
            return 0;
        } else if (suitStr.contains("2")) {
            return 1;
        } else if (suitStr.contains("3")) {
            return 2;
        } else if (suitStr.contains("4")) {
            return 3;
        } else if (suitStr.contains("5")) {
            return 4;
        } else if (suitStr.contains("6")) {
            return 5;
        } else if (suitStr.contains("7")) {
            return 6;
        } else if (suitStr.contains("8")) {
            return 7;
        } else if (suitStr.contains("9")) {
            return 8;
        } else if (suitStr.contains("10")) {
            return 9;
        } else if (suitStr.contains("J")) {
            return 10;
        } else if (suitStr.contains("Q")) {
            return 11;
        } else if (suitStr.contains("K")) {
            return 12;
        } else {
            return -1;
        }
    }


} 