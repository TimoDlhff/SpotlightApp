package org.vosk.demo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsActivityPresentation extends BaseActivity {

    private BarChart barChart;
    private PieChart pieChart;
    private ImageView homeButton;
    private TextView pieChartDescription;

    private HashMap<String, Integer> fillerWordCounts;
    private int totalWordCount;

    // Unser Roboto-Light Typeface
    private Typeface robotoLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_training);
        setupToolbar();

        // 1) Lade Roboto Light aus res/font/roboto_light.ttf
        robotoLight = ResourcesCompat.getFont(this, R.font.roboto_light);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        homeButton = findViewById(R.id.homeButton);
        pieChartDescription = findViewById(R.id.pieChartDescription);

        // Optional: Setze das Typeface auch für das TextView
        if (pieChartDescription != null && robotoLight != null) {
            pieChartDescription.setTypeface(robotoLight);
        }

        Intent intent = getIntent();
        fillerWordCounts = (HashMap<String, Integer>) intent.getSerializableExtra("fillerWordCounts");
        totalWordCount = intent.getIntExtra("totalWordCount", 0);

        if (fillerWordCounts == null) {
            fillerWordCounts = new HashMap<>();
            Toast.makeText(this, "Keine Füllwörter erkannt", Toast.LENGTH_SHORT).show();
        }

        setupBarChart();
        setupPieChart();
    }

    private void setupBarChart() {
        // Datensätze für Balkendiagramm erstellen
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        float maxCount = 0f;
        int index = 0;
        for (String word : fillerWordCounts.keySet()) {
            float count = fillerWordCounts.get(word);
            entries.add(new BarEntry(index, count));
            labels.add(word);

            if (count > maxCount) {
                maxCount = count;
            }
            index++;
        }
        if (maxCount == 0f) {
            maxCount = 1f;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        // Füge Farbalternation hinzu (lila/weiß)
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (i % 2 == 0) {
                colors.add(Color.parseColor("#949FFF")); // Lila
            } else {
                colors.add(Color.WHITE);
            }
        }
        dataSet.setColors(colors);

        // 2) Werte als ganze Zahlen anzeigen
        dataSet.setDrawValues(true); // Damit überhaupt Werte über den Balken stehen
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);
        // Setze unsere Schriftart
        if (robotoLight != null) {
            dataSet.setValueTypeface(robotoLight);
        }
        // Formatter: Keine Dezimalstellen
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                // Runden auf int
                return String.valueOf((int) barEntry.getY());
            }
        });

        // Erstelle BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.3f);

        // Konfiguriere Chart
        barChart.setData(barData);
        barChart.setBackgroundColor(Color.parseColor("#292929"));
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // X-Achse
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(16f);
        xAxis.setGranularity(1f);
        // Eigene Schriftart
        if (robotoLight != null) {
            xAxis.setTypeface(robotoLight);
        }
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));

        // Achsen-Min/Max je nach Anzahl Einträge
        if (entries.size() == 1) {
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(0.5f);
        } else {
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(entries.size() - 0.5f);
        }

        // Y-Achse links
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(maxCount);
        leftAxis.setDrawLabels(false);    // aktuell keine Zahlen an der linken Achse
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        if (robotoLight != null) {
            leftAxis.setTypeface(robotoLight);
        }

        // Y-Achse rechts aus
        barChart.getAxisRight().setEnabled(false);

        // Renderer für abgerundete Balken
        barChart.setRenderer(new RoundedBarChartRendererPres(
                barChart, barChart.getAnimator(), barChart.getViewPortHandler()
        ));

        barChart.animateY(1000);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);

        // Offsets
        barChart.setExtraTopOffset(40f);
        barChart.setExtraBottomOffset(40f);

        barChart.invalidate();
    }

    private void setupPieChart() {
        // Kuchendiagramm
        List<PieEntry> entries = new ArrayList<>();

        // Füllwörter gesamt ermitteln
        int fillerWords = 0;
        for (int count : fillerWordCounts.values()) {
            fillerWords += count;
        }
        int otherWords = totalWordCount - fillerWords;
        if (otherWords < 0) otherWords = 0;

        // Wir wollen KEINE Labels ("Füllwörter", "Andere Wörter") zeigen,
        // also PieEntry ohne label oder wir deaktivieren die Label-Anzeige.
        entries.add(new PieEntry(fillerWords));   // kein Label
        entries.add(new PieEntry(otherWords));    // kein Label

        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#949FFF")); // Lila
        colors.add(Color.WHITE);                 // Weiß
        dataSet.setColors(colors);

        // Werteanzeige im Kreis ebenfalls ausschalten
        dataSet.setDrawValues(false);

        // PieData
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Keine Slice-Labels
        pieChart.setDrawEntryLabels(false);

        // Weitere Einstellungen
        pieChart.setDrawHoleEnabled(false);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setDrawCenterText(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setBackgroundColor(Color.TRANSPARENT);

        // Animation
        pieChart.animateY(1000);
        pieChart.invalidate();

        // Prozentualer Anteil Füllwörter
        float percentage = (totalWordCount > 0)
                ? ((float) fillerWords / totalWordCount) * 100f
                : 0f;
        // Runden auf eine Nachkommastelle
        percentage = Math.round(percentage * 10f) / 10f;

        // Setze den Beschreibungstext mit Fettdruck nur auf dem Prozentwert
        String descriptionText = "Füllwörter: " + percentage + "% / gesamter Vortrag";
        SpannableString spannableString = new SpannableString(descriptionText);

        String percentageString = percentage + "%";
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
        if (robotoLight != null) {
            pieChartDescription.setTypeface(robotoLight);
        }
    }
}

