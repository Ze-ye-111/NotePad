package com.example.notepad;

public final class Note {
    public final long id;
    public final String title;
    public final String content;
    public final String category;
    public final String imagePath;
    public final boolean pinned;
    public final long modified;

    public Note(long id, String title, String content, String category,
                String imagePath, boolean pinned, long modified) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.category = NoteCategories.safe(category);
        this.imagePath = imagePath;
        this.pinned = pinned;
        this.modified = modified;
    }
}
