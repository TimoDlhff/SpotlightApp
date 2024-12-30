package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeviceSelectionTrainingActivity extends AppCompatActivity {

    private String selectedMode; // Speichert den Modus (speed oder filler_words)
    private static final String MESSAGE_PATH = "/message_path";
    private static final String START_TIMER_PATH = "/start_timer"; // Neuer Pfad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_device_training);

        // Empfange den Modus aus der vorherigen Activity
        selectedMode = getIntent().getStringExtra("mode");

        // Toolbar Navigation
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Rückkehr zur ModeSelectionTwoPresentationActivity
            Intent intent = new Intent(DeviceSelectionTrainingActivity.this, ModeSelectionTwoTrainingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceSelectionTrainingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Handy-Auswahl
        LinearLayout handyModeCard = findViewById(R.id.handyModeCard);
        handyModeCard.setOnClickListener(v -> {
            if ("speed".equals(selectedMode)) {
                Intent intent = new Intent(DeviceSelectionTrainingActivity.this, SpeechSpeedAdjustmentTraining.class);
                startActivity(intent);
            } else if ("filler_words".equals(selectedMode)) {
                Intent intent = new Intent(DeviceSelectionTrainingActivity.this, FillerWordsAdjustmentTraining.class);
                startActivity(intent);
            }
            finish();
        });

        // Watch-Auswahl
        LinearLayout watchModeCard = findViewById(R.id.watchModeCard);
        watchModeCard.setOnClickListener(v -> {
            // Senden der Timer-Nachricht an die Watch


            // Senden der Begrüßungsnachricht für Vibration
            String message = "Willkommen beim";
            if ("filler_words".equals(selectedMode)) {
                message += "\nFillerword Modus";
            } else if ("speed".equals(selectedMode)) {
                message += "\nSpeech Speed Mode";
            }

            String finalMessage = message;
            new Thread(() -> {
                try {
                    Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
                    List<Node> nodes = Tasks.await(nodeListTask);
                    for (Node node : nodes) {
                        Tasks.await(Wearable.getMessageClient(getApplicationContext()).sendMessage(
                                node.getId(), MESSAGE_PATH, finalMessage.getBytes()
                        ));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            Intent intent;
            if ("speed".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionTrainingActivity.this, SpeechSpeedAdjustmentTraining.class);
            } else if ("filler_words".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionTrainingActivity.this, FillerWordsAdjustmentTraining.class);
            } else {
                return;
            }
            intent.putExtra("useWatchVibration", true);  // Flag für Watch Vibration setzen
            startActivity(intent);
            finish();
        });
    }



    private void sendSelectedWordsToWatch() {
        // Implement the logic to send selected words to watch
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DeviceSelectionTrainingActivity.this, ModeSelectionTwoPresentationActivity.class);
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

