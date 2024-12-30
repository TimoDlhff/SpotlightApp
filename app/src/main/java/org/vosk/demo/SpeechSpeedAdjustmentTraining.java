package org.vosk.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SpeechSpeedAdjustmentTraining extends AppCompatActivity {
    private SeekBar speedSeekBar;
    private ImageView snailIcon, rabbitIcon;
    private TextView speedLabel;
    private LinearLayout playButtonContainer;
    private Vibrator vibrator;
    private int selectedSpeed = 120; // Standardwert

    private static final String START_TIMER_PATH = "/start_timer"; // Neuer Pfad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_speed_adjustment_training);

        // UI-Elemente initialisieren
        speedSeekBar = findViewById(R.id.speedSeekBar);
        speedLabel = findViewById(R.id.speedLabel);
        snailIcon = findViewById(R.id.snailIcon);
        rabbitIcon = findViewById(R.id.rabbitIcon);
        playButtonContainer = findViewById(R.id.playButtonContainer);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Standardfarben setzen
        snailIcon.setColorFilter(Color.WHITE);
        rabbitIcon.setColorFilter(Color.WHITE);

        // SeekBar-Listener für die Farbänderung
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateIconColors(progress);
                updateSpeedLabel(progress);
                selectedSpeed = mapProgressToSpeed(progress);

                // Handy vibrieren lassen
                if (fromUser && vibrator != null) {
                    vibrator.vibrate(10); // Vibriert für 10 Millisekunden
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        // Play-Button Listener
        playButtonContainer.setOnClickListener(v -> navigateToSpeechSpeedActivity());

        // Zurück-Button in der Toolbar
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SpeechSpeedAdjustmentTraining.this, DeviceSelectionTrainingActivity.class);
            startActivity(intent);
            finish();
        });

        // Home-Button in der Toolbar
        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SpeechSpeedAdjustmentTraining.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToSpeechSpeedActivity() {
        Intent intent = new Intent(SpeechSpeedAdjustmentTraining.this, SpeechSpeedActivityTraining.class);
        intent.putExtra("speech_speed", selectedSpeed);

        // Boolean übernehmen, den Sie in DeviceSelectionTrainingActivity gesetzt haben:
        boolean watchVibration = getIntent().getBooleanExtra("useWatchVibration", false);
        intent.putExtra("useWatchVibration", watchVibration);

        // Senden der Timer-Nachricht an die Watch


        startActivity(intent);
    }



    private int mapProgressToSpeed(int progress) {
        if (progress <= 20) return 80;
        else if (progress <= 40) return 100;
        else if (progress <= 60) return 120;
        else if (progress <= 80) return 140;
        else return 160;
    }

    private int calculateColor(int progress, boolean isLeft) {
        int midPoint = 50;
        int intensity;

        if (isLeft) {
            intensity = Math.min(255, (midPoint - progress) * 255 / midPoint);
        } else {
            intensity = Math.min(255, (progress - midPoint) * 255 / midPoint);
        }

        int red = 255 - (intensity * 255 / 255);
        int green = 255;
        int blue = 255;

        return Color.rgb(red, green, blue);
    }

    private void updateIconColors(int progress) {
        int midPoint = 50;

        int snailColor = calculateColor(progress, true);
        int rabbitColor = calculateColor(progress, false);

        if (progress == midPoint) {
            snailIcon.setColorFilter(Color.WHITE);
            rabbitIcon.setColorFilter(Color.WHITE);
        } else {
            snailIcon.setColorFilter(progress < midPoint ? snailColor : Color.WHITE);
            rabbitIcon.setColorFilter(progress > midPoint ? rabbitColor : Color.WHITE);
        }
    }

    private void updateSpeedLabel(int progress) {
        String speedText;
        if (progress <= 20) speedText = "80 WPM: Sehr langsam";
        else if (progress <= 40) speedText = "100 WPM: Langsam";
        else if (progress <= 60) speedText = "120 WPM: Optimal";
        else if (progress <= 80) speedText = "140 WPM: Schnell";
        else speedText = "160 WPM: Sehr schnell";

        speedLabel.setText(speedText);
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






