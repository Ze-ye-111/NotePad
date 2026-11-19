package com.example.notepad;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CategoryPreferences {
    private static final String PREFS_NAME = "notepad_categories";
    private static final String KEY_CATEGORIES = "categories";
    private static final String[] PALETTE = {
            "#F4C95D", "#F4A6A6", "#A8D8C5", "#AABDF0",
            "#E2B6EA", "#F3D8A8", "#76C5D6", "#B8D98D"
    };

    private CategoryPreferences() {
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static List<Category> getCategories(Context context) {
        String stored = preferences(context).getString(KEY_CATEGORIES, null);
        if (stored == null) {
            List<Category> defaults = defaultCategories();
            save(context, defaults);
            return defaults;
        }
        List<Category> categories = parse(stored);
        if (categories.isEmpty()) {
            categories = defaultCategories();
            save(context, categories);
        }
        return categories;
    }

    public static boolean addCategory(Context context, String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || NoteCategories.ALL.equals(name)
                || NoteCategories.PRIVATE.equals(name)) {
            return false;
        }
        List<Category> categories = getCategories(context);
        if (contains(categories, name)) {
            return false;
        }
        String color = PALETTE[categories.size() % PALETTE.length];
        categories.add(new Category(name, color));
        save(context, categories);
        return true;
    }

    public static boolean ensureCategory(Context context, String rawName) {
        String name = NoteCategories.safe(rawName);
        if (NoteCategories.PRIVATE.equals(name)) {
            return false;
        }
        if (contains(getCategories(context), name)) {
            return false;
        }
        return addCategory(context, name);
    }

    private static List<Category> parse(String stored) {
        List<Category> categories = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(stored);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String name = item.optString("name").trim();
                String color = item.optString("color", PALETTE[i % PALETTE.length]);
                if (!name.isEmpty() && !contains(categories, name)) {
                    categories.add(new Category(name, color));
                }
            }
        } catch (JSONException ignored) {
            return new ArrayList<>();
        }
        return categories;
    }

    private static boolean contains(List<Category> categories, String name) {
        for (Category category : categories) {
            if (category.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private static void save(Context context, List<Category> categories) {
        JSONArray array = new JSONArray();
        for (Category category : categories) {
            JSONObject item = new JSONObject();
            try {
                item.put("name", category.name);
                item.put("color", category.color);
                array.put(item);
            } catch (JSONException ignored) {
                // Values are plain strings, so this is only defensive.
            }
        }
        preferences(context).edit().putString(KEY_CATEGORIES, array.toString()).apply();
    }

    private static List<Category> defaultCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(NoteCategories.UNCATEGORIZED, "#E5E3E9"));
        categories.add(new Category(NoteCategories.STUDY, PALETTE[0]));
        categories.add(new Category(NoteCategories.WORK, PALETTE[1]));
        categories.add(new Category(NoteCategories.LIFE, PALETTE[2]));
        categories.add(new Category(NoteCategories.IDEA, PALETTE[3]));
        return categories;
    }
}
