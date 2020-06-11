package com.b_lam.resplash.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun File.save(context: Context, fileName: String): Uri? {
    val cr = context.contentResolver

    if (!exists()) return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.IS_PENDING, 1)
            put(MediaStore.Images.Media.SIZE, length())
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}${File.separator}Resplash")
        }
        val uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        info("saveImage (>= Q): $uri, file: $this")

        transfer(cr, this, uri)

        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        cr.update(uri, contentValues, null, null)

        return uri
    } else {
        val uri = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Resplash${File.separator}${fileName}").toUri()

        info("saveImage (< Q): $uri, file: $this")

        transfer(cr, this, uri)

        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri).also { context.sendBroadcast(it) }

        return uri
    }
}

private fun transfer(cr: ContentResolver, srcFile: File, outUri: Uri): Boolean {
    val outputFileDescriptor = cr.openFileDescriptor(
        outUri, "w")?.fileDescriptor ?: return false

    val inputFileDescriptor = cr.openFileDescriptor(
        Uri.fromFile(srcFile), "r")?.fileDescriptor ?: return false

    try {
        val fos = FileOutputStream(outputFileDescriptor)
        val fis = FileInputStream(inputFileDescriptor)

        val outputChannel = fos.channel
        val inputChannel = fis.channel

        outputChannel.use { oc ->
            inputChannel.use { ic ->
                ic.transferTo(0, ic.size(), oc)
            }
        }
    } catch (e: Exception) {
        return false
    }

    return true
}