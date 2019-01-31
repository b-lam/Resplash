package com.b_lam.resplash.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.db.Wallpaper
import com.b_lam.resplash.util.LocaleUtils
import com.b_lam.resplash.views.CircleImageView
import com.bumptech.glide.Glide
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class WallpaperListAdapter(wallpapers: ArrayList<Wallpaper>, listener: OnItemClickListener) : RecyclerView.Adapter<WallpaperListAdapter.RecyclerViewHolder>() {

    private var wallpaperList: List<Wallpaper> = wallpapers

    private var wallpaperListener: OnItemClickListener = listener

    interface OnItemClickListener {
        fun onItemClick(wallpaper: Wallpaper)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_wallpaper_history, parent, false))
    }

    override fun getItemCount(): Int {
        return wallpaperList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentWallpaper: Wallpaper = wallpaperList[position]

        val wallpaperThumbnail = currentWallpaper.thumbnail
        val wallpaperUserName = currentWallpaper.userName
        val wallpaperSetDate = currentWallpaper.date

        Glide.with(holder.itemView.context)
                .load(wallpaperThumbnail)
                .into(holder.mThumbnailImageView)

        holder.mUserName.text = holder.itemView.context.getString(R.string.by_author, wallpaperUserName)
        holder.mSetDate.text = PrettyTime(Date(System.currentTimeMillis()),
                LocaleUtils.getLocale(holder.itemView.context)).format(Date(wallpaperSetDate))
                .capitalize()

        holder.bind(currentWallpaper, wallpaperListener)
    }

    fun addWallpapers(wallpaperList: List<Wallpaper>) {
        this.wallpaperList = wallpaperList
        notifyDataSetChanged()
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mThumbnailImageView: CircleImageView = itemView.findViewById(R.id.wallpaper_thumbnail)
        var mUserName = itemView.findViewById<TextView>(R.id.wallpaper_user_name)!!
        var mSetDate = itemView.findViewById<TextView>(R.id.wallpaper_set_date)!!

        fun bind(wallpaper: Wallpaper, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(wallpaper) }
        }

    }
}