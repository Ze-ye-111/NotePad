package com.example.notepad;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class PlanningEditorActivity extends AppCompatActivity {
    public static final String EXTRA_PLAN_TYPE = "plan_type";

    private String planType;
    private String originalText;
    private TextInputEditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_editor);

        Toolbar toolbar = findViewById(R.id.planning_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> handleBack());

        planType = getIntent().getStringExtra(EXTRA_PLAN_TYPE);
        boolean goals = PlanningStore.TYPE_GOALS.equals(planType);
        int titleRes = goals ? R.string.planning_goals_title
                : R.string.planning_daily_title;
        setTitle(titleRes);
        toolbar.setTitle(titleRes);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleRes);
        }
        originalText = goals ? PlanningStore.getGoals(this) : PlanningStore.getDailyPlan(this);
        input = findViewById(R.id.planning_input);
        input.setText(originalText);
        ((android.widget.TextView) findViewById(R.id.planning_date)).setText(
                goals ? getString(R.string.planning_goals_meta)
                        : getString(R.string.planning_daily_meta,
                                PlanningStore.getDailyDateLabel()));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_planning_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save_planning) {
            saveAndFinish();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            handleBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleBack() {
        if (input.getText() == null
                || input.getText().toString().equals(originalText)) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.unsaved_changes_title)
                .setMessage(R.string.unsaved_changes_message)
                .setPositiveButton(R.string.save, (dialog, which) -> saveAndFinish())
                .setNegativeButton(R.string.discard_changes, (dialog, which) -> finish())
                .setNeutralButton(R.string.keep_editing, null)
                .show();
    }

    private void saveAndFinish() {
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (PlanningStore.TYPE_GOALS.equals(planType)) {
            PlanningStore.setGoals(this, text);
        } else {
            PlanningStore.setDailyPlan(this, text);
        }
        finish();
    }
}
