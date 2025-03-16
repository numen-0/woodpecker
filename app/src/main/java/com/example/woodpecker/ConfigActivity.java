package com.example.woodpecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

public class ConfigActivity extends AppCompatActivity {
    private MorseCharSet morseCharSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_config);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(v -> openMainActivity());

        this.morseCharSet = (MorseCharSet) getIntent().getSerializableExtra("morseCharset");
        assert this.morseCharSet != null;

        getSupportFragmentManager().setFragmentResultListener("updateMorseCharset", this, (requestKey, bundle) -> {
            MorseCharSet updatedCharset = (MorseCharSet) bundle.getSerializable("morseCharset");
            if (updatedCharset != null) {
                this.morseCharSet = updatedCharset;
            }
        });
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("morseCharset", this.morseCharSet);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NotificationHelper.NOTIFICATION_ID) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                boolean notificationsEnabled = sharedPreferences.getBoolean("enable_notifications", false);
                if (notificationsEnabled) {
                    NotificationHelper.scheduleDailyNotification(this);
                }
            } else {
                sharedPreferences.edit().putBoolean("enable_notifications", false).apply();
            }
        }
    }
}