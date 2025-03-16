package com.example.woodpecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.List;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        String selectedCharset = requireActivity().getIntent().getStringExtra("morseCharset");
        updateMorseCodeList(selectedCharset);
    }

    public void updateMorseCodeList(String selectedCharset) {
        MyDB db = new MyDB(requireContext());
        List<String> charsets = db.getAllMorseCharsets();

        if (charsets.isEmpty()) {
            return;
        }

        // Convert List<String> to String[]
        String[] entries = charsets.toArray(new String[0]);
        String[] entryValues = charsets.toArray(new String[0]);

        // Find the ListPreference for morse_code
        ListPreference morseCodePref = findPreference("morse_code");
        if (morseCodePref != null) {
            morseCodePref.setEntries(entries);
            morseCodePref.setEntryValues(entryValues);

            if (selectedCharset == null || !charsets.contains(selectedCharset)) {
                selectedCharset = charsets.get(0);
            }

            // Set the selected charset in ListPreference
            morseCodePref.setValue(selectedCharset);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        System.out.println("Preference changed: " + key);

        assert key != null;
        switch (key) {
            case "language":
                String langCode = sharedPreferences.getString(key, "en");
                setAppLocale(langCode);
                break;
            case "theme":
                String themePref = sharedPreferences.getString(key, "system");
                switch (themePref) {
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
                break;
            case "morse_code":
                String standard = sharedPreferences.getString(key, "International Morse Code");
                MyDB db = new MyDB(requireContext());
                MorseCharSet updatedMorseCharSet = db.loadMorseCharset(standard);

                // Notify ConfigActivity that the charset was updated
                Bundle result = new Bundle();
                result.putSerializable("morseCharset", updatedMorseCharSet);
                getParentFragmentManager().setFragmentResult("updateMorseCharset", result);
                break;
            case "enable_notifications":
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    boolean notificationsEnabled = sharedPreferences.getBoolean(key, true);

                    if (notificationsEnabled) {
                        NotificationHelper.scheduleDailyNotification(requireContext());
                    } else {
                        NotificationHelper.cancelDailyNotification(requireContext());
                    }
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        android.Manifest.permission.POST_NOTIFICATIONS)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            NotificationHelper.NOTIFICATION_ID);
                }
                break;
        }
    }


    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        // Update the context
        requireActivity().getBaseContext().getResources().updateConfiguration(
                config,
                requireActivity().getBaseContext().getResources().getDisplayMetrics()
        );

        // Restart activity to apply changes
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}