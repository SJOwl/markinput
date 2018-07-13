package com.example.sj.formattableedittext.inputitems

import android.util.Log

open class InputItem(var type: InputTypes, text: String = "") {
    open var text: String = text
    var start = 0
    var end = 0
    fun isInRange(index: Int) = index in start..end

    fun isInRemoveRange(index: Int) = index in start until end

    open fun append(toAppend: String) {
        this.text = "${this.text}$toAppend"
    }

    /**
     * pos : 0 is at begin of @text
     */
    open fun add(pos: Int, toAdd: String) {
        try {
            this.text = this.text.replaceRange(pos, pos, toAdd)
        } catch (e: IndexOutOfBoundsException) {
            Log.d("vorobeisj", "IOOBE")
        }
    }

    open fun removeRange(start: Int, end: Int) {
        text = text.replaceRange(start - this.start, end - this.start, "")
    }

    override fun toString(): String = text
}