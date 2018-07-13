package com.example.sj.formattableedittext

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    private val layoutId = R.layout.activity_main

    private val friendsAdapter: FriendsAdapter by lazy {
        FriendsAdapter().apply {
            onItemClickListener = { user -> setUserId(user) }
        }
    }

    @Volatile
    var notToWatchText = false

    private val inputs: MutableList<InputItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)

        setRecycler()

        sendButton.setOnClickListener {
            notToWatchText = true
            editText.clearFocus()
            editText.setText("")
            inputs.clear()
            inputs.add(InputItemText())
            notToWatchText = false
        }

        tagFriendButton.setOnClickListener {
            // todo vorobei add user item where editing now
            val start = editText.selectionStart
            var s = getStringFromInputs(inputs)
            val ins = if (s.length == 0) "@" else " @"
            val res = "${s.substring(0, start)}$ins${s.substring(start, s.length)}"
            inputs.filter { it.type == InputTypes.TYPE_USER }
                    .filter { (it as InputItemUser).userId.isEmpty() }
                    .forEach { replaceWithTextItem(it as InputItemUser) }

            editAdd(res, start, ins.length)
            printInputs()
        }

        closeTagFriensListButton.setOnClickListener {
            tagFriendsRecycler.setInvisible()

            // todo vorobei set editing user textItem
            val edited = inputs.filter { it.type == InputTypes.TYPE_USER }.filter { (it as InputItemUser).userId.isEmpty() }
            edited.forEach { replaceWithTextItem(it as InputItemUser) }
            Log.d("vorobeisj", "close dialog clicked")
            showRich(editText.selectionStart)
        }

        inputs.add(InputItemText())

        editText.addTextChangedListener(object : EditableTextWatcher() {
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
                cursorStartBefore = editText.selectionStart
                cursorEndBefore = editText.selectionEnd
            }

            override fun afterTextChange(s: Editable) {
                if (notToWatchText) return
                cursorStartAfter = editText.selectionStart
                cursorEndAfter = editText.selectionEnd


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

                if (editText.text.length < start + added)
                    Log.d("vorobeisj", "trouble")

                if (added != 0) editAdd(s, start, added)
                if (removed != 0) editRemoved(s, start, removed)

                concatTextItems()

                recalcStringFromInputs(inputs)
//                if (start + after > getStringFromInputs(inputs).length)
//                    Log.d("vorobeisj", "trouble")
//                showRich(start + after)

                printInputs()

                Log.d("vorobeisj", "***************************************")
            }
        })

        tagFriendsRecycler.setInvisible()
    }

    fun printInputs() {
        Log.d("vorobeisj", "inputs *******************************")
        inputs.forEach {
            Log.d("vorobeisj", "${it.type} s=${it.start} e=${it.end} text=\'${it.text}\'")
        }
        Log.d("vorobeisj", "**************************************************************")
    }

    /**
     * iterate through list and concat neighbor text items
     */
    fun concatTextItems() {
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
            queryUser(user)
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
            queryUser(user)
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
                                queryUser(edited)
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
                        queryUser(userItem)
//                        showRich(start + count)
                    }
                }
            }
        }
    }

    fun editRemoveUserSearch() {

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
        hideFriendsList()
        return newText
    }

    fun editReplace(newString: CharSequence, start: Int, countWas: Int, countNow: Int) {
        Log.d("vorobeisj", "editReplace start=$start, countWas=$countWas, countNow=$countNow")
        editRemoved(getStringFromInputs(inputs), start, countWas)
        editAdd(newString, start, countNow)
    }

    fun showRich(cursorPos: Int) {
        notToWatchText = true
        editText.setText(getSpannableFromInputs(inputs))
        editText.placeCursor(cursorPos)
        notToWatchText = false
    }

    fun queryUser(user: InputItemUser) {
        suggestFriends()
        getUsersByQuery(user.displayName).subscribe { items ->
            friendsAdapter.items = items
        }
    }

    /**
     * create new UserItem and add to list
     */
    fun openUserItem(start: Int) {

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

            hideFriendsList()
            recalcStringFromInputs(inputs)
            showRich(newText.end)
        }
        if (openedUsers.isEmpty()) throw IllegalStateException("attempt to set user id when no edited userInputs")
    }

    fun suggestFriends() = tagFriendsRecycler.setVisible()

    fun hideFriendsList() = tagFriendsRecycler.setInvisible()

    private fun setRecycler() {
        tagFriendsContainer
        tagFriendsTitle
        closeTagFriensListButton
        with(tagFriendsRecycler) {
            adapter = friendsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        getUsersByQuery("").subscribe { items ->
            (tagFriendsRecycler.adapter as FriendsAdapter).items = items
        }
    }

    fun getUsersByQuery(searchQuery: String, limit: Int = 50): Single<List<UserItem>> {
        val friends: MutableList<UserItem> = mutableListOf()
        with(friends) {
            add(UserItem("rkWkZdQkkM", "Galina Fedorova"))
            add(UserItem("HJg4CjkOL", "Dmitryw"))
            add(UserItem("B1l7QqAgHW", "Sergey Petrov"))
            add(UserItem("r1LMMJOxEZ", "Full DiveOne"))
            add(UserItem("SymZ-MC8", "Alexandr Telegin"))
            add(UserItem("SkeyZFMHfl", "Eddie Ow"))
            add(UserItem("B1M272J5GZ", "Alex Suvorov"))
            add(UserItem("S17hVlEsEe", "Dmitry Chernov"))
            add(UserItem("HyH7Odi6x", "Grant Hao-wei Lin"))
            add(UserItem("H1Qi6aSYMg", "Giovanno Yosen Utomo"))
            add(UserItem("HyStUWxreW", "Fausto Sihite"))
            add(UserItem("BJNnTVYCm-", "Josh Holtgrewe"))
            add(UserItem("BJ4ZEc7rLb", "Tom Griffin"))
            add(UserItem("HkvZ-WdI", "Tookey Williams"))
            add(UserItem("BJe8O3m0I", "Marina Gonokhina"))
            add(UserItem("rJ5Gu-1Le", "Brandon Torres"))
            add(UserItem("SJVoTq9w", "Павел Кравцов"))
            add(UserItem("B1gPdCN2kG", "Роман Тяглик"))
            add(UserItem("B1bkhsewIz", "Воробей"))
        }


        if (searchQuery.isEmpty()) return Single.just(friends)
        return Single.just(friends.filter { it.displayName.contains(searchQuery, true) }) // intellectual sorting algorithm
    }
}

fun EditText.placeCursorAtEnd() = this.setSelection(this.text.length)
fun EditText.placeCursor(pos: Int) {
    Log.e("vorobeisj", "text at edittext = ${editText.text}, length = ${editText.text.length}, pos = $pos")
    this.setSelection(pos)
}

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

data class UserTag(var start: Int,
                   var end: Int,
                   var userId: String = "",
                   var userName: String = "",
                   var opened: Boolean = true) {
    fun isIsInRange(pos: Int) = pos in start..end
    fun getName() = userName.replace("@", "")
}

enum class InputTypes {
    TYPE_TEXT,
    TYPE_USER
}

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

class InputItemText(text: String = "") : InputItem(InputTypes.TYPE_TEXT, text)

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

class StringDifference(cursorStartBefore: Int,
                       cursorEndBefore: Int,
                       cursorStartAfter: Int,
                       cursorEndAfter: Int
) {
    var start: Int = Math.min(cursorStartBefore, cursorStartAfter)
    var count: Int = cursorEndBefore - start
    var after: Int = cursorEndAfter - start

    var added: Int = Math.max(0, after - count)

    var removed = Math.max(0, count - after)

    var noDiff = false
        get() = count == 0 && after == 0

/*    var replaced = 0
        get() = if (count != 0 && after != 0) after else 0

    var noDiff = false
        get() = count == 0 && after == 0

    init {
        var end = 0
        var lw = sw.length - 1
        var ln = sn.length - 1
        val min = Math.min(lw, ln) + 1

        for (i in cursor..min) {
            start = i
            if (i >= min) break
            if (sw[i] != sn[i]) {
                break
            }
        }

        for (i in 0..min) {
            end = i
            if (i >= min) break
            if (sw[lw - i] != sn[ln - i]) break
        }

//        "x", "xxx"
        if (ln > lw) {
            val endFromStart = ln - end + 1
            if (endFromStart < start) {
                val s = Math.min(endFromStart, start)
                val e = Math.max(endFromStart, start)
                start = s
                end = e
            }
        }
        count = Math.max(lw - ln, 0)
        after = Math.max(ln - lw, 0)
        val lenDiff = ln - lw
    }*/
}