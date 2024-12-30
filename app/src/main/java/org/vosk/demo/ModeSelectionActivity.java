package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ModeSelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_selection);

        // Toolbar einrichten
        setupToolbar();

        // Presentation Mode Card
        LinearLayout presentationModeCard = findViewById(R.id.presentationModeCard);
        presentationModeCard.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, ModeSelectionTwoPresentationActivity.class);
            intent.putExtra("mode", "presentation");
            startActivity(intent);
        });

        // Training Mode Card
        LinearLayout trainingModeCard = findViewById(R.id.trainingModeCard);
        trainingModeCard.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, ModeSelectionTwoTrainingActivity.class);
            intent.putExtra("mode", "training");
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Standard-Back-Verhalten
    }
}

