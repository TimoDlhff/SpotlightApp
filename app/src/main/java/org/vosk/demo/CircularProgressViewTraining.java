package org.vosk.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularProgressViewTraining extends View {

    private int maxProgress = 140; // Maximale WPM
    private int currentProgress = 0; // Aktueller Fortschritt
    private int strokeWidth = 20; // Breite des äußeren Rings

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;

    public CircularProgressViewTraining(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Hintergrund für den Kreis
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(0xFF444444); // Dunkelgrau
        backgroundPaint.setAntiAlias(true);

        // Fortschrittskreis
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(0xFF00CADD); // Lila
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Bereich für den Kreis
        rectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF.set(strokeWidth, strokeWidth, w - strokeWidth, h - strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Hintergrundkreis zeichnen
        canvas.drawArc(rectF, -90, 360, false, backgroundPaint);

        // Fortschrittskreis zeichnen
        float sweepAngle = (360f * currentProgress) / maxProgress;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    // Fortschrittswert aktualisieren
    public void setProgress(int progress) {
        this.currentProgress = Math.min(progress, maxProgress);
        invalidate();
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }
}
