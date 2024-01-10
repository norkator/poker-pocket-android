package com.nitramite.cardlogic.holdem;

public enum HandValueType {

    /**
     * Royal flush (Ace-high Straight Flush).
     */
    ROYAL_FLUSH("a Royal Flush", 9),

    /**
     * Straight Flush (a Straight and a Flush, less than Ace-high).
     */
    STRAIGHT_FLUSH("a Straight Flush", 8),

    /**
     * Four of a Kind (four cards of the same rank).
     */
    FOUR_OF_A_KIND("Four of a Kind", 7),

    /**
     * Full House (a Three of a Kind and Two Pairs).
     */
    FULL_HOUSE("a Full House", 6),

    /**
     * Flush (five cards of the same suit).
     */
    FLUSH("a Flush", 5),

    /**
     * Straight (five cards in sequential order).
     */
    STRAIGHT("a Straight", 4),

    /**
     * Three of a Kind (three cards of the same rank).
     */
    THREE_OF_A_KIND("Three of a Kind", 3),

    /**
     * Two Pairs (two pairs).
     */
    TWO_PAIRS("Two Pairs", 2),

    /**
     * One Pair (two cards of the same rank).
     */
    ONE_PAIR("One Pair", 1),

    /**
     * Highest Card (the card with the highest rank).
     */
    HIGH_CARD("a High Card", 0),;

    private String description;

    private int value;

    HandValueType(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

} 