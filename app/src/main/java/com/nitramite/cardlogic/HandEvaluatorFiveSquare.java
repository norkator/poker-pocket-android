package com.nitramite.cardlogic;

import android.util.Log;

public class HandEvaluatorFiveSquare {

    // Using this evaluators string output only with square solitaire

    // Logging
    private final static String TAG = "HandEvaluatorFiveSquare";

    private Card[] hand;
    private int rank;

    public HandEvaluatorFiveSquare(CardVisual cards[]) {
        hand = new Card[5];
        hand[0] = new Card(((int)cards[0].getSuit()) + 1, ((int)cards[0].getRank()) == 0 ? 14 : ((int)cards[0].getRank()) + 1);
        hand[1] = new Card(((int)cards[1].getSuit()) + 1, ((int)cards[1].getRank()) == 0 ? 14 : ((int)cards[1].getRank()) + 1);
        hand[2] = new Card(((int)cards[2].getSuit()) + 1, ((int)cards[2].getRank()) == 0 ? 14 : ((int)cards[2].getRank()) + 1);
        hand[3] = new Card(((int)cards[3].getSuit()) + 1, ((int)cards[3].getRank()) == 0 ? 14 : ((int)cards[3].getRank()) + 1);
        hand[4] = new Card(((int)cards[4].getSuit()) + 1, ((int)cards[4].getRank()) == 0 ? 14 : ((int)cards[4].getRank()) + 1);
        evalRank();
    }//end Constructor


    public int getRank() {
        return rank;
    }//end getRank


    public boolean hasAce() {
        for(int i = 0; i < 5; i++)
            if(hand[i].rank() == 14)
                return true;
        return false;
    }//end hasAce


    public static String rankToString(int rank) {
        switch(rank) {
            case 0: return "Bust";
            case 1: return "Pair";
            case 2: return "Two Pair";
            case 3: return "Three of a Kind";
            case 4: return "Straight";
            case 5: return "Flush";
            case 6: return "Full House";
            case 7: return "Four of a Kind";
            case 8: return "Straight Flush";
            case 9: return "Royal Flush";
            default: return "Error";
        }
    }//end rankToString


    public void draw(int index, Card c) {
        if(0 <= index && index <= 4) {
            hand[index] = c;
        }
        evalRank();
    }//end draw


    public boolean hasGoodPair() {
        boolean result;
        result = false;
        if(rank == 1) {
            for(int i = 0; i < 5; i++) {
                for(int j = i+1; j < 5; j++)
                    if(hand[i].rank() == hand[j].rank() && hand[i].rank() > 10)
                        result = true;
            }
        }
        return result;
    }


    public int getPokerHandAsValued() {
        return this.rank;
    }


    public String getPokerHandAsString() {
        evalRank();
        String result = "";
        switch(rank) {
            case 0:
                result += "Bust";
                break;
            case 1:
                result += "Pair";
                break;
            case 2:
                result += "Two Pair";
                break;
            case 3:
                result += "Three of a Kind";
                break;
            case 4:
                result += "Straight";
                break;
            case 5:
                result += "Flush";
                break;
            case 6:
                result += "Full House";
                break;
            case 7:
                result += "Four of a Kind";
                break;
            case 8:
                result += "Straight Flush";
                break;
            case 9:
                result += "Royal Flush";
                break;
            default:
                result += "Nothing";
                break;
        }
        return result;
    }//end toString


    private void evalRank() {
        Card [] sortedHand = new Card[5];
        for(int i = 0; i < 5; i++)
            sortedHand[i] = hand[i];

        this.sort(sortedHand);
        int pairIndex = -1;

        rank = 0;  //assume its a BUST

        //check for pair
        for(int i = 0; i < 4; i++)
            if(sortedHand[i].rank() == sortedHand[i+1].rank()) {
                pairIndex = i;
                rank = 1;
                i = 4;
            }

        //check for 2 pair
        if(rank == 1) {
            for(int i = pairIndex + 2; i < 4; i++)
                if(sortedHand[i].rank() == sortedHand[i+1].rank())
                    rank = 2;
        }

        //check for 3 of a kind or full house
        for(int i = 0; i < 3; i++)
            if(sortedHand[i].rank() == sortedHand[i+1].rank() && sortedHand[i+1].rank() == sortedHand[i+2].rank()) {
                rank = 3;
                if(i==0 && sortedHand[3].rank()==sortedHand[4].rank() || i==2 && sortedHand[0].rank() == sortedHand[1].rank())
                    rank = 6;
            }

        //check for 4 of a kind
        for(int i = 0; i < 2; i++)
            if(sortedHand[i].rank() == sortedHand[i+1].rank() && sortedHand[i+1].rank() == sortedHand[i+2].rank() &&
                    sortedHand[i+2].rank() == sortedHand[i+3].rank()) {
                rank = 7;
            }

        //check for straight (if we haven't already found any pairs)
        if(rank == 0)
            if((sortedHand[4].rank() - sortedHand[0].rank() == 4) ||
                    (sortedHand[3].rank() - sortedHand[0].rank() == 3 && sortedHand[4].rank() == 14 && sortedHand[0].rank() == 2)) {
                rank = 4;
            }

        //check for flush (if we haven't already found any pairs)
        boolean flush;
        if(rank == 0 || rank == 4) {
            flush = true;
            for(int i = 0; i < 4; i++)
                if(sortedHand[i].suite() != sortedHand[i+1].suite())
                    flush = false;
            if(flush && rank == 4)
                rank = 8; //straight flush!
            else if(flush)
                rank = 5;
        }

        //check for royal flush (if it's a straight flush)
        if(rank == 8 && sortedHand[4].rank() == 14 && sortedHand[0].rank() == 10)
            rank = 9; //royal flush!
    }//end evalRank


    private void sort(Card [] a) {
        Card temp;
        int minIndex;
        for(int i = 0; i < a.length; i++) {
            minIndex = i;
            for(int j = i; j < a.length; j++) {
                if(a[minIndex].isHigher(a[j]))
                    minIndex = j;
            }
            //swap the elements at i and j
            temp = a[minIndex];
            a[minIndex] = a[i];
            a[i] = temp;
        }
    }//end sort


} 