package com.nitramite.cardlogic.holdem;

import com.nitramite.cardlogic.CardVisual;

public class HandEvaluatorSeven {


    // Variables from other classes
    private static final int NO_OF_RANKS = 13;
    private static final int NO_OF_SUITS = 4;

    // The ranks.
    private static final int ACE = 0; // stock 12
    private static final int KING = 13;
    private static final int QUEEN = 12;
    private static final int JACK = 11;
    private static final int TEN = 10;
    private static final int NINE = 9;
    private static final int EIGHT = 8;
    private static final int SEVEN = 7;
    private static final int SIX = 6;
    private static final int FIVE = 5;
    private static final int FOUR = 4;
    private static final int THREE = 3;
    private static final int DEUCE = 2;

    // The suits.
    private static final int SPADES = 0;
    private static final int CLUBS = 1;
    private static final int HEARTS = 2;
    private static final int DIAMONDS = 3;

    // Variables from this current class
    private static final int NO_OF_RANKINGS = 6;
    private static final int MAX_NO_OF_PAIRS = 2;
    private static final int[] RANKING_FACTORS = {371293, 28561, 2197, 169, 13, 1};
    private HandValueType type;
    private int value = 0;
    private final CardVisual[] cards;
    private int[] rankDist = new int[NO_OF_RANKS];
    private int[] suitDist = new int[NO_OF_SUITS];
    private int noOfPairs = 0;
    private int[] pairs = new int[MAX_NO_OF_PAIRS];
    private int flushSuit = -1;
    private int flushRank = -1;
    private int straightRank = -1;
    private boolean wheelingAce = false;
    private int tripleRank = -1;
    private int quadRank = -1;
    private int[] rankings = new int[NO_OF_RANKINGS];


    public HandEvaluatorSeven(CardVisual[] cards_) {
        cards = cards_;

        // Find patterns.
        calculateDistributions();
        findStraight();
        findFlush();
        findDuplicates();

        // Find special values.
        boolean isSpecialValue =
                (isStraightFlush() ||
                        isFourOfAKind() ||
                        isFullHouse() ||
                        isFlush() ||
                        isStraight() ||
                        isThreeOfAKind() ||
                        isTwoPairs() ||
                        isOnePair());
        if (!isSpecialValue) {
            calculateHighCard();
        }

        // Calculate value.
        for (int i = 0; i < NO_OF_RANKINGS; i++) {
            value += rankings[i] * RANKING_FACTORS[i];
        }
    }


    public HandValueType getType() {
        return type;
    }


    public int getValue() {
        return value;
    }


    void calculateDistributions() {
        for (CardVisual card : cards) {
            rankDist[card.getRank()]++;
            suitDist[card.getSuit()]++;
        }
    }


    private void findFlush() {
        for (int i = 0; i < NO_OF_SUITS; i++) {
            if (suitDist[i] >= 5) {
                flushSuit = i;
                for (CardVisual card : cards) {
                    if (card.getSuit() == flushSuit) {
                        if (!wheelingAce || card.getRank() != ACE) {
                            flushRank = card.getRank();
                            break;
                        }
                    }
                }
                break;
            }
        }
    }


    private void findStraight() {
        boolean inStraight = false;
        int rank = -1;
        int count = 0;
        for (int i = NO_OF_RANKS - 1; i >= 0; i--) {
            if (rankDist[i] == 0) {
                inStraight = false;
                count = 0;
            } else {
                if (!inStraight) {
                    inStraight = true;
                    rank = i;
                }
                count++;
                if (count >= 5) {
                    straightRank = rank;
                    break;
                }
            }
        }
        if ((count == 4) && (rank == FIVE) && (rankDist[ACE] > 0)) {
            wheelingAce = true;
            straightRank = rank;
        }
    }


    private void findDuplicates() {
        for (int i = NO_OF_RANKS - 1; i >= 0; i--) {
            if (rankDist[i] == 4) {
                quadRank = i;
            } else if (rankDist[i] == 3 && tripleRank == -1) {
                tripleRank = i;
            } else if (rankDist[i] >= 2) {
                if (noOfPairs < MAX_NO_OF_PAIRS) {
                    pairs[noOfPairs++] = i;
                }
            }
        }
    }

    private void calculateHighCard() {
        type = HandValueType.HIGH_CARD;
        rankings[0] = type.getValue();
        int index = 1;
        for (CardVisual card : cards) {
            rankings[index++] = card.getRank();
            if (index > 5) {
                break;
            }
        }
    }

