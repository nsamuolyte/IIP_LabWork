package com.example.labwork;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.Arrays;

public class Lab9Activity extends AppCompatActivity {

    private static final GeoPoint VGTU = new GeoPoint(54.7222, 25.3374);   // Saulėtekio al. 11

    private MapView zemelapis;
    private EditText etIlguma;
    private EditText etPlatuma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osmdroid nustatymai – būtina PRIEŠ setContentView
        Configuration.getInstance().setUserAgentValue("IIPLabDarbai/1.0 (studento laboratorinis darbas)");
        Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab9);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etIlguma = findViewById(R.id.etIlguma);
        etPlatuma = findViewById(R.id.etPlatuma);
        Button btnKoordinates = findViewById(R.id.btnKoordinates);

        // Žemėlapis: pradžioje rodom Kauną
        zemelapis = findViewById(R.id.zemelapis);
        zemelapis.setTileSource(TileSourceFactory.MAPNIK);   // standartinis OpenStreetMap vaizdas
        zemelapis.setMultiTouchControls(true);
        zemelapis.getOverlays().add(new CopyrightOverlay(this));   // "© OpenStreetMap contributors"// artinimas dviem pirštais
        zemelapis.getController().setZoom(16.0);
        zemelapis.getController().setCenter(VGTU);
        pridetiZymekli(VGTU, "VILNIUS TECH (VGTU)");

        btnKoordinates.setOnClickListener(v -> rodytiKoordinates());

        // Siūlomos vietos – pasirinkus, koordinatės užsipildo automatiškai
        AutoCompleteTextView acVieta = findViewById(R.id.acVieta);
        String[] vietos = getResources().getStringArray(R.array.lab9_vietos);
        String[] koordinates = getResources().getStringArray(R.array.lab9_koordinates);

        ArrayAdapter<String> adapteris = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, vietos);
        acVieta.setAdapter(adapteris);

        // Paspaudus ant lauko – iškart rodom visą sąrašą
        acVieta.setOnClickListener(v -> acVieta.showDropDown());

        acVieta.setOnItemClickListener((parent, view, position, id) -> {
            String pasirinkta = (String) parent.getItemAtPosition(position);
            int indeksas = Arrays.asList(vietos).indexOf(pasirinkta);
            String[] dalys = koordinates[indeksas].split(",");
            etIlguma.setText(dalys[0]);
            etPlatuma.setText(dalys[1]);
        });
    }

    // Padeda žymeklį nurodytoje vietoje
    private void pridetiZymekli(GeoPoint vieta, String pavadinimas) {
        Marker zymeklis = new Marker(zemelapis);
        zymeklis.setPosition(vieta);
        zymeklis.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        zymeklis.setTitle(pavadinimas);
        zemelapis.getOverlays().add(zymeklis);
        zemelapis.invalidate();
    }

    // Mygtukas: perskaito koordinates, padeda žymeklį ir nuskrenda į tą vietą
    private void rodytiKoordinates() {
        String ilgumaTekstas = etIlguma.getText().toString().trim().replace(',', '.');
        String platumaTekstas = etPlatuma.getText().toString().trim().replace(',', '.');

        try {
            double ilguma = Double.parseDouble(ilgumaTekstas);
            double platuma = Double.parseDouble(platumaTekstas);

            if (platuma < -85 || platuma > 85 || ilguma < -180 || ilguma > 180) {
                Toast.makeText(this, "Platuma: -85…85, ilguma: -180…180",
                        Toast.LENGTH_LONG).show();
                return;
            }

            GeoPoint vieta = new GeoPoint(platuma, ilguma);   // DĖMESIO: pirma platuma, paskui ilguma
            pridetiZymekli(vieta, platuma + ", " + ilguma);
            zemelapis.getController().animateTo(vieta, 10.0, 1000L);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Įveskite teisingus skaičius", Toast.LENGTH_SHORT).show();
        }
    }

    // osmdroid reikalauja perduoti veiklos būsenas žemėlapiui
    @Override
    protected void onResume() {
        super.onResume();
        zemelapis.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zemelapis.onPause();
    }
}