package com.example.sj.formattableedittext

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.example.sj.formattableedittext.ext.setInvisible
import com.example.sj.formattableedittext.ext.setVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val layoutId = R.layout.activity_main

    private val friendsAdapter: FriendsAdapter by lazy {
        FriendsAdapter().apply {
            onItemClickListener = { user ->
                mentionEditText.setUserId(user)
                hideFriendsList()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)

        setRecycler()

        mentionEditText.onQueryUser = { query -> queryUser(query) }

        mentionEditText.onStopQuery = { hideFriendsList() }

        sendButton.setOnClickListener {
            Log.d("vorobeisj", "markdown is \"${mentionEditText.markdownText}\"")
            mentionEditText.clear()
        }

        tagFriendButton.setOnClickListener { mentionEditText.startMentioning() }

        closeTagFriensListButton.setOnClickListener {
            tagFriendsRecycler.setInvisible()
            mentionEditText.stopMentioning()
        }

        tagFriendsRecycler.setInvisible()
    }

    fun queryUser(displayName: String) {
        suggestFriends()
        UsersPresenter.getUsersByQuery(displayName).subscribe { items ->
            friendsAdapter.items = items
        }
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

        UsersPresenter.getUsersByQuery("").subscribe { items ->
            (tagFriendsRecycler.adapter as FriendsAdapter).items = items
        }
    }
}