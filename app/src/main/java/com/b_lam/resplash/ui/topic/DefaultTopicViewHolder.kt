package com.b_lam.resplash.ui.topic

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.topic.model.Topic
import com.b_lam.resplash.databinding.ItemTopicDefaultBinding
import com.b_lam.resplash.ui.widget.TopicStatusView.Companion.toTopicStatus
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import com.b_lam.resplash.util.loadProfilePicture
import com.b_lam.resplash.util.margin

class DefaultTopicViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemTopicDefaultBinding by viewBinding()

    fun bind(
        topic: Topic?,
        loadQuality: String?,
        callback: TopicAdapter.ItemEventCallback
    ) {
        with(binding) {
            topic?.let {
                itemView.margin(bottom = itemView.resources.getDimensionPixelSize(R.dimen.keyline_6))
                topic.owners?.firstOrNull()?.let { user ->
                    userContainer.isVisible = true
                    userContainer.setOnClickListener { callback.onUserClick(user) }
                    userImageView.loadProfilePicture(user)
                    userTextView.text = user.name ?: itemView.context.getString(R.string.unknown)
                }
                topic.cover_photo?.let { photo ->
                    val url = getPhotoUrl(photo, loadQuality)
                    topicImageView.minimumHeight = itemView.resources.getDimensionPixelSize(R.dimen.collection_max_height)
                    topicImageView.loadPhotoUrlWithThumbnail(url, photo.urls.thumb, photo.color, true)
                }
                topic.status?.let { status ->
                    topicStatus.setStatus(status.toTopicStatus())
                } ?: run {
                    topicStatus.isVisible = false
                }
                topicNameTextView.text = topic.title
                topicCountTextView.text = itemView.resources.getQuantityString(
                        R.plurals.photos,
                        topic.total_photos,
                        topic.total_photos
                    )
                itemView.setOnClickListener { callback.onTopicClick(topic) }
            }
        }
    }
}
