package org.vosk.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FillerWordsAdjustmentTraining extends BaseActivity{
    private LinearLayout fillerWordsContainer;
    private Button word1Button, word2Button, word3Button, addWordButton;
    private LinearLayout currentRow;
    private ArrayList<String> selectedWords = new ArrayList<>();
    private boolean isFirstWordAdded = false, isSecondWordAdded = false, isThirdWordAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filler_words_adjustment_training);
        setupToolbar();
        // Vorherige Auswahl aus Intent laden
        String mode = getIntent().getStringExtra("mode");

        // UI-Elemente initialisieren
        fillerWordsContainer = findViewById(R.id.fillerWordsContainer);
        word1Button = findViewById(R.id.word1Button);
        word2Button = findViewById(R.id.word2Button);
        word3Button = findViewById(R.id.word3Button);
        addWordButton = findViewById(R.id.addWordButton);

        // Bestehende Buttons konfigurieren
        setupWordButton(word1Button, "Ähm");
        setupWordButton(word2Button, "Gut");
        setupWordButton(word3Button, "Genau");
        addWordButton.setOnClickListener(v -> showAddWordDialog());

        // Aktuelle Zeile für Buttons erstellen
        currentRow = createNewRow();
        fillerWordsContainer.addView(currentRow);

        // Start-Mikrofon Button unten
        findViewById(R.id.startMicrophone).setOnClickListener(v -> {
            if (selectedWords.isEmpty()) {
                Toast.makeText(this, "Keine Wörter ausgewählt!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(FillerWordsAdjustmentTraining.this, FillerWordActivityTraining.class);
                intent.putStringArrayListExtra("SELECTED_WORDS", selectedWords);

                // NEU: Sag FillerWordActivityTraining, ob wir die Uhr verwenden wollen
                boolean useWatch = getIntent().getBooleanExtra("useWatchVibration", false);
                intent.putExtra("useWatchVibration", useWatch);

                startActivity(intent);
            }
        });

    }

    private void setupWordButton(Button button, String word) {
        button.setOnClickListener(v -> toggleWordSelection(word, button));
    }

    private void toggleWordSelection(String word, Button button) {
        if (selectedWords.contains(word)) {
            selectedWords.remove(word);
            button.setBackgroundResource(R.drawable.rounded_button_empty);
            button.setTextColor(Color.WHITE);
        } else {
            selectedWords.add(word);
            button.setBackgroundResource(R.drawable.rounded_button_cyan);
            button.setTextColor(Color.WHITE);
        }
    }

    private void showAddWordDialog() {
        if (isThirdWordAdded) {
            Toast.makeText(this, "Maximal 3 Wörter erlaubt!", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText inputField = new EditText(this);
        inputField.setHint("Neues Wort eingeben");
        inputField.setHintTextColor(Color.LTGRAY); // Hint-Text in hellem Grau
        inputField.setTextColor(Color.WHITE); // Textfarbe auf Weiß setzen
        inputField.setBackgroundResource(R.drawable.rounded_input_background);

        // Layout-Parameter für Abstand hinzufügen
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0); // Abstand von oben (20dp)
        inputField.setLayoutParams(params);

        new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle("Wort hinzufügen")
                .setView(inputField)
                .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Hinzufügen", (dialog, which) -> {
                    String newWord = inputField.getText().toString().trim();
                    if (!newWord.isEmpty()) addNewWordButton(newWord);
                })
                .show();
    }

    // Neues Wort hinzufügen, maximal 3 Wörter, "+" Button bleibt immer das letzte Element
    private void addNewWordButton(String word) {
        Button newButton = createButton(word);

        if (!isFirstWordAdded) {
            LinearLayout parentRow = (LinearLayout) addWordButton.getParent();
            int index = parentRow.indexOfChild(addWordButton);
            parentRow.removeView(addWordButton);
            parentRow.addView(newButton, index);
            isFirstWordAdded = true;

            currentRow = createNewRow();
            currentRow.addView(addWordButton);
            fillerWordsContainer.addView(currentRow);
        } else if (!isSecondWordAdded) {
            currentRow.addView(newButton);
            moveAddButtonToEnd();
            isSecondWordAdded = true;
        } else if (!isThirdWordAdded) {
            LinearLayout parentRow = (LinearLayout) addWordButton.getParent();
            int index = parentRow.indexOfChild(addWordButton);
            parentRow.removeView(addWordButton);
            parentRow.addView(newButton, index);
            isThirdWordAdded = true;
            addWordButton = null;
        }

        updateRowWeights(currentRow);
    }

    private void moveAddButtonToEnd() {
        if (addWordButton.getParent() != null) {
            ((LinearLayout) addWordButton.getParent()).removeView(addWordButton);
        }
        currentRow.addView(addWordButton);
    }

    private LinearLayout createNewRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(5, 5, 5, 5);
        return row;
    }

    private Button createButton(String word) {
        Button button = new Button(this);
        button.setText(word);
        button.setTextColor(Color.WHITE);
        button.setBackgroundResource(R.drawable.rounded_button_empty);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(5, 5, 5, 5);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> toggleWordSelection(word, button));
        return button;
    }

    private void updateRowWeights(LinearLayout row) {
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(5, 5, 5, 5);
            child.setLayoutParams(params);
        }
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
