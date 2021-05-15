package com.b_lam.resplash.ui.topic

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.topic.model.Topic
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagedListAdapter
import com.b_lam.resplash.util.LAYOUT_DEFAULT

class TopicAdapter(
    private val callback: ItemEventCallback,
    sharedPreferencesRepository: SharedPreferencesRepository
) : BasePagedListAdapter<Topic, RecyclerView.ViewHolder>(diffCallback) {

    private val layout = sharedPreferencesRepository.layout
    private val loadQuality = sharedPreferencesRepository.loadQuality

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_topic_default -> DefaultTopicViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(getItem(position)) {
            when (getItemViewType(position)) {
                R.layout.item_topic_default ->
                    (holder as DefaultTopicViewHolder).bind(this, loadQuality, callback)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            layout == LAYOUT_DEFAULT && orientation == Configuration.ORIENTATION_PORTRAIT ->
                R.layout.item_topic_default
            else ->
                R.layout.item_topic_default
        }
    }

    interface ItemEventCallback {

        fun onTopicClick(topic: Topic)
        fun onUserClick(user: User)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Topic>() {
            override fun areItemsTheSame(oldItem: Topic, newItem: Topic) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Topic, newItem: Topic) = oldItem == newItem
        }
    }
}