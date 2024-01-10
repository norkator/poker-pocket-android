package com.nitramite.pokerpocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

public class AudioPlayer {
    private MediaPlayer mMediaPlayer;
    private Boolean ENABLE_SOUNDS = true;
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    public void play(Context c, int rid, float volume) {
        try {
            stop();
            mMediaPlayer = MediaPlayer.create(c, rid);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stop();
                }
            });
            mMediaPlayer.setVolume(volume, volume);
            mMediaPlayer.start();
            // Shared preferences
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
            ENABLE_SOUNDS = sp.getBoolean(Constants.SP_ENABLE_SOUNDS, true);
        } catch (IllegalStateException ignored) {
        }
    }

    public void playCardOpenPackage(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.open_package, 100);
        }
    }

    public void playCardPlaceOne(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.place_one, 100);
        }
    }

    public void playCardPlaceTwo(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.place_two, 100);
        }
    }

    public void playCardSlideOne(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.slide_one, 100);
        }
    }

    public void playCardFoldOne(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.fold_one, 100);
        }
    }

    public void playCardPlaceChipsOne(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.place_chips_one, 100);
        }
    }

    public void playCardChipLayOne(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.chip_lay_one, 100);
        }
    }

    public void playChipsHandleFive(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.chips_handle_five, 100);
        }
    }

    public void playCardSlideSix(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.card_slide_six, 100);
        }
    }

    public void playCardTakeOutFromPackageOne(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.card_take_out_from_package_one, 100);
        }
    }

    public void playCardShoveTwo(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.card_shove_two, 100);
        }
    }

    public void playSlotMachineInsertCoin(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.slot_machine_insert_coin, 100);
        }
    }

    public void playSlotMachineMoneyDropping(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.slot_machine_money_dropping, 100);
        }
    }

    public void playSlotMachineWheelSpinning(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.slot_machine_wheel_spinning, 100);
        }
    }

    public void playSlotMachineNoCoins(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.slot_machine_no_coins, 100);
        }
    }

    public void playSlotMachineWheelStop(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.slot_machine_wheel_stop, 100);
        }
    }

    public void playCollectChipsToPot(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.collect_chips_to_pot, 100);
        }
    }

    public void playCheckSound(Context context) {
        if (ENABLE_SOUNDS) {
            play(context, R.raw.check_sound, 0.2f);
        }
    }

} 