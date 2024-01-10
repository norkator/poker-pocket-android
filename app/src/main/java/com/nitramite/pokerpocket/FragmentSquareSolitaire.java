package com.nitramite.pokerpocket;

import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FragmentSquareSolitaire extends Fragment {

    // Variables
    private AudioPlayer audioPlayer = new AudioPlayer();

    // Constructor
    public FragmentSquareSolitaire() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_square_solitaire, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        LinearLayout squareSolitaireBtn = (LinearLayout)getActivity().findViewById(R.id.squareSolitaireBtn);
        ImageView squareSolitaireInfoBtn = (ImageView)getActivity().findViewById(R.id.squareSolitaireInfoBtn);

        // Square solitaire
        squareSolitaireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SquareSolitaire.class));
            }
        });

        // Five card draw introduction
        squareSolitaireInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.nitramite.com/solitaire-tutorial.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }


} // End of fragment