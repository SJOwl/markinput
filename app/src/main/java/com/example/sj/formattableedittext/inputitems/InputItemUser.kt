package com.example.sj.formattableedittext.inputitems

import android.util.Log

class InputItemUser(var displayName: String = "", var prefix: String = "@", var userId: String = "") : InputItem(InputTypes.TYPE_USER, displayName) {
    override var text: String = displayName
        get() = "$prefix$displayName"

    override fun append(toAppend: String) {
        this.displayName = "${this.displayName}$toAppend"
    }

    override fun removeRange(start: Int, end: Int) {
        try {
            val s = start - this.start - 1
            val e = end - this.start - 1
            displayName = displayName.replaceRange(s, e, "")
        } catch (e: IndexOutOfBoundsException) {
            Log.d("vorobeisj", "")
        }
    }
}