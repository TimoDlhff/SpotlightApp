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

public class DeviceSelectionPresentationActivity extends BaseActivity {

    private String selectedMode; // Hier wird der Modus (speed oder filler_words) gespeichert
    private static final String MESSAGE_PATH = "/message_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_device_presentation);

        // Toolbar einrichten (zentrale Navbar-Logik)
        setupToolbar();

        // Empfange den Modus aus der vorherigen Activity
        selectedMode = getIntent().getStringExtra("mode");

        // Phone-Auswahl
        LinearLayout phoneModeCard = findViewById(R.id.handyModeCard);
        phoneModeCard.setOnClickListener(v -> {
            Intent intent;
            if ("speed".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionPresentationActivity.this, SpeechSpeedAdjustmentPresentation.class);
            } else if ("filler_words".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionPresentationActivity.this, FillerWordAdjustmentPresentation.class);
            } else {
                return;
            }
            startActivity(intent);
            finish();
        });

        // Watch-Auswahl
        LinearLayout watchModeCard = findViewById(R.id.watchModeCard);
        watchModeCard.setOnClickListener(v -> {
            // Sende Nachricht über den ausgewählten Modus an die Watch
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
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(getApplicationContext()).sendMessage(
                                        node.getId(), MESSAGE_PATH, finalMessage.getBytes());
                        Tasks.await(sendMessageTask);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            Intent intent;
            if ("speed".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionPresentationActivity.this, SpeechSpeedAdjustmentPresentation.class);
            } else if ("filler_words".equals(selectedMode)) {
                intent = new Intent(DeviceSelectionPresentationActivity.this, FillerWordAdjustmentPresentation.class);
            } else {
                return;
            }
            intent.putExtra("useWatchVibration", true);  // Flag für Watch-Vibration setzen
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Standard-Back-Verhalten (zur vorherigen Activity)
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}



