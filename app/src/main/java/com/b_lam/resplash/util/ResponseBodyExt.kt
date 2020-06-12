package com.b_lam.resplash.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.b_lam.resplash.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

fun ResponseBody.saveImage(
    context: Context,
    fileName: String,
    scope: CoroutineScope,
    onProgress: ((Int) -> Unit)?
): Uri? {
    val cr = context.contentResolver

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.IS_PENDING, 1)
            put(MediaStore.Images.Media.SIZE, contentLength())
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}${File.separator}Resplash")
        }
        val uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        info("saveImage (>= Q): $uri")

        val success = writeToFile(cr, uri, scope, onProgress)

        if (!success) return null

        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        cr.update(uri, contentValues, null, null)

        return uri
    } else {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Resplash${File.separator}${fileName}")

        val uri = file.toUri()

        info("saveImage (< Q): $uri")

        val success = writeToFile(cr, uri, scope, onProgress)

        if (!success) return null

        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri).also { context.sendBroadcast(it) }

        val contentUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)

        info("saveImage (< Q): contentUri - $contentUri")

        return contentUri
    }
}

private fun ResponseBody.writeToFile(
    cr: ContentResolver,
    outUri: Uri,
    scope: CoroutineScope,
    onProgress: ((Int) -> Unit)?
): Boolean {
    val body = this@writeToFile

    val inputStream = body.byteStream()

    inputStream.use { `is` ->
        val buffer = ByteArray(4096)

        val fileSize = body.contentLength()
        var fileSizeDownloaded: Long = 0

        var progressToReport = 0

        val outputFileDescriptor = cr.openFileDescriptor(
            outUri, "w")?.fileDescriptor ?: return false

        FileOutputStream(outputFileDescriptor).use { os ->
            while (true) {
                if (!scope.isActive) {
                    throw CancellationException("Cancelled inside while")
                }

                val read = `is`.read(buffer)
                if (read == -1) break

                os.write(buffer, 0, read)
                fileSizeDownloaded += read.toLong()

                val progress = (fileSizeDownloaded / fileSize.toDouble() * 100).toInt()
                if (progress - progressToReport >= 5) {
                    progressToReport = progress
                    onProgress?.invoke(progress)
                }
            }
        }
    }
    return true
}