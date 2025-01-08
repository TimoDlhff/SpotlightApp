package org.vosk.demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SpeechSpeedAdjustmentPresentation extends BaseActivity {
    private SeekBar speedSeekBar;
    private ImageView snailIcon, rabbitIcon;
    private TextView speedLabel;
    private LinearLayout playButtonContainer;
    private Vibrator vibrator;
    private int selectedSpeed = 120; // Standardwert für die Geschwindigkeit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_speed_adjustment_presentation);
        setupToolbar();
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

        // SeekBar-Listener für Farbänderung und Geschwindigkeit
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



        // Play-Button OnClickListener hinzufügen
        playButtonContainer.setOnClickListener(v -> navigateToSpeechSpeedActivity());



    }

    private void navigateToSpeechSpeedActivity() {
        Intent intent = new Intent(SpeechSpeedAdjustmentPresentation.this, SpeechSpeedActivityPresentation.class);

        // Deine eingestellte Sprechgeschwindigkeit
        intent.putExtra("speech_speed", selectedSpeed);

        // WICHTIG: Hier das useWatchVibration-Flag aus dem eigenen Intent lesen und weitergeben
        boolean watchVibration = getIntent().getBooleanExtra("useWatchVibration", false);
        intent.putExtra("useWatchVibration", watchVibration);

        startActivity(intent);
    }


    private int mapProgressToSpeed(int progress) {
        // Fortschritt der SeekBar zu einer Geschwindigkeit zuordnen
        if (progress <= 20) return 80;
        else if (progress <= 40) return 100;
        else if (progress <= 60) return 120;
        else if (progress <= 80) return 140;
        else return 160;
    }

    private int calculateColor(int progress, boolean isLeft) {
        int midPoint = 50; // Mitte der SeekBar
        int intensity;

        if (isLeft) {
            intensity = Math.min(255, (midPoint - progress) * 255 / midPoint);
        } else {
            intensity = Math.min(255, (progress - midPoint) * 255 / midPoint);
        }

        // Weiß -> Lila (148,159,255)
        int red = 255 - (intensity * (255 - 148) / 255);
        int green = 255 - (intensity * (255 - 159) / 255);
        int blue = 255 - (intensity * (255 - 255) / 255);

        return Color.rgb(red, green, blue);
    }

    private void updateIconColors(int progress) {
        int midPoint = 50;

        // Farben für Schnecke und Hase berechnen
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
        // Geschwindigkeitstext je nach Position der SeekBar anpassen
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
