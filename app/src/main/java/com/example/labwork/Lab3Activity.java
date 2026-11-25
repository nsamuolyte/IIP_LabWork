package com.example.labwork;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Lab3Activity extends AppCompatActivity {
    private EditText etPavadinimas;
    private AutoCompleteTextView acPadalinys;
    private RatingBar rbSudetingumas;
    private TimePicker tpLaikas;
    private DatePicker dpData;
    private Spinner spMiestas;
    private MaterialSwitch swRegistruoti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etPavadinimas = findViewById(R.id.etPavadinimas);
        acPadalinys = findViewById(R.id.acPadalinys);
        rbSudetingumas = findViewById(R.id.rbSudetingumas);
        tpLaikas = findViewById(R.id.tpLaikas);
        dpData = findViewById(R.id.dpData);
        spMiestas = findViewById(R.id.spMiestas);
        swRegistruoti = findViewById(R.id.swRegistruoti);
        Button btnSaugoti = findViewById(R.id.btnSaugoti);

        // 2.1 Padalinių masyvas nuskaitomas iš strings.xml programiškai
        String[] padaliniai = getResources().getStringArray(R.array.padaliniai);
        ArrayAdapter<String> padaliniuAdapteris = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, padaliniai);
        acPadalinys.setAdapter(padaliniuAdapteris);

        // 2.2 Pasirinkus padalinį – Toast
        acPadalinys.setOnItemClickListener((parent, view, position, id) ->
                Toast.makeText(this, "Pasirinktas padalinys: " + parent.getItemAtPosition(position),
                        Toast.LENGTH_SHORT).show());

        // 6 Miestų sąrašas Spinner'yje
        String[] miestai = getResources().getStringArray(R.array.miestai);
        ArrayAdapter<String> miestuAdapteris = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, miestai);
        miestuAdapteris.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMiestas.setAdapter(miestuAdapteris);

        // 4 Laikas 24 val. formatu
        tpLaikas.setIs24HourView(true);

        // 8 Mygtukas surenka duomenis ir parodo Snackbar
        btnSaugoti.setOnClickListener(v -> rodytiSnackbar(sudarytiEilute()));
    }

    // 8 Nuskaito visų objektų reikšmes ir sudaro eilutę
    private String sudarytiEilute() {
        String pavadinimas = etPavadinimas.getText().toString().trim();
        String padalinys = acPadalinys.getText().toString().trim();
        int sudetingumas = (int) rbSudetingumas.getRating();
        int valanda = tpLaikas.getHour();
        int minutes = tpLaikas.getMinute();
        int metai = dpData.getYear();
        int menuo = dpData.getMonth() + 1;   // mėnesiai skaičiuojami nuo 0!
        int diena = dpData.getDayOfMonth();
        String miestas = spMiestas.getSelectedItem().toString();
        String registruoti = swRegistruoti.isChecked() ? "taip" : "ne";

        return String.format(Locale.getDefault(),
                "Pavadinimas: %s; sudėtingumas: %d;\nLaikas: %02d:%02d, data: %d-%02d-%02d\n"
                        + "Padalinys: %s; Miestas: %s; Registruoti: %s",
                pavadinimas, sudetingumas, valanda, minutes, metai, menuo, diena,
                padalinys, miestas, registruoti);
    }

    // 9–11 Snackbar su eilute, 6 s trukme ir paspaudžiamu tekstu
    private void rodytiSnackbar(String eilute) {
        View ekranas = findViewById(R.id.main);
        Snackbar snackbar = Snackbar.make(ekranas, eilute, 6000);   // 6000 ms = 6 s

        // Kad tilptų visos eilutės (pagal nutylėjimą rodo tik 2)
        TextView tekstas = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        tekstas.setMaxLines(6);

        // 10 Paspaudžiamas tekstas – įrašo failą
        snackbar.setAction("Patvirtinu, informacija teisinga", v -> irasytiFaila(eilute));

        // 11 Jei per 6 s niekas nepaspausta – pranešimas
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar sb, int event) {
                if (event == DISMISS_EVENT_TIMEOUT) {
                    Snackbar.make(ekranas,
                            "Laikas baigėsi, bandykite dar kartą išsaugoti duomenis",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
        snackbar.show();
    }

    // 10 Įrašo eilutę į vidinę atmintį; failo vardas – iš EditText be tarpų + .txt
    private void irasytiFaila(String eilute) {
        String vardas = etPavadinimas.getText().toString().replaceAll("\\s+", "");
        if (vardas.isEmpty()) vardas = "duomenys";
        String failas = vardas + ".txt";

        try (FileOutputStream fos = openFileOutput(failas, MODE_PRIVATE)) {
            fos.write(eilute.getBytes(StandardCharsets.UTF_8));

            String kelias = new File(getFilesDir(), failas).getAbsolutePath();
            Snackbar pranesimas = Snackbar.make(findViewById(R.id.main),
                    "Duomenys įrašyti sėkmingai: " + kelias, Snackbar.LENGTH_LONG);
            TextView t = pranesimas.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            t.setMaxLines(4);
            pranesimas.show();
        } catch (IOException e) {
            Toast.makeText(this, "Klaida įrašant: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}