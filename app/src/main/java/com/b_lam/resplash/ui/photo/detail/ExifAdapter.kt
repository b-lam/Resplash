package com.b_lam.resplash.ui.photo.detail

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.italic
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import java.util.*

class ExifAdapter(
    val context: Context
) : ListAdapter<Pair<Int, SpannableStringBuilder>, ExifViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExifViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_exif, parent, false)
        return ExifViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExifViewHolder, position: Int) {
        val pair = getItem(position)
        holder.bind(pair.first, pair.second)
    }

    fun setExif(photo: Photo) {
        val pairs = mutableListOf<Pair<Int, SpannableStringBuilder>>()
        val unknown = SpannableStringBuilder(context.getString(R.string.unknown))
        photo.exif?.let {
            pairs.add(R.string.camera to if (it.model != null) SpannableStringBuilder().append(formCameraName(it.make, it.model)) else unknown)
            pairs.add(R.string.aperture to if (it.aperture != null) SpannableStringBuilder().italic { append("f") }.append("/${it.aperture}") else unknown)
            pairs.add(R.string.focal_length to if (it.focal_length != null) SpannableStringBuilder("${it.focal_length}mm") else unknown)
            pairs.add(R.string.shutter_speed to if (it.exposure_time != null) SpannableStringBuilder("${it.exposure_time}s") else unknown)
            pairs.add(R.string.iso to if (it.iso != null) SpannableStringBuilder(it.iso.toString()) else unknown)
            pairs.add(R.string.dimensions to if (photo.width != null && photo.height != null) SpannableStringBuilder("${photo.width} × ${photo.height}") else unknown)
        }
        submitList(pairs)
    }

    private fun formCameraName(make: String?, model: String): String {
        val makeList = make?.split(" ")?.map { it.trim() }
        val modelList = model.split(" ").map { it.trim() }
        return if (makeList?.map { it.toLowerCase(Locale.ROOT) }
                ?.intersect(modelList.map { it.toLowerCase(Locale.ROOT) })
                ?.isEmpty() == true) {
            "${makeList.first()} $model"
        } else {
            model
        }
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Pair<Int, SpannableStringBuilder>>() {
            override fun areItemsTheSame(oldItem: Pair<Int, SpannableStringBuilder>, newItem: Pair<Int, SpannableStringBuilder>) = oldItem.first == newItem.first
            override fun areContentsTheSame(oldItem: Pair<Int, SpannableStringBuilder>, newItem: Pair<Int, SpannableStringBuilder>) = oldItem == newItem
        }
    }
}