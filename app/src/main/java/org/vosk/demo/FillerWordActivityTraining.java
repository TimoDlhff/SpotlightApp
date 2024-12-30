package org.vosk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class FillerWordActivityTraining extends Activity implements RecognitionListener {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final String AUDIO_PATH = "/audio_path";
    private static final String START_TIMER_PATH = "/start_timer";

    private Model model;
    private SpeechService speechService;
    private Recognizer recognizer;
    private TextView resultView;
    private LinearLayout mainLayout;
    private List<String> selectedWords = new ArrayList<>();
    private HashMap<String, Integer> fillerWordCounts = new HashMap<>();
    private int totalWordCount = 0; // Gesamtanzahl der gesprochenen Wörter
    private boolean isListening = false;
    private AudioVisualizerViewTraining visualizerView;
    private TextView timerTextView; // Timer TextView
    private Handler timerHandler = new Handler();
    private int timerSeconds = 0; // Timer Startzeit in Sekunden
    private final Handler handler = new Handler();
    private boolean useWatchVibration = false; // Standard
    private boolean useWatchAudio = false;
    private String remoteNodeId;

    private final Handler cooldownHandler = new Handler(); // Handler für Verzögerung
    private boolean canVibrate = true; // Cooldown für die Vibration

    // Neuer Zähler für Füllwort-Erkennungen
    private int fillerWordDetectedCount = 0;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.filler_words_training);

        // UI-Elemente initialisieren
        resultView = findViewById(R.id.result_text);
        visualizerView = findViewById(R.id.audioVisualizer);
        timerTextView = findViewById(R.id.timerText);

        // Extras aus dem Intent übernehmen
        useWatchVibration = getIntent().getBooleanExtra("useWatchVibration", false);
        useWatchAudio = getIntent().getBooleanExtra("useWatchAudio", false);

        LibVosk.setLogLevel(LogLevel.INFO);

        // Wörter aus StarterActivity übernehmen und initialisieren
        ArrayList<String> baseWords = getIntent().getStringArrayListExtra("SELECTED_WORDS");
        if (baseWords != null) {
            expandSelectedWords(baseWords);
            // Initialisiere die HashMap
            for (String word : selectedWords) {
                fillerWordCounts.put(word.toLowerCase(), 0);
            }
        }

        Log.d("FillerWordActivityTraining", "Empfangene Wörter: " + selectedWords);

        // Mikrofonberechtigungen prüfen
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            startListeningAutomatically();
        }

        if (timerTextView == null) {
            Log.e("FillerWordActivityTraining", "timerTextView ist null! Überprüfe die XML-ID.");
            return;
        }



        // Buttons initialisieren
        ImageView backButton = findViewById(R.id.backButton);
        ImageView homeButton = findViewById(R.id.homeButton);
        ImageView closeButton = findViewById(R.id.finishButton);

        // Zurück-Button: Zur vorherigen Aktivität (AdjustmentsActivity)
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(FillerWordActivityTraining.this, FillerWordsAdjustmentTraining.class);
            startActivity(intent);
            finish(); // Optional: Schließe die aktuelle Aktivität
        });

        // Home-Button: Zur Hauptaktivität (MainActivity)
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(FillerWordActivityTraining.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Beendet die aktuelle Aktivität
        });

        // Close-Button: Zur Statistik-Activity
        closeButton.setOnClickListener(v -> {
            // Stoppe alle Hintergrundprozesse
            stopBackgroundProcesses();

            // Navigiere zur StatisticsActivityTraining
            Intent intent = new Intent(FillerWordActivityTraining.this, StatisticsActivityTraining.class);
            intent.putExtra("fillerWordCounts", fillerWordCounts);
            intent.putExtra("totalWordCount", totalWordCount);
            startActivity(intent);

            // Beende die aktuelle Aktivität
            finish();
        });

        // Watch Message Listener aktivieren, falls erforderlich
        if (useWatchAudio) {
            Wearable.getMessageClient(this).addListener(messageListener);
        }
        sendStartTimerMessage();
    }

    /**
     * Beendet alle Hintergrundprozesse wie SpeechService und entfernt Listener.
     */
    private void stopBackgroundProcesses() {
        // Stoppe den SpeechService
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
            isListening = false;
            Log.d("FillerWordActivityTraining", "SpeechService gestoppt.");
        }

        // Entferne den Wearable Message Listener, falls aktiv
        if (useWatchAudio) {
            Wearable.getMessageClient(this).removeListener(messageListener);
            Log.d("FillerWordActivityTraining", "Wearable Message Listener entfernt.");
        }

        // Weitere Hintergrundprozesse können hier gestoppt werden
    }

    private final MessageClient.OnMessageReceivedListener messageListener = new MessageClient.OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if (messageEvent.getPath().equals(AUDIO_PATH)) {
                // Verarbeite empfangene Audio-Daten
                processAudioData(messageEvent.getData());
            }
        }
    };

    private void processAudioData(byte[] audioData) {
        if (model != null && recognizer != null) {
            recognizer.acceptWaveForm(audioData, audioData.length);
            String result = recognizer.getResult();

            // Überprüfe auf Füllwörter im erkannten Text
            handleVibration(result);
        }
    }

    private void expandSelectedWords(List<String> baseWords) {
        for (String word : baseWords) {
            if (word.equalsIgnoreCase("ähm")) {
                selectedWords.addAll(Arrays.asList("ähm", "äh", "hm", "emm", "am", "m", "öhm", "uhm", "ähh", "hmm", "und am", "und im"));
            } else {
                selectedWords.add(word);
            }
        }
    }

    private void startListeningAutomatically() {
        loadModelIfNeeded();
        startListening();
    }

    private void loadModelIfNeeded() {
        if (model == null) {
            StorageService.unpack(this, "vosk-model-small-de-0.15", "model",
                    (model) -> {
                        this.model = model;
                        try {
                            recognizer = new Recognizer(model, 16000.0f);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        startListening();
                    },
                    (exception) -> setErrorState("Failed to unpack the model: " + exception.getMessage()));
        }
    }

    private void startListening() {
        if (speechService == null && model != null) {
            try {
                speechService = new SpeechService(new Recognizer(model, 16000.0f), 16000.0f);
                speechService.startListening(this);
                isListening = true;
                Log.d("FillerWordActivityTraining", "Listening started");
            } catch (IOException e) {
                setErrorState("Error starting recognition: " + e.getMessage());
            }
        }
    }

    private boolean containsSelectedWord(String text) {
        for (String word : selectedWords) {
            if (Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    private String extractTextFromHypothesis(String hypothesis) {
        try {
            JSONObject jsonObject = new JSONObject(hypothesis);
            return jsonObject.has("partial") ? jsonObject.getString("partial") : jsonObject.getString("text");
        } catch (JSONException e) {
            return "";
        }
    }

    private void handleVibration(String spokenText) {
        if (canVibrate && containsSelectedWord(spokenText)) {
            Log.d("FillerWordActivityTraining", "Füllwort erkannt: " + spokenText);

            // Visualizer für Rot-Highlight triggern
            visualizerView.triggerRedHighlight();

            String detectedWord = null;
            for (String word : selectedWords) {
                if (Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE).matcher(spokenText).find()) {
                    detectedWord = word.toLowerCase();
                    break;
                }
            }

            if (detectedWord != null) {
                // Inkrementiere die Zählung des erkannten Füllworts
                fillerWordCounts.put(detectedWord, fillerWordCounts.getOrDefault(detectedWord, 0) + 1);
                fillerWordDetectedCount++; // Gesamtzahl der erkannten Füllwörter erhöhen
            }

            // Zähle die Gesamtwörter
            totalWordCount += countWords(spokenText);

            // Setze shouldVibrate auf true für jede Erkennung
            boolean shouldVibrate = true;

            if (shouldVibrate && useWatchVibration && detectedWord != null) {
                // Sende das erkannte Wort an die Watch
                sendVibrationCommandToWatch(detectedWord);
            } else if (shouldVibrate) {
                // Sofort vibrieren auf dem Handy
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500); // Vibrieren für 500 ms
                    }
                }
            }

            // Cooldown aktivieren (optional)
            canVibrate = false;
            cooldownHandler.postDelayed(() -> {
                canVibrate = true;
                Log.d("FillerWordActivityTraining", "Cooldown beendet. Vibration wieder erlaubt.");
            }, 1000); // Reduziere die Cooldown-Zeit auf 1 Sekunde, falls erforderlich

            // Spracherkennung neustarten
            restartListening();
        }
    }



    private void sendVibrationCommandToWatch(String detectedWord) {
        // 1. NodeId herausfinden
        Wearable.getNodeClient(this).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (!nodes.isEmpty()) {
                        String nodeId = nodes.get(0).getId();
                        String path = "/message_path";  // Gleicher Pfad wie bei DeviceSelection

                        Wearable.getMessageClient(this)
                                .sendMessage(nodeId, path, detectedWord.getBytes())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FillerWordActivityTraining", "Word sent to watch: " + detectedWord);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FillerWordActivityTraining", "Failed to send word", e);
                                });
                    } else {
                        Log.w("FillerWordActivityTraining", "No watch connected!");
                    }
                });
    }

    @Override
    public void onPartialResult(String hypothesis) {
        String spokenText = extractTextFromHypothesis(hypothesis);
        resultView.setText(spokenText);

        // Berechne die Lautstärke basierend auf der Eingabelänge
        float amplitude = Math.min(spokenText.length() / 10f, 1.0f);
        visualizerView.updateAmplitude(amplitude);

        // Direktes Prüfen auf Füllwort während der Erkennung
        handleVibration(spokenText);
    }

    @Override
    public void onResult(String hypothesis) {
        String spokenText = extractTextFromHypothesis(hypothesis);
        resultView.append(spokenText + "\n");
        handleVibration(spokenText);
        speechService.reset();
        // Neustart der Spracherkennung mit kurzer Verzögerung
    }

    @Override
    public void onFinalResult(String hypothesis) {
        String spokenText = extractTextFromHypothesis(hypothesis);
        resultView.append(spokenText + "\n");

        // Verhalten wie bei Pause: Text löschen und neu starten
        resultView.setText("");
        restartListening();
    }

    @Override
    public void onError(Exception exception) {
        setErrorState("Error: " + exception.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.d("FillerWordActivityTraining", "Recognition timeout");
    }

    private void setErrorState(String message) {
        resultView.setText(message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListeningAutomatically();
        } else {
            finish();
        }
    }



    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void finish() {
        // Beende alle Hintergrundprozesse
        stopBackgroundProcesses();

        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void restartListening() {
        if (speechService != null) {
            // Stoppen der aktuellen Erkennung
            speechService.reset();
            Log.d("FillerWordActivityTraining", "Spracherkennung gestoppt.");
        }

        // Kurze Verzögerung, bevor die Erkennung neu startet
        cooldownHandler.postDelayed(() -> {
            startListening();
            Log.d("FillerWordActivityTraining", "Spracherkennung neu gestartet.");
        }, 500);
    }

    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
    private void sendStartTimerMessage() {
        String message = "start_timer"; // Optional: Fügen Sie zusätzliche Informationen hinzu
        new Thread(() -> {
            try {
                Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(getApplicationContext()).sendMessage(
                                    node.getId(), START_TIMER_PATH, message.getBytes());
                    Tasks.await(sendMessageTask);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}








