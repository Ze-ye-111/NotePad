package com.example.notepad;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class PlanningStore {
    public static final String TYPE_GOALS = "goals";
    public static final String TYPE_DAILY = "daily";

    private static final String PREFS_NAME = "notepad_planning";
    private static final String KEY_GOALS = "goals";

    private PlanningStore() {
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getGoals(Context context) {
        return preferences(context).getString(KEY_GOALS, "");
    }

    public static void setGoals(Context context, String text) {
        preferences(context).edit().putString(KEY_GOALS, text).apply();
    }

    public static String getDailyPlan(Context context) {
        return preferences(context).getString(dailyKey(), "");
    }

    public static void setDailyPlan(Context context, String text) {
        preferences(context).edit().putString(dailyKey(), text).apply();
    }

    public static String getDailyDateLabel() {
        return new SimpleDateFormat("MM月dd日", Locale.CHINA).format(new Date());
    }

    private static String dailyKey() {
        return "daily_" + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }
}
