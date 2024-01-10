package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nitramite.adapters.SlotMachineAdapter;
import com.nitramite.wheel.OnWheelChangedListener;
import com.nitramite.wheel.OnWheelScrollListener;
import com.nitramite.wheel.WheelView;

import java.util.ArrayList;
import java.util.Arrays;

public class SlotMachine extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {

    // Logging
    private final static String TAG = SlotMachine.class.getSimpleName();

    // Shared prefs
    private SharedPreferences sharedPreferences;

    // Game controlling
    private double MY_MONEY = 10.0;
    private double POT = 0.0;
    private boolean coinInsertEnabled = true;
    private boolean lockScrollFinishListener = false;

    // Staging
    private int CURRENT_STAGE = 1;
    private final int STAGE_ONE_MIX = 1;
    private final int STAGE_TWO_LOCK = 2;
    private final int STAGE_THREE_MIX = 3;

    // Activity features
    private AudioPlayerSP audioPlayer;
    private LinearLayout coinSlotIV;
    private FrameLayout fiftyCentsCoin, oneDollarCoin, twoDollarCoin;
    private LinearLayout startBtn, lockOneBtn, lockTwoBtn, lockThreeBtn, lockFourBtn;
    private TextView potTV, resultTV, myMoneyTV;
    private final ArrayList<WheelView> wheelViews = new ArrayList<>();
    private final Boolean[] wheelsLocked = {false, false, false, false};
    private AlphaAnimation blinkAnimation;


    protected void onDestroy(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_machine);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on always
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Audio player
        audioPlayer = new AudioPlayerSP(this);

        // Find views
        coinSlotIV = findViewById(R.id.coinSlotIV);
        coinSlotIV.setOnDragListener(this);
        fiftyCentsCoin = findViewById(R.id.fiftyCentsCoin);
        oneDollarCoin = findViewById(R.id.oneDollarCoin);
        twoDollarCoin = findViewById(R.id.twoDollarCoin);
        fiftyCentsCoin.setTag(0.50);
        oneDollarCoin.setTag(1.0);
        twoDollarCoin.setTag(2.0);
        fiftyCentsCoin.setOnDragListener(this);
        fiftyCentsCoin.setOnTouchListener(this);
        oneDollarCoin.setOnDragListener(this);
        oneDollarCoin.setOnTouchListener(this);
        twoDollarCoin.setOnDragListener(this);
        twoDollarCoin.setOnTouchListener(this);
        startBtn = findViewById(R.id.startBtn);
        startBtn.setSoundEffectsEnabled(false);
        resultTV = findViewById(R.id.resultTV);
        setCustomTypefaces(resultTV);
        potTV = findViewById(R.id.potTV);
        setCustomTypefaces(potTV);
        myMoneyTV = findViewById(R.id.myMoneyTV);
        setCustomTypefaces(myMoneyTV);
        final WheelView wheelOne = findViewById(R.id.wheelOne);
        final WheelView wheelTwo = findViewById(R.id.wheelTwo);
        final WheelView wheelThree = findViewById(R.id.wheelThree);
        final WheelView wheelFour = findViewById(R.id.wheelFour);
        wheelViews.add(wheelOne);
        wheelViews.add(wheelTwo);
        wheelViews.add(wheelThree);
        wheelViews.add(wheelFour);
        lockOneBtn = findViewById(R.id.lockOneBtn);
        lockTwoBtn = findViewById(R.id.lockTwoBtn);
        lockThreeBtn = findViewById(R.id.lockThreeBtn);
        lockFourBtn = findViewById(R.id.lockFourBtn);
        lockOneBtn.setSoundEffectsEnabled(false);
        lockTwoBtn.setSoundEffectsEnabled(false);
        lockThreeBtn.setSoundEffectsEnabled(false);
        lockFourBtn.setSoundEffectsEnabled(false);

        // Get settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        MY_MONEY = sharedPreferences.getFloat(Constants.SP_SLOT_MACHINE_MONEY_LEFT, 10.00f);
        setMyMoney(MY_MONEY);

        // Init animation
        blinkAnimation = new AlphaAnimation(1, 0.5f);
        blinkAnimation.setDuration(500);
        blinkAnimation.setInterpolator(new LinearInterpolator());
        blinkAnimation.setRepeatCount(10000);
        blinkAnimation.setRepeatMode(Animation.REVERSE);

        // Init click listeners
        setOnClickListeners();

