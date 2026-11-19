package com.example.notepad;

import android.text.Editable;
import android.text.TextWatcher;

public final class SimpleTextWatcher implements TextWatcher {

    public interface OnTextChanged {
        void run();
    }

    private final OnTextChanged listener;

    public SimpleTextWatcher(OnTextChanged listener) {
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        listener.run();
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}
