package com.example.notepad;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class NotePreferences {
    private static final String PREFS_NAME = "notepad_preferences";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_SORT = "sort_order";

    public static final String SORT_MODIFIED = "modified";
    public static final String SORT_CREATED = "created";
    public static final String SORT_TITLE = "title";

    private static NotePadApplication application;

    private NotePreferences() {
    }

    public static void initialize(NotePadApplication app) {
        application = app;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static int getThemeMode(Context context) {
        return preferences(context).getInt(
                KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static int getThemeMode() {
        return application == null
                ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                : getThemeMode(application);
    }

    public static void setThemeMode(Context context, int mode) {
        preferences(context).edit().putInt(KEY_THEME, mode).apply();
    }

    public static String getSortOrder(Context context) {
        return preferences(context).getString(KEY_SORT, SORT_MODIFIED);
    }

    public static void setSortOrder(Context context, String order) {
        preferences(context).edit().putString(KEY_SORT, order).apply();
    }

    public static String getSortSql(Context context) {
        String order = getSortOrder(context);
        if (SORT_CREATED.equals(order)) {
            return NotePad.Notes.COLUMN_NAME_PINNED + " DESC, "
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " DESC";
        }
        if (SORT_TITLE.equals(order)) {
            return NotePad.Notes.COLUMN_NAME_PINNED + " DESC, "
                    + NotePad.Notes.COLUMN_NAME_TITLE + " COLLATE NOCASE ASC";
        }
        return NotePad.Notes.DEFAULT_SORT_ORDER;
    }
}
