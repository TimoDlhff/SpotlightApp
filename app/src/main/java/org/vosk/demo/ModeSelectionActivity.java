

package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_selection);

        // Zurück-Button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> navigateToMain());

        // Home-Button
        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> navigateToMain());

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
        navigateToMain(); // Bei Zurück-Button direkt zur MainActivity navigieren
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
