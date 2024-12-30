package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeviceSelectionTrainingActivity extends BaseActivity {

    private String selectedMode; // Speichert den Modus (speed oder filler_words)
    private static final String MESSAGE_PATH = "/message_path";
    private static final String START_TIMER_PATH = "/start_timer"; // Neuer Pfad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_device_training);

        // Toolbar einrichten (zentral verwaltete Navbar)
        setupToolbar();

        // Empfange den Modus aus der vorherigen Activity
        selectedMode = getIntent().getStringExtra("mode");

        // Handy-Auswahl
        LinearLayout handyModeCard = findViewById(R.id.handyModeCard);
        handyModeCard.setOnClickListener(v -> {
            Intent intent;
            if ("speed".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionTrainingActivity.this, SpeechSpeedAdjustmentTraining.class);
            } else if ("filler_words".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionTrainingActivity.this, FillerWordsAdjustmentTraining.class);
            } else {
                return;
            }
            startActivity(intent);
            finish();
        });

        // Watch-Auswahl
        LinearLayout watchModeCard = findViewById(R.id.watchModeCard);
        watchModeCard.setOnClickListener(v -> {
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

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}


