package com.example.notepad;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.preferences_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        bindTheme(findViewById(R.id.theme_options));
        bindSort(findViewById(R.id.sort_options));
    }

    private void bindTheme(RadioGroup group) {
        int mode = NotePreferences.getThemeMode(this);
        int checked = mode == AppCompatDelegate.MODE_NIGHT_NO
                ? R.id.theme_light
                : mode == AppCompatDelegate.MODE_NIGHT_YES
                        ? R.id.theme_dark : R.id.theme_system;
        group.check(checked);
        group.setOnCheckedChangeListener((radioGroup, id) -> {
            int newMode;
            if (id == R.id.theme_light) {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (id == R.id.theme_dark) {
                newMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            NotePreferences.setThemeMode(this, newMode);
            AppCompatDelegate.setDefaultNightMode(newMode);
        });
    }

    private void bindSort(RadioGroup group) {
        String sort = NotePreferences.getSortOrder(this);
        int checked = NotePreferences.SORT_CREATED.equals(sort)
                ? R.id.sort_created
                : NotePreferences.SORT_TITLE.equals(sort) ? R.id.sort_title : R.id.sort_modified;
        group.check(checked);
        group.setOnCheckedChangeListener((radioGroup, id) -> {
            String value;
            if (id == R.id.sort_created) {
                value = NotePreferences.SORT_CREATED;
            } else if (id == R.id.sort_title) {
                value = NotePreferences.SORT_TITLE;
            } else {
                value = NotePreferences.SORT_MODIFIED;
            }
            NotePreferences.setSortOrder(this, value);
        });
    }
}
