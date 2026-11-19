package com.example.notepad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotePadProviderTest {

    @Test
    public void providerSupportsNoteLifecycle() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String uniqueTitle = "Provider test " + System.nanoTime();
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_TITLE, uniqueTitle);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, "searchable body");
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, NoteCategories.STUDY);

        Uri noteUri = context.getContentResolver()
                .insert(NotePad.Notes.CONTENT_URI, values);
        assertNotNull(noteUri);

        long createdModified;
        try (Cursor cursor = context.getContentResolver().query(
                noteUri, NotePad.Notes.PROJECTION, null, null, null)) {
            assertNotNull(cursor);
            assertTrue(cursor.moveToFirst());
            assertEquals(uniqueTitle, cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TITLE)));
            assertEquals("searchable body", cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_NOTE)));
            assertEquals(NoteCategories.STUDY, cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_CATEGORY)));
            createdModified = cursor.getLong(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            assertTrue(createdModified > 0);
        }

        ContentValues update = new ContentValues();
        update.put(NotePad.Notes.COLUMN_NAME_TITLE, uniqueTitle + " updated");
        update.put(NotePad.Notes.COLUMN_NAME_PINNED, 1);
        assertEquals(1, context.getContentResolver().update(noteUri, update, null, null));

        try (Cursor cursor = context.getContentResolver().query(
                noteUri, NotePad.Notes.PROJECTION, null, null, null)) {
            assertNotNull(cursor);
            assertTrue(cursor.moveToFirst());
            assertEquals(uniqueTitle + " updated", cursor.getString(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_TITLE)));
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_PINNED)));
            assertEquals(createdModified, cursor.getLong(cursor.getColumnIndexOrThrow(
                    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)));
        }

        assertEquals(1, context.getContentResolver().delete(noteUri, null, null));
        try (Cursor cursor = context.getContentResolver().query(
                noteUri, NotePad.Notes.PROJECTION, null, null, null)) {
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        }
    }
}
