package com.nitramite.pokerpocket;

public class Constants {

    // Development mode
    static final boolean isDev = false; // Development mode

    static String DEVELOPMENT_SERVER = "ws://192.168.1.107:8000"; // Same network with server
    static String PRODUCTION_SERVER = "wss://pokerpocket-wss.nitramite.com";

    static String SEVEN_DEVELOPMENT_SERVER = ""; // Same network with server
    static String SEVEN_PRODUCTION_SERVER = "";

    // User settings keys
    static String SP_IS_FIRST_TIME_LAUNCH = "IS_FIRST_TIME_LAUNCH";
    static String SP_README_COMPLETED = "SP_README_COMPLETED";
    static String SP_PLAYER_NICKNAME = "SP_PLAYER_NICKNAME";
    static String SP_ENABLE_SOUNDS = "SP_ENABLE_SOUNDS";
    static String SP_DEVELOPMENT_SERVER = "SP_DEVELOPMENT_SERVER";
    static String SP_SHOW_DEVELOPMENT_FEATURES = "SP_SHOW_DEVELOPMENT_FEATURES";
    public static String SP_USE_BLACK_CARDS = "SP_USE_BLACK_CARDS";
    public static String SP_USE_PURPLE_HOLDEM_TABLE = "SP_USE_PURPLE_HOLDEM_TABLE";

    // Hold'em online settings keys
    static String SP_ONLINE_HOLDEM_IS_LOGGED_IN = "SP_ONLINE_HOLDEM_IS_LOGGED_IN";
    static String SP_ONLINE_HOLDEM_USERNAME = "SP_ONLINE_HOLDEM_USERNAME";
    static String SP_ONLINE_HOLDEM_PASSWORD = "SP_ONLINE_HOLDEM_PASSWORD";
    static String SP_HOLDEM_AUTO_PLAY = "SP_HOLDEM_AUTO_PLAY";

    // Hold'em offline settings keys
    static String SP_OFFLINE_HOLDEM_SHOW_DEBUG_WINDOW = "SP_OFFLINE_HOLDEM_SHOW_DEBUG_WINDOW";      // Holdem offline
    static String SP_OFFLINE_HOLDEM_SHOW_TUTORIAL_WINDOW = "SP_OFFLINE_HOLDEM_SHOW_TUTORIAL_WINDOW";// Holdem offline
    static String SP_OFFLINE_HOLDEM_AUTO_NEXT_ROUND = "SP_OFFLINE_HOLDEM_AUTO_NEXT_ROUND";          // Holdem offline
    static String SP_OFFLINE_HOLDEM_BOT_COUNT = "SP_OFFLINE_HOLDEM_BOT_COUNT";                      // Holdem offline
    static String SP_OFFLINE_HOLDEM_SAVE_PROGRESS = "SP_OFFLINE_HOLDEM_SAVE_PROGRESS";              // Holdem offline
    static String SP_OFFLINE_HOLDEM_SAVE_PARAMS = "SP_OFFLINE_HOLDEM_SAVE_PARAMS";                  // Holdem offline

    // Blackjack settings keys
    static String SP_OFFLINE_BLACKJACK_SHOW_TUTORIAL_WINDOW = "SP_OFFLINE_BLACKJACK_SHOW_TUTORIAL_WINDOW";  // Blackjack

    // 5-Card Draw settings keys
    static String SP_OFFLINE_FIVE_CARD_DRAW_AUTO_NEXT_ROUND = "SP_OFFLINE_FIVE_CARD_DRAW_AUTO_NEXT_ROUND";  // 5-Card draw offline
    static String SP_OFFLINE_FIVE_CARD_DRAW_BOT_COUNT = "SP_OFFLINE_FIVE_CARD_DRAW_BOT_COUNT";              // 5-Card draw offline

    // Square solitaire
    public static String SP_SQUARE_SOLITAIRE_HIGH_SCORE = "SP_SQUARE_SOLITAIRE_HIGH_SCORE";

    // Virtual deck
    public static String SP_VIRTUAL_DECK_PLAYERS_COUNT = "SP_VIRTUAL_DECK_PLAYERS_COUNT";
    public static String SP_VIRTUAL_DECK_PLAYER_HAND_SIZE = "SP_VIRTUAL_DECK_PLAYER_HAND_SIZE";

    // Slot machine
    public static String SP_SLOT_MACHINE_MONEY_LEFT = "SP_SLOT_MACHINE_MONEY_LEFT";


} 