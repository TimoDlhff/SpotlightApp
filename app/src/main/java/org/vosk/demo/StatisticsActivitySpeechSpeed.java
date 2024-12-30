package org.vosk.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivitySpeechSpeed extends BaseActivity {

    private PieChart pieChart;
    private TextView pieChartDescription;
    private ImageView homeButton;
    private float fastSpeechPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_speechespeed);
        setupToolbar();

        // UI-Elemente initialisieren
        pieChart = findViewById(R.id.pieChart);
        pieChartDescription = findViewById(R.id.pieChartDescription);
        homeButton = findViewById(R.id.homeButton);

        // Daten aus Intent empfangen
        Intent intent = getIntent();
        fastSpeechPercentage = intent.getFloatExtra("fastSpeechPercentage", 0f);

        Log.d("StatisticsActivitySS", "FastSpeechPercentage: " + fastSpeechPercentage);

        // Kuchendiagramm einrichten
        setupPieChart();

        // Beschreibungstext aktualisieren
        updateDescription();

        // Home Button Listener hinzufügen, falls benötigt
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent homeIntent = new Intent(StatisticsActivitySpeechSpeed.this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            });
        }
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        // Berechne die Prozentsätze, aufgerundet
        float roundedFastSpeechPercentage = Math.round(fastSpeechPercentage);
        float roundedNormalSpeechPercentage = Math.round(100f - fastSpeechPercentage);

        // Falls fastSpeechPercentage > 100 oder <0, korrigiere es
        roundedFastSpeechPercentage = clamp(roundedFastSpeechPercentage, 0f, 100f);
        roundedNormalSpeechPercentage = clamp(roundedNormalSpeechPercentage, 0f, 100f);

        // Füge Einträge hinzu, auch wenn sie 0 sind
        entries.add(new PieEntry(roundedFastSpeechPercentage, "Zu schnell"));
        entries.add(new PieEntry(roundedNormalSpeechPercentage, "Normal"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Farben für die Segmente
        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.cyan));    // Zu schnell
        colors.add(ContextCompat.getColor(this, R.color.white));  // Normal (statt Weiß, um Sichtbarkeit zu gewährleisten)
        dataSet.setColors(colors);

        // Formatierung des Diagramms
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE); // Sichtbare Farbe für die Werte
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%d%%", Math.round(value)); // Ganze Zahl ohne Dezimalstellen
            }
        });

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Weitere Formatierungen
        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false);

        // Legende konfigurieren
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE); // Sichtbare Farbe für die Legende
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setDrawInside(false);
        legend.setXEntrySpace(40f); // Platz zwischen den Einträgen erhöhen
        legend.setYEntrySpace(10f);

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setDrawEntryLabels(false); // Je nach Design anpassen

        // Animation
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    /**
     * Hilfsmethode zur Begrenzung von Werten.
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateDescription() {
        String descriptionText = "Zu schnell gesprochen: " + Math.round(fastSpeechPercentage) + "%";
        SpannableString spannableString = new SpannableString(descriptionText);
        String percentageString = Math.round(fastSpeechPercentage) + "%";
        int start = descriptionText.indexOf(percentageString);
        if (start >= 0) {
            int end = start + percentageString.length();
            spannableString.setSpan(
                    new StyleSpan(android.graphics.Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        pieChartDescription.setText(spannableString);
    }
}



