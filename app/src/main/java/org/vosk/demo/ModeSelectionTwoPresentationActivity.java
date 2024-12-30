package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectionTwoPresentationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_selection_two_presentation);
        setupToolbar();
        // Toolbar Navigation
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Zurück zur ModeSelectionActivity
            Intent intent = new Intent(ModeSelectionTwoPresentationActivity.this, ModeSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });


        // Button für Sprechgeschwindigkeit
        LinearLayout speedModeCard = findViewById(R.id.speedModeCard);
        speedModeCard.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionTwoPresentationActivity.this, DeviceSelectionPresentationActivity.class);
            intent.putExtra("mode", "speed"); // Speichert "speed" als Parameter
            startActivity(intent);
        });

        // Button für Füllwörter
        LinearLayout fillerWordsModeCard = findViewById(R.id.fillerWordsModeCard);
        fillerWordsModeCard.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionTwoPresentationActivity.this, DeviceSelectionPresentationActivity.class);
            intent.putExtra("mode", "filler_words"); // Speichert "filler_words" als Parameter
            startActivity(intent);
        });
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
