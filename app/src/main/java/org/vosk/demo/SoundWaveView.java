package org.vosk.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class SoundWaveView extends View {

    private Paint paint;
    private float[] waveHeights; // Aktuelle Höhe der Linien
    private float[] waveTargets; // Zielhöhen für sanfte Bewegung
    private int waveCount = 20; // Anzahl der Linien angepasst, um zur schmaleren Breite zu passen
    private Random random;
    private final float amplitude = 400; // Maximale Höhe weiter erhöht
    private final float animationSpeed = 4f; // Geschwindigkeit der Bewegung leicht angepasst
    private final float minHeight = 40; // Mindesthöhe für die Linien (auch außen) angepasst

    public SoundWaveView(Context context) {
        super(context);
        init();
    }

    public SoundWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SoundWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF); // Alle Farben auf Weiß gesetzt
        paint.setStrokeCap(Paint.Cap.ROUND); // Abrunden der Linien-Enden
        waveHeights = new float[waveCount];
        waveTargets = new float[waveCount];
        random = new Random();

        // Startwerte und Ziele initialisieren
        for (int i = 0; i < waveCount; i++) {
            waveHeights[i] = random.nextFloat() * amplitude * 0.5f + minHeight;
            waveTargets[i] = getRandomHeight(i);
        }
    }

    private float getRandomHeight(int index) {
        // Zufällige Bewegung unabhängig von der Position, aber mit abgestufter Höhe
        float positionFactor = 1 - (float) Math.pow((2.0 * index / (waveCount - 1)) - 1, 2); // Parabel
        float randomFactor = random.nextFloat() * 0.5f + 0.5f; // Zufällige Skalierung zwischen 0.5 und 1.0
        return minHeight + positionFactor * amplitude * randomFactor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerY = height / 2f;

        for (int i = 0; i < waveCount; i++) {
            float x = (i + 1) * width / (waveCount + 1); // Gleichmäßige X-Position

            // Sanfte Bewegung zur Zielhöhe
            if (waveHeights[i] < waveTargets[i]) {
                waveHeights[i] += animationSpeed;
                if (waveHeights[i] > waveTargets[i]) waveHeights[i] = waveTargets[i];
            } else {
                waveHeights[i] -= animationSpeed;
                if (waveHeights[i] < waveTargets[i]) waveHeights[i] = waveTargets[i];
            }

            // Zielhöhe neu berechnen, wenn erreicht
            if (Math.abs(waveHeights[i] - waveTargets[i]) < animationSpeed) {
                waveTargets[i] = getRandomHeight(i);
            }

            float startY = centerY - waveHeights[i];
            float endY = centerY + waveHeights[i];

            // Linie zeichnen
            canvas.drawLine(x, startY, x, endY, paint);
        }

        // Erneutes Zeichnen für kontinuierliche Animation
        postInvalidateDelayed(50);
    }
}


