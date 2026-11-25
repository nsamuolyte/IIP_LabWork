package com.example.labwork;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BeAFragment extends Fragment {

    private static final String ARG_TEKSTAS = "tekstas";
    private static final String BALSES = "aąeęėiįyouųū";

    public BeAFragment() {
        super(R.layout.fragment_be_a);
    }

    public static BeAFragment naujas(String tekstas) {
        BeAFragment fragmentas = new BeAFragment();
        Bundle argumentai = new Bundle();
        argumentai.putString(ARG_TEKSTAS, tekstas);
        fragmentas.setArguments(argumentai);
        return fragmentas;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String tekstas = requireArguments().getString(ARG_TEKSTAS, "");

        int ilgis = tekstas.length();
        int balses = 0, didziosios = 0, mazosios = 0;

        for (char c : tekstas.toCharArray()) {
            if (BALSES.indexOf(Character.toLowerCase(c)) >= 0) balses++;
            if (Character.isUpperCase(c)) didziosios++;
            if (Character.isLowerCase(c)) mazosios++;
        }

        TextView tv = view.findViewById(R.id.tvBeARezultatas);
        tv.setText(getString(R.string.lab5_be_a_rezultatas,
                tekstas, ilgis, balses, didziosios, mazosios));
    }
}
