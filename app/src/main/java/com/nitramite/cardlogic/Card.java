package com.nitramite.cardlogic;

/* This imitates one card in a stack */
public class Card {

    private final int suit; // Maa
    private final int rank; // Numero

    public Card(int aSuit, int aRank) {
        suit = aSuit;
        rank = aRank;
    }

    // Maa
    public int suite() {
        return suit;
    }

    // Numero
    public int rank() {
        return rank;
    }


    // Is higher method
    public boolean isHigher(Card c) {
        if(this.rank() > c.rank() || (this.rank() == c.rank() && this.suite() > c.suite()))
            return true;
        else
            return false;
    }


} 