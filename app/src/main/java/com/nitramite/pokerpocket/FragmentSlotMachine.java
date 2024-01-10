package com.nitramite.pokerpocket;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentSlotMachine extends Fragment {

    // Variables
    private AudioPlayer audioPlayer = new AudioPlayer();

    // Constructor
    public FragmentSlotMachine() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment_slot_machine, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Find components
        LinearLayout slotMachineBtn = (LinearLayout)getActivity().findViewById(R.id.slotMachineBtn);

        // Slot machine
        slotMachineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SlotMachine.class));
            }
        });

    }

} // End of fragment