    private boolean isOnePair() {
        if (noOfPairs == 1) {
            type = HandValueType.ONE_PAIR;
            rankings[0] = type.getValue();
            int pairRank = pairs[0];
            rankings[1] = pairRank;
            int index = 2;
            for (CardVisual card1 : cards) {
                int rank = card1.getRank();
                if (rank != pairRank) {
                    rankings[index++] = rank;
                    if (index > 4) {
                        break;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isTwoPairs() {
        if (noOfPairs == 2) {
            type = HandValueType.TWO_PAIRS;
            rankings[0] = type.getValue();
            int highRank = pairs[0];
            int lowRank = pairs[1];
            rankings[1] = highRank;
            rankings[2] = lowRank;
            for (CardVisual card : cards) {
                int rank = card.getRank();
                if ((rank != highRank) && (rank != lowRank)) {
                    rankings[3] = rank;
                    break;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isThreeOfAKind() {
        if (tripleRank != -1) {
            type = HandValueType.THREE_OF_A_KIND;
            rankings[0] = type.getValue();
            rankings[1] = tripleRank;
            int index = 2;
            for (CardVisual card : cards) {
                int rank = card.getRank();
                if (rank != tripleRank) {
                    rankings[index++] = rank;
                    if (index > 3) {
                        break;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private boolean isStraight() {
        if (straightRank != -1) {
            type = HandValueType.STRAIGHT;
            rankings[0] = type.getValue();
            rankings[1] = straightRank;
            return true;
        } else {
            return false;
        }
    }


    private boolean isFlush() {
        if (flushSuit != -1) {
            type = HandValueType.FLUSH;
            rankings[0] = type.getValue();
            int index = 1;
            for (CardVisual card : cards) {
                if (card.getSuit() == flushSuit) {
                    int rank = card.getRank();
                    if (index == 1) {
                        flushRank = rank;
                    }
                    rankings[index++] = rank;
                    if (index > 5) {
                        break;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private boolean isFullHouse() {
        if ((tripleRank != -1) && (noOfPairs > 0)) {
            type = HandValueType.FULL_HOUSE;
            rankings[0] = type.getValue();
            rankings[1] = tripleRank;
            rankings[2] = pairs[0];
            return true;
        } else {
            return false;
        }
    }


    private boolean isFourOfAKind() {
        if (quadRank != -1) {
            type = HandValueType.FOUR_OF_A_KIND;
            rankings[0] = type.getValue();
            rankings[1] = quadRank;
            int index = 3;
            for (CardVisual card : cards) {
                int rank = card.getRank();
                if (rank != quadRank) {
                    rankings[index] = rank;
                    break;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private boolean isStraightFlush() {
        if (straightRank != -1 && flushRank == straightRank) {
            int straightRank2 = -1;
            int lastSuit = -1;
            int lastRank = -1;
            int inStraight = 1;
            int inFlush = 1;
            for (CardVisual card : cards) {
                int rank = card.getRank();
                int suit = card.getSuit();
                if (suit != flushSuit) continue;
                if (lastRank != -1) {
                    int rankDiff = lastRank - rank;
                    if (rankDiff == 1) {
                        inStraight++;
                        if (straightRank2 == -1) {
                            straightRank2 = lastRank;
                        }
                        if (suit == lastSuit) {
                            inFlush++;
                        } else {
                            inFlush = 1;
                        }
                        if (inStraight >= 5 && inFlush >= 5) {
                            break;
                        }
                    } else if (rankDiff == 0) {
                    } else {
                        straightRank2 = -1;
                        inStraight = 1;
                        inFlush = 1;
                    }
                }
                lastRank = rank;
                lastSuit = suit;
            }
            if (inStraight >= 5 && inFlush >= 5) {
                if (straightRank == ACE) {
                    type = HandValueType.ROYAL_FLUSH;
                    rankings[0] = type.getValue();
                    return true;
                } else {
                    type = HandValueType.STRAIGHT_FLUSH;
                    rankings[0] = type.getValue();
                    rankings[1] = straightRank2;
                    return true;
                }
            } else if (wheelingAce && inStraight >= 4 && inFlush >= 4) {
                type = HandValueType.STRAIGHT_FLUSH;
                rankings[0] = type.getValue();
                rankings[1] = straightRank2;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


} 