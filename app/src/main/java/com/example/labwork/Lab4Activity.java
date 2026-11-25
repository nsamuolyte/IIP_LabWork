package com.example.labwork;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Lab4Activity extends AppCompatActivity {
    private static final String NUSTATYMAI = "lab4_nustatymai";
    private static final String RAKTAS_ADRESAS = "paskutinis_adresas";

    private EditText etAdresas;
    private WebView wvNarsykle;
    private boolean kitasAtgalUzdaro = false;

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etAdresas = findViewById(R.id.etAdresas);
        wvNarsykle = findViewById(R.id.wvNarsykle);
        Button btnUzkrauti = findViewById(R.id.btnUzkrauti);

        // WebView nustatymai
        wvNarsykle.getSettings().setJavaScriptEnabled(true);
        wvNarsykle.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Lauke visada rodom tikrą atidaryto puslapio adresą
                if (url != null && url.startsWith("http")) {
                    etAdresas.setText(url);
                }
            }
        });

        // Du kartus bakstelėjus ant lauko – įrašom "https://www"
        GestureDetector detektorius = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        etAdresas.setText("https://www");
                        etAdresas.setSelection(etAdresas.length());
                        return true;
                    }
                });
        etAdresas.setOnTouchListener((v, event) -> {
            detektorius.onTouchEvent(event);
            return false;
        });

        // Mygtukas
        btnUzkrauti.setOnClickListener(v -> uzkrauti(etAdresas.getText().toString().trim()));

        // Paleidus programą – nuskaitom paskutinį adresą iš vidinės atminties
        SharedPreferences nustatymai = getSharedPreferences(NUSTATYMAI, MODE_PRIVATE);
        String paskutinis = nustatymai.getString(RAKTAS_ADRESAS, null);
        if (paskutinis != null) {
            etAdresas.setText(paskutinis);
            wvNarsykle.loadUrl(paskutinis);
        } else {
            String html = "<p style='font-size:18px'>Paspauskite du kartus ant įvedimo lango "
                    + "jeigu reikalingas https://www</p>";
            wvNarsykle.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }

        // Mygtukas "Atgal" ◁
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (wvNarsykle.canGoBack()) {
                    wvNarsykle.goBack();             // ankstesnis tinklapis
                } else if (kitasAtgalUzdaro) {
                    finish();                        // uždarom langą
                } else {
                    Toast.makeText(Lab4Activity.this,
                            "Kitą kartą paspaudus ATGAL bus uždarytas naršyklės langas",
                            Toast.LENGTH_SHORT).show();
                    kitasAtgalUzdaro = true;
                }
            }
        });
    }

    // Užkrauna adresą WebView lange
    private void uzkrauti(String adresas) {
        if (adresas.isEmpty()) {
            Toast.makeText(this, "Įveskite tinklapio adresą", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!adresas.startsWith("http://") && !adresas.startsWith("https://")) {
            adresas = "https://" + adresas;
        }
        kitasAtgalUzdaro = false;
        wvNarsykle.loadUrl(adresas);
    }

    // Uždarant (ar paliekant) langą – išsaugom paskutinį adresą vidinėje atmintyje
    @Override
    protected void onPause() {
        super.onPause();
        String url = wvNarsykle.getUrl();
        if (url != null && url.startsWith("http")) {
            getSharedPreferences(NUSTATYMAI, MODE_PRIVATE)
                    .edit()
                    .putString(RAKTAS_ADRESAS, url)
                    .apply();
        }
    }
}