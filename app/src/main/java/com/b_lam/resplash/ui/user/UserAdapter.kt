package com.b_lam.resplash.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.widget.recyclerview.BasePagedListAdapter

class UserAdapter(
    private val callback: ItemEventCallback
) : BasePagedListAdapter<User, UserViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_default, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }

    override fun onViewRecycled(holder: UserViewHolder) {
        super.onViewRecycled(holder)
        val userImageView: ImageView? = holder.itemView.findViewById(R.id.user_image_view)
        userImageView?.let { GlideApp.with(it.context).clear(it) }
    }

    interface ItemEventCallback {

        fun onUserClick(user: User)
        fun onPhotoClick(photo: Photo)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
        }
    }
}
