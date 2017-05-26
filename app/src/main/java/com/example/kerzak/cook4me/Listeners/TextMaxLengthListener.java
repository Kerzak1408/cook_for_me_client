package com.example.kerzak.cook4me.Listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Kerzak on 26-May-17.
 */

public class TextMaxLengthListener implements TextWatcher {
    int allowedLength;

    public TextMaxLengthListener(int allowedLength) {
        this.allowedLength = allowedLength;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > allowedLength) {
            s.delete(allowedLength,s.length());
        }
    }
}
