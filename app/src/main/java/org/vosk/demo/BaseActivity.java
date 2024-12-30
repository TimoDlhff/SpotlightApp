package org.vosk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Das Setup für die Toolbar wird später aufgerufen
    }

    // Diese Methode wird explizit nach setContentView in der abgeleiteten Activity aufgerufen
    protected void setupToolbar() {
        ImageView backButton = findViewById(R.id.backButton);
        ImageView homeButton = findViewById(R.id.homeButton);

        if (backButton != null) {
            Log.d(TAG, "BackButton gefunden");
            backButton.setOnClickListener(v -> onBackPressed());
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

    protected void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}



