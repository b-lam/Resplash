package com.b_lam.resplash.ui.topic

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.topic.model.Topic
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.user.UserActivity

abstract class TopicFragment : BaseSwipeRecyclerViewFragment<Topic, RecyclerView.ViewHolder>() {

    abstract override val pagedListAdapter: TopicAdapter

    val itemEventCallback = object : TopicAdapter.ItemEventCallback {

        override fun onTopicClick(topic: Topic) {

        }

        override fun onUserClick(user: User) {
            Intent(context, UserActivity::class.java).apply {
                putExtra(UserActivity.EXTRA_USER, user)
                startActivity(this)
            }
        }
    }
    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
