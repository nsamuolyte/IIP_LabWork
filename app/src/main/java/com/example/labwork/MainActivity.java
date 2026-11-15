package com.example.labwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Raktai, pagal kuriuos perduodami duomenys tarp veiklų
    public static final String EXTRA_TEKSTAS = "tekstas";
    public static final String EXTRA_REZULTATAS = "rezultatas";

    // (1) Globalūs intento objektai
    private Intent intentas2;
    private Intent intentas3;

    private TextView tvRezultatai;
    private String ivestasTekstas = "";

    // (2) registerForActivityResult() – laukia atsakymo iš 2 veiklos
    private final ActivityResultLauncher<Intent> launcher2 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ivestasTekstas = result.getData().getStringExtra(EXTRA_TEKSTAS);
                    tvRezultatai.setText(ivestasTekstas);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvRezultatai = findViewById(R.id.tvRezultatai);
        Button btnIvesti = findViewById(R.id.btnIvesti);

        intentas2 = new Intent(this, MainActivity2.class);

        btnIvesti.setOnClickListener(v -> {
            intentas2.putExtra(EXTRA_TEKSTAS, ivestasTekstas);
            launcher2.launch(intentas2);
        });
    }
}