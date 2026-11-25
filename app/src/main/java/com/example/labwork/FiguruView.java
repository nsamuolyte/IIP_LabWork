package com.example.labwork;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

public class FiguruView extends View {

    // Per šią sąsają veikla sužino rezultatą (kad galėtų groti garsą)
    public interface RezultatoKlausytojas {
        void onRezultatas(boolean pavyko, float figurosSkersmuo, float apskritimoSkersmuo);
    }

    private static final int LINIJA = 0, KETURKAMPIS = 1, TRIKAMPIS = 2, ELIPSE = 3, APSKRITIMAS = 4;
    private static final int FIGURU_SKAICIUS = 5;
    private static final float PAKLAIDA = 0.15f;      // leidžiama 15 % paklaida

    private final Paint figurosDazai = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint apskritimoDazai = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pirstuDazai = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path trikampis = new Path();
    private final Random atsitiktinis = new Random();
    private final GestureDetector gestai;

    // Dabartinė figūra
    private int figurosTipas = -1;
    private float spindulys;        // apskritimo, į kurį figūra tiksliai telpa, spindulys
    private float kampas;           // pasukimas laipsniais
    private float santykis;         // stačiakampio / elipsės kraštinių santykis

    // Būsenos
    private boolean rodytiFigura = true;
    private boolean piesiamApskritima = false;
    private boolean rodytiApskritima = false;
    private boolean buvoDuPirstai = false;
    private float x1, y1, x2, y2;   // dviejų pirštų koordinatės

    private RezultatoKlausytojas klausytojas;

    public FiguruView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        figurosDazai.setStyle(Paint.Style.FILL);
        figurosDazai.setStrokeWidth(16f);
        figurosDazai.setStrokeCap(Paint.Cap.ROUND);

        apskritimoDazai.setStyle(Paint.Style.STROKE);
        apskritimoDazai.setStrokeWidth(6f);
        apskritimoDazai.setColor(Color.rgb(63, 81, 181));

        pirstuDazai.setColor(Color.LTGRAY);

        // Perbraukimo (fling) gestas – nauja figūra
        gestai = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                   float greitisX, float greitisY) {
                if (!buvoDuPirstai) {
                    naujaFigura();
                }
                return true;
            }
        });
    }

    public void setRezultatoKlausytojas(RezultatoKlausytojas klausytojas) {
        this.klausytojas = klausytojas;
    }

    // Kai sužinomas View dydis – sukuriam pirmą figūrą
    @Override
    protected void onSizeChanged(int w, int h, int senasW, int senasH) {
        super.onSizeChanged(w, h, senasW, senasH);
        if (figurosTipas < 0) {
            naujaFigura();
        }
    }

    // Nauja atsitiktinė figūra: kitas tipas, dydis, pasukimas, spalva
    private void naujaFigura() {
        int naujasTipas;
        do {
            naujasTipas = atsitiktinis.nextInt(FIGURU_SKAICIUS);
        } while (naujasTipas == figurosTipas);
        figurosTipas = naujasTipas;

        int mazesnis = Math.min(getWidth(), getHeight());
        spindulys = mazesnis * (0.25f + atsitiktinis.nextFloat() * 0.28f);   // 12–40 % ekrano
        kampas = atsitiktinis.nextFloat() * 360f;
        santykis = 0.3f + atsitiktinis.nextFloat() * 0.6f;
        figurosDazai.setColor(Color.rgb(atsitiktinis.nextInt(150),
                120 + atsitiktinis.nextInt(130), atsitiktinis.nextInt(150)));

        rodytiApskritima = false;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Figūra – ekrano centre, pasukta
        if (rodytiFigura) {
            canvas.save();
            canvas.translate(getWidth() / 2f, getHeight() / 2f);
            canvas.rotate(kampas);
            piestiFigura(canvas);
            canvas.restore();
        }

        // Pirštais piešiamas apskritimas: centras – tarp pirštų, skersmuo – atstumas tarp jų
        if (piesiamApskritima || rodytiApskritima) {
            float centrasX = (x1 + x2) / 2f;
            float centrasY = (y1 + y2) / 2f;
            canvas.drawCircle(centrasX, centrasY, apskritimoSkersmuo() / 2f, apskritimoDazai);
        }
        if (piesiamApskritima) {
            canvas.drawCircle(x1, y1, 24f, pirstuDazai);
            canvas.drawCircle(x2, y2, 24f, pirstuDazai);
        }
    }

    // Visos figūros piešiamos aplink tašką (0,0); jų kraštai – ant apskritimo su spinduliu r
    private void piestiFigura(Canvas c) {
        float r = spindulys;
        switch (figurosTipas) {
            case LINIJA:
                c.drawLine(-r, 0, r, 0, figurosDazai);
                break;
            case KETURKAMPIS: {
                double a = Math.atan(santykis);
                float pusPlocio = (float) (r * Math.cos(a));
                float pusAukscio = (float) (r * Math.sin(a));
                c.drawRect(-pusPlocio, -pusAukscio, pusPlocio, pusAukscio, figurosDazai);
                break;
            }
            case TRIKAMPIS:
                trikampis.reset();
                for (int i = 0; i < 3; i++) {
                    double k = Math.toRadians(-90 + i * 120);
                    float x = (float) (r * Math.cos(k));
                    float y = (float) (r * Math.sin(k));
                    if (i == 0) trikampis.moveTo(x, y);
                    else trikampis.lineTo(x, y);
                }
                trikampis.close();
                c.drawPath(trikampis, figurosDazai);
                break;
            case ELIPSE:
                c.drawOval(-r, -r * santykis, r, r * santykis, figurosDazai);
                break;
            case APSKRITIMAS:
            default:
                c.drawCircle(0, 0, r, figurosDazai);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        gestai.onTouchEvent(e);

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:                 // 1 pirštas – figūra dingsta
                rodytiFigura = false;
                rodytiApskritima = false;
                buvoDuPirstai = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:         // 2 pirštas – pradedam piešti apskritimą
                if (e.getPointerCount() == 2) {
                    buvoDuPirstai = true;
                    piesiamApskritima = true;
                    atnaujintiTaskus(e);
                }
                break;

            case MotionEvent.ACTION_MOVE:                 // pirštai juda – apskritimas keičiasi
                if (piesiamApskritima && e.getPointerCount() >= 2) {
                    atnaujintiTaskus(e);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:           // vienas pirštas atleistas – tikrinam
                if (piesiamApskritima) {
                    piesiamApskritima = false;
                    patikrinti();
                }
                break;

            case MotionEvent.ACTION_UP:                   // visi pirštai atleisti
            case MotionEvent.ACTION_CANCEL:
                if (piesiamApskritima) {
                    piesiamApskritima = false;
                    patikrinti();
                }
                rodytiFigura = true;
                performClick();
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void atnaujintiTaskus(MotionEvent e) {
        x1 = e.getX(0);
        y1 = e.getY(0);
        x2 = e.getX(1);
        y2 = e.getY(1);
    }

    private float apskritimoSkersmuo() {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }

    // Ar figūra telpa į nupieštą apskritimą (su paklaida)?
    private void patikrinti() {
        float figurosSkersmuo = 2 * spindulys;
        float apskritimoSkersmuo = apskritimoSkersmuo();
        boolean pavyko = Math.abs(apskritimoSkersmuo - figurosSkersmuo)
                <= figurosSkersmuo * PAKLAIDA;

        rodytiApskritima = true;      // paliekam apskritimą matomą palyginimui
        if (klausytojas != null) {
            klausytojas.onRezultatas(pavyko, figurosSkersmuo, apskritimoSkersmuo);
        }
    }
}