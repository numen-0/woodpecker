package com.example.woodpecker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CheatSheetActivity extends AppCompatActivity {
    private MorseCharSet morseCharSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cheat_sheet);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(v -> openMainActivity());

        // Get the MorseCharSet object passed via Intent
        this.morseCharSet = (MorseCharSet) getIntent().getSerializableExtra("morseCharset");
        assert this.morseCharSet != null;
        String standard = this.morseCharSet.name;
        TextView tv = findViewById(R.id.standard_name);
        tv.setText(standard);

        // Initialize the ListView with the Morse Adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MorseAdapter adapter = new MorseAdapter(this, morseCharSet.getCharset(), mc -> {});
        recyclerView.setAdapter(adapter);
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("morseCharset", this.morseCharSet);
        startActivity(intent);
        finish();
    }
}
