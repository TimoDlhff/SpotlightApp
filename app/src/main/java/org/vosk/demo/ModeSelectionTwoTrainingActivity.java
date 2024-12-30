package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectionTwoTrainingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_selection_two_training);

        // Toolbar Navigation
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Zurück zur ModeSelectionActivity
            Intent intent = new Intent(ModeSelectionTwoTrainingActivity.this, ModeSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            finish();
        });

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            // Zurück zur MainActivity
            Intent intent = new Intent(ModeSelectionTwoTrainingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            finish();
        });

        // Button für Sprechgeschwindigkeit
        LinearLayout speechSpeedCard = findViewById(R.id.speedModeCard);
        speechSpeedCard.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionTwoTrainingActivity.this, DeviceSelectionTrainingActivity.class);
            intent.putExtra("mode", "speed");
            startActivity(intent);

        });

        // Button für Füllwörter
        LinearLayout fillerWordsCard = findViewById(R.id.fillerWordsModeCard);
        fillerWordsCard.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionTwoTrainingActivity.this, DeviceSelectionTrainingActivity.class);
            intent.putExtra("mode", "filler_words");
            startActivity(intent);

        });
    }

    @Override
    public void onBackPressed() {
        // Rückwärtsnavigation zur ModeSelectionActivity
        Intent intent = new Intent(ModeSelectionTwoTrainingActivity.this, ModeSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        finish();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}



