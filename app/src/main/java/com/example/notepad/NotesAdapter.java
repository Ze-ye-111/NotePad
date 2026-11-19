package com.example.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public final class NotesAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Note> notes;

    public NotesAdapter(Context context, List<Note> notes) {
        this.inflater = LayoutInflater.from(context);
        this.notes = notes;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Note getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return notes.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_note, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Note note = getItem(position);
        Context context = parent.getContext();
        holder.title.setText(NoteTextUtils.displayTitle(note.title));
        holder.excerpt.setText(NoteTextUtils.excerpt(note.content, note.imagePath));
        holder.date.setText(NoteTextUtils.formatModified(context, note.modified));
        holder.category.setText(note.category);
        holder.pinned.setVisibility(note.pinned ? View.VISIBLE : View.GONE);
        holder.hasImage.setVisibility(note.imagePath == null ? View.GONE : View.VISIBLE);
        return convertView;
    }

    private static final class ViewHolder {
        final TextView title;
        final TextView excerpt;
        final TextView date;
        final TextView category;
        final TextView pinned;
        final TextView hasImage;

        ViewHolder(View view) {
            title = view.findViewById(R.id.note_title);
            excerpt = view.findViewById(R.id.note_excerpt);
            date = view.findViewById(R.id.note_date);
            category = view.findViewById(R.id.note_category);
            pinned = view.findViewById(R.id.note_pinned);
            hasImage = view.findViewById(R.id.note_has_image);
        }
    }
}
