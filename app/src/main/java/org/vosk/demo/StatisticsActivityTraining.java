package org.vosk.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsActivityTraining extends BaseActivity{

    private BarChart barChart;
    private PieChart pieChart;
    private ImageView homeButton;
    private TextView pieChartDescription;

    private HashMap<String, Integer> fillerWordCounts;
    private int totalWordCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_training);
        setupToolbar();
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        homeButton = findViewById(R.id.homeButton);
        pieChartDescription = findViewById(R.id.pieChartDescription);

        // Daten aus Intent
        Intent intent = getIntent();
        fillerWordCounts = (HashMap<String, Integer>) intent.getSerializableExtra("fillerWordCounts");
        totalWordCount = intent.getIntExtra("totalWordCount", 0);

        android.util.Log.d("StatisticsActivityTraining", "FillerWordCounts: " + fillerWordCounts);
        android.util.Log.d("StatisticsActivityTraining", "TotalWordCount: " + totalWordCount);

        if (fillerWordCounts == null || fillerWordCounts.isEmpty()) {
            Toast.makeText(this, "Keine Füllwörter erkannt", Toast.LENGTH_SHORT).show();
            return;
        }

        setupBarChart();
        setupPieChart();


    }

    private void setupBarChart() {
        android.util.Log.d("StatisticsActivityTraining", "Setting up BarChart");
        // 1) Daten aufbereiten
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        float maxCount = 0f;
        int index = 0;
        for (String word : fillerWordCounts.keySet()) {
            float count = fillerWordCounts.get(word);
            entries.add(new BarEntry(index, count));
            labels.add(word);
            android.util.Log.d("StatisticsActivityTraining", "Adding entry: word=" + word + ", count=" + count + ", index=" + index);

            if (count > maxCount) {
                maxCount = count;
            }
            index++;
        }
        if (maxCount == 0f) {
            maxCount = 1f;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        // Abwechselnd Cyan/Weiß
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (i % 2 == 0) {
                colors.add(Color.parseColor("#949FFF"));
            } else {
                colors.add(Color.WHITE);
            }
        }
        dataSet.setColors(colors);
        // Keine Standard-Werteanzeige
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.3f);

        // 2) BarChart konfigurieren
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
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // Nur 1 Balken => -0.5 bis +0.5, mehrere => -0.5 bis (size - 0.5)
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
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);

        // Y-Achse rechts
        barChart.getAxisRight().setEnabled(false);

        // 3) Custom Renderer
        barChart.setRenderer(
                new RoundedBarChartRenderer(
                        barChart,
                        barChart.getAnimator(),
                        barChart.getViewPortHandler()
                )
        );

        // 4) Animation & Interaktionsverbot
        barChart.animateY(1000);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);

        // 5) Extra Offsets
        // Mach sie groß genug, damit du die dicke Unterkante sicher siehst
        barChart.setExtraTopOffset(40f);
        barChart.setExtraBottomOffset(40f);

        barChart.invalidate();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        int fillerWords = 0;
        for (int count : fillerWordCounts.values()) {
            fillerWords += count;
        }
        int otherWords = totalWordCount - fillerWords;
        if (otherWords < 0) otherWords = 0;

        entries.add(new PieEntry(fillerWords, "Füllwörter"));
        entries.add(new PieEntry(otherWords, "Andere Wörter"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#00CADD"));
        colors.add(Color.WHITE);
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setDrawCenterText(false);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setBackgroundColor(Color.TRANSPARENT);

        pieChart.animateY(1000);
        pieChart.invalidate();

        float percentage = (totalWordCount > 0)
                ? ((float) fillerWords / totalWordCount) * 100f
                : 0f;
        percentage = Math.round(percentage * 10f) / 10f;

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
    }
}
