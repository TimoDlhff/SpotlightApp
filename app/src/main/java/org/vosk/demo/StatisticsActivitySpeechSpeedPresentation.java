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

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;


import java.util.ArrayList;
import java.util.List;

public class StatisticsActivitySpeechSpeedPresentation extends BaseActivity {

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

        // 1) Roboto Light laden (Achte darauf, dass res/font/roboto_light.ttf existiert)
        robotoLight = ResourcesCompat.getFont(this, R.font.roboto_light);

        // UI-Elemente initialisieren
        pieChart = findViewById(R.id.pieChart);
        pieChartDescription = findViewById(R.id.pieChartDescription);
        homeButton = findViewById(R.id.homeButton);

        // Daten aus Intent empfangen
        Intent intent = getIntent();
        fastSpeechPercentage = intent.getFloatExtra("fastSpeechPercentage", 0f);

        Log.d("StatisticsActivitySS", "FastSpeechPercentage: " + fastSpeechPercentage);

        if (fastSpeechPercentage == 0f) {
            Toast.makeText(this, "Keine Daten zur Sprachgeschwindigkeit verfügbar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kuchendiagramm einrichten
        setupPieChart();

        // Beschreibungstext aktualisieren (und Roboto Light setzen)
        updateDescription();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        // Runden
        float normalSpeech = 100f - fastSpeechPercentage;
        normalSpeech = Math.round(normalSpeech);
        fastSpeechPercentage = Math.round(fastSpeechPercentage);

        // Einträge
        entries.add(new PieEntry(fastSpeechPercentage, "Zu schnell"));
        entries.add(new PieEntry(normalSpeech, "Normal"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Farben
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.lila));  // Zu schnell
        colors.add(getResources().getColor(R.color.white)); // Normal
        dataSet.setColors(colors);

        // 2) Keine Werte auf den Segments
        dataSet.setDrawValues(false);

        // PieData
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Kuchendiagramm-Einstellungen
        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false);

        // Legende
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(14f);
        legend.setTypeface(robotoLight);           // Roboto Light für die Legende
        legend.setFormSize(14f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setDrawInside(false);
        legend.setXEntrySpace(40f);
        legend.setYEntrySpace(10f);

        // Keine Slice-Labels im Diagramm
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelTypeface(robotoLight);
        pieChart.setDrawEntryLabels(false);

        // Animation
        pieChart.animateY(1000);
        pieChart.invalidate();
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


