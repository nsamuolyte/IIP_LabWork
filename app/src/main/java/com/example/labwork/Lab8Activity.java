package com.example.labwork;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Lab8Activity extends AppCompatActivity {
    private MediaPlayer sekmesGarsas;
    private MediaPlayer nesekmesGarsas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab8);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sekmesGarsas = MediaPlayer.create(this, R.raw.sekme);
        nesekmesGarsas = MediaPlayer.create(this, R.raw.nesekme);

        FiguruView figuros = findViewById(R.id.figuruView);
        figuros.setRezultatoKlausytojas((pavyko, figurosSkersmuo, apskritimoSkersmuo) -> {
            MediaPlayer garsas = pavyko ? sekmesGarsas : nesekmesGarsas;
            garsas.seekTo(0);
            garsas.start();

            String tekstas = getString(pavyko ? R.string.lab8_pavyko : R.string.lab8_nepavyko,
                    figurosSkersmuo, apskritimoSkersmuo);
            Toast.makeText(this, tekstas, Toast.LENGTH_SHORT).show();
        });
    }

    // Uždarant – atlaisvinam garso grotuvus
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sekmesGarsas.release();
        nesekmesGarsas.release();
    }
}