package com.example.woodpecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled = preferences.getBoolean("enable_notifications", false);

        if (notificationsEnabled) {
            String language = preferences.getString("language", "en"); // Default to English
            List<String> words = loadWordsFromFile(context, language);

            if (!words.isEmpty()) {
                String wordOfTheDay = words.get(new Random().nextInt(words.size()));
                NotificationHelper.sendWordOfTheDayNotification(context, wordOfTheDay);
            }
        }
    }

    private List<String> loadWordsFromFile(Context context, String language) {
        List<String> words = new ArrayList<>();
        int fileId;

        switch (language) {
            case "es":
                fileId = R.raw.spanish_words;
                break;
            case "eu":
                fileId = R.raw.basque_words;
                break;
            case "en":
            default:
                fileId = R.raw.english_words;
                break;
        }

        try (InputStream inputStream = context.getResources().openRawResource(fileId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }
}
