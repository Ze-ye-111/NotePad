package com.example.notepad;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NotePad {
    public static final String AUTHORITY = "com.example.notepad.provider";

    private NotePad() {
    }

    public static final class Notes implements BaseColumns {
        public static final String TABLE_NAME = "notes";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_NOTE = "note";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_IMAGE_PATH = "image_path";
        public static final String COLUMN_NAME_PINNED = "pinned";
        public static final String COLUMN_NAME_CREATE_DATE = "created";
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/notes");
        public static final Uri CONTENT_ID_URI_BASE =
                Uri.parse("content://" + AUTHORITY + "/notes/");
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.example.notepad.note";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.example.notepad.note";
        public static final String DEFAULT_SORT_ORDER =
                COLUMN_NAME_PINNED + " DESC, " + COLUMN_NAME_MODIFICATION_DATE + " DESC";

        public static final String[] PROJECTION = {
                _ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_NOTE,
                COLUMN_NAME_CATEGORY,
                COLUMN_NAME_IMAGE_PATH,
                COLUMN_NAME_PINNED,
                COLUMN_NAME_CREATE_DATE,
                COLUMN_NAME_MODIFICATION_DATE
        };

        private Notes() {
        }
    }
}
