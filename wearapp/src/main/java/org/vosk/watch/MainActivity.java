package org.vosk.watch;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends FragmentActivity {

    private TextView textView; // Verwendet sowohl für Willkommensnachricht als auch für Timer
    private static final String MESSAGE_PATH = "/message_path";
    private static final String START_TIMER_PATH = "/start_timer";

    private Handler timerHandler = new Handler();
    private int timerSeconds = 0;
    private Runnable timerRunnable;

    private MessageClient.OnMessageReceivedListener messageListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verhindert, dass das Display ausgeschaltet wird
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView = findViewById(R.id.textView);

        // Initialisiere den Message Listener
        messageListener = new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if (messageEvent.getPath().equals(MESSAGE_PATH)) {
                    // Behandlung von Willkommensnachrichten und Füllwort-Vibrationen
                    String message = new String(messageEvent.getData());
                    runOnUiThread(() -> {
                        // Setzen der Willkommensnachricht
                        textView.setText(message);

                        // Vibration
                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(500);
                            }
                        }
                    });
                } else if (messageEvent.getPath().equals(START_TIMER_PATH)) {
                    // Behandlung von Timer-Nachrichten: Starten des Timers
                    runOnUiThread(() -> {
                        startTimerOnWatch();
                    });
                }
            }
        };

        // Registriere den Listener
        Wearable.getMessageClient(this).addListener(messageListener);
    }

    private void startTimerOnWatch() {
        // Initialisieren Sie den Timer
        timerSeconds = 0;
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int minutes = timerSeconds / 60;
                int seconds = timerSeconds % 60;

                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                textView.setText(timeFormatted); // Aktualisieren der TextView zur Anzeige des Timers

                timerSeconds++;
                timerHandler.postDelayed(this, 1000);
            }
        };

        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        // Entferne den Message Listener
        Wearable.getMessageClient(this).removeListener(messageListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Beende die App, wenn sie nicht mehr sichtbar ist
        closeApp();
    }

    /**
     * Methode zum vollständigen Beenden der App
     */
    private void closeApp() {
        // Beende alle Aktivitäten und schließe die App vollständig
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
        // Optional: Beende den Prozess (nicht empfohlen)
        // System.exit(0);
    }
}

