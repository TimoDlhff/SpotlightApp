package org.vosk.demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Button Click-Listener
        Button newSessionButton = findViewById(R.id.newSessionButton);
        newSessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ModeSelectionActivity.class);
            startActivity(intent);
        });
    }
}