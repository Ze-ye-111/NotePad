package com.example.notepad;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotePadProvider extends ContentProvider {
    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final String DATABASE_NAME = "notepad.db";
    private static final int DATABASE_VERSION = 1;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(NotePad.AUTHORITY, "notes", NOTES);
        URI_MATCHER.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);
    }

    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                cursor = database.query(
                        NotePad.Notes.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder == null ? NotePad.Notes.DEFAULT_SORT_ORDER : sortOrder);
                break;
            case NOTE_ID:
                cursor = database.query(
                        NotePad.Notes.TABLE_NAME,
                        projection,
                        NotePad.Notes._ID + " = ?",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        null);
                break;
            default:
                throw new IllegalArgumentException("未知的笔记 URI: " + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                return NotePad.Notes.CONTENT_TYPE;
            case NOTE_ID:
                return NotePad.Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("未知的笔记 URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues initialValues) {
        if (URI_MATCHER.match(uri) != NOTES) {
            throw new IllegalArgumentException("只能向笔记列表插入数据: " + uri);
        }
        ContentValues values = initialValues == null
                ? new ContentValues() : new ContentValues(initialValues);
        long now = System.currentTimeMillis();
        putIfMissing(values, NotePad.Notes.COLUMN_NAME_TITLE, "");
        putIfMissing(values, NotePad.Notes.COLUMN_NAME_NOTE, "");
        putIfMissing(values, NotePad.Notes.COLUMN_NAME_CATEGORY, NoteCategories.UNCATEGORIZED);
        putIfMissing(values, NotePad.Notes.COLUMN_NAME_PINNED, 0);
        putIfMissing(values, NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
        putIfMissing(values, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);

        long rowId = databaseHelper.getWritableDatabase()
                .insertOrThrow(NotePad.Notes.TABLE_NAME, null, values);
        Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, rowId);
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(noteUri, null);
        }
        return noteUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                count = database.delete(NotePad.Notes.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTE_ID:
                count = database.delete(
                        NotePad.Notes.TABLE_NAME,
                        NotePad.Notes._ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new IllegalArgumentException("未知的笔记 URI: " + uri);
        }
        if (getContext() != null && count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        if (values == null) {
            return 0;
        }
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                count = database.update(
                        NotePad.Notes.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTE_ID:
                count = database.update(
                        NotePad.Notes.TABLE_NAME,
                        values,
                        NotePad.Notes._ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new IllegalArgumentException("未知的笔记 URI: " + uri);
        }
        if (getContext() != null && count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private static final class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT NOT NULL DEFAULT '',"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT NOT NULL DEFAULT '',"
                    + NotePad.Notes.COLUMN_NAME_CATEGORY + " TEXT NOT NULL DEFAULT '未分类',"
                    + NotePad.Notes.COLUMN_NAME_IMAGE_PATH + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_PINNED + " INTEGER NOT NULL DEFAULT 0,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER NOT NULL,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER NOT NULL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            // Version 1 is the initial schema. Migrations for future versions belong here.
        }
    }

    private static void putIfMissing(ContentValues values, String key, String value) {
        if (!values.containsKey(key)) {
            values.put(key, value);
        }
    }

    private static void putIfMissing(ContentValues values, String key, Integer value) {
        if (!values.containsKey(key)) {
            values.put(key, value);
        }
    }

    private static void putIfMissing(ContentValues values, String key, Long value) {
        if (!values.containsKey(key)) {
            values.put(key, value);
        }
    }
}
