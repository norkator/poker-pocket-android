package com.nitramite.pokerpocket;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class FragmentOthers extends Fragment {

    // Variables
    private AudioPlayer audioPlayer = new AudioPlayer();
    private int handSize = 0;
    private int playersCount = 0;

    // Constructor
    public FragmentOthers() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_others, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        LinearLayout virtualDeckBtn = (LinearLayout)getActivity().findViewById(R.id.virtualDeckBtn);
        ImageView virtualDeckSettingsBtn = (ImageView)getActivity().findViewById(R.id.virtualDeckSettingsBtn);

        // Virtual deck
        virtualDeckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), VirtualDeck.class));
            }
        });

        // Virtual deck settings
        virtualDeckSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardSlideOne(getActivity());
                virtualDeckSettingsDialog();
            }
        });
    }



    // ---------------------------------------------------------------------------------------------


    // Virtual deck settings
    private void virtualDeckSettingsDialog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        playersCount = sp.getInt(Constants.SP_VIRTUAL_DECK_PLAYERS_COUNT, 4);
        handSize = sp.getInt(Constants.SP_VIRTUAL_DECK_PLAYER_HAND_SIZE, 5);
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.offline_game_settings_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final TextView titleTV = (TextView)dialog.findViewById(R.id.titleTV);
        titleTV.setText("Virtual deck settings");
        final Switch autoNextRoundSW = (Switch)dialog.findViewById(R.id.autoNextRoundSW);
        autoNextRoundSW.setEnabled(false);
        final Switch showDebugWindowSW = (Switch)dialog.findViewById(R.id.showDebugWindowSW);
        showDebugWindowSW.setEnabled(false);
        final Switch showTutorialWindowSW = (Switch)dialog.findViewById(R.id.showTutorialWindowSW);
        showTutorialWindowSW.setEnabled(false);

        // Number option menu one
        final TextView optionOneTitle = (TextView)dialog.findViewById(R.id.optionOneTitle);
        optionOneTitle.setText("Player count (2 - 6)");
        final Button optionOneIncrementBtn = (Button)dialog.findViewById(R.id.optionOneIncrementBtn);
        final TextView optionOneCountTV = (TextView)dialog.findViewById(R.id.optionOneCountTV);
        final Button optionOneDecrementBtn = (Button)dialog.findViewById(R.id.optionOneDecrementBtn);
        optionOneCountTV.setText(String.valueOf(playersCount));
        final Button saveSettingsBtn = (Button)dialog.findViewById(R.id.saveSettingsBtn);
        optionOneIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playersCount < 6) {
                    audioPlayer.playCardChipLayOne(getActivity());
                    playersCount = playersCount + 1;
                    optionOneCountTV.setText(String.valueOf(playersCount));
                }
            }
        });
        optionOneDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playersCount > 2) {
                    audioPlayer.playCardChipLayOne(getActivity());
                    playersCount = playersCount - 1;
                    optionOneCountTV.setText(String.valueOf(playersCount));
                }
            }
        });

        // Number option menu two
        final LinearLayout optionTwoView = (LinearLayout)dialog.findViewById(R.id.optionTwoView);
        optionTwoView.setVisibility(View.VISIBLE);
        final TextView optionTwoTitle = (TextView)dialog.findViewById(R.id.optionTwoTitle);
        optionTwoTitle.setText("Player hand size");
        final Button optionTwoIncrementBtn = (Button)dialog.findViewById(R.id.optionTwoIncrementBtn);
        final TextView optionTwoCountTV = (TextView)dialog.findViewById(R.id.optionTwoCountTV);
        optionTwoCountTV.setText(String.valueOf(handSize));
        final Button optionTwoDecrementBtn = (Button)dialog.findViewById(R.id.optionTwoDecrementBtn);
        optionTwoIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (handSize < 10) {
                    audioPlayer.playCardChipLayOne(getActivity());
                    handSize = handSize + 1;
                    optionTwoCountTV.setText(String.valueOf(handSize));
                }
            }
        });
        optionTwoDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (handSize > 1) {
                    audioPlayer.playCardChipLayOne(getActivity());
                    handSize = handSize - 1;
                    optionTwoCountTV.setText(String.valueOf(handSize));
                }
            }
        });

        // Saving settings
        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                SharedPreferences.Editor editor = setSharedPreferences.edit();
                editor.putInt(Constants.SP_VIRTUAL_DECK_PLAYER_HAND_SIZE, handSize);
                editor.putInt(Constants.SP_VIRTUAL_DECK_PLAYERS_COUNT, playersCount);
                editor.apply();
                dialog.dismiss();
                audioPlayer.playCardPlaceTwo(getActivity());
            }
        });
    }


    // ---------------------------------------------------------------------------------------------


} // End of fragment