package org.vosk.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class AudioVisualizerViewPresentation extends View {

    private Paint paintCircle, paintLine, paintHighlight;
    private float angleInner = 0f; // Winkel für den inneren Kreis
    private float angleOuter = 0f; // Winkel für den "Planeten" auf der äußersten Umlaufbahn
    private Handler handler = new Handler();

    private int baseRadius = 166; // Radius des Hauptkreises
    private final int distanceToFirstLine = 100; // Abstand zur ersten äußeren Linie (erhöht)
    private final int lineThickness = 8; // Dünnere Linien
    private final int lineSpacing = 15; // Reduzierter Abstand zwischen Linien

    private float currentAmplitude = 0f; // Amplitudenwert für Visualisierung
    private boolean highlightRed = false; // Trigger für rote Hervorhebung

    private float currentRadiusScale = 1.0f; // Skalierungsfaktor für die Größe
    private float movementSpeed = 1.5f; // Standardgeschwindigkeit für Animation
    private ValueAnimator scaleAnimator;
    private float lineThicknessDynamic = lineThickness; // Dynamische Linienstärke

    public AudioVisualizerViewPresentation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Hauptkreis (Lila gefüllt)
        paintCircle = new Paint();
        paintCircle.setAntiAlias(true);

        // Äußerer Linienkreis
        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setColor(Color.parseColor("#949FFF"));
        paintLine.setStrokeWidth(lineThickness);

        // Hervorhebung (rot)
        paintHighlight = new Paint();
        paintHighlight.setAntiAlias(true);
        paintHighlight.setColor(Color.RED);

        startAnimation();
        setupScaleAnimation();
    }

    private void setupScaleAnimation() {
        scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.1f, 1.0f);
        scaleAnimator.setDuration(600); // Smooth scaling animation
        scaleAnimator.setInterpolator(new LinearInterpolator());
        scaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator.setRepeatMode(ValueAnimator.RESTART);
        scaleAnimator.addUpdateListener(animation -> {
            currentRadiusScale = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    public void updateAmplitude(float amplitude) {
        currentAmplitude = amplitude * 10;
        if (amplitude > 0.1f) {
            startScaling();
            movementSpeed = 5f; // Erhöhte Geschwindigkeit beim Reden
            lineThicknessDynamic = lineThickness + 4; // Dynamische Erhöhung der Linienstärke
        } else {
            stopScaling();
            movementSpeed = 2f; // Langsame Geschwindigkeit im Idle-Zustand
            lineThicknessDynamic = lineThickness; // Zurück zur Standardlinienstärke
        }
        invalidate();
    }

    public void startScaling() {
        if (!scaleAnimator.isRunning()) {
            scaleAnimator.start();
        }
    }

    public void stopScaling() {
        if (scaleAnimator.isRunning()) {
            scaleAnimator.cancel();
            currentRadiusScale = 1.0f;
            invalidate();
        }
    }

    public void triggerRedHighlight() {
        highlightRed = true;
        handler.postDelayed(() -> {
            highlightRed = false;
            invalidate();
        }, 500);
    }

    private void startAnimation() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                angleInner += movementSpeed; // Geschwindigkeit für den inneren Kreis
                angleOuter += movementSpeed; // Geschwindigkeit für den äußeren Planeten
                if (angleInner >= 360) angleInner = 0;
                if (angleOuter >= 360) angleOuter = 0;
                invalidate();
                handler.postDelayed(this, 16); // 60 FPS
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float scaledBaseRadius = baseRadius * currentRadiusScale;

        // 1. Hauptkreis (Lila gefüllt)
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(highlightRed ? Color.RED : Color.parseColor("#8C8CFF"));
        canvas.drawCircle(centerX, centerY, scaledBaseRadius, paintCircle);

        // 2. Drei äußeren Kreise (Umlaufbahnen mit dynamischer Linienstärke)
        paintLine.setStrokeWidth(lineThicknessDynamic);
        paintLine.setColor(highlightRed ? Color.RED : Color.parseColor("#949FFF"));
        for (int i = 0; i < 3; i++) {
            float radius = scaledBaseRadius + distanceToFirstLine + i * (lineSpacing + lineThicknessDynamic);
            canvas.drawCircle(centerX, centerY, radius, paintLine);
        }

        // 3. Innerer Kreis (3/5 des Hauptkreises, bewegt sich genau am Rand)
        float smallCircleRadius = scaledBaseRadius * 0.4f; // 3/5 des Hauptkreises
        float smallCircleX = (float) (centerX + Math.cos(Math.toRadians(angleInner)) * (scaledBaseRadius - smallCircleRadius));
        float smallCircleY = (float) (centerY + Math.sin(Math.toRadians(angleInner)) * (scaledBaseRadius - smallCircleRadius));

        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(highlightRed ? Color.RED : Color.parseColor("#D3C8FF"));
        canvas.drawCircle(smallCircleX, smallCircleY, smallCircleRadius, paintCircle);

        // 4. "Planet" entlang der äußersten Umlaufbahn (Segment entlang der Bahn)
        float outerRadius = scaledBaseRadius + distanceToFirstLine + 2 * (lineSpacing + lineThicknessDynamic);
        float planetWidth = 50; // Breite des Planetensegments
        float planetHeight = 12; // Höhe des Planetensegments
        float planetAngleStart = angleOuter - 20; // Segment-Startwinkel
        float planetAngleEnd = angleOuter + 20;   // Segment-Endwinkel

        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(planetHeight);
        paintCircle.setColor(highlightRed ? Color.RED : Color.parseColor("#BBC2FF"));

        canvas.drawArc(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius,
                planetAngleStart, 40, false, paintCircle); // Teilsegment entlang der Umlaufbahn
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}



