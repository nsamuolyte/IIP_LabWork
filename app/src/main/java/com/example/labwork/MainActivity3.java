package com.example.labwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity3 extends AppCompatActivity {
    private TextView tvSakinys;
    private String sakinys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvSakinys = findViewById(R.id.tvSakinys);
        Button btnSkaiciuok = findViewById(R.id.btnSkaiciuok);

        // Gaunam sakinį iš 1 veiklos
        sakinys = getIntent().getStringExtra(MainActivity1.EXTRA_TEKSTAS);
        if (sakinys == null) sakinys = "";
        tvSakinys.setText(sakinys);

        // (iii) Žodžiai suskaičiuojami tik paspaudus mygtuką
        btnSkaiciuok.setOnClickListener(v -> tvSakinys.setText(rezultatas()));
        // Mygtukas "Atgal" – veikia taip pat kaip ◁
        Button btnAtgal = findViewById(R.id.btnAtgal);
        btnAtgal.setOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        // (iv) Paspaudus "Atgal" ◁ – rezultatas grąžinamas į 1 veiklą
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent atsakymas = new Intent();
                atsakymas.putExtra(MainActivity1.EXTRA_REZULTATAS, rezultatas());
                setResult(RESULT_OK, atsakymas);
                finish();
            }
        });
    }
    // Sudaro sakinį: Sakinyje „...“ esti 4 žodžiai
    private String rezultatas() {
        int n = skaiciuotiZodzius(sakinys);
        return "Sakinyje „" + sakinys + "“ esti " + n + " " + zodzioForma(n);
    }

    // Suskaičiuoja žodžius (atskirtus tarpais)
    private int skaiciuotiZodzius(String tekstas) {
        String t = tekstas.trim();
        if (t.isEmpty()) return 0;
        return t.split("\\s+").length;
    }

    // Teisinga lietuviška forma: 1 žodis, 2–9 žodžiai, 10–20 žodžių
    private String zodzioForma(int n) {
        int p = n % 10;
        int d = n % 100;
        if (p == 1 && d != 11) return "žodis";
        if (p >= 2 && (d < 10 || d > 20)) return "žodžiai";
        return "žodžių";
    }
}