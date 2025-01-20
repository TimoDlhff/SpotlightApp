package org.vosk.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class SpeechSpeedActivityPresentation extends BaseActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final String TAG = "SpeechSpeedDetector";

    private CircularProgressViewPresentation outerCircle;
    private TextView wpmNumber, wpmLabel, timerText;
    private int thresholdSpeed;
    private SpeechRecognizer speechRecognizer;
    private Vibrator vibrator;

    private long startTime;
    private int totalWords = 0;
    private boolean isInitialPhase = true;

    private ArrayList<Long> wordTimestamps = new ArrayList<>();
    private static final int TIME_WINDOW_MS = 5000;
    private static final int INITIAL_DELAY_MS = 3000; // Reduzierte initiale Verzögerung
    private static final int TOLERANCE_WPM = 5;

    private boolean isOutOfThreshold = false;
    private static final int VIBRATION_COOLDOWN_MS = 3000;
    private long lastVibrationTime = 0;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private int elapsedSeconds = 0;

    private boolean useWatchVibration = false;
    private static final String MESSAGE_PATH = "/message_path";
    private static final String START_TIMER_PATH = "/start_timer"; // Neuer Pfad

    // Neue Variablen zur genauen Erfassung der schnellen Sprechdauer
    private long fastStartTime = 0; // Zeitpunkt, wann das schnelle Sprechen beginnt
    private long fastDuration = 0;  // Gesamtdauer des schnellen Sprechens in Millisekunden

    // Neue Variable zur Vermeidung der doppelten Wortzählung
    private Set<String> previousWords = new HashSet<>();

    // Handler und Runnable für die Vibration-Verzögerung
    private Handler vibrationHandler = new Handler();
    private Runnable vibrationRunnable;
    private boolean vibrationScheduled = false;

    // Neue Runnable für die verzögerte Vibration nach 8 Sekunden
    private Runnable delayedVibrationRunnable;

    // Neue Konstante für die Verzögerung von 8 Sekunden vor der Vibration
    private static final int VIBRATION_DELAY_MS = 8000; // 8 Sekunden Verzögerung

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_speed_presentation);
        setupToolbar();

        Intent intent = getIntent();
        thresholdSpeed = intent.getIntExtra("speech_speed", 120);
        useWatchVibration = intent.getBooleanExtra("useWatchVibration", false);

        // UI-Elemente initialisieren
        outerCircle = findViewById(R.id.outerCircle);
        wpmNumber = findViewById(R.id.wpmNumber);
        wpmLabel = findViewById(R.id.wpmLabel);
        timerText = findViewById(R.id.timerText);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        outerCircle.setMaxProgress(thresholdSpeed);

        // Timer starten
        startTimer();

        // Berechtigungen prüfen
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startListening();
        }

        // Finish-Button (X) Listener
        ImageView finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(v -> {
            // Stoppe alle Hintergrundprozesse
            stopBackgroundProcesses();

            // Falls das schnelle Sprechen gerade aktiv ist, die verbleibende Zeit hinzufügen
            long currentTime = System.currentTimeMillis();
            if (fastStartTime != 0) {
                fastDuration += (currentTime - fastStartTime);
                fastStartTime = 0;
            }

            // Berechne den Prozentsatz zu schnelles Sprechen
            float fastSpeechPercentage = (elapsedSeconds > 0) ? (fastDuration / 1000f) * 100f / elapsedSeconds : 0f;

            // Begrenzen auf 0-100%
            fastSpeechPercentage = clamp(fastSpeechPercentage, 0f, 100f);

            // Navigiere zur StatisticsActivitySpeechSpeed
            Intent statsIntent = new Intent(SpeechSpeedActivityPresentation.this, StatisticsActivitySpeechSpeedPresentation.class);
            statsIntent.putExtra("fastSpeechPercentage", fastSpeechPercentage);
            startActivity(statsIntent);

            // Beende die aktuelle Aktivität
            finish();
        });

        // Initialisierung des vibrationRunnable für die Cooldown-Reset
        vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                vibrationScheduled = false;
                // Keine Aktion erforderlich, da wir vibrationScheduled zurücksetzen
            }
        };

        // Initialisierung des delayedVibrationRunnable für die 8 Sekunden Verzögerung
        delayedVibrationRunnable = new Runnable() {
            @Override
            public void run() {
                triggerVibration();
                lastVibrationTime = System.currentTimeMillis();
                vibrationScheduled = false;
            }
        };

        // Senden der Timer-Nachricht an die Smartwatch
        sendStartTimerMessage();
    }

    /**
     * Beendet alle Hintergrundprozesse wie SpeechRecognizer, Vibrator und geplante Vibrationen.
     */
    private void stopBackgroundProcesses() {
        // Stoppe die Spracherkennung
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
            Log.d(TAG, "SpeechRecognizer gestoppt und zerstört.");
        }

        // Stoppe den Timer
        if (timerHandler != null) {
            timerHandler.removeCallbacksAndMessages(null);
        }

        // Stoppe den Vibrator, falls nötig
        if (vibrator != null) {
            vibrator.cancel();
            Log.d(TAG, "Vibrator gestoppt.");
        }

        // Stoppe geplante Vibrationen
        if (vibrationHandler != null) {
            vibrationHandler.removeCallbacks(vibrationRunnable);
            vibrationHandler.removeCallbacks(delayedVibrationRunnable); // Sicherstellen, dass auch die verzögerte Vibration gestoppt wird
        }

        // Weitere Hintergrundprozesse können hier gestoppt werden
    }

    /**
     * Startet den Timer, der die verstrichene Zeit in Sekunden zählt und fastDuration aktualisiert.
     */
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                int minutes = elapsedSeconds / 60;
                int seconds = elapsedSeconds % 60;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);

                // Kontinuierliche Aktualisierung von fastDuration
                // Die Dauer wird bereits in checkSpeedThreshold gemessen
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    /**
     * Initialisiert und startet den SpeechRecognizer.
     */
    private void startListening() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                startTime = System.currentTimeMillis();
                totalWords = 0;
                previousWords.clear(); // Reset previous words when ready for speech
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partialData = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialData != null && !partialData.isEmpty()) {
                    trackSpeechSpeed(partialData.get(0));
                }
            }

            @Override
            public void onResults(Bundle results) {
                // Verwenden von onResults statt onPartialResults für genauere Ergebnisse
                ArrayList<String> resultData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (resultData != null && !resultData.isEmpty()) {
                    trackSpeechSpeed(resultData.get(0));
                }
                startListening(); // Kontinuierliches Zuhören
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Fehler: " + error);
                startListening();
            }

            @Override
            public void onEndOfSpeech() {
                // Entfernen des UI-Resets, um die WPM-Anzeige intakt zu halten
                // resetUI(); // Entfernt, um WPM nicht zurückzusetzen bei Pausen
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    /**
     * Verfolgt die Sprechgeschwindigkeit basierend auf dem gesprochenen Text.
     *
     * @param spokenText Der erkannte gesprochene Text
     */
    private void trackSpeechSpeed(String spokenText) {
        long currentTime = System.currentTimeMillis();
        List<String> words = splitAndCleanWords(spokenText);
        int currentWordCount = words.size();

        // Finden der neuen Wörter seit dem letzten Update
        List<String> newWords = new ArrayList<>();
        for (String word : words) {
            if (!previousWords.contains(word)) {
                newWords.add(word);
                previousWords.add(word);
            }
        }

        // Hinzufügen von Zeitstempeln für neue Wörter
        for (String word : newWords) {
            wordTimestamps.add(currentTime);
            totalWords++;
        }

        // Entfernen von Zeitstempeln außerhalb des Zeitfensters
        while (!wordTimestamps.isEmpty() && (currentTime - wordTimestamps.get(0)) > TIME_WINDOW_MS) {
            wordTimestamps.remove(0);
        }

        if (isInitialPhase) {
            if ((currentTime - startTime) < INITIAL_DELAY_MS) {
                wpmNumber.setText("--");
                wpmLabel.setText("WPM");
                return;
            } else {
                isInitialPhase = false;
            }
        }

        double wordsPerSecond = wordTimestamps.size() / (TIME_WINDOW_MS / 1000.0);
        int wpm = (int) (wordsPerSecond * 60);

        updateUI(wpm);
        checkSpeedThreshold(wpm);
    }

    /**
     * Teilt den gesprochenen Text in Wörter auf und bereinigt sie.
     *
     * @param text Gesprochener Text
     * @return Liste der bereinigten Wörter
     */
    private List<String> splitAndCleanWords(String text) {
        List<String> cleanWords = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return cleanWords;
        }
        String[] words = text.trim().split("\\s+");
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-ZäöüÄÖÜß]", "").toLowerCase();
            if (!cleanWord.isEmpty()) {
                cleanWords.add(cleanWord);
            }
        }
        return cleanWords;
    }

    /**
     * Aktualisiert die UI-Komponenten basierend auf der aktuellen WPM.
     *
     * @param wpm Wörter pro Minute
     */
    private void updateUI(int wpm) {
        wpmNumber.setText(String.valueOf(wpm));
        wpmLabel.setText("WPM");

        if (thresholdSpeed <= 100) {
            int progress = Math.max(0, thresholdSpeed - wpm);
            outerCircle.setProgress(progress);
        } else {
            int progress = Math.min(wpm, thresholdSpeed);
            outerCircle.setProgress(progress);
        }
    }

    /**
     * Überprüft, ob die Sprechgeschwindigkeit außerhalb des festgelegten Schwellenwerts liegt.
     *
     * @param wpm Aktuelle Wörter pro Minute
     */
    private void checkSpeedThreshold(int wpm) {
        long currentTime = System.currentTimeMillis();
        int lowerLimit = Math.max(0, thresholdSpeed - TOLERANCE_WPM);
        int upperLimit = thresholdSpeed + TOLERANCE_WPM;

        boolean currentlyOutOfThreshold =
                (thresholdSpeed <= 100 && wpm < lowerLimit) ||
                        (thresholdSpeed > 100 && wpm > upperLimit);

        if (currentlyOutOfThreshold) {
            if (fastStartTime == 0) {
                fastStartTime = currentTime; // Startzeitpunkt setzen

                // Überprüfen, ob der Cooldown abgelaufen ist und keine Vibration geplant ist
                if (currentTime - lastVibrationTime >= VIBRATION_COOLDOWN_MS && !vibrationScheduled) {
                    // Vibration erst nach VIBRATION_DELAY_MS (8 Sekunden) auslösen
                    vibrationHandler.postDelayed(delayedVibrationRunnable, VIBRATION_DELAY_MS);
                    vibrationScheduled = true;
                }
            }
        } else {
            if (fastStartTime != 0) {
                fastDuration += (currentTime - fastStartTime); // Dauer hinzufügen
                fastStartTime = 0; // Reset
            }

            // Abbrechen der geplanten Vibration, da die Sprechgeschwindigkeit wieder innerhalb des Schwellenwerts liegt
            if (vibrationScheduled) {
                vibrationHandler.removeCallbacks(delayedVibrationRunnable);
                vibrationScheduled = false;
            }
        }

        isOutOfThreshold = currentlyOutOfThreshold;
    }

    /**
     * Führt eine Vibration aus oder sendet eine Nachricht an die Smartwatch.
     */
    private void triggerVibration() {
        if (useWatchVibration) {
            sendMessageToWatch("Zu schnell!");
        } else {
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        }
    }

    /**
     * Sendet eine Nachricht an die verbundene Smartwatch.
     *
     * @param message Die Nachricht, die gesendet werden soll
     */
    private void sendMessageToWatch(String message) {
        Wearable.getNodeClient(this).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (!nodes.isEmpty()) {
                        String nodeId = nodes.get(0).getId();

                        Wearable.getMessageClient(this)
                                .sendMessage(nodeId, MESSAGE_PATH, message.getBytes())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Message sent to watch: " + message);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to send message", e);
                                });
                    } else {
                        Log.w(TAG, "No watch connected!");
                    }
                });
    }

    /**
     * Sendet eine Timer-Nachricht an die Smartwatch.
     */
    private void sendStartTimerMessage() {
        String message = "start_timer"; // Zusätzliche Informationen können hinzugefügt werden
        new Thread(() -> {
            try {
                List<Node> nodes = Tasks.await(Wearable.getNodeClient(getApplicationContext()).getConnectedNodes());
                for (Node node : nodes) {
                    Tasks.await(Wearable.getMessageClient(getApplicationContext()).sendMessage(
                            node.getId(), START_TIMER_PATH, message.getBytes()
                    ));
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Berechtigung erforderlich", Toast.LENGTH_SHORT).show();
                // Optional: Beende die Aktivität, wenn die Berechtigung nicht erteilt wurde
                finish();
            }
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

        // Entferne alle geplanten Vibrationen
        vibrationHandler.removeCallbacks(vibrationRunnable);
        vibrationHandler.removeCallbacks(delayedVibrationRunnable); // Sicherstellen, dass auch die verzögerte Vibration entfernt wird

        // Entferne alle Timer-Callbacks
        timerHandler.removeCallbacks(timerRunnable);

        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Diese Methode ist nicht mehr notwendig und kann entfernt werden.
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    @Override
    protected void onDestroy() {
        // Beende alle Hintergrundprozesse
        stopBackgroundProcesses();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        stopBackgroundProcesses();
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopBackgroundProcesses();
        super.onStop();
    }

    /**
     * Hilfsmethode zur Begrenzung von Werten.
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

