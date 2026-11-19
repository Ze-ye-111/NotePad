package com.example.notepad;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private LinearLayout planningRow;
    private LinearLayout categoryContainer;
    private TextView homeDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        planningRow = findViewById(R.id.planning_row);
        categoryContainer = findViewById(R.id.category_container);
        homeDate = findViewById(R.id.home_date);
        FloatingActionButton addButton = findViewById(R.id.home_add_note_button);
        addButton.setOnClickListener(view -> openEditor(NoteCategories.UNCATEGORIZED));
        findViewById(R.id.all_notes_button).setOnClickListener(
                view -> openCategory(NoteCategories.ALL));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            findViewById(R.id.home_top_area).setPadding(0, top, 0, 0);
            view.setPadding(0, 0, 0, bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_all_notes) {
            openCategory(NoteCategories.ALL);
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshDashboard() {
        homeDate.setText(new SimpleDateFormat(
                "M月d日 EEEE", Locale.CHINA).format(new Date()));
        buildPlanningTiles();
        buildCategoryTiles();
    }

    private void buildPlanningTiles() {
        planningRow.removeAllViews();
        addPlanningTile(
                getString(R.string.planning_goals_title),
                getString(R.string.planning_goals_meta),
                PlanningStore.getGoals(this),
                "#F4C95D");
        addPlanningTile(
                getString(R.string.planning_daily_title),
                getString(R.string.planning_daily_meta, PlanningStore.getDailyDateLabel()),
                PlanningStore.getDailyPlan(this),
                "#FFF0C9");
    }

    private void addPlanningTile(String title, String meta, String content, String color) {
        View tile = LayoutInflater.from(this).inflate(R.layout.item_planning_tile, planningRow, false);
        tile.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
        ((TextView) tile.findViewById(R.id.planning_tile_title)).setText(title);
        ((TextView) tile.findViewById(R.id.planning_tile_meta)).setText(meta);
        TextView preview = tile.findViewById(R.id.planning_tile_preview);
        preview.setText(content == null || content.trim().isEmpty()
                ? getString(R.string.empty_planning_hint)
                : content.trim());
        tile.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlanningEditorActivity.class);
            intent.putExtra(PlanningEditorActivity.EXTRA_PLAN_TYPE,
                    title.equals(getString(R.string.planning_goals_title))
                            ? PlanningStore.TYPE_GOALS : PlanningStore.TYPE_DAILY);
            startActivity(intent);
        });
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(176), 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        planningRow.addView(tile, params);
    }

    private void buildCategoryTiles() {
        Map<String, Integer> counts = loadCategoryCounts();
        List<Category> storedCategories = CategoryPreferences.getCategories(this);
        for (String category : counts.keySet()) {
            if (NoteCategories.PRIVATE.equals(category)) {
                continue;
            }
            boolean present = false;
            for (Category stored : storedCategories) {
                if (stored.name.equals(category)) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                CategoryPreferences.ensureCategory(this, category);
            }
        }

        List<Category> categories = CategoryPreferences.getCategories(this);
        List<TileData> tiles = new ArrayList<>();
        tiles.add(new TileData(
                NoteCategories.PRIVATE,
                "#CFCBF7",
                counts.getOrDefault(NoteCategories.PRIVATE, 0),
                false,
                true));
        for (Category category : categories) {
            if (NoteCategories.PRIVATE.equals(category.name)) {
                continue;
            }
            tiles.add(new TileData(category.name, category.color,
                    counts.getOrDefault(category.name, 0), false, false));
        }
        tiles.add(new TileData(
                getString(R.string.add_category), "#EEEAF9", 0, true, false));

        categoryContainer.removeAllViews();
        for (int index = 0; index < tiles.size(); index += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            categoryContainer.addView(row);
            addCategoryTile(row, tiles.get(index));
            if (index + 1 < tiles.size()) {
                addCategoryTile(row, tiles.get(index + 1));
            } else {
                View spacer = new View(this);
                LinearLayout.LayoutParams spacerParams =
                        new LinearLayout.LayoutParams(0, dp(146), 1f);
                spacerParams.setMargins(dp(5), dp(5), dp(5), dp(5));
                row.addView(spacer, spacerParams);
            }
        }
    }

    private void addCategoryTile(LinearLayout row, TileData data) {
        View tile = LayoutInflater.from(this).inflate(R.layout.item_category_tile, row, false);
        tile.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(data.color)));
        TextView name = tile.findViewById(R.id.category_tile_name);
        TextView count = tile.findViewById(R.id.category_tile_count);
        TextView action = tile.findViewById(R.id.category_tile_action);
        View lock = tile.findViewById(R.id.category_tile_lock);
        lock.setVisibility(data.privateTile ? View.VISIBLE : View.GONE);
        name.setText(data.addTile ? "+" : data.name);
        if (data.addTile) {
            count.setText(R.string.create_custom_category);
        } else if (data.privateTile) {
            count.setText(getString(R.string.private_tile_locked, data.count));
        } else {
            count.setText(getString(R.string.category_note_count, data.count));
        }
        if (data.addTile) {
            action.setText(R.string.tap_to_add);
        } else if (data.privateTile) {
            action.setText(R.string.unlock_sticky_category);
        } else {
            action.setText(R.string.open_category);
        }
        tile.setOnClickListener(view -> {
            if (data.addTile) {
                CategoryDialogHelper.showAddCategory(this, category -> refreshDashboard());
            } else if (data.privateTile) {
                startActivity(new Intent(this, PrivateNotebookActivity.class));
            } else {
                openCategory(data.name);
            }
        });
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(146), 1f);
        params.setMargins(dp(5), dp(5), dp(5), dp(5));
        row.addView(tile, params);
    }

    private Map<String, Integer> loadCategoryCounts() {
        Map<String, Integer> counts = new HashMap<>();
        try (Cursor cursor = getContentResolver().query(
                NotePad.Notes.CONTENT_URI,
                new String[]{NotePad.Notes.COLUMN_NAME_CATEGORY},
                null, null, null)) {
            if (cursor != null) {
                int categoryIndex = cursor.getColumnIndexOrThrow(
                        NotePad.Notes.COLUMN_NAME_CATEGORY);
                while (cursor.moveToNext()) {
                    String category = NoteCategories.safe(cursor.getString(categoryIndex));
                    counts.put(category, counts.getOrDefault(category, 0) + 1);
                }
            }
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.load_notes_failed, Toast.LENGTH_SHORT).show();
        }
        return counts;
    }

    private void openCategory(String category) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CATEGORY, category);
        startActivity(intent);
    }

    private void openEditor(String category) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_INITIAL_CATEGORY, category);
        startActivity(intent);
    }

    private void showAbout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class TileData {
        final String name;
        final String color;
        final int count;
        final boolean addTile;
        final boolean privateTile;

        TileData(String name, String color, int count, boolean addTile) {
            this(name, color, count, addTile, false);
        }

        TileData(String name, String color, int count, boolean addTile,
                 boolean privateTile) {
            this.name = name;
            this.color = color;
            this.count = count;
            this.addTile = addTile;
            this.privateTile = privateTile;
        }
    }
}
