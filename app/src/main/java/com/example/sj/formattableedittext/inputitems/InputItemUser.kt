package com.example.sj.formattableedittext.inputitems

class InputItemUser(var displayName: String = "", var prefix: String = "@", var userId: String = "") : InputItem(InputTypes.TYPE_USER, displayName) {
    override var text: String = displayName
        get() = "$prefix$displayName"

    // [john Doe](evry://user/pUpkENid)
    override var markdown: String = ""
        get() = "[$displayName](evry://user/${userId})"

    override fun append(toAppend: String) {
        this.displayName = "${this.displayName}$toAppend"
    }

    override fun removeRange(start: Int, end: Int) {
        val s = start - this.start - prefix.length
        val e = end - this.start - prefix.length
        displayName = displayName.replaceRange(s, e, "")
    }
}