package com.example.sj.formattableedittext

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val layoutId = R.layout.activity_main

    private val userTags: MutableList<UserTag> = mutableListOf()

    private val tagSymbol = '@'

    private var textEdit = ""

    private val friendsAdapter: FriendsAdapter by lazy {
        FriendsAdapter().apply {
            onItemClickListener = { user ->
                closeOpenedTag(user)
                setRichText()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        setRecycler()

        tagFriendButton.setOnClickListener { tagFriend() }

        closeTagFriensListButton.setOnClickListener {
            Log.d("vorobeisj", "close dialog clicked")
            tagFriendsRecycler.setInvisible()
        }

        editText.addTextChangedListener(object : EditableTextWatcher() {

            override fun beforeTextChange(s: CharSequence, start: Int, count: Int, after: Int) {
                Log.d("vorobeisj", "beforeTextChange s=$s, start=$start, count=$count, after=$after ")
                moveBorders(start, count, after)
                removeRemovedTags(s, start, count, after)
            }

            override fun onTextChange(s: CharSequence,
                                      wordStart: Int,
                                      oldTextLengthFromStart: Int,
                                      newTextLengthFromStart: Int) {
                Log.d("vorobeisj", "onTextChanged     s=\"$s\", wordStart=$wordStart, oldTextLengthFromStart=$oldTextLengthFromStart, newTextLengthFromStart=$newTextLengthFromStart") //To change body of created functions use File | Settings | File Templates.
                showOpenedTagsBefore(s)

                // todo vorobei check if one of tags editing: remove tags logic
                if (reopenTagToEdit(s, wordStart, oldTextLengthFromStart, newTextLengthFromStart)) {
                    // todo vorobei close other opened tags
                    requestTagName()
                    return
                }

                textEdit = s.toString()

                if (!isAnyTagOpened() && isTagFriendStart(s, wordStart, oldTextLengthFromStart, newTextLengthFromStart)) {
                    openNewTag(s, wordStart, oldTextLengthFromStart, newTextLengthFromStart)
                }
                if (isAnyTagOpened()) {
                    editOpenedTag(s, wordStart, oldTextLengthFromStart, newTextLengthFromStart)
                    requestTagName()
                }

                showOpenedTagsAfter(s)
            }
        })

        tagFriendsRecycler.setInvisible()
    }

    /**
     * count - before
     * after
     */
    fun moveBorders(start: Int, count: Int, after: Int) {
        if (count == 0) { // add symbols
            val toMove = userTags.filter { it.start > start }
            toMove.forEach {
                Log.d("vorobeisj", "moved r $it ")
                it.start += after
                it.end += after
            }
        } else if (after == 0) { // remove symbols
            val toMove = userTags.filter { it.start > start }
            toMove.forEach {
                Log.d("vorobeisj", "moved l $it ")
                it.start -= count
                it.end -= count
            }
        } else { // pasted instead of prev text
            val toMove = userTags.filter { it.start > start + count }
            toMove.forEach {
                Log.d("vorobeisj", "moved r $it ")
                it.start += after - count
                it.end += after - count
            }
        }
    }

    fun removeRemovedTags(s: CharSequence, start: Int, count: Int, after: Int) {
        if (count == s.length) return // add name with id
        val toRemove = userTags.filter { it.start > start && it.end < start + count }
        toRemove.forEach {
            Log.d("vorobeisj", "removed $it ")
            userTags.remove(it)
        }
    }

    fun reopenTagToEdit(s: CharSequence,
                        wordStart: Int,
                        oldTextLengthFromStart: Int,
                        newTextLengthFromStart: Int): Boolean {
        Log.d("vorobeisj", "reopenTagToEdit")
        // if have closed tags which in range of tag, open it, clear id and name
        val editedTag = userTags.filter { it.isIsInRange(wordStart) }
                .filter { !it.opened }
                .firstOrNull()
        editedTag?.run {
            val s = start
            val e = end

            Log.d("vorobeisj", "editing $this")
            val replaced = textEdit.replaceRange(start + 1, end, "")
            val removed = textEdit.length - replaced.length
            textEdit = replaced
            opened = true
            userName = ""
            userId = ""
            end = start + 1
            Log.d("vorobeisj", "after editing $this")

            moveBorders(start, removed - 1, 0)

            return true
        }
        return false
    }

    private fun showOpenedTagsBefore(s: CharSequence) {
        Log.d("vorobeisj", "*************************** before \"$s\"")
        userTags.forEach { Log.d("vorobeisj", "$it") }
        Log.d("vorobeisj", "***************************")
    }

    private fun showOpenedTagsAfter(s: CharSequence) {
        Log.d("vorobeisj", "=========================== after \"$s\"")
        userTags.forEach { Log.d("vorobeisj", "$it") }
        Log.d("vorobeisj", "===========================")
    }

    fun isTagFriendStart(s: CharSequence,
                         wordStart: Int,
                         oldTextLengthFromStart: Int,
                         newTextLengthFromStart: Int): Boolean {
        if (wordStart >= s.length) return false
        userTags.forEach { if (it.start == wordStart) return false }

        if (wordStart > 0) return s[wordStart - 1] == ' ' && s[wordStart] == '@'
        else return s[wordStart] == '@'
    }

    fun getOpenedTag(): UserTag? {
        val openedList = userTags.filter { it.opened }
        if (openedList.size > 1) throw IllegalStateException("Only one tag can be opened at a time")
        if (openedList.isEmpty()) return null
        return openedList[0]
    }

    fun closeOpenedTag(user: UserItem) {
        // todo vorobei move ranges of all tags that later than current user
        getOpenedTag()?.let { tag ->
            val t = editText.text.toString()
            textEdit = t.replaceRange(tag.start + 1, tag.end, "${user.displayName} ")

            val start = tag.end
            val count = 0
            val after = textEdit.length - t.length

            tag.end = tag.start + user.displayName.length + 1
            tag.userName = user.displayName
            tag.userId = user.id
            tag.opened = false

            moveBorders(start, count, after)
        }
        hideFriendsList()
        Log.d("vorobeisj", "tag closed $user")
    }

    fun openNewTag(s: CharSequence,
                   wordStart: Int,
                   oldTextLengthFromStart: Int,
                   newTextLengthFromStart: Int) {
        userTags.add(UserTag(wordStart, wordStart + newTextLengthFromStart)) // or +1
        Log.d("vorobeisj", "new tag opened ${getOpenedTag()}")
    }

    fun isAnyTagOpened() = userTags.filter { it.opened }.isNotEmpty()

    fun setRichText(text: String = textEdit) {

        Log.d("vorobeisj", "text = \"$text\"")
        // todo vorobei can not input space
        val spannable = SpannableString(text).apply {
            userTags.forEach { tag ->
                val start = tag.start
                val end = tag.end

                try {
                    setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
                    setSpan(ForegroundColorSpan(Color.parseColor("#ff9900")), start, end, 0)
                } catch (e: IndexOutOfBoundsException) {
                    Log.d("vorobeisj", "can not set span IOOBE ")
                }
            }
        }
        editText.setText(spannable)
        placeCursor()
    }

    fun editOpenedTag(s: CharSequence,
                      wordStart: Int,
                      oldTextLengthFromStart: Int,
                      newTextLengthFromStart: Int) {
        getOpenedTag()?.let { tag ->
            //            val delta = newTextLengthFromStart-oldTextLengthFromStart
//            userTags.filter { it.start > tag.end }.forEach { tag ->
//                tag.end += delta
//                tag.start += delta
//            }

            tag.end = wordStart + newTextLengthFromStart
            tag.userName = s.substring(tag.start, tag.end)
        }
    }

    fun requestTagName() {
        suggestFriends()
        getOpenedTag()?.let { tag ->
            getUsersByQuery(tag.getName()).subscribe { users -> friendsAdapter.items = users }
        }
        setRichText(textEdit)
    }

    fun placeCursor() {
        val tag = getOpenedTag()
        var pos = textEdit.length
        if (tag != null) pos = tag.end
        Log.d("vorobeisj", "place cursor at $pos")
        editText.placeCursor(pos)
    }

    fun tagFriend() {
        editText.requestFocus()
        val rawText = editText.text.trim()
        textEdit = if (rawText.isEmpty()) "@" else "$rawText @"
        suggestFriends()
        openNewTag(textEdit, rawText.length + 1, 0, 1)
        setRichText()
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
fun EditText.placeCursor(pos: Int) = this.setSelection(pos)

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