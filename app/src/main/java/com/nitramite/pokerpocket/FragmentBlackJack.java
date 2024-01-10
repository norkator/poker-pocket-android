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

public class FragmentBlackJack extends Fragment {

    // Variables
    private AudioPlayer audioPlayer = new AudioPlayer();

    // Constructor
    public FragmentBlackJack() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_black_jack, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        ImageView blackJackInfoBtn = (ImageView)getActivity().findViewById(R.id.blackJackInfoBtn);
        LinearLayout blackJackNormalModeBtn = (LinearLayout) getActivity().findViewById(R.id.blackJackNormalModeBtn);
        LinearLayout blackJackCountingModeBtn = (LinearLayout) getActivity().findViewById(R.id.blackJackCountingModeBtn);
        Button blackJackOfflineLeaderBoardBtn = (Button)getActivity().findViewById(R.id.blackJackOfflineLeaderBoardBtn);
        ImageView blackJackOfflineSettingsBtn = (ImageView)getActivity().findViewById(R.id.blackJackOfflineSettingsBtn);

        // Normal mode
        blackJackNormalModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BlackJackOffline.class);
                intent.putExtra("mode", 0); // Game mode
                startActivity(intent);
            }
        });

        // Counting mode
        blackJackCountingModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BlackJackOffline.class);
                intent.putExtra("mode", 1); // Game mode
                startActivity(intent);
            }
        });

        blackJackOfflineLeaderBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });

        blackJackOfflineSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardSlideOne(getActivity());
                blackJackOfflineSettingsDialog();
            }
        });

        // Blackjack introduction
        blackJackInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.nitramite.com/blackjack-tutorial.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    } // End of onActivityCreated()



    // Holdem offline settings dialog
    private void blackJackOfflineSettingsDialog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.offline_game_settings_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final TextView titleTV = (TextView)dialog.findViewById(R.id.titleTV);
        titleTV.setText("Blackjack offline settings");
        final Switch autoNextRoundSW = (Switch)dialog.findViewById(R.id.autoNextRoundSW);
        autoNextRoundSW.setEnabled(false);
        final Switch showDebugWindowSW = (Switch)dialog.findViewById(R.id.showDebugWindowSW);
        showDebugWindowSW.setEnabled(false);
        final Switch showTutorialWindowSW = (Switch)dialog.findViewById(R.id.showTutorialWindowSW);
        final LinearLayout optionTwoView = (LinearLayout)dialog.findViewById(R.id.optionTwoView);
        optionTwoView.setVisibility(View.GONE);
        final TextView optionOneTitle = (TextView)dialog.findViewById(R.id.optionOneTitle);
        optionOneTitle.setText("-");
        Button optionOneIncrementBtn = (Button)dialog.findViewById(R.id.optionOneIncrementBtn);
        optionOneIncrementBtn.setEnabled(false);
        Button optionOneDecrementBtn = (Button)dialog.findViewById(R.id.optionOneDecrementBtn);
        optionOneDecrementBtn.setEnabled(false);
        final TextView optionOneCountTV = (TextView)dialog.findViewById(R.id.optionOneCountTV);
        optionOneCountTV.setText("0");
        showTutorialWindowSW.setChecked(sp.getBoolean(Constants.SP_OFFLINE_BLACKJACK_SHOW_TUTORIAL_WINDOW, false));
        Button saveSettingsBtn = (Button)dialog.findViewById(R.id.saveSettingsBtn);
        showTutorialWindowSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });
        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                SharedPreferences.Editor editor = setSharedPreferences.edit();
                editor.putBoolean(Constants.SP_OFFLINE_BLACKJACK_SHOW_TUTORIAL_WINDOW, showTutorialWindowSW.isChecked());
                editor.apply();
                dialog.dismiss();
                audioPlayer.playCardPlaceTwo(getActivity());
            }
        });
    }



} // End of fragment