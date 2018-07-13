package com.example.sj.formattableedittext

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText
import com.example.sj.formattableedittext.ext.placeCursor
import com.example.sj.formattableedittext.inputitems.InputItem
import com.example.sj.formattableedittext.inputitems.InputItemText
import com.example.sj.formattableedittext.inputitems.InputItemUser
import com.example.sj.formattableedittext.inputitems.InputTypes

/**
 * Created by vorobei on 13/07/2018
 */
class MentionEditText : EditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var onQueryUser: ((query: String) -> Unit)? = null
    var onStopQuery: (() -> Unit)? = null

    private var notToWatchText = false

    private val inputs: MutableList<InputItem> = ArrayList()

    init {
        inputs.add(InputItemText())

        this.addTextChangedListener(object : EditableTextWatcher() {
            private var added: Int = 0
            private var removed: Int = 0
            private var start: Int = 0
            private var after: Int = 0
            private var cursorStartBefore = 0
            private var cursorEndBefore = 0
            private var cursorStartAfter = 0
            private var cursorEndAfter = 0

            override fun beforeTextChange(s: CharSequence, strt: Int, count: Int, after: Int) {
                if (notToWatchText) return
                cursorStartBefore = selectionStart
                cursorEndBefore = selectionStart
            }

            override fun onTextChange(s: CharSequence, strt: Int, before: Int, count: Int) {
                if (notToWatchText) return

                cursorStartAfter = selectionStart
                cursorEndAfter = selectionEnd

                val diff = StringDifference(cursorStartBefore, cursorEndBefore, cursorStartAfter, cursorEndAfter)

                added = diff.added
                removed = diff.removed
                start = diff.start
                after = diff.after

                if (removed != 0) editRemove(start, removed)
                if (added != 0) editAdd(s, start, added)

                concatTextItems()

                updateInputs(inputs)

                printInputs()
            }
        })
    }

    fun clear() {
        notToWatchText = true
        setText("")
        inputs.clear()
        inputs.add(InputItemText())
        notToWatchText = false
    }

    /**
     * called when user closes friends list
     */
    fun stopMentioning() {
        clearUsers()
        showRich(selectionStart)
    }

    fun startMentioning() {
        val start = selectionStart
        var s = getStringFromInputs(inputs)
        val ins = if (s.length == 0) "@" else " @"
        val res = "${s.substring(0, start)}$ins${s.substring(start, s.length)}"
        clearUsers()
        editAdd(res, start, ins.length)
        printInputs()
    }

    fun setUserId(user: UserItem) {
        val openedUsers = inputs
                .filter { it is InputItemUser && it.userId.isEmpty() }
        if (openedUsers.size > 1) throw IllegalStateException("2 users can not be edited at the same time")
        if (openedUsers.isEmpty()) throw IllegalStateException("Attempt to set user id when no edited userInputs")

        (openedUsers[0] as InputItemUser).run {
            userId = user.id
            displayName = user.displayName
        }
        // add text item after user
        val index = inputs.indexOf(openedUsers[0])
        val newText = InputItemText(" ")
        inputs.add(index + 1, newText)

        updateInputs(inputs)
        showRich(newText.end)
    }

    private fun editAdd(newString: CharSequence, start: Int, count: Int) {
        updateInputs(inputs)

        val addedText = newString.substring(start, start + count)
        Log.d("vorobeisj", "add \"$addedText\" start=$start, count=$count")

        var edited = getItemToAdd(start)
                ?: throw IllegalStateException("Edited item can not be null")

        if (addedText == " @") {
            // todo vorobei what if pasted "some text @"?
            val left = InputItemText(edited.text.substring(0, start - edited.start))
            val right = InputItemText(edited.text.substring(start - edited.start, edited.end - edited.start))
            val index = inputs.indexOf(edited)
            inputs.removeAt(index)
            left.append(" ")
            inputs.add(index, left)
            val user = InputItemUser()
            inputs.add(index + 1, user)
            onQueryUser?.invoke(user.displayName)
            inputs.add(index + 2, right)
        }

        if ((addedText == "@" && start == 0) ||
                (addedText == "@" && start - edited.start - 1 > 0 &&
                        edited.text[start - edited.start - 1] == ' ')) {
            val left = InputItemText(edited.text.substring(0, start - edited.start))
            val right = InputItemText(edited.text.substring(start - edited.start, edited.end - edited.start))
            val index = inputs.indexOf(edited)
            inputs.removeAt(index)
            inputs.add(index, left)
            val user = InputItemUser()
            inputs.add(index + 1, user)
            onQueryUser?.invoke(user.displayName)
            inputs.add(index + 2, right)
        } else {
            if (edited.end == start) { // append
                when (edited) {
                    is InputItemUser -> {
                        if (edited.userId.isEmpty()) { // append to edited user
                            if (addedText.contains(Regex("\\s"))) {
                                edited = replaceWithTextItem(edited)
                                edited.append(addedText)
                            } else {
                                edited.append(addedText)
                                onQueryUser?.invoke(edited.displayName)
                            }
                        } else { // append to already mentioned user
                            val inp = InputItemText(addedText)
                            inputs.add(inputs.indexOf(edited) + 1, inp)
                        }
                    }
                    is InputItemText -> {
                        edited.append(addedText)
                    }
                }
            } else { // edit center
                if (edited is InputItemUser) edited = replaceWithTextItem(edited)
                edited.add(start - edited.start, addedText)
            }
        }
//        if (edited is InputItemUser) queryUser(edited)
        showRich(start + count)
    }

    private fun editRemove(start: Int, count: Int) {
        Log.d("vorobeisj", "remove start=$start, count=$count")

        updateInputs(inputs)
        val editStartList = inputs.filter { it.isInRemoveRange(start) }
        var end = start + count - 1
        end = Math.max(start, end)
        val editEndList = inputs.filter { it.isInRemoveRange(end) }

        if (editStartList.isEmpty()) throw IllegalStateException("Start of removing is empty")
        if (editEndList.isEmpty()) throw IllegalStateException("End of removing is empty")
        val editStartIndex = inputs.indexOf(editStartList[0])
        val editEndIndex = inputs.indexOf(editEndList[0])

        if (editStartIndex < editEndIndex) {
            // remove all items between
            (editEndIndex - 1 downTo editStartIndex + 1)
                    .forEach { inputs.removeAt(it) }
            // edit edge items
            inputs[editEndIndex].let { removeUserItem(it) }
            updateInputs(inputs)
            inputs[editEndIndex].let { (it as InputItemText).removeRange(it.start, start + count) }

            inputs[editStartIndex].let { removeUserItem(it) }
            updateInputs(inputs)
            inputs[editStartIndex].let { it.removeRange(start, it.end) }
        } else {
            inputs[editStartIndex].let {

                try {
                    it.removeRange(start, start + count)
                    // edit user search
                    if (it is InputItemUser) {
                        if (it.userId.isNotEmpty()) {
                            removeUserItem(it)
                            showRich(it.start)
                        } else {
                            it.userId = ""
                            onQueryUser?.invoke(it.displayName)
                        }
                    }
                } catch (e: Exception) {
                    if (it is InputItemUser) {
                        removeUserItem(it)
                        showRich(it.start)
                    }
                }
            }
        }
    }

    private fun clearUsers() {
        inputs.filter { it.type == InputTypes.TYPE_USER }
                .filter { (it as InputItemUser).userId.isEmpty() }
                .forEach { replaceWithTextItem(it as InputItemUser) }
    }

    private fun getItemToAdd(start: Int): InputItem? {
        updateInputs(inputs)
        val edited = inputs.filter { it.isInRange(start) }
        return if (edited.isNotEmpty()) edited[0] else null
    }

    private fun updateInputs(inputs: List<InputItem>): String {
        Log.e("vorobeisj", "recalc inputs called") // todo vorobei check calls are needed
        concatTextItems()
        val stringBuilder = StringBuilder()
        inputs.forEach { input ->
            input.start = stringBuilder.length
            stringBuilder.append(input.text)
            input.end = stringBuilder.length
        }
        return stringBuilder.toString()
    }

    private fun removeUserItem(userItem: InputItem) {
        if (userItem !is InputItemUser) return
        replaceWithTextItem(userItem, text = "")
        showRich(userItem.start)
    }

    /**
     * @param index: index of @userItem
     */
    private fun replaceWithTextItem(userItem: InputItemUser, index: Int = inputs.indexOf(userItem), text: String = userItem.text): InputItemText {
        val newText = InputItemText(text)
        inputs.removeAt(index)
        inputs.add(index, newText)
        onStopQuery?.invoke()
        return newText
    }

    private fun showRich(cursorPos: Int) {
        notToWatchText = true
        setText(getSpannableFromInputs(inputs))
        placeCursor(cursorPos)
        notToWatchText = false
    }

    private fun getSpannableFromInputs(inputs: List<InputItem>): SpannableString {
        return SpannableString(updateInputs(inputs)).apply {
            inputs.filter { it is InputItemUser }.forEach { tag ->
                setSpan(StyleSpan(Typeface.BOLD), tag.start, tag.end, 0)
                setSpan(ForegroundColorSpan(Color.parseColor("#ff9900")), tag.start, tag.end, 0)
            }
        }
    }

    private fun getStringFromInputs(inputs: List<InputItem>) = inputs.joinToString("") { it.text }

    private fun printInputs() {
        Log.d("vorobeisj", "inputs *******************************")
        inputs.forEach {
            Log.d("vorobeisj", "${it.type} s=${it.start} e=${it.end} text=\'${it.text}\'")
        }
        Log.d("vorobeisj", "**************************************")
    }

    /**
     * iterate through list and concat neighbor text items
     */
    private fun concatTextItems() {
        var i = 0
        while (i < inputs.size) {
            if (i + 1 < inputs.size &&
                    inputs[i].type == InputTypes.TYPE_TEXT &&
                    inputs[i + 1].type == InputTypes.TYPE_TEXT) {
                inputs[i].append(inputs[i + 1].text)
                inputs.removeAt(i + 1)
                i--
            }
            i++
        }
    }
}