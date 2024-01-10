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

public class FragmentSevens extends Fragment {

    // Variables
    private AudioPlayer audioPlayer = new AudioPlayer();

    // Constructor
    public FragmentSevens() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_sevens, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        LinearLayout sevensBtn = (LinearLayout)getActivity().findViewById(R.id.sevensBtn);
        ImageView sevensInfoBtn = (ImageView)getActivity().findViewById(R.id.sevensInfoBtn);

        // Sevens
        sevensBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Sevens.class));
            }
        });

        // Sevens introduction
        sevensInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.nitramite.com/sevens-tutorial.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }


} // End of fragment