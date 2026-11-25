package com.example.labwork;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class SarasoFragment extends Fragment {

    public SarasoFragment() {
        super(R.layout.fragment_sarasas);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView lvSarasas = view.findViewById(R.id.lvSarasas);
        String[] irasai = getResources().getStringArray(R.array.lab5_irasai);

        ArrayAdapter<String> adapteris = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, irasai);
        lvSarasas.setAdapter(adapteris);

        lvSarasas.setOnItemClickListener((parent, v, position, id) -> {
            String tekstas = irasai[position];

            // Yra A raidė -> 2 fragmentas, nėra -> 3 fragmentas
            Fragment rezultatas;
            if (tekstas.toLowerCase(Locale.ROOT).contains("a")) {
                rezultatas = AFragment.naujas(tekstas);
            } else {
                rezultatas = BeAFragment.naujas(tekstas);
            }

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.detaliuKonteineris, rezultatas)
                    .addToBackStack(null)
                    .commit();
        });
    }
}