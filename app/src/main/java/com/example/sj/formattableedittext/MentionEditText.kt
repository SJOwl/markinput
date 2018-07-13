package com.example.sj.formattableedittext

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
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

    var notToWatchText = false

    private val inputs: MutableList<InputItem> = ArrayList()

    /**
     * called when MentionEditText queries username and id
     */
    var onQueryUser: ((query: String) -> Unit)? = null
    /**
     * called when not querying any more
     */
    var onStopQuery: (() -> Unit)? = null

    fun clear() {
        notToWatchText = true
        clearFocus()
        setText("")
        inputs.clear()
        inputs.add(InputItemText())
        notToWatchText = false
    }

    /**
     * called when user closes friends list
     */
    fun stopMentioning() {
        val edited = inputs.filter { it.type == InputTypes.TYPE_USER }.filter { (it as InputItemUser).userId.isEmpty() }
        edited.forEach { replaceWithTextItem(it as InputItemUser) }
        Log.d("vorobeisj", "close dialog clicked")
        showRich(selectionStart)
    }

    fun startMentioning() {
        // todo vorobei add user item where editing now
        val start = selectionStart
        var s = getStringFromInputs(inputs)
        val ins = if (s.length == 0) "@" else " @"
        val res = "${s.substring(0, start)}$ins${s.substring(start, s.length)}"
        inputs.filter { it.type == InputTypes.TYPE_USER }
                .filter { (it as InputItemUser).userId.isEmpty() }
                .forEach { replaceWithTextItem(it as InputItemUser) }

        editAdd(res, start, ins.length)
        printInputs()
    }

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

            var sw = ""

            override fun beforeTextChange(s: CharSequence, start: Int, count: Int, after: Int) {
                if (notToWatchText) return
                cursorStartBefore = this@MentionEditText.selectionStart
                cursorEndBefore = this@MentionEditText.selectionEnd
            }

            override fun afterTextChange(s: Editable) {
                if (notToWatchText) return
                cursorStartAfter = this@MentionEditText.selectionStart
                cursorEndAfter = this@MentionEditText.selectionEnd


                sw = getStringFromInputs(inputs)
                Log.d("vorobeisj", "beforech = \"$sw\"")
                Log.d("vorobeisj", "afterch  = \"$s\"")

                val diff = StringDifference(cursorStartBefore, cursorEndBefore, cursorStartAfter, cursorEndAfter)

//                if (diff.noDiff) return

                added = diff.added
                removed = diff.removed
                start = diff.start
                after = diff.after

                Log.d("vorobeisj", "cursorStartBefore=$cursorStartBefore,cursorEndBefore=$cursorEndBefore,cursorStartAfter=$cursorStartAfter,cursorEndAfter=$cursorEndAfter add=$added, rem=$removed, start=$start, after=$after")

                if (this@MentionEditText.text.length < start + added)
                    Log.d("vorobeisj", "trouble")

                if (added != 0) editAdd(s, start, added)
                if (removed != 0) editRemoved(s, start, removed)

                concatTextItems()

                recalcStringFromInputs(inputs)

                printInputs()

                Log.d("vorobeisj", "***************************************")
            }
        })

    }

    private fun printInputs() {
        Log.d("vorobeisj", "inputs *******************************")
        inputs.forEach {
            Log.d("vorobeisj", "${it.type} s=${it.start} e=${it.end} text=\'${it.text}\'")
        }
        Log.d("vorobeisj", "**************************************************************")
    }

    /**
     * iterate through list and concat neighbor text items
     */
    private fun concatTextItems() {
        var i = 0
        while (i < inputs.size) {
            if (i + 1 < inputs.size && inputs[i + 1].type == InputTypes.TYPE_TEXT && inputs[i].type == InputTypes.TYPE_TEXT) {
                inputs[i].append(inputs[i + 1].text)
                inputs.removeAt(i + 1)
                i--
            }
            i++
        }
    }

    fun isConcatNeeded(inputs: List<InputItem>): Boolean {
        for (i in 0 until inputs.size)
            if (i + 1 < inputs.size &&
                    inputs[i].type == InputTypes.TYPE_TEXT &&
                    inputs[i + 1].type == InputTypes.TYPE_TEXT) return true
        return false
    }

    fun editAdd(newString: CharSequence, start: Int, count: Int) {
        recalcStringFromInputs(inputs)

        val addedText = newString.substring(start, start + count)
        Log.d("vorobeisj", "add \"$addedText\" start=$start, count=$count")

        var edited = getItemToAdd(start)
                ?: throw IllegalStateException("edited item can not be null")

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
//                            concatTextItems()
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

    fun getItemToAdd(start: Int): InputItem? {
        recalcStringFromInputs(inputs)
        val edited = inputs.filter { it.isInRange(start) }
        if (edited.isNotEmpty()) return edited[0]
        else return null
    }

    fun getItemToRemove(start: Int, count: Int) {
        recalcStringFromInputs(inputs)
        val editStart = inputs.filter { it.isInRange(start) }
        val end = start + count
        val editEnd = inputs.filter { it.isInRange(end) }
    }

    fun getPrevious(start: Int): InputItem? {
        val edited = inputs.filter { it.isInRange(start) }
        if (edited.isNotEmpty()) {
            val index = inputs.indexOf(edited[0])
            return if (index > 0) inputs[index - 1] else null
        }
        return null
    }

    fun recalcStringFromInputs(inputs: List<InputItem>): String {
//        Log.w("vorobeisj", "recalc inputs called") // todo vorobei check calls are needed
        concatTextItems()
        val stringBuilder = StringBuilder()
        inputs.forEach { input ->
            input.start = stringBuilder.length
            stringBuilder.append(input.text)
            input.end = stringBuilder.length
        }
        return stringBuilder.toString()
    }

    fun editRemoved(newString: CharSequence, start: Int, count: Int) {
        Log.d("vorobeisj", "remove start=$start, count=$count")

        recalcStringFromInputs(inputs)
        val editStartList = inputs.filter { it.isInRemoveRange(start) }
        var end = start + count - 1
        end = Math.max(start, end)
        val editEndList = inputs.filter { it.isInRemoveRange(end) }

        if (editStartList.isEmpty()) throw IllegalStateException("start of removing is empty")
        if (editEndList.isEmpty()) throw IllegalStateException("end of removing is empty")
        val editStartIndex = inputs.indexOf(editStartList[0])
        val editEndIndex = inputs.indexOf(editEndList[0])

        if (editStartIndex < editEndIndex) {
            // remove all items between
            (editEndIndex - 1 downTo editStartIndex + 1)
                    .forEach { inputs.removeAt(it) }
            // edit edge items
            inputs[editEndIndex].run { removeUserItem(this) }
            recalcStringFromInputs(inputs)
            inputs[editEndIndex].run { (this as InputItemText).removeRange(this.start, start + count) }

            inputs[editStartIndex].run { removeUserItem(this) }
            recalcStringFromInputs(inputs)
            inputs[editStartIndex].run { this.removeRange(start, this.end) }
            // check remove user-text and text-user (user-user at one user) (text-text at one text)
            // check indexes
        } else {
            inputs[editStartIndex].run {
                this.removeRange(start, start + count)

                // edit user search
                val userItem = this
                if (userItem is InputItemUser) {
                    if (userItem.userId.isNotEmpty()) {
                        removeUserItem(userItem)
                        showRich(userItem.start)
                    } else {
                        userItem.userId = ""
                        onQueryUser?.invoke(userItem.displayName)
//                        showRich(start + count)
                    }
                }
            }
        }
    }

    fun removeUserItem(userItem: InputItem) {
        if (userItem !is InputItemUser) return
//        if (userItem.userId.isEmpty()) return
        replaceWithTextItem(userItem, text = "")
        showRich(userItem.start)
    }

    /**
     * @param index: index of @userItem
     */
    fun replaceWithTextItem(userItem: InputItemUser, index: Int = inputs.indexOf(userItem), text: String = userItem.text): InputItemText {
        val newText = InputItemText(text)
        inputs.removeAt(index)
        inputs.add(index, newText)
        onStopQuery?.invoke()
        return newText
    }

    fun editReplace(newString: CharSequence, start: Int, countWas: Int, countNow: Int) {
        Log.d("vorobeisj", "editReplace start=$start, countWas=$countWas, countNow=$countNow")
        editRemoved(getStringFromInputs(inputs), start, countWas)
        editAdd(newString, start, countNow)
    }

    fun showRich(cursorPos: Int) {
        notToWatchText = true
        this.setText(getSpannableFromInputs(inputs))
        this.placeCursor(cursorPos)
        notToWatchText = false
    }

    fun getSpannableFromInputs(inputs: List<InputItem>): SpannableString {
        return SpannableString(recalcStringFromInputs(inputs)).apply {
            inputs.filter { it is InputItemUser }.forEach { tag ->
                try {
                    setSpan(StyleSpan(Typeface.BOLD), tag.start, tag.end, 0)
                    setSpan(ForegroundColorSpan(Color.parseColor("#ff9900")), tag.start, tag.end, 0)
                } catch (e: IndexOutOfBoundsException) {
                    Log.d("vorobeisj", "can not set span IOOBE ")
                }
            }
        }
    }

    fun getStringFromInputs(inputs: List<InputItem>): String {
        val res = inputs.joinToString("") { it.text }
        return res
    }

    fun setUserId(user: UserItem) {
//        Log.d("vorobeisj", "setUserId==========================================")
        val openedUsers = inputs.filter { it is InputItemUser }.filter { (it as InputItemUser).userId.isEmpty() }
        if (openedUsers.size > 1) throw IllegalStateException("2 users can not be edited at the same time")
        if (openedUsers.size == 1) {
            (openedUsers[0] as InputItemUser).run {
                userId = user.id
                displayName = user.displayName
            }
            // add text item after user
            val index = inputs.indexOf(openedUsers[0])
            val newText = InputItemText(" ")
            inputs.add(index + 1, newText)

            recalcStringFromInputs(inputs)
            showRich(newText.end)
        }
        if (openedUsers.isEmpty()) throw IllegalStateException("attempt to set user id when no edited userInputs")
    }

}