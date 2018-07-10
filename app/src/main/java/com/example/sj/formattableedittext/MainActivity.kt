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

    private val friendsAdapter: FriendsAdapter by lazy {
        FriendsAdapter().apply {
            onItemClickListener = { user -> setUserId(user) }
        }
    }

    private val inputs: List<InputItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        setRecycler()

        tagFriendButton.setOnClickListener { }

        closeTagFriensListButton.setOnClickListener {
            Log.d("vorobeisj", "close dialog clicked")
            tagFriendsRecycler.setInvisible()
        }

        editText.addTextChangedListener(object : EditableTextWatcher() {
            override fun onTextChange(s: CharSequence,
                                      wordStart: Int,
                                      oldTextLengthFromStart: Int,
                                      newTextLengthFromStart: Int) {
                // check for " @" or "^@" - create new UserItem and add to inputs

                // change text

                // request friend if string not empty

            }
        })

        tagFriendsRecycler.setInvisible()
    }

    fun tagFriend(s: CharSequence,
                  wordStart: Int,
                  oldTextLengthFromStart: Int,
                  newTextLengthFromStart: Int,
                  inputs: List<InputItem>) {
        Log.d("vorobeisj", "string  ")
        val end = wordStart + newTextLengthFromStart
        val newText = s.subSequence(wordStart, end)
        if (newText == "@") {
            if (((end > 0 && s[end - 1] == ' ') ||
                            (end < s.length - 1 && s[end + 1] == ' '))) { // "<text> @| <text>"
                // find place for new mention, split previously created text items
            } else if (end == 1) { // "@| <text>"
                // put new mention as first item
            }
        }
    }

    fun getSearchRequest(inputs: List<InputItem>): String? {
        val edited = inputs.filter { it.type == InputTypes.TYPE_USER }.filter { (it as InputItemUser).userId.isNotEmpty() }
        if (edited.size > 1) throw IllegalStateException("At one time 2 mentions are edited")
        if (edited.isNotEmpty()) return edited[0].text
        return null
    }

    fun getSpannableFromInputs(inputs: List<InputItem>): SpannableString {
        val stringBuilder = StringBuilder()
        val tags: List<Range> = mutableListOf()
        val max = inputs.size - 1
        inputs.forEachIndexed { index, inputItem ->
            val start = stringBuilder.length
            stringBuilder.append("${inputItem.text}")
            val end = stringBuilder.length
            if (index < max) stringBuilder.append(" ")
        }

        return SpannableString(stringBuilder.toString()).apply {
            tags.forEach { tag ->
                try {
                    setSpan(StyleSpan(Typeface.BOLD), tag.start, tag.end, 0)
                    setSpan(ForegroundColorSpan(Color.parseColor("#ff9900")), tag.start, tag.end, 0)
                } catch (e: IndexOutOfBoundsException) {
                    Log.d("vorobeisj", "can not set span IOOBE ")
                }
            }
        }
    }

    fun getStringFromInputs(inputs: List<InputItem>): String = inputs.joinToString(" ") { it.text }

    fun changeText(s: CharSequence,
                   wordStart: Int,
                   oldTextLengthFromStart: Int,
                   newTextLengthFromStart: Int,
                   inputs: List<InputItem>): String {
        // find replaced text at inputs
        // text replace with text
        // if user's display name changed - change it's type, put new item with text, not user
        // if user's name changed, but user has no id - return user name to request


        return ""
    }

    fun setUserId(user: UserItem) {
        // find input item with text = requestedString and empty id, set id and name
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

enum class InputTypes {
    TYPE_TEXT,
    TYPE_USER
}

open class InputItem(var type: InputTypes, text: String = "") {
    open var text: String = text
}

class InputItemText(text: String) : InputItem(InputTypes.TYPE_TEXT, text)

class InputItemUser(displayName: String, val prefix: String = "@", var userId: String = "") : InputItem(InputTypes.TYPE_USER, displayName) {
    override var text: String = displayName
        get() = "$prefix$field"
}

data class Range(var start: Int = 0, var end: Int = 0)
