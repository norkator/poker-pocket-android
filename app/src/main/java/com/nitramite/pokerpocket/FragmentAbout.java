package com.nitramite.pokerpocket;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class FragmentAbout extends Fragment {

    // Constructor
    public FragmentAbout() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_about, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        LinearLayout nitramiteBtn = (LinearLayout)getActivity().findViewById(R.id.nitramiteBtn);
        nitramiteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainMenu)getActivity()).openNitramiteLink();
            }
        });
    }

} // End of fragment
