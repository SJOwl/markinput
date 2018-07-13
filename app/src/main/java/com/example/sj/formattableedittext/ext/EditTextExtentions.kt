package com.example.sj.formattableedittext.ext

import android.widget.EditText

fun EditText.placeCursorAtEnd() = this.setSelection(this.text.length)
fun EditText.placeCursor(pos: Int) = this.setSelection(pos)