package com.example.woodpecker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MorseCharSet morseCharSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.playButton);
        button.setOnClickListener(v -> openActivity(GameActivity.class));

        button = findViewById(R.id.editorButton);
        button.setOnClickListener(v -> openActivity(EditorActivity.class));

        button = findViewById(R.id.cheatButton);
        button.setOnClickListener(v -> openActivity(CheatSheetActivity.class));

        button = findViewById(R.id.configButton);
        button.setOnClickListener(v -> openActivity(ConfigActivity.class));

        morseCharSet = (MorseCharSet) getIntent().getSerializableExtra("morseCharset");

        if (morseCharSet == null) { // DB
            MyDB db = new MyDB(this);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String standard = prefs.getString("morse_code", "International Morse Code");
            this.morseCharSet = db.loadMorseCharset(standard);
            System.out.println(this.morseCharSet);
        }
    }

    public void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("morseCharset", this.morseCharSet);
        startActivity(intent);
        finish();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(newBase);
        String language = prefs.getString("language", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        switch (prefs.getString("theme", "system")) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        Context newContext = newBase.createConfigurationContext(config);
        super.attachBaseContext(newContext);
    }
}