package org.vosk.demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Wearable;

import org.vosk.Recognizer;
import org.vosk.android.SpeechService;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    // Gemeinsame Ressourcen, die in vielen Activities verwendet werden
    protected SpeechService speechService;
    protected Recognizer recognizer;
    protected Handler handler;
    protected boolean useWatchAudio;
    protected MessageClient.OnMessageReceivedListener messageListener;
    protected Vibrator vibrator;
    protected boolean isListening;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
    }

    /**
     * Diese Methode sollte explizit nach setContentView in der abgeleiteten Activity aufgerufen werden.
     */
    protected void setupToolbar() {
        ImageView backButton = findViewById(R.id.backButton);
        ImageView homeButton = findViewById(R.id.homeButton);

        if (backButton != null) {
            Log.d(TAG, "BackButton gefunden");
            backButton.setOnClickListener(v -> handleBackButton());
        } else {
            Log.e(TAG, "BackButton nicht gefunden!");
        }

        if (homeButton != null) {
            Log.d(TAG, "HomeButton gefunden");
            homeButton.setOnClickListener(v -> navigateToHome());
        } else {
            Log.e(TAG, "HomeButton nicht gefunden!");
        }
    }

    /**
     * Standardverhalten für den Back-Button: Aufruf der Standard-Back-Funktion.
     */
    protected void handleBackButton() {
        onBackPressed();
    }

    /**
     * Navigiert zur MainActivity und schließt alle anderen Activities.
     */
    protected void navigateToHome() {
        // Ressourcen freigeben
        cleanupResources();

        // Intent zur MainActivity erstellen
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Beendet alle Activities in der Task
        finishAffinity();
    }


    /**
     * Zentrale Methode zum Freigeben aller Ressourcen.
     * Diese Methode kümmert sich um die häufigsten Ressourcen.
     * Wenn eine Activity zusätzliche Ressourcen hat, kann sie diese Methode überschreiben
     * und super.cleanupResources() aufrufen.
     */
    protected void cleanupResources() {
        // Stoppe den SpeechService
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
            isListening = false;
            Log.d(TAG, "SpeechService gestoppt und beendet");
        }

        // Beende die Spracherkennung
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
            Log.d(TAG, "Recognizer geschlossen");
        }

        // Entferne den Wearable Message Listener
        if (useWatchAudio && messageListener != null) {
            Wearable.getMessageClient(this).removeListener(messageListener);
            Log.d(TAG, "Wearable Message Listener entfernt");
        }

        // Stoppe alle Handler
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG, "Handler-Callbacks entfernt");
        }

        // Vibrator freigeben
        if (vibrator != null) {
            vibrator.cancel();
            Log.d(TAG, "Vibrator gestoppt");
        }
    }

    @Override
    protected void onDestroy() {
        cleanupResources();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        cleanupResources();
        super.onBackPressed();
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
