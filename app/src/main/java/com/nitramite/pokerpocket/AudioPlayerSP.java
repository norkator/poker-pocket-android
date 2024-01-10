package com.nitramite.pokerpocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;

public class AudioPlayerSP {

    // Variables
    private SoundPool soundPool;
    private Boolean ENABLE_SOUNDS = true;
    private int soundIds[] = new int[10];
    private int wheelSpinningStreamId = -1;


    // This is the sound pool version
    public AudioPlayerSP(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(attrs)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        // Load sounds
        soundIds[0] = soundPool.load(c, R.raw.chip_lay_one, 1);
        soundIds[1] = soundPool.load(c, R.raw.slot_machine_insert_coin, 1);
        soundIds[2] = soundPool.load(c, R.raw.slot_machine_money_dropping, 1);
        soundIds[3] = soundPool.load(c, R.raw.slot_machine_wheel_spinning, 1);
        soundIds[4] = soundPool.load(c, R.raw.slot_machine_no_coins, 1);
        soundIds[5] = soundPool.load(c, R.raw.slot_machine_wheel_stop, 1);
        soundIds[6] = soundPool.load(c, R.raw.slot_machine_money_dropping_half, 1);
        // Shared preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        ENABLE_SOUNDS = sp.getBoolean(Constants.SP_ENABLE_SOUNDS, true);
    }

    // Release sound pool onDestroy event
    public void release() {
        soundPool.release();
    }

    // ---------------------------------------------------------------------------------------------

    public void playCardChipLayOne() {
        if (ENABLE_SOUNDS) {
            soundPool.play(soundIds[0], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playSlotMachineInsertCoin() {
        if (ENABLE_SOUNDS) {
            soundPool.play(soundIds[1], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playSlotMachineMoneyDropping() {
        if (ENABLE_SOUNDS) {
            soundPool.play(soundIds[2], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playSlotMachineWheelSpinning() {
        if (ENABLE_SOUNDS) {
            wheelSpinningStreamId = soundPool.play(soundIds[3], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void stopSlotMachineWheelSpinning() {
        if (wheelSpinningStreamId != -1) {
            soundPool.stop(wheelSpinningStreamId);
        }
    }

    public void playSlotMachineNoCoins() {
        if (ENABLE_SOUNDS) {
            soundPool.play(soundIds[4], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playSlotMachineWheelStop() {
        if (ENABLE_SOUNDS) {
            soundPool.play(soundIds[5], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playSlotMachineMoneyDroppingHalf() {
        if (ENABLE_SOUNDS) {
            soundPool.play(soundIds[6], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    // ---------------------------------------------------------------------------------------------

} 