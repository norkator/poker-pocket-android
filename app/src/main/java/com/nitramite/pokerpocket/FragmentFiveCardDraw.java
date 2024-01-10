package com.nitramite.pokerpocket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
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

public class FragmentFiveCardDraw extends Fragment {

    // Variables
    private int botCount = 3; // For settings
    private AudioPlayer audioPlayer = new AudioPlayer();

    // Constructor
    public FragmentFiveCardDraw() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_five_card_draw, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        ImageView fiveCardDrawInfoBtn = (ImageView)getActivity().findViewById(R.id.fiveCardDrawInfoBtn);
        LinearLayout fiveCardDrawOfflineBtn = (LinearLayout) getActivity().findViewById(R.id.fiveCardDrawOfflineBtn);
        ImageView FiveCardDrawSettingsBtn = (ImageView) getActivity().findViewById(R.id.FiveCardDrawSettingsBtn);
        Button fiveCardDrawOfflineLeaderBoardBtn = (Button)getActivity().findViewById(R.id.fiveCardDrawOfflineLeaderBoardBtn);

        fiveCardDrawOfflineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), FiveCardDrawOffline.class));
            }
        });

        FiveCardDrawSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardSlideOne(getActivity());
                fiveCardDrawOfflineSettingsDialog();
            }
        });

        fiveCardDrawOfflineLeaderBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });

        // Five card draw introduction
        fiveCardDrawInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.nitramite.com/5-card-draw-tutorial.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    } // End of onActivityCreated()

    // ---------------------------------------------------------------------------------------------

    // Five Card Draw settings dialog
    private void fiveCardDrawOfflineSettingsDialog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        botCount = sp.getInt(Constants.SP_OFFLINE_FIVE_CARD_DRAW_BOT_COUNT, 3);
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.offline_game_settings_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final Switch showDebugWindowSW = (Switch)dialog.findViewById(R.id.showDebugWindowSW);
        showDebugWindowSW.setEnabled(false);
        final Switch showTutorialWindowSW = (Switch)dialog.findViewById(R.id.showTutorialWindowSW);
        showTutorialWindowSW.setEnabled(false);
        final Switch autoNextRoundSW = (Switch)dialog.findViewById(R.id.autoNextRoundSW);
        autoNextRoundSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playCardChipLayOne(getActivity());
            }
        });
        Button saveSettingsBtn = (Button)dialog.findViewById(R.id.saveSettingsBtn);
        autoNextRoundSW.setChecked(sp.getBoolean(Constants.SP_OFFLINE_FIVE_CARD_DRAW_AUTO_NEXT_ROUND, false));
        final TextView titleTV = (TextView)dialog.findViewById(R.id.titleTV);
        titleTV.setText("5-Card Draw offline settings");

        // Number option menu one
        final TextView optionOneTitle = (TextView)dialog.findViewById(R.id.optionOneTitle);
        optionOneTitle.setText("Player count (2 - 9 | Player + Bots)");
        Button optionOneIncrementBtn = (Button)dialog.findViewById(R.id.optionOneIncrementBtn);
        Button optionOneDecrementBtn = (Button)dialog.findViewById(R.id.optionOneDecrementBtn);
        final TextView optionOneCountTV = (TextView)dialog.findViewById(R.id.optionOneCountTV);
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
        final LinearLayout optionTwoView = (LinearLayout)dialog.findViewById(R.id.optionTwoView);
        optionTwoView.setVisibility(View.GONE);

        // Save settings
        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                SharedPreferences.Editor editor = setSharedPreferences.edit();
                editor.putInt(Constants.SP_OFFLINE_FIVE_CARD_DRAW_BOT_COUNT, botCount);
                editor.putBoolean(Constants.SP_OFFLINE_FIVE_CARD_DRAW_AUTO_NEXT_ROUND, autoNextRoundSW.isChecked());
                editor.apply();
                dialog.dismiss();
                audioPlayer.playCardPlaceTwo(getActivity());
            }
        });
    }

    // ---------------------------------------------------------------------------------------------

} // End of fragment