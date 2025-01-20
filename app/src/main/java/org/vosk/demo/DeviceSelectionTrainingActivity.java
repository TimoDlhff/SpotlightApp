package org.vosk.demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

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
            // Überprüfen, ob eine Uhr verbunden ist
            checkWatchConnectionAndProceed();
        });
    }

    /**
     * Überprüft, ob eine Smartwatch verbunden ist.
     * Wenn ja, sendet die Begrüßungsnachricht und startet die entsprechende Activity.
     * Wenn nein, zeigt eine Toast-Nachricht an.
     */
    private void checkWatchConnectionAndProceed() {
        // Abrufen der verbundenen Nodes (Smartwatches)
        Task<List<Node>> nodeListTask = Wearable.getNodeClient(this).getConnectedNodes();

        nodeListTask.addOnSuccessListener(new OnSuccessListener<List<Node>>() {
            @Override
            public void onSuccess(List<Node> nodes) {
                if (nodes != null && !nodes.isEmpty()) {
                    // Eine oder mehrere Uhren sind verbunden
                    sendWelcomeMessageToWatch(nodes);
                    proceedToTrainingActivity();
                } else {
                    // Keine Uhr verbunden, Toast anzeigen
                    Toast.makeText(DeviceSelectionTrainingActivity.this, "Keine Uhr verbunden", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Fehler bei der Abfrage der verbundenen Nodes
                Toast.makeText(DeviceSelectionTrainingActivity.this, "Fehler beim Überprüfen der Uhrverbindung", Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
        });
    }

    /**
     * Sendet eine Begrüßungsnachricht an alle verbundenen Uhren.
     *
     * @param nodes Liste der verbundenen Nodes (Uhren)
     */
    private void sendWelcomeMessageToWatch(List<Node> nodes) {
        String message = "Willkommen beim";
        if ("filler_words".equals(selectedMode)) {
            message += "\nFüllwortmodus";
        } else if ("speed".equals(selectedMode)) {
            message += "\nSpeech Speed Mode";
        }

        byte[] messageBytes = message.getBytes();

        for (Node node : nodes) {
            Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(
                    node.getId(), MESSAGE_PATH, messageBytes
            );

            sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    // Nachricht erfolgreich gesendet
                    // Sie können hier zusätzliche Aktionen durchführen, falls erforderlich
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Fehler beim Senden der Nachricht
                    Toast.makeText(DeviceSelectionTrainingActivity.this, "Fehler beim Senden der Nachricht an die Uhr", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Startet die entsprechende Trainings-Activity basierend auf dem ausgewählten Modus.
     */
    private void proceedToTrainingActivity() {
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


