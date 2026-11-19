package com.example.notepad;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "category";

    private final List<Note> notes = new ArrayList<>();
    private NotesAdapter adapter;
    private EditText searchInput;
    private ListView notesList;
    private TextView emptyTitle;
    private TextView emptyMessage;
    private String categoryFilter = NoteCategories.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        String requestedCategory = getIntent().getStringExtra(EXTRA_CATEGORY);
        categoryFilter = requestedCategory == null
                ? NoteCategories.ALL : NoteCategories.safe(requestedCategory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        searchInput = findViewById(R.id.search_input);
        notesList = findViewById(R.id.notes_list);
        emptyTitle = findViewById(R.id.empty_title);
        emptyMessage = findViewById(R.id.empty_message);
        FloatingActionButton addButton = findViewById(R.id.add_note_button);

        adapter = new NotesAdapter(this, notes);
        notesList.setAdapter(adapter);
        notesList.setOnItemClickListener((parent, view, position, id) ->
                openEditor(notes.get(position).id));
        notesList.setOnItemLongClickListener((parent, view, position, id) -> {
            showNoteActions(notes.get(position));
            return true;
        });

        addButton.setOnClickListener(view -> openEditor(-1));
        searchInput.addTextChangedListener(new SimpleTextWatcher(this::refreshNotes));
        searchInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            findViewById(R.id.top_area).setPadding(0, top, 0, 0);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem changePassword = menu.findItem(R.id.action_change_private_password);
        changePassword.setVisible(NoteCategories.PRIVATE.equals(categoryFilter));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_change_private_password) {
            PrivatePasswordDialog.showChangePassword(this);
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.about_title)
                    .setMessage(R.string.about_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshNotes() {
        notes.clear();
        List<String> clauses = new ArrayList<>();
        List<String> args = new ArrayList<>();

        String query = searchInput.getText().toString().trim();
        if (!query.isEmpty()) {
            clauses.add("(" + NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR "
                    + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?)");
            args.add("%" + query + "%");
            args.add("%" + query + "%");
        }
        if (NoteCategories.ALL.equals(categoryFilter)) {
            clauses.add(NotePad.Notes.COLUMN_NAME_CATEGORY + " != ?");
            args.add(NoteCategories.PRIVATE);
        } else {
            clauses.add(NotePad.Notes.COLUMN_NAME_CATEGORY + " = ?");
            args.add(categoryFilter);
        }

        String selection = clauses.isEmpty() ? null : String.join(" AND ", clauses);
        String[] selectionArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        try (Cursor cursor = getContentResolver().query(
                NotePad.Notes.CONTENT_URI,
                NotePad.Notes.PROJECTION,
                selection,
                selectionArgs,
                NotePreferences.getSortSql(this))) {
            if (cursor != null) {
                int idIndex = cursor.getColumnIndexOrThrow(NotePad.Notes._ID);
                int titleIndex = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE);
                int noteIndex = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE);
                int categoryIndex = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CATEGORY);
                int imageIndex = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_IMAGE_PATH);
                int pinnedIndex = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_PINNED);
                int modifiedIndex = cursor.getColumnIndexOrThrow(
                        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
                while (cursor.moveToNext()) {
                    notes.add(new Note(
                            cursor.getLong(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(noteIndex),
                            cursor.getString(categoryIndex),
                            cursor.getString(imageIndex),
                            cursor.getInt(pinnedIndex) == 1,
                            cursor.getLong(modifiedIndex)));
                }
            }
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.load_notes_failed, Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
        updateHeader();
        updateEmptyState(query);
    }

    private void updateHeader() {
        TextView title = findViewById(R.id.toolbar_title);
        TextView subtitle = findViewById(R.id.toolbar_subtitle);
        title.setText(NoteCategories.ALL.equals(categoryFilter)
                ? getString(R.string.all_notes) : categoryFilter);
        if (notes.isEmpty()) {
            subtitle.setText(R.string.no_notes);
        } else {
            subtitle.setText(getResources().getQuantityString(
                    R.plurals.note_count, notes.size(), notes.size()));
        }
    }

    private void updateEmptyState(String query) {
        boolean empty = notes.isEmpty();
        notesList.setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.empty_state).setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!query.isEmpty()) {
            emptyTitle.setText(R.string.empty_search_title);
            emptyMessage.setText(R.string.empty_search_message);
        } else if (!NoteCategories.ALL.equals(categoryFilter)) {
            emptyTitle.setText(R.string.empty_category_title);
            emptyMessage.setText(R.string.empty_category_message);
        } else {
            emptyTitle.setText(R.string.empty_notes_title);
            emptyMessage.setText(R.string.empty_notes_message);
        }
    }

    private void showSortDialog() {
        String[] options = getResources().getStringArray(R.array.sort_options);
        String[] values = getResources().getStringArray(R.array.sort_values);
        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(NotePreferences.getSortOrder(this))) {
                selected = i;
                break;
            }
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sort_notes)
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    NotePreferences.setSortOrder(this, values[which]);
                    refreshNotes();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showNoteActions(Note note) {
        String pinAction = getString(note.pinned ? R.string.unpin_note : R.string.pin_note);
        String[] actions = {pinAction, getString(R.string.delete_note)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(note.title)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        togglePinned(note);
                    } else {
                        confirmDelete(note);
                    }
                })
                .show();
    }

    private void togglePinned(Note note) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_PINNED, note.pinned ? 0 : 1);
        Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, note.id);
        getContentResolver().update(uri, values, null, null);
        refreshNotes();
    }

    private void confirmDelete(Note note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_note)
                .setMessage(getString(R.string.delete_note_message, note.title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, note.id);
                    getContentResolver().delete(uri, null, null);
                    NoteImageStore.delete(this, note.imagePath);
                    refreshNotes();
                })
                .show();
    }

    private void openEditor(long noteId) {
        Intent intent = new Intent(this, EditorActivity.class);
        if (noteId >= 0) {
            intent.putExtra(EditorActivity.EXTRA_NOTE_ID, noteId);
        } else {
            intent.putExtra(EditorActivity.EXTRA_INITIAL_CATEGORY,
                    NoteCategories.ALL.equals(categoryFilter)
                            ? NoteCategories.UNCATEGORIZED : categoryFilter);
        }
        startActivity(intent);
    }

    private void hideKeyboard() {
        InputMethodManager manager = getSystemService(InputMethodManager.class);
        if (manager != null) {
            manager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
        searchInput.clearFocus();
    }
}
