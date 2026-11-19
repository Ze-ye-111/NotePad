package com.example.notepad;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;

public final class CategoryDialogHelper {
    private CategoryDialogHelper() {
    }

    public static void showAddCategory(Activity activity, Consumer<String> onAdded) {
        View content = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_add_category, null, false);
        EditText nameInput = content.findViewById(R.id.category_name_input);
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.add_category)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, null)
                .create();
        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        nameInput.setError(activity.getString(R.string.category_name_required));
                        return;
                    }
                    if (name.length() > 12) {
                        nameInput.setError(activity.getString(R.string.category_name_too_long));
                        return;
                    }
                    if (!CategoryPreferences.addCategory(activity, name)) {
                        nameInput.setError(activity.getString(R.string.category_exists));
                        return;
                    }
                    dialog.dismiss();
                    onAdded.accept(name);
                }));
        dialog.show();
    }
}
