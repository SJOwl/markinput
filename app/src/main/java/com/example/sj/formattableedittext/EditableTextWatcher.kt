package com.example.sj.formattableedittext

import android.text.Editable
import android.text.TextWatcher

/**
 * Created by vorobei on 04/07/2018
 * An extension of TextWatcher which stops further callbacks being called as
 * a result of a change happening within the callbacks themselves.
 */
abstract class EditableTextWatcher : TextWatcher {

    var isEditing: Boolean = false
        private set(value) {
            field = value
        }

    override fun beforeTextChanged(s: CharSequence, start: Int,
                                   count: Int, after: Int) {
        if (isEditing) return

        isEditing = true
        try {
            beforeTextChange(s, start, count, after)
        } finally {
            isEditing = false
        }
    }

    protected open fun beforeTextChange(s: CharSequence, start: Int,
                                        count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int,
                               before: Int, count: Int) {
        if (isEditing) return

        isEditing = true
        try {
            onTextChange(s, start, before, count)
        } finally {
            isEditing = false
        }
    }

    protected open fun onTextChange(s: CharSequence, start: Int,
                                    before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        if (isEditing) return

        isEditing = true
        try {
            afterTextChange(s)
        } finally {
            isEditing = false
        }
    }

    protected open fun afterTextChange(s: Editable) {}
}