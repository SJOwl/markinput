package com.example.sj.formattableedittext

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_tag_friend_item.view.*

/**
 * Created by vorobei on 03/07/2018
 */
class FriendsAdapter : RecyclerView.Adapter<FriendsAdapter.ItemViewHolder>() {
    var onItemClickListener: ((user: UserItem) -> Unit)? = null
    var onAvatarClickListener: ((user: UserItem) -> Unit)? = null

    private val diffCallback = DiffCallback()
    var items = listOf<UserItem>()
        set(value) {
            diffCallback.itemsNew = value
            diffCallback.itemsOld = field
            val diffRes = DiffUtil.calculateDiff(diffCallback)
            field = value
            diffRes.dispatchUpdatesTo(this@FriendsAdapter)
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_tag_friend_item, parent, false), viewType)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[holder.adapterPosition])
    }

    inner class ItemViewHolder(view: View, val type: Int) : RecyclerView.ViewHolder(view) {
        fun bind(item: UserItem) {
            val v = itemView
            v.userName.text = item.displayName
            v.setOnClickListener { onItemClickListener?.invoke(item) }
            v.profileAvatar.setOnClickListener { onAvatarClickListener?.invoke(item) }
        }
    }

    private class DiffCallback(var itemsOld: List<UserItem> = listOf(),
                               var itemsNew: List<UserItem> = listOf()) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return itemsOld[oldItemPosition].id == itemsNew[newItemPosition].id
        }

        override fun getOldListSize(): Int = itemsOld.size

        override fun getNewListSize(): Int = itemsNew.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return true
        }
    }
}