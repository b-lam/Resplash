package com.b_lam.resplash.ui.autowallpaper.history

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory
import com.b_lam.resplash.util.loadPhotoUrl
import com.b_lam.resplash.util.loadProfilePicture
import com.b_lam.resplash.util.setAspectRatio
import kotlinx.android.synthetic.main.item_auto_wallpaper_history.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class AutoWallpaperHistoryViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    @SuppressLint("DefaultLocale")
    fun bind(
        wallpaper: AutoWallpaperHistory?,
        prettyTime: PrettyTime,
        callback: AutoWallpaperHistoryAdapter.ItemEventCallback
    ) {
        wallpaper?.let {
            with(itemView) {
                wallpaper.thumbnail_url?.let {
                    photo_image_view.loadPhotoUrl(it, colorString = wallpaper.color)
                }
                photo_image_view.setAspectRatio(wallpaper.width, wallpaper.height)
                photo_image_view.setOnClickListener {
                    wallpaper.photo_id?.let { callback.onPhotoClick(it) }
                }
                user_text_view.text = wallpaper.name
                user_image_view.loadProfilePicture(wallpaper.profile_picture)
                user_container.setOnClickListener {
                    wallpaper.username?.let { username -> callback.onUserClick(username) }
                }
                wallpaper.date?.let {
                    set_date_text_view.text = prettyTime.format(Date(it)).capitalize()
                }
            }
        }
    }
}