package com.example.labwork;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

public class Lab7Activity extends AppCompatActivity {

    private static final String KANALO_ID = "baterijos_kanalas";
    private static final int PRANESIMO_ID = 1;
    private static final int ZEMAS_LYGIS = 30;                    // % – kada laikom, kad senka
    // private static final long INTERVALAS = 15 * 60 * 1000L;       // 15 min. milisekundėmis
    private static final long INTERVALAS = 30 * 1000L;
    private static final String NUSTATYMAI = "lab7_nustatymai";
    private static final String RAKTAS_IJUNGTA = "sekimas_ijungtas";
    private static final String RAKTAS_PASKUTINIS = "paskutinis_pranesimas";

    private TextView tvLygis;
    private SharedPreferences nustatymai;
    private boolean imtuvasRegistruotas = false;

    // Prašymas leisti rodyti pranešimus (Android 13+)
    private final ActivityResultLauncher<String> leidimoPrasymas = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            suteiktas -> {
                if (!suteiktas) {
                    Toast.makeText(this, "Be leidimo pranešimai nebus rodomi",
                            Toast.LENGTH_LONG).show();
                }
            });

    // Imtuvas: suveikia kaskart, kai pasikeičia akumuliatoriaus būsena
    private final BroadcastReceiver baterijosImtuvas = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int lygis = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int skale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (lygis < 0 || skale <= 0) return;

            int procentai = lygis * 100 / skale;
            tvLygis.setText(getString(R.string.lab7_lygis, procentai));

            if (procentai <= ZEMAS_LYGIS) {
                rodytiPranesimaJeiReikia(procentai);
            } else {
                // Lygis vėl normalus – kitą kartą nusileidus pranešim iškart
                nustatymai.edit().putLong(RAKTAS_PASKUTINIS, 0).apply();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lab7);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvLygis = findViewById(R.id.tvLygis);
        MaterialSwitch swSekimas = findViewById(R.id.swSekimas);
        nustatymai = getSharedPreferences(NUSTATYMAI, MODE_PRIVATE);

        sukurtiKanala();

        // Atkuriam jungiklio būseną (sekimas prisimenamas)
        boolean ijungta = nustatymai.getBoolean(RAKTAS_IJUNGTA, false);
        swSekimas.setChecked(ijungta);
        if (ijungta) {
            ijungtiSekima();
        }

        swSekimas.setOnCheckedChangeListener((mygtukas, ijungtas) -> {
            nustatymai.edit().putBoolean(RAKTAS_IJUNGTA, ijungtas).apply();
            if (ijungtas) {
                prasytiLeidimo();
                ijungtiSekima();
            } else {
                isjungtiSekima();
            }
        });
    }

    // Pranešimų kanalas (būtinas nuo Android 8)
    private void sukurtiKanala() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel kanalas = new NotificationChannel(KANALO_ID,
                    "Akumuliatoriaus lygis", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(kanalas);
        }
    }

    // Leidimo prašymas (būtinas nuo Android 13)
    private void prasytiLeidimo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            leidimoPrasymas.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // Užregistruojam imtuvą – pradedam gauti pranešimus apie akumuliatorių
    private void ijungtiSekima() {
        if (!imtuvasRegistruotas) {
            ContextCompat.registerReceiver(this, baterijosImtuvas,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED);
            imtuvasRegistruotas = true;
        }
    }

    // Išregistruojam imtuvą ir panaikinam pranešimą
    private void isjungtiSekima() {
        if (imtuvasRegistruotas) {
            unregisterReceiver(baterijosImtuvas);
            imtuvasRegistruotas = false;
        }
        NotificationManagerCompat.from(this).cancel(PRANESIMO_ID);
        tvLygis.setText("Sekimas išjungtas");
    }

    // Rodom pranešimą, jei nuo paskutinio praėjo bent 15 min.
    private void rodytiPranesimaJeiReikia(int procentai) {
        long dabar = System.currentTimeMillis();
        long paskutinis = nustatymai.getLong(RAKTAS_PASKUTINIS, 0);
        if (dabar - paskutinis < INTERVALAS) return;

        // Ar turim leidimą rodyti pranešimus?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Paspaudus pranešimą – atidaroma ši veikla
        Intent atidaryti = new Intent(this, Lab7Activity.class);
        atidaryti.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent paspaudus = PendingIntent.getActivity(this, 0, atidaryti,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder pranesimas = new NotificationCompat.Builder(this, KANALO_ID)
                .setSmallIcon(R.drawable.ic_baterija)
                .setContentTitle("Senka akumuliatorius")
                .setContentText("Žinokite, kad jau senka jūsų akumuliatorius (" + procentai + " %)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(paspaudus)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(PRANESIMO_ID, pranesimas.build());
        nustatymai.edit().putLong(RAKTAS_PASKUTINIS, dabar).apply();
    }

    // Uždarant langą – išregistruojam imtuvą
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imtuvasRegistruotas) {
            unregisterReceiver(baterijosImtuvas);
            imtuvasRegistruotas = false;
        }
    }


}