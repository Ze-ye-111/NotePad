package com.example.notepad;

import android.app.Application;

public class NotePadApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotePreferences.initialize(this);
        NoteTextUtils.applyThemePreference();
    }
}
