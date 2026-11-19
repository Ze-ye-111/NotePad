package com.example.notepad;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public final class PrivatePasswordDialog {
    private PrivatePasswordDialog() {
    }

    public static void showChangePassword(Activity activity) {
        View content = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_change_password, null, false);
        TextInputLayout oldLayout = content.findViewById(R.id.old_password_layout);
        TextInputLayout newLayout = content.findViewById(R.id.new_password_layout);
        TextInputLayout confirmLayout = content.findViewById(R.id.confirm_password_layout);
        TextInputEditText oldInput = content.findViewById(R.id.old_password_input);
        TextInputEditText newInput = content.findViewById(R.id.new_password_input);
        TextInputEditText confirmInput = content.findViewById(R.id.confirm_password_input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.change_private_password)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm_change, null)
                .create();
        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    oldLayout.setError(null);
                    newLayout.setError(null);
                    confirmLayout.setError(null);
                    String oldPassword = text(oldInput);
                    String newPassword = text(newInput);
                    String confirmation = text(confirmInput);
                    if (!PrivatePasswordManager.verify(activity, oldPassword)) {
                        oldLayout.setError(activity.getString(R.string.private_password_wrong));
                        return;
                    }
                    if (newPassword.length() < 4) {
                        newLayout.setError(activity.getString(R.string.private_password_too_short));
                        return;
                    }
                    if (!newPassword.equals(confirmation)) {
                        confirmLayout.setError(
                                activity.getString(R.string.private_password_mismatch));
                        return;
                    }
                    PrivatePasswordManager.setPassword(activity, newPassword);
                    dialog.dismiss();
                    Toast.makeText(activity, R.string.private_password_changed,
                            Toast.LENGTH_SHORT).show();
                }));
        dialog.show();
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
