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

import androidx.appcompat.app.AppCompatActivity;

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

        if (fastSpeechPercentage == 0f) {
            Toast.makeText(this, "Keine Daten zur Sprachgeschwindigkeit verfügbar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kuchendiagramm einrichten
        setupPieChart();

        // Beschreibungstext aktualisieren
        updateDescription();


    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        // Füge die Einträge hinzu (zu schnell und normal), gerundet auf ganze Zahlen
        float normalSpeechPercentage = 100f - fastSpeechPercentage;
        normalSpeechPercentage = Math.round(normalSpeechPercentage); // Auf ganze Zahl runden
        fastSpeechPercentage = Math.round(fastSpeechPercentage);     // Auf ganze Zahl runden

        entries.add(new PieEntry(fastSpeechPercentage, "Zu schnell"));
        entries.add(new PieEntry(normalSpeechPercentage, "Normal"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Farben für die Segmente
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.cyan));    // Zu schnell
        colors.add(getResources().getColor(R.color.white));  // Normal
        dataSet.setColors(colors);

        // Formatierung des Diagramms
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);
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
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setDrawInside(false);
        legend.setXEntrySpace(40f); // Platz zwischen den Einträgen erhöhen
        legend.setYEntrySpace(10f);

        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setDrawEntryLabels(false);

        // Animation
        pieChart.animateY(1000);
        pieChart.invalidate();
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



