package com.example.labwork;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AFragment extends Fragment {

    private static final String ARG_TEKSTAS = "tekstas";

    public AFragment() {
        super(R.layout.fragment_a);
    }

    // Sukuria fragmentą ir "įdeda" į jį tekstą
    public static AFragment naujas(String tekstas) {
        AFragment fragmentas = new AFragment();
        Bundle argumentai = new Bundle();
        argumentai.putString(ARG_TEKSTAS, tekstas);
        fragmentas.setArguments(argumentai);
        return fragmentas;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String tekstas = requireArguments().getString(ARG_TEKSTAS, "");

        int kiekA = 0;
        for (char c : tekstas.toCharArray()) {
            if (c == 'a' || c == 'A') {
                kiekA++;
            }
        }

        TextView tv = view.findViewById(R.id.tvARezultatas);
        tv.setText(getString(R.string.lab5_a_rezultatas, tekstas, kiekA));
    }
}