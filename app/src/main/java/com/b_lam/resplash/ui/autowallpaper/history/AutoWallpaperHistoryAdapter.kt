package com.b_lam.resplash.ui.autowallpaper.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory
import org.ocpsoft.prettytime.PrettyTime

class AutoWallpaperHistoryAdapter(
    private val callback: ItemEventCallback,
    sharedPreferencesRepository: SharedPreferencesRepository
) : PagedListAdapter<AutoWallpaperHistory, AutoWallpaperHistoryViewHolder>(diffCallback) {

    private val prettyTime = PrettyTime(sharedPreferencesRepository.locale)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AutoWallpaperHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return AutoWallpaperHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AutoWallpaperHistoryViewHolder, position: Int) {
        holder.bind(getItem(position), prettyTime, callback)
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_auto_wallpaper_history

    interface ItemEventCallback {

        fun onPhotoClick(id: String)
        fun onUserClick(username: String)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<AutoWallpaperHistory>() {
            override fun areItemsTheSame(oldItem: AutoWallpaperHistory, newItem: AutoWallpaperHistory) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AutoWallpaperHistory, newItem: AutoWallpaperHistory) = oldItem == newItem
        }
    }
}