package org.vosk.demo;

import android.Manifest;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private static final int INITIAL_DELAY_MS = 5000;
    private static final int TOLERANCE_WPM = 5;

    private boolean isOutOfThreshold = false;
    private static final int VIBRATION_COOLDOWN_MS = 3000;
    private long lastVibrationTime = 0;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private int elapsedSeconds = 0;

    private boolean useWatchVibration = false;
    private static final String MESSAGE_PATH = "/message_path";
    private int fastSpeechTimeSeconds = 0; // Variable für zu schnelles Sprechen

    // Handler und Runnable für die 10-Sekunden-Verzögerung
    private Handler vibrationHandler = new Handler();
    private Runnable vibrationRunnable;
    private boolean vibrationScheduled = false;

    private static final String START_TIMER_PATH = "/start_timer"; // Neuer Pfad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_speed_presentation);
        setupToolbar();

        // Initialize UI components
        outerCircle = findViewById(R.id.outerCircle);
        wpmNumber = findViewById(R.id.wpmNumber);
        wpmLabel = findViewById(R.id.wpmLabel);
        timerText = findViewById(R.id.timerText);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Schwellenwert aus vorheriger Activity
        thresholdSpeed = getIntent().getIntExtra("speech_speed", 120);
        useWatchVibration = getIntent().getBooleanExtra("useWatchVibration", false);
        outerCircle.setMaxProgress(thresholdSpeed);

        // Timer starten
        startTimer();

        // Berechtigungen prüfen
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startListening();
        }


        // Finish-Button (X) Listener hinzufügen
        ImageView finishButton = findViewById(R.id.finishButton);
        if (finishButton != null) {
            finishButton.setVisibility(View.VISIBLE); // Finish-Button anzeigen
            finishButton.setOnClickListener(v -> {
                // Stoppe alle Hintergrundprozesse
                stopBackgroundProcesses();

                // Berechne den Prozentsatz zu schnelles Sprechen
                float fastSpeechPercentage = (elapsedSeconds > 0) ? (fastSpeechTimeSeconds * 100f) / elapsedSeconds : 0f;

                // Navigiere zur StatisticsActivitySpeechSpeedPresentation
                Intent statsIntent = new Intent(SpeechSpeedActivityPresentation.this, StatisticsActivitySpeechSpeedPresentation.class);
                statsIntent.putExtra("fastSpeechPercentage", fastSpeechPercentage);
                startActivity(statsIntent);

                // Beende die aktuelle Aktivität
                finish();
            });
        }

        // Initialisierung des vibrationRunnable für 10-Sekunden-Verzögerung
        vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                triggerVibration();
                vibrationScheduled = false;
                lastVibrationTime = System.currentTimeMillis();
            }
        };

        // Senden der Timer-Nachricht an die Smartwatch
        sendStartTimerMessage();
    }

    /**
     * Beendet alle Hintergrundprozesse wie SpeechRecognizer und Vibrator.
     */
    private void stopBackgroundProcesses() {
        // Stoppe die Spracherkennung
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        // Stoppe den Timer
        if (timerHandler != null) {
            timerHandler.removeCallbacksAndMessages(null);
        }

        // Stoppe den Vibrator, falls nötig
        if (vibrator != null) {
            vibrator.cancel();
        }

        // Weitere Hintergrundprozesse können hier gestoppt werden
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                int minutes = elapsedSeconds / 60;
                int seconds = elapsedSeconds % 60;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

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
                startListening(); // Kontinuierliches Zuhören
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Fehler: " + error);
                startListening();
            }

            @Override
            public void onEndOfSpeech() {
                resetUI(); // Kreis zurücksetzen
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

    private void trackSpeechSpeed(String spokenText) {
        long currentTime = System.currentTimeMillis();
        int currentWordCount = spokenText.split("\\s+").length;

        for (int i = totalWords; i < currentWordCount; i++) {
            wordTimestamps.add(currentTime);
        }
        totalWords = currentWordCount;

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

    private void resetUI() {
        wpmNumber.setText("0");
        wpmLabel.setText("WPM");
        outerCircle.setProgress(0);
    }

    private void checkSpeedThreshold(int wpm) {
        long currentTime = System.currentTimeMillis();
        int lowerLimit = Math.max(0, thresholdSpeed - TOLERANCE_WPM);
        int upperLimit = thresholdSpeed + TOLERANCE_WPM;

        boolean currentlyOutOfThreshold =
                (thresholdSpeed <= 100 && wpm < lowerLimit) ||
                        (thresholdSpeed > 100 && wpm > upperLimit);

        if (currentlyOutOfThreshold && !isOutOfThreshold) {
            fastSpeechTimeSeconds++; // Erhöhe die Zeit bei zu schnellem Sprechen
            if (currentTime - lastVibrationTime >= VIBRATION_COOLDOWN_MS) {
                triggerVibration();
                lastVibrationTime = currentTime;
            }
        }
        isOutOfThreshold = currentlyOutOfThreshold;
    }

    private void triggerVibration() {
        if (useWatchVibration) {
            sendMessageToWatch("Zu schnell!");
        } else {
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }
            }
        }
    }

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

    private void sendStartTimerMessage() {
        String message = "start_timer"; // Zusätzliche Informationen können hinzugefügt werden
        new Thread(() -> {
            try {
                Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
                List<Node> nodes = Tasks.await(nodeListTask);
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

        // Entferne alle Timer-Callbacks
        timerHandler.removeCallbacks(timerRunnable);

        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void restartListening() {
        if (speechRecognizer != null) {
            // Stoppen der aktuellen Erkennung
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
            Log.d(TAG, "Spracherkennung gestoppt und zerstört.");
        }

        // Kurze Verzögerung, bevor die Erkennung neu startet
        timerHandler.postDelayed(() -> {
            startListening();
            Log.d(TAG, "Spracherkennung neu gestartet.");
        }, 500);
    }

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
}
