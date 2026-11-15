package com.example.labwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
    // (2) registerForActivityResult() – laukia atsakymo iš 3 veiklos
    private final ActivityResultLauncher<Intent> launcher3 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        tvRezultatai.setText(result.getData().getStringExtra(EXTRA_REZULTATAS));
                    }
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
        Button btnSkaiciuoti = findViewById(R.id.btnSkaiciuoti);

        intentas3 = new Intent(this, MainActivity3.class);

        btnSkaiciuoti.setOnClickListener(v -> {
            intentas3.putExtra(EXTRA_TEKSTAS, ivestasTekstas);
            launcher3.launch(intentas3);
        });
        Button btnSiusti = findViewById(R.id.btnSiusti);

        // (vi) Neišreikštinis intentas su ACTION_SEND
        btnSiusti.setOnClickListener(v -> {
            Intent siuntimas = new Intent();
            siuntimas.setAction(Intent.ACTION_SEND);
            siuntimas.putExtra(Intent.EXTRA_TEXT, tvRezultatai.getText().toString());
            siuntimas.setType("text/plain");
            startActivity(Intent.createChooser(siuntimas, "Siųsti tekstą su rezultatais"));
        });
        // (viii) Jei programa atidaryta per "Siųsti" meniu – iškart parodom gautą tekstą
        Intent gautas = getIntent();
        if (Intent.ACTION_SEND.equals(gautas.getAction())
                && "text/plain".equals(gautas.getType())) {
            String tekstas = gautas.getStringExtra(Intent.EXTRA_TEXT);
            if (tekstas != null) {
                ivestasTekstas = tekstas;
                tvRezultatai.setText(tekstas);
            }
        }
    }
}