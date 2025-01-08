package org.vosk.demo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;


import java.util.ArrayList;
import java.util.List;

public class StatisticsActivitySpeechSpeed extends BaseActivity {

    private PieChart pieChart;
    private TextView pieChartDescription;
    private ImageView homeButton;
    private float fastSpeechPercentage;

    // Roboto Light TypeFace
    private Typeface robotoLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_speechespeed);
        setupToolbar();

        // 1) Roboto Light laden (Achte darauf, dass du res/font/roboto_light.ttf hast)
        robotoLight = ResourcesCompat.getFont(this, R.font.roboto_light);

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

        // Beschreibungstext aktualisieren (auch mit Roboto Light)
        updateDescription();

        // Home Button Listener hinzufügen
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

        // Prozente runden
        float roundedFastSpeechPercentage = Math.round(fastSpeechPercentage);
        float roundedNormalSpeechPercentage = Math.round(100f - fastSpeechPercentage);

        // clamp für sichere Werte
        roundedFastSpeechPercentage = clamp(roundedFastSpeechPercentage, 0f, 100f);
        roundedNormalSpeechPercentage = clamp(roundedNormalSpeechPercentage, 0f, 100f);

        // Segmente (Labels für die Legende)
        entries.add(new PieEntry(roundedFastSpeechPercentage, "Zu schnell"));
        entries.add(new PieEntry(roundedNormalSpeechPercentage, "Normal"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        // Farben
        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.cyan));   // Zu schnell
        colors.add(ContextCompat.getColor(this, R.color.white));  // Normal
        dataSet.setColors(colors);

        // 2) Werte NICHT anzeigen
        dataSet.setDrawValues(false);

        // PieData
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Loch ausschalten, Beschriftung ausschalten
        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false);

        // Legende konfigurieren
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTypeface(robotoLight);         // Roboto Light für die Legende
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setDrawInside(false);
        legend.setXEntrySpace(40f); // Platz zwischen den Einträgen
        legend.setYEntrySpace(10f);

        // Entry-Labels auf dem Diagramm
        pieChart.setEntryLabelTypeface(robotoLight); // Falls du die Label im Chart sehen willst
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(14f);
        // Da du sie nicht anzeigen möchtest, ausschalten:
        pieChart.setDrawEntryLabels(false);

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
        // Roboto Light für TextView
        pieChartDescription.setTypeface(robotoLight);

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




