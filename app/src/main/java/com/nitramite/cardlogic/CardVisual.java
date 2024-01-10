package com.nitramite.cardlogic;

public class CardVisual {

    private byte suit; // Maa
    private byte rank; // Numero

    public CardVisual(int suit_, int rank_) {
        this.suit = (byte) suit_;
        this.rank = (byte) rank_;
    }

    public byte getRank() {
        return rank;
    }

    public byte getSuit() {
        return suit;
    }

    public int getValue() {
        return (this.rank % 13) + 1;
    }

    public String valueToString() {
        String value = "Error";
        int temp = this.rank % 13;
        if (temp == 0) {
            value = "fourteen"; // Ace
        } else if (temp == 1) {
            value = "two";
        } else if (temp == 2) {
            value = "three";
        } else if (temp == 3) {
            value = "four";
        } else if (temp == 4) {
            value = "five";
        } else if (temp == 5) {
            value = "six";
        } else if (temp == 6) {
            value = "seven";
        } else if (temp == 7) {
            value = "eight";
        } else if (temp == 8) {
            value = "nine";
        } else if (temp == 9) {
            value = "ten";
        } else if (temp == 10) {
            value = "eleven"; // Jack
        } else if (temp == 11) {
            value = "twelve"; // Queen
        } else if (temp == 12) {
            value = "thirteen"; // King
        }
        return value;
    }

    public boolean isFaceCard() {
        return this.rank == 10 || this.rank == 11 || this.rank == 12;
    }

    public String suitToString() {
        String suit = "Errors";
        if (this.suit == 0) {
            suit = "spades";
        } else if (this.suit == 1) {
            suit = "clubs";
        } else if (this.suit == 2) {
            suit = "hearts";
        } else if (this.suit == 3) {
            suit = "diamonds";
        }
        return suit;
    }

    public String toWords() {
        String words = "";
        words = suitToString() + "_" + valueToString();
        return words;
    }

} 