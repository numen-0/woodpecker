package com.example.woodpecker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {
    private MorseCanvasView morseCanvasView;
    private MorseCharSet morseCharSet;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        morseCanvasView = findViewById(R.id.morseCanvas);

        morseCanvasView.setTextView(findViewById(R.id.textView));

        this.morseCharSet = (MorseCharSet) getIntent().getSerializableExtra("morseCharset");
        assert this.morseCharSet != null;
        morseCanvasView.setCharSet(this.morseCharSet);
        String standard = this.morseCharSet.name;
        TextView tv = findViewById(R.id.standard_name);
        tv.setText(standard);

        // Hook up button with touch listener
        Button tapButton = findViewById(R.id.tapButton);
        tapButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    morseCanvasView.tap(); // tap (start)
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    morseCanvasView.untap(); // untap (stop)
                    return true;
                default:
                    return false;
            }
        });

        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("morseCharset", this.morseCharSet);
        startActivity(intent);
        finish();
    }
}
