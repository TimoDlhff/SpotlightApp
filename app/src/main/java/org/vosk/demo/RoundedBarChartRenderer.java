package org.vosk.demo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * - Balken abwechselnd in Cyan/Weiß
 * - Passender Rand (Cyan -> Weißer Rand, Weiß -> Cyan Rand)
 * - dickerer Rand unten
 * - inset, damit nichts abgeschnitten wird
 * - Werte manuell über dem Balken
 */
public class RoundedBarChartRenderer extends BarChartRenderer {

    private final float cornerRadius       = 16f;  // Abgerundete Ecken
    // Dickere Balkenränder:
    private final float borderWidthNormal  = 6f;   // „normale“ Randdicke (oben + Seite)
    private final float borderWidthBottom  = 12f;  // Extra dicke Linie unten

    private final Paint mBorderPaint;  // Für den Rahmen
    private final Paint mValuePaint;   // Für die Zahlen
    private final float valueOffsetY = Utils.convertDpToPixel(10f);

    // Feste Farben:
    private final int colorCyan  = Color.parseColor("#00CADD");
    private final int colorWhite = Color.WHITE;

    public RoundedBarChartRenderer(
            BarDataProvider chart,
            ChartAnimator animator,
            ViewPortHandler viewPortHandler
    ) {
        super(chart, animator, viewPortHandler);

        // Rahmen-Pinsel
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

        // Werte-Pinsel
        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setStyle(Paint.Style.FILL);
        mValuePaint.setTextAlign(Paint.Align.CENTER);
        mValuePaint.setTextSize(Utils.convertDpToPixel(16f));
        mValuePaint.setColor(Color.WHITE);
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
        BarBuffer buffer  = mBarBuffers[index];

        buffer.setPhases(mAnimator.getPhaseX(), mAnimator.getPhaseY());
        buffer.setDataSet(index);
        buffer.setBarWidth(mChart.getBarData().getBarWidth());
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.feed(dataSet);

        // Koordinaten in Pixel
        trans.pointValuesToPixel(buffer.buffer);

        for (int i = 0; i < buffer.buffer.length; i += 4) {

            float left   = buffer.buffer[i];
            float top    = buffer.buffer[i + 1];
            float right  = buffer.buffer[i + 2];
            float bottom = buffer.buffer[i + 3];

            RectF barRect = new RectF(left, top, right, bottom);

            // 1) Füllfarbe bestimmen (abwechselnd Cyan/Weiß)
            boolean isEven = ((i / 4) % 2 == 0);
            int fillColor  = isEven ? colorCyan : colorWhite;
            mRenderPaint.setColor(fillColor);
            mRenderPaint.setStyle(Paint.Style.FILL);

            // 2) Balken füllen (mit abgerundeten Ecken)
            c.drawRoundRect(barRect, cornerRadius, cornerRadius, mRenderPaint);

            // 3) Rahmenfarbe an Fill anpassen
            int borderColor = (fillColor == colorCyan) ? colorWhite : colorCyan;
            mBorderPaint.setColor(borderColor);

            // 4) Normaler Rand (oben & Seite) -> inset
            RectF borderRect = new RectF(barRect);
            float halfStroke = borderWidthNormal / 2f;
            borderRect.inset(halfStroke, halfStroke);

            mBorderPaint.setStrokeWidth(borderWidthNormal);
            c.drawRoundRect(borderRect, cornerRadius, cornerRadius, mBorderPaint);

            // 5) Extra dicke Linie unten
            float originalStroke = mBorderPaint.getStrokeWidth();
            mBorderPaint.setStrokeWidth(borderWidthBottom);

            // Y-Koordinate weiter nach unten,
            // damit die dicke Linie nicht halb abgeschnitten wird
            float bottomLineY = borderRect.bottom + (borderWidthBottom / 2f);

            // Zeichne nur die untere Linie zwischen den abgerundeten Ecken
            c.drawLine(
                    borderRect.left  + cornerRadius,
                    bottomLineY,
                    borderRect.right - cornerRadius,
                    bottomLineY,
                    mBorderPaint
            );

            mBorderPaint.setStrokeWidth(originalStroke);
        }
    }

    @Override
    public void drawValues(Canvas c) {
        if (!isDrawingValuesAllowed(mChart)) return;

        for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {
            IBarDataSet dataSet = mChart.getBarData().getDataSetByIndex(i);
            if (!shouldDrawValues(dataSet)) continue;

            applyValueTextStyle(dataSet);
            ValueFormatter formatter = dataSet.getValueFormatter();

            BarBuffer buffer = mBarBuffers[i];
            float phaseY = mAnimator.getPhaseY();

            for (int j = 0; j < dataSet.getEntryCount(); j++) {
                BarEntry entry = dataSet.getEntryForIndex(j);
                if (entry == null) continue;

                float xVal = entry.getX();
                float yVal = entry.getY() * phaseY;

                float[] pos = {xVal, yVal};
                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
                trans.pointValuesToPixel(pos);

                int bufferIndex = j * 4;
                if (bufferIndex + 1 >= buffer.buffer.length) break;

                float barTop   = buffer.buffer[bufferIndex + 1];
                float barLeft  = buffer.buffer[bufferIndex];
                float barRight = buffer.buffer[bufferIndex + 2];

                float valueX = (barLeft + barRight) / 2f;
                float valueY = barTop - valueOffsetY;

                String valText = formatter.getBarLabel(entry);
                c.drawText(valText, valueX, valueY, mValuePaint);
            }
        }
    }

    @Override
    public void drawExtras(Canvas c) {
        // Keine super.drawExtras(c)
        // Weiße X-Achse am unteren Rand
        Paint customXAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        customXAxisPaint.setColor(Color.WHITE);
        customXAxisPaint.setStrokeWidth(4f);
        customXAxisPaint.setStyle(Paint.Style.STROKE);
        customXAxisPaint.setStrokeCap(Paint.Cap.ROUND);

        float xStart = mViewPortHandler.contentLeft();
        float xEnd   = mViewPortHandler.contentRight();
        float y      = mViewPortHandler.contentBottom();
        c.drawLine(xStart, y, xEnd, y, customXAxisPaint);
    }
}