        // Init wheel's
        initWheels();
    } // End of onCreate();

    // ---------------------------------------------------------------------------------------------


    // Always run from 'start' button
    private void mixController() {
        lockScrollFinishListener = false;
        if (POT == 0.0) {
            audioPlayer.playSlotMachineNoCoins();
            if (MY_MONEY <= 0.0) {
                runningOutOfMoneyDialog();
            } else {
                resultTV.setText("! INSERT COINS !");
            }
        } else {
            CURRENT_STAGE = CURRENT_STAGE + 1;
            coinInsertEnabled = false;
            if (noneLocked() && CURRENT_STAGE == STAGE_THREE_MIX) {
                Toast.makeText(this, "Resetting...", Toast.LENGTH_SHORT).show();
                resetPot();
                disableLockButtons();
                CURRENT_STAGE = 1;
                beforeWheelSpin();
                startBtn.startAnimation(blinkAnimation);
            } else {
                if (CURRENT_STAGE == STAGE_THREE_MIX) {
                    disableLockButtons();
                }
                beforeWheelSpin();
                mixWheels();
            }
        }
    }


    // Game controller
    private void gameController() {
        switch (CURRENT_STAGE) {
            case STAGE_TWO_LOCK:
                beforeWheelSpin();
                enableLockButtons();
                if (isWheelsMatching()) {
                    updateTextViews();
                    wheelsLocked[0] = false;
                    wheelsLocked[1] = false;
                    wheelsLocked[2] = false;
                    wheelsLocked[3] = false;
                    CURRENT_STAGE = 1;
                }
                break;
            case STAGE_THREE_MIX:
                updateTextViews();
                wheelsLocked[0] = false;
                wheelsLocked[1] = false;
                wheelsLocked[2] = false;
                wheelsLocked[3] = false;
                CURRENT_STAGE = 1;
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Wheels match (win)
    private boolean isWheelsMatching() {
        int a = wheelViews.get(0).getCurrentItem();
        int b = wheelViews.get(1).getCurrentItem();
        int c = wheelViews.get(2).getCurrentItem();
        int d = wheelViews.get(3).getCurrentItem();
        return a == b && b == c && c == d;
    }

    // Three matches (gives half of the money back)
    private boolean isThreeMatchingOnes() {
        int[] arr = new int[4];
        arr[0] = wheelViews.get(0).getCurrentItem();
        arr[1] = wheelViews.get(1).getCurrentItem();
        arr[2] = wheelViews.get(2).getCurrentItem();
        arr[3] = wheelViews.get(3).getCurrentItem();
        Arrays.sort(arr);
        return (arr[0] == arr[1] && arr[1] == arr[2]) || arr[1] == arr[2] && arr[2] == arr[3];
    }

    private void updateTextViews() {
        if (isWheelsMatching()) {
            win();
        } else {
            if (isThreeMatchingOnes()) {
                halfBack();
            } else {
                lose();
            }
        }
    }

    // Win event
    private void win() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            audioPlayer.playSlotMachineMoneyDropping();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        resultTV.setText("ROUND WIN ROUND WIN");
        MY_MONEY = MY_MONEY + (POT * 4);
        setMyMoney(MY_MONEY);
        resetPot();
    }


    private void halfBack() {
        resultTV.setText("3 SAME - HALF BACK");
        audioPlayer.playSlotMachineMoneyDroppingHalf();
        MY_MONEY = MY_MONEY + (POT / 2);
        setMyMoney(MY_MONEY);
        resetPot();
    }

    // Lose event
    private void lose() {
        resultTV.setText("No win");
        resetPot();
    }

    // ---------------------------------------------------------------------------------------------

    // Mix wheel's
    private void mixWheels() {
        audioPlayer.playSlotMachineWheelSpinning();
        for (int i = 0; i < wheelViews.size(); i++) {
            if (!wheelsLocked[i]) {
                wheelViews.get(i).scroll(-350 + (int) (Math.random() * 50), 2000);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Initialized wheel's
    private void initWheels() {
        startBtn.startAnimation(blinkAnimation);
        disableLockButtons();
        resetPot();
        for (int i = 0; i < wheelViews.size(); i++) {
            wheelViews.get(i).setViewAdapter(new SlotMachineAdapter(this, toDpi(50), toDpi(50)));
            wheelViews.get(i).setCurrentItem((int) (Math.random() * 10));
            wheelViews.get(i).addChangingListener(changedListener);
            wheelViews.get(i).addScrollingListener(scrolledListener);
            wheelViews.get(i).setCyclic(true);
            wheelViews.get(i).setEnabled(false);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void beforeWheelSpin() {
        startBtn.clearAnimation();
        resultTV.setText("-");
    }

    private void afterWheelSpin() {
        startBtn.startAnimation(blinkAnimation);
    }

    private void enableLockButtons() {
        lockOneBtn.startAnimation(blinkAnimation);
        lockTwoBtn.startAnimation(blinkAnimation);
        lockThreeBtn.startAnimation(blinkAnimation);
        lockFourBtn.startAnimation(blinkAnimation);
        lockOneBtn.setEnabled(true);
        lockTwoBtn.setEnabled(true);
        lockThreeBtn.setEnabled(true);
        lockFourBtn.setEnabled(true);
    }

    private void disableLockButtons() {
        lockOneBtn.clearAnimation();
        lockTwoBtn.clearAnimation();
        lockThreeBtn.clearAnimation();
        lockFourBtn.clearAnimation();
        lockOneBtn.setEnabled(false);
        lockTwoBtn.setEnabled(false);
        lockThreeBtn.setEnabled(false);
        lockFourBtn.setEnabled(false);
    }

    // ---------------------------------------------------------------------------------------------

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
        }

        public void onScrollingFinished(WheelView wheel) {
            if (!lockScrollFinishListener) {
                lockScrollFinishListener = true;
                Log.i(TAG, "Finished call " + wheel.toString());
                audioPlayer.stopSlotMachineWheelSpinning();
                audioPlayer.playSlotMachineWheelStop();
                afterWheelSpin();
                gameController();
            }
        }
    };
    // Wheel changed listener
    OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
        }
    };

    private void setOnClickListeners() {
        startBtn.setOnClickListener(view -> mixController());
        startBtn.setOnLongClickListener(view -> {
            if (Constants.isDev) {
                MY_MONEY = MY_MONEY + 10.0;
                setMyMoney(MY_MONEY);
            }
            return true;
        });
        lockOneBtn.setOnClickListener(view -> {
            audioPlayer.playCardChipLayOne();
            if (lockWheel(0)) {
                lockOneBtn.clearAnimation();
                lockOneBtn.setEnabled(false);
            }
        });
        lockTwoBtn.setOnClickListener(view -> {
            audioPlayer.playCardChipLayOne();
            if (lockWheel(1)) {
                lockTwoBtn.clearAnimation();
                lockTwoBtn.setEnabled(false);
            }
        });
        lockThreeBtn.setOnClickListener(view -> {
            audioPlayer.playCardChipLayOne();
            if (lockWheel(2)) {
                lockThreeBtn.clearAnimation();
                lockThreeBtn.setEnabled(false);
            }
        });
        lockFourBtn.setOnClickListener(view -> {
            audioPlayer.playCardChipLayOne();
            if (lockWheel(3)) {
                lockFourBtn.clearAnimation();
                lockFourBtn.setEnabled(false);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------

    // Set custom typeface for wanted textView
    private void setCustomTypefaces(final TextView textView) {
        Typeface tf = Typeface.createFromAsset(getAssets(), "digit_font_one.ttf");
        textView.setTypeface(tf);
    }

    // ---------------------------------------------------------------------------------------------

    // Locks selected wheel
    private Boolean lockWheel(final int wheelPosition) {
        if (notAllLocked()) {
            wheelsLocked[wheelPosition] = true;
            return true;
        } else {
            Toast.makeText(this, "Cant lock more wheels!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // All cannot be locked
    private Boolean notAllLocked() {
        int lockedCount = 0;
        for (int i = 0; i < wheelsLocked.length; i++) {
            if (wheelsLocked[i]) {
                lockedCount = lockedCount + 1;
            }
        }
        return lockedCount < 3;
    }

    // If none locked, start over again
    private Boolean noneLocked() {
        int lockedCount = 0;
        for (int i = 0; i < wheelsLocked.length; i++) {
            if (wheelsLocked[i]) {
                lockedCount = lockedCount + 1;
            }
        }
        return lockedCount == 0;
    }

    // Insert coin
    private void insertCoin(final double amount) {
        if (amount <= MY_MONEY) {
            audioPlayer.playSlotMachineInsertCoin();
            MY_MONEY = MY_MONEY - amount;
            POT = POT + amount;
            setMyMoney(MY_MONEY);
            setPot(POT);
        } else {
            if (MY_MONEY <= 0.0) {
                runningOutOfMoneyDialog();
            }
            resultTV.setText("! NO MONEY !");
        }
    }

    // Update my money
    private void setMyMoney(final double moneyLeft) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(Constants.SP_SLOT_MACHINE_MONEY_LEFT, (float) MY_MONEY);
        editor.apply();
        myMoneyTV.setText(moneyLeft + "$");
    }

    // Update pot
    private void setPot(final double pot) {
        potTV.setText(pot + "$");
    }

    // Reset pot
    private void resetPot() {
        coinInsertEnabled = true;
        POT = 0.0;
        setPot(POT);
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDrag(View targetView, DragEvent dragEvent) {
        int action = dragEvent.getAction();
        View coin = (View) dragEvent.getLocalState();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                break;
            case DragEvent.ACTION_DROP:
                if (coinInsertEnabled) {
                    insertCoin((double) coin.getTag());
                    coin.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Cannot insert more coins middle of game...", Toast.LENGTH_SHORT).show();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                if (dropEventNotHandled(dragEvent)) {
                    coin.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean dropEventNotHandled(DragEvent dragEvent) {
        return !dragEvent.getResult();
    }

    // ---------------------------------------------------------------------------------------------

    // To dpi
    private int toDpi(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // ---------------------------------------------------------------------------------------------

    private void runningOutOfMoneyDialog() {
        new AlertDialog.Builder(SlotMachine.this)
                .setTitle("Funds")
                .setMessage("Looks like you are running out of funds. You have " + MY_MONEY + "$ left. Click ok to get additional 6.00$ of funds more?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    MY_MONEY = MY_MONEY + 6.0;
                    setMyMoney(MY_MONEY);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // Return
                })
                .setIcon(R.mipmap.logo)
                .show();
    }

    // ---------------------------------------------------------------------------------------------

} 