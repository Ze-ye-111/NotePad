package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PrivateNotebookActivity extends AppCompatActivity {
    private boolean firstSetup;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmGroup;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_notebook);

        Toolbar toolbar = findViewById(R.id.private_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        firstSetup = !PrivatePasswordManager.hasPassword(this);
        passwordLayout = findViewById(R.id.private_password_layout);
        confirmGroup = findViewById(R.id.private_confirm_group);
        passwordInput = findViewById(R.id.private_password_input);
        confirmInput = findViewById(R.id.private_confirm_input);
        TextView title = findViewById(R.id.private_title);
        TextView description = findViewById(R.id.private_description);
        MaterialButton submit = findViewById(R.id.private_submit);

        title.setText(firstSetup
                ? R.string.private_notebook_new_title
                : R.string.private_notebook_unlock_title);
        description.setText(firstSetup
                ? R.string.private_notebook_new_description
                : R.string.private_notebook_unlock_description);
        submit.setText(firstSetup
                ? R.string.create_private_password
                : R.string.unlock_private_notebook);
        confirmGroup.setVisibility(firstSetup ? View.VISIBLE : View.GONE);
        submit.setOnClickListener(view -> submitPassword());
        confirmInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitPassword();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void submitPassword() {
        passwordLayout.setError(null);
        if (confirmGroup.getVisibility() == View.VISIBLE) {
            confirmGroup.setError(null);
        }
        String password = text(passwordInput);
        if (password.length() < 4) {
            passwordLayout.setError(getString(R.string.private_password_too_short));
            return;
        }
        if (firstSetup) {
            if (!password.equals(text(confirmInput))) {
                confirmGroup.setError(getString(R.string.private_password_mismatch));
                return;
            }
            PrivatePasswordManager.setPassword(this, password);
        } else if (!PrivatePasswordManager.verify(this, password)) {
            passwordLayout.setError(getString(R.string.private_password_wrong));
            return;
        }
        Toast.makeText(this, R.string.private_unlocked, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CATEGORY, NoteCategories.PRIVATE);
        startActivity(intent);
        finish();
    }

    @NonNull
    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
