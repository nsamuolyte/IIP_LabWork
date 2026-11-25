package com.example.labwork;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;

public class Lab6Activity extends AppCompatActivity {

    private TextView tvPirmas;
    private TextView tvAntras;
    private TextView tvIsvedimas;

    private TextView paspaustasTv;     // ant kurio TextView atidarytas kontekstinis meniu
    private Thread vardijimoGija;      // gija, kuri išveda simbolius po vieną

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab6);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar paverčiam ActionBar'u
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvPirmas = findViewById(R.id.tvPirmas);
        tvAntras = findViewById(R.id.tvAntras);
        tvIsvedimas = findViewById(R.id.tvIsvedimas);

        // Kontekstinis meniu atsiras ilgai paspaudus ant šių laukų
        registerForContextMenu(tvPirmas);
        registerForContextMenu(tvAntras);
    }

    // Sukuriam viršutinį meniu iš XML
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lab6_meniu, menu);
        return true;
    }

    // Kas vyksta paspaudus viršutinio meniu punktą
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.meniuBaigti) {
            finishAffinity();                 // uždaro visą programą
            return true;
        } else if (id == R.id.meniuValanduSkirtumas) {
            rodytiLaikoDialoga();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Laiko įvedimo dialogas
    private void rodytiLaikoDialoga() {
        Calendar dabar = Calendar.getInstance();
        int dabarValanda = dabar.get(Calendar.HOUR_OF_DAY);
        int dabarMinute = dabar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, valanda, minute) -> {
            int dabarMin = dabarValanda * 60 + dabarMinute;
            int pasirinktaMin = valanda * 60 + minute;
            int skirtumas = Math.abs(pasirinktaMin - dabarMin);

            String tekstas = "Skirtumas tarp dabar ir nurodyto laiko yra " + skirtumas + " min.";
            tvPirmas.setText(tekstas);                       // į 1 TextView
            rodytiPranesima("Laiko skirtumas", tekstas);     // ir į AlertDialog
        }, dabarValanda, dabarMinute, true).show();
    }

    // Bendras AlertDialog
    private void rodytiPranesima(String antraste, String tekstas) {
        new AlertDialog.Builder(this)
                .setTitle(antraste)
                .setMessage(tekstas)
                .setPositiveButton("Gerai", null)
                .show();
    }

    // Sukuriam kontekstinį meniu ir įsimenam, ant kurio lauko paspausta
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.lab6_kontekstinis, menu);
        paspaustasTv = (TextView) v;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        String tekstas = paspaustasTv.getText().toString();
        int id = item.getItemId();

        if (id == R.id.meniuSimboliuSkaicius) {
            String rezultatas = "Tekste yra " + tekstas.length() + " simbolių";
            tvAntras.setText(rezultatas);                        // į 2 TextView
            rodytiPranesima("Simbolių skaičius", rezultatas);    // ir į AlertDialog
            return true;
        } else if (id == R.id.meniuVardijimas) {
            vardytiSimbolius(tekstas);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void vardytiSimbolius(String tekstas) {
        // Jei ankstesnė gija dar veikia – sustabdom
        if (vardijimoGija != null) {
            vardijimoGija.interrupt();
        }

        vardijimoGija = new Thread(() -> {
            try {
                for (int i = 0; i < tekstas.length(); i++) {
                    String simbolis = String.valueOf(tekstas.charAt(i));
                    runOnUiThread(() -> tvIsvedimas.setText(simbolis));
                    Thread.sleep(1000);              // laukiam 1 sekundę
                }
            } catch (InterruptedException e) {
                // gija sustabdyta – nieko daryti nereikia
            }
        });
        vardijimoGija.start();
    }

    // Uždarant langą – sustabdom giją
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vardijimoGija != null) {
            vardijimoGija.interrupt();
        }
    }
}