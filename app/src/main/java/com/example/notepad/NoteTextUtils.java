package com.example.notepad;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class NoteTextUtils {

    private NoteTextUtils() {
    }

    public static String displayTitle(String title) {
        return title == null || title.trim().isEmpty() ? "无标题" : title.trim();
    }

    public static String excerpt(String content, String imagePath) {
        String compact = content == null ? "" : content.trim().replaceAll("\\s+", " ");
        if (!compact.isEmpty()) {
            return compact.length() > 90 ? compact.substring(0, 90) + "…" : compact;
        }
        return imagePath == null ? "暂无正文" : "图片笔记";
    }

    public static String formatModified(Context context, long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return new SimpleDateFormat("HH:mm MM-dd-yyyy", Locale.US).format(new Date(timestamp));
    }

    public static void applyThemePreference() {
        int mode = NotePreferences.getThemeMode();
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
