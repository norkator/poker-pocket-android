package com.nitramite.pokerpocket;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class FragmentHoldem extends Fragment {

    // Logging
    private final static String TAG = FragmentHoldem.class.getSimpleName();

    // Variables
    private int botCount = 3; // For settings
    private AudioPlayer audioPlayer = new AudioPlayer();
    private SharedPreferences sharedPreferences;
    private Button holdemOnlineLoginLogoutBtn;
    private Button holdemOnlineMyStatisticsBtn;

    // Constructor
    public FragmentHoldem() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_holdem, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayout texasHoldemOnlineBtn = (LinearLayout) getActivity().findViewById(R.id.texasHoldemOnlineBtn);
        holdemOnlineLoginLogoutBtn = (Button) getActivity().findViewById(R.id.holdemOnlineLoginLogoutBtn);
        holdemOnlineMyStatisticsBtn = (Button) getActivity().findViewById(R.id.holdemOnlineMyStatisticsBtn);
        Button holdemOnlineRankingsBtn = (Button) getActivity().findViewById(R.id.holdemOnlineRankingsBtn);

        // Shared prefs
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());


        holdemOnlineLoginLogoutBtn.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), TexasHoldemLogin.class);
            startActivityForResult(i, 1);
        });

        holdemOnlineMyStatisticsBtn.setOnClickListener(view -> startActivity(new Intent(getActivity(), TexasHoldemStats.class)));

        holdemOnlineRankingsBtn.setOnClickListener(view -> startActivity(new Intent(getActivity(), TexasHoldemRankings.class)));

        texasHoldemOnlineBtn.setOnClickListener(view -> startActivity(new Intent(getActivity(), TexasHoldem.class)));

        onlineButtonChecks();
    } // End of onCreate();


    public void onlineButtonChecks() {
        if (sharedPreferences.getBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false)) {
            holdemOnlineLoginLogoutBtn.setText("LOGOUT");
            holdemOnlineMyStatisticsBtn.setEnabled(true);
        } else {
            holdemOnlineLoginLogoutBtn.setText("LOGIN");
            holdemOnlineMyStatisticsBtn.setEnabled(false);
        }
    }


    // ---------------------------------------------------------------------------------------------


    // Hold'em offline settings dialog
    private void holdemOfflineSettingsDialog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        botCount = sp.getInt(Constants.SP_OFFLINE_HOLDEM_BOT_COUNT, 3);
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.offline_game_settings_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final TextView titleTV = (TextView) dialog.findViewById(R.id.titleTV);
        titleTV.setText("Hold'em offline settings");
        final Switch autoNextRoundSW = (Switch) dialog.findViewById(R.id.autoNextRoundSW);
        final Switch showDebugWindowSW = (Switch) dialog.findViewById(R.id.showDebugWindowSW);
        final Switch showTutorialWindowSW = (Switch) dialog.findViewById(R.id.showTutorialWindowSW);
        autoNextRoundSW.setChecked(sp.getBoolean(Constants.SP_OFFLINE_HOLDEM_AUTO_NEXT_ROUND, false));
        showDebugWindowSW.setChecked(sp.getBoolean(Constants.SP_OFFLINE_HOLDEM_SHOW_DEBUG_WINDOW, false));
        showTutorialWindowSW.setChecked(sp.getBoolean(Constants.SP_OFFLINE_HOLDEM_SHOW_TUTORIAL_WINDOW, false));
        autoNextRoundSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });
        showDebugWindowSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });
        showTutorialWindowSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });

        // Number option menu one
        final TextView optionOneTitle = (TextView) dialog.findViewById(R.id.optionOneTitle);
        optionOneTitle.setText("Player count (2 - 9 | Player + Bots)");
        Button optionOneIncrementBtn = (Button) dialog.findViewById(R.id.optionOneIncrementBtn);
        final TextView optionOneCountTV = (TextView) dialog.findViewById(R.id.optionOneCountTV);
        Button optionOneDecrementBtn = (Button) dialog.findViewById(R.id.optionOneDecrementBtn);
        Button saveSettingsBtn = (Button) dialog.findViewById(R.id.saveSettingsBtn);
        optionOneCountTV.setText(String.valueOf(botCount));
        optionOneIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (botCount < 6) {
                    audioPlayer.playCardChipLayOne(getActivity());
                    botCount = botCount + 1;
                    optionOneCountTV.setText(String.valueOf(botCount));
                }
            }
        });
        optionOneDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (botCount > 2) {
                    audioPlayer.playCardChipLayOne(getActivity());
                    botCount = botCount - 1;
                    optionOneCountTV.setText(String.valueOf(botCount));
                }
            }
        });

        // Number option menu two
        final LinearLayout optionTwoView = (LinearLayout) dialog.findViewById(R.id.optionTwoView);
        optionTwoView.setVisibility(View.GONE);

        // Save settings
        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                SharedPreferences.Editor editor = setSharedPreferences.edit();
                editor.putInt(Constants.SP_OFFLINE_HOLDEM_BOT_COUNT, botCount);
                editor.putBoolean(Constants.SP_OFFLINE_HOLDEM_AUTO_NEXT_ROUND, autoNextRoundSW.isChecked());
                editor.putBoolean(Constants.SP_OFFLINE_HOLDEM_SHOW_DEBUG_WINDOW, showDebugWindowSW.isChecked());
                editor.putBoolean(Constants.SP_OFFLINE_HOLDEM_SHOW_TUTORIAL_WINDOW, showTutorialWindowSW.isChecked());
                editor.apply();
                dialog.dismiss();
                audioPlayer.playCardPlaceTwo(getActivity());
            }
        });
    }


} // End of fragment