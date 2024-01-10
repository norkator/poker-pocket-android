package com.nitramite.cardlogic;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;

public class CardDeckGenerator {


    /* Returns shuffled card deck consisting Card card instances */
    public static Card[] shuffledCardDeck() {
        Card[] deck = new Card[52];
        Integer[] rand = new Integer[52];
        SecureRandom secureRandom = null;
        try {
            new SecureRandom();
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(new byte[20]);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (int c = 0; c < 52; c++) {
            rand[c] = c;
        }

        assert secureRandom != null;
        Collections.shuffle(Arrays.asList(rand), secureRandom);

        for (int i = 0; i < rand.length; i++) {
            switch (rand[i]) {
                case 0:
                    deck[i] = new Card(0, 0);
                    break;
                case 1:
                    deck[i] = new Card(0, 1);
                    break;
                case 2:
                    deck[i] = new Card(0, 2);
                    break;
                case 3:
                    deck[i] = new Card(0, 3);
                    break;
                case 4:
                    deck[i] = new Card(0, 4);
                    break;
                case 5:
                    deck[i] = new Card(0, 5);
                    break;
                case 6:
                    deck[i] = new Card(0, 6);
                    break;
                case 7:
                    deck[i] = new Card(0, 7);
                    break;
                case 8:
                    deck[i] = new Card(0, 8);
                    break;
                case 9:
                    deck[i] = new Card(0, 9);
                    break;
                case 10:
                    deck[i] = new Card(0, 10);
                    break;
                case 11:
                    deck[i] = new Card(0, 11);
                    break;
                case 12:
                    deck[i] = new Card(0, 12);
                    break;
                case 13:
                    deck[i] = new Card(1, 0);
                    break;
                case 14:
                    deck[i] = new Card(1, 1);
                    break;
                case 15:
                    deck[i] = new Card(1, 2);
                    break;
                case 16:
                    deck[i] = new Card(1, 3);
                    break;
                case 17:
                    deck[i] = new Card(1, 4);
                    break;
                case 18:
                    deck[i] = new Card(1, 5);
                    break;
                case 19:
                    deck[i] = new Card(1, 6);
                    break;
                case 20:
                    deck[i] = new Card(1, 7);
                    break;
                case 21:
                    deck[i] = new Card(1, 8);
                    break;
                case 22:
                    deck[i] = new Card(1, 9);
                    break;
                case 23:
                    deck[i] = new Card(1, 10);
                    break;
                case 24:
                    deck[i] = new Card(1, 11);
                    break;
                case 25:
                    deck[i] = new Card(1, 12);
                    break;
                case 26:
                    deck[i] = new Card(2, 0);
                    break;
                case 27:
                    deck[i] = new Card(2, 1);
                    break;
                case 28:
                    deck[i] = new Card(2, 2);
                    break;
                case 29:
                    deck[i] = new Card(2, 3);
                    break;
                case 30:
                    deck[i] = new Card(2, 4);
                    break;
                case 31:
                    deck[i] = new Card(2, 5);
                    break;
                case 32:
                    deck[i] = new Card(2, 6);
                    break;
                case 33:
                    deck[i] = new Card(2, 7);
                    break;
                case 34:
                    deck[i] = new Card(2, 8);
                    break;
                case 35:
                    deck[i] = new Card(2, 9);
                    break;
                case 36:
                    deck[i] = new Card(2, 10);
                    break;
                case 37:
                    deck[i] = new Card(2, 11);
                    break;
                case 38:
                    deck[i] = new Card(2, 12);
                    break;
                case 39:
                    deck[i] = new Card(3, 0);
                    break;
                case 40:
                    deck[i] = new Card(3, 1);
                    break;
                case 41:
                    deck[i] = new Card(3, 2);
                    break;
                case 42:
                    deck[i] = new Card(3, 3);
                    break;
                case 43:
                    deck[i] = new Card(3, 4);
                    break;
                case 44:
                    deck[i] = new Card(3, 5);
                    break;
                case 45:
                    deck[i] = new Card(3, 6);
                    break;
                case 46:
                    deck[i] = new Card(3, 7);
                    break;
                case 47:
                    deck[i] = new Card(3, 8);
                    break;
                case 48:
                    deck[i] = new Card(3, 9);
                    break;
                case 49:
                    deck[i] = new Card(3, 10);
                    break;
                case 50:
                    deck[i] = new Card(3, 11);
                    break;
                case 51:
                    deck[i] = new Card(3, 12);
                    break;
                default:
                    break;

            }
        }
        return deck;
    }


} 