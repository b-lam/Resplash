package com.b_lam.resplash.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

const val RESPLASH_DIRECTORY = "Resplash"

const val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"

val RELATIVE_PATH = "${Environment.DIRECTORY_PICTURES}${File.separator}$RESPLASH_DIRECTORY"

val LEGACY_PATH = "${Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_PICTURES)}${File.separator}$RESPLASH_DIRECTORY"

fun Context.fileExists(fileName: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} like ? and " +
                "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("%$RELATIVE_PATH%", fileName)
        contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
            selectionArgs, null)?.use {
            return it.count > 0
        } ?: return false
    } else {
        return File(LEGACY_PATH, fileName).exists()
    }
}

fun Context.getUriForPhoto(fileName: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} like ? and " +
                "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("%$RELATIVE_PATH%", fileName)
        contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
            selectionArgs, null)?.use {
            return if (it.moveToFirst()) {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)))
            } else {
                null
            }
        } ?: return null
    } else {
        val file = File(LEGACY_PATH, fileName)
        return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
    }
}

fun showFileExistsDialog(context: Context, action: () -> Unit) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.file_exists_title)
        .setMessage(R.string.file_exists_message)
        .setPositiveButton(R.string.yes) { _, _ -> action.invoke() }
        .setNegativeButton(R.string.no, null)
        .show()
}