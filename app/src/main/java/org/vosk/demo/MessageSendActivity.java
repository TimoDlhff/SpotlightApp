package org.vosk.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MessageSendActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;
    private EditText editText;
    private Button sendButton;
    private static final String MESSAGE_PATH = "/message_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);

        // Message receiver
        Wearable.getMessageClient(this).addListener((messageEvent) -> {
            if (messageEvent.getPath().equals(MESSAGE_PATH)) {
                final String message = new String(messageEvent.getData());
                runOnUiThread(() -> {
                    textView.setText(message);
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendButton) {
            String message = editText.getText().toString();
            new Thread(() -> {
                try {
                    Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
                    List<Node> nodes = Tasks.await(nodeListTask);
                    for (Node node : nodes) {
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(getApplicationContext()).sendMessage(
                                        node.getId(), MESSAGE_PATH, message.getBytes());
                        Tasks.await(sendMessageTask);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}