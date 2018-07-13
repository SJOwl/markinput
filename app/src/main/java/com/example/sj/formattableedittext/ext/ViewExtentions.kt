package com.example.sj.formattableedittext.ext

import android.view.View

fun View.toggleVisibility() {
    if (this.visibility == View.VISIBLE)
        this.visibility = View.INVISIBLE
    else this.visibility = View.VISIBLE
}

fun View.setVisible() {
    this.visibility = View.VISIBLE
}

fun View.setInvisible() {
    this.visibility = View.INVISIBLE
}
