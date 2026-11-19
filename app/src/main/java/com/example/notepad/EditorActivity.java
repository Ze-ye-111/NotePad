package com.example.notepad;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditorActivity extends AppCompatActivity {
    public static final String EXTRA_NOTE_ID = "note_id";
    public static final String EXTRA_INITIAL_CATEGORY = "initial_category";

    private long noteId = -1;
    private EditText titleInput;
    private EditText contentInput;
    private TextView editedTime;
    private View imagePreviewContainer;
    private ImageView imagePreview;
    private View imageActions;
    private MaterialButton removeImageButton;
    private ChipGroup categoryChips;
    private String imagePath;
    private String originalImagePath;
    private String originalTitle = "";
    private String originalContent = "";
    private String originalCategory = NoteCategories.UNCATEGORIZED;
    private boolean privateCategoryContext;
    private final List<String> replacedImages = new ArrayList<>();
    private final List<String> createdImages = new ArrayList<>();
    private ActivityResultLauncher<String> imagePicker;
    private boolean saved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.editor_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> handleBack());

        titleInput = findViewById(R.id.editor_title);
        contentInput = findViewById(R.id.editor_content);
        editedTime = findViewById(R.id.editor_time);
        imagePreviewContainer = findViewById(R.id.image_preview_container);
        imagePreview = findViewById(R.id.image_preview);
        imageActions = findViewById(R.id.image_actions);
        removeImageButton = findViewById(R.id.remove_image_button);
        categoryChips = findViewById(R.id.editor_categories);
        MaterialButton addImageButton = findViewById(R.id.add_image_button);

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(), this::handleSelectedImage);
        addImageButton.setOnClickListener(view -> imagePicker.launch("image/*"));
        removeImageButton.setOnClickListener(view -> removeImage());
        imagePreview.setOnClickListener(view -> openImagePreview());

        if (noteId >= 0) {
            loadNote();
        } else {
            editedTime.setText(R.string.new_note);
            String initialCategory = NoteCategories.safe(getIntent().getStringExtra(
                    EXTRA_INITIAL_CATEGORY));
            privateCategoryContext = NoteCategories.PRIVATE.equals(initialCategory);
            if (!privateCategoryContext) {
                CategoryPreferences.ensureCategory(this, initialCategory);
            }
            refreshCategoryChips(initialCategory);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveAndFinish();
            return true;
        }
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNote() {
        Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId);
        try (Cursor cursor = getContentResolver().query(
                uri, NotePad.Notes.PROJECTION, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            originalTitle = cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TITLE));
            originalContent = cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_NOTE));
            titleInput.setText(originalTitle);
            contentInput.setText(originalContent);
            imagePath = cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_IMAGE_PATH));
            originalImagePath = imagePath;
            originalCategory = NoteCategories.safe(cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_CATEGORY)));
            privateCategoryContext = NoteCategories.PRIVATE.equals(originalCategory);
            if (!privateCategoryContext) {
                CategoryPreferences.ensureCategory(this, originalCategory);
            }
            refreshCategoryChips(originalCategory);
            long modified = cursor.getLong(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            editedTime.setText(getString(R.string.edited_at,
                    NoteTextUtils.formatModified(this, modified)));
            showImage();
        }
    }

    private void refreshCategoryChips(String selectedCategory) {
        categoryChips.removeAllViews();
        int selectedId = View.NO_ID;
        if (privateCategoryContext) {
            Chip privateChip = (Chip) getLayoutInflater().inflate(
                    R.layout.chip_category, categoryChips, false);
            privateChip.setId(View.generateViewId());
            privateChip.setText(R.string.private_notebook);
            privateChip.setTag(NoteCategories.PRIVATE);
            privateChip.setChipIconResource(R.drawable.ic_lock);
            privateChip.setChipIconVisible(true);
            categoryChips.addView(privateChip);
            if (NoteCategories.PRIVATE.equals(selectedCategory)) {
                selectedId = privateChip.getId();
            }
        }
        for (Category category : CategoryPreferences.getCategories(this)) {
            if (NoteCategories.PRIVATE.equals(category.name)) {
                continue;
            }
            Chip chip = (Chip) getLayoutInflater().inflate(
                    R.layout.chip_category, categoryChips, false);
            chip.setId(View.generateViewId());
            chip.setText(category.name);
            chip.setTag(category.name);
            categoryChips.addView(chip);
            if (category.name.equals(selectedCategory)) {
                selectedId = chip.getId();
            }
        }
        if (selectedId == View.NO_ID && categoryChips.getChildCount() > 0) {
            selectedId = categoryChips.getChildAt(0).getId();
        }
        if (selectedId != View.NO_ID) {
            categoryChips.check(selectedId);
        }

        Chip addCategory = (Chip) getLayoutInflater().inflate(
                R.layout.chip_category, categoryChips, false);
        addCategory.setText(R.string.add_category);
        addCategory.setCheckable(false);
        addCategory.setOnClickListener(view ->
                CategoryDialogHelper.showAddCategory(this, name ->
                        refreshCategoryChips(name)));
        categoryChips.addView(addCategory);
    }

    private String selectedCategory() {
        int checkedId = categoryChips.getCheckedChipId();
        Chip chip = findViewById(checkedId);
        if (chip == null || chip.getTag() == null) {
            return NoteCategories.UNCATEGORIZED;
        }
        return NoteCategories.safe(chip.getTag().toString());
    }

    private void handleSelectedImage(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            String newImage = NoteImageStore.copyFromUri(this, uri);
            createdImages.add(newImage);
            if (imagePath != null) {
                replacedImages.add(imagePath);
            }
            imagePath = newImage;
            showImage();
        } catch (IOException exception) {
            Toast.makeText(this, R.string.image_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeImage() {
        if (imagePath != null) {
            replacedImages.add(imagePath);
            imagePath = null;
        }
        showImage();
    }

    private void showImage() {
        if (imagePath == null) {
            imagePreviewContainer.setVisibility(View.GONE);
            imageActions.setVisibility(View.VISIBLE);
            removeImageButton.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            return;
        }
        Bitmap bitmap = NoteImageStore.decodePreview(this, imagePath, 1200, 900);
        if (bitmap == null) {
            imagePath = null;
            imagePreviewContainer.setVisibility(View.GONE);
            imageActions.setVisibility(View.VISIBLE);
            removeImageButton.setVisibility(View.GONE);
            return;
        }
        imagePreview.setImageBitmap(bitmap);
        imagePreviewContainer.setVisibility(View.VISIBLE);
        imageActions.setVisibility(View.VISIBLE);
        removeImageButton.setVisibility(View.VISIBLE);
    }

    private void openImagePreview() {
        if (imagePath == null || NoteImageStore.resolve(this, imagePath) == null) {
            return;
        }
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_PATH, imagePath);
        startActivity(intent);
    }

    private void handleBack() {
        if (saved || !hasUnsavedChanges()) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.unsaved_changes_title)
                .setMessage(R.string.unsaved_changes_message)
                .setPositiveButton(R.string.save, (dialog, which) -> saveAndFinish())
                .setNegativeButton(R.string.discard_changes,
                        (dialog, which) -> discardAndFinish())
                .setNeutralButton(R.string.keep_editing, null)
                .show();
    }

    private boolean hasUnsavedChanges() {
        return !titleInput.getText().toString().equals(originalTitle)
                || !contentInput.getText().toString().equals(originalContent)
                || !selectedCategory().equals(originalCategory)
                || !Objects.equals(imagePath, originalImagePath);
    }

    private void discardAndFinish() {
        cleanupDiscardedImages();
        saved = true;
        setResult(RESULT_CANCELED);
        finish();
    }

    private void saveAndFinish() {
        if (saved) {
            finish();
            return;
        }
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();
        if (title.isEmpty() && content.isEmpty() && imagePath == null) {
            cleanupDiscardedImages();
            Toast.makeText(this, R.string.empty_note_not_saved, Toast.LENGTH_SHORT).show();
            saved = true;
            finish();
            return;
        }
        if (title.isEmpty()) {
            title = content.length() > 24 ? content.substring(0, 24) : content;
        }
        if (title.isEmpty()) {
            title = getString(R.string.image_note_title);
        }

        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, content);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, selectedCategory());
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
        if (imagePath == null) {
            values.putNull(NotePad.Notes.COLUMN_NAME_IMAGE_PATH);
        } else {
            values.put(NotePad.Notes.COLUMN_NAME_IMAGE_PATH, imagePath);
        }

        boolean success;
        if (noteId < 0) {
            Uri inserted = getContentResolver().insert(NotePad.Notes.CONTENT_URI, values);
            success = inserted != null;
        } else {
            Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId);
            success = getContentResolver().update(uri, values, null, null) > 0;
        }
        if (!success) {
            Toast.makeText(this, R.string.save_note_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        for (String oldImage : replacedImages) {
            if (!oldImage.equals(imagePath)) {
                NoteImageStore.delete(this, oldImage);
            }
        }
        replacedImages.clear();
        createdImages.clear();
        saved = true;
        setResult(RESULT_OK);
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_note)
                .setMessage(R.string.delete_current_note_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteNote())
                .show();
    }

    private void deleteNote() {
        List<String> images = new ArrayList<>(replacedImages);
        images.addAll(createdImages);
        if (originalImagePath != null) {
            images.add(originalImagePath);
        }
        if (imagePath != null) {
            images.add(imagePath);
        }
        if (noteId >= 0) {
            Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId);
            try (Cursor cursor = getContentResolver().query(
                    uri,
                    new String[]{NotePad.Notes.COLUMN_NAME_IMAGE_PATH},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String storedImage = cursor.getString(0);
                    if (storedImage != null && !images.contains(storedImage)) {
                        images.add(storedImage);
                    }
                }
            }
            getContentResolver().delete(uri, null, null);
        }
        for (String image : images) {
            NoteImageStore.delete(this, image);
        }
        saved = true;
        setResult(RESULT_OK);
        finish();
    }

    private void cleanupDiscardedImages() {
        for (String image : createdImages) {
            NoteImageStore.delete(this, image);
        }
        createdImages.clear();
        replacedImages.clear();
    }
}
