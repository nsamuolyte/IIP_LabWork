package com.example.labwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    private EditText etTekstas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTekstas = findViewById(R.id.etTekstas);
        Button btnIvesti = findViewById(R.id.btnIvesti2);

        // Parodom anksčiau įvestą tekstą, kad būtų galima papildyti
        String ankstesnis = getIntent().getStringExtra(MainActivity1.EXTRA_TEKSTAS);
        if (ankstesnis != null) {
            etTekstas.setText(ankstesnis);
        }

        // Grąžinam tekstą paspaudus mygtuką
        btnIvesti.setOnClickListener(v -> grazintiIrBaigti());

        // Grąžinam tekstą paspaudus "Atgal" ◁
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                grazintiIrBaigti();
            }
        });
    }

    private void grazintiIrBaigti() {
        Intent atsakymas = new Intent();
        atsakymas.putExtra(MainActivity1.EXTRA_TEKSTAS, etTekstas.getText().toString());
        setResult(RESULT_OK, atsakymas);
        finish();
    }
}