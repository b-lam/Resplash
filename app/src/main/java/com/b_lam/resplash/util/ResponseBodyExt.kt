package com.b_lam.resplash.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

fun ResponseBody.writeToFile(
    file: File,
    scope: CoroutineScope,
    onProgress: ((Int) -> Unit)?
): File {
    val body = this@writeToFile

    val inputStream = body.byteStream()

    inputStream.use {
        val buffer = ByteArray(4096)

        val fileSize = body.contentLength()
        var fileSizeDownloaded: Long = 0

        var progressToReport = 0

        val outputStream = FileOutputStream(file)

        while (true) {
            if (!scope.isActive) {
                throw CancellationException("Cancelled inside while")
            }

            val read = it.read(buffer)
            if (read == -1) break

            outputStream.write(buffer, 0, read)
            fileSizeDownloaded += read.toLong()

            val progress = (fileSizeDownloaded / fileSize.toDouble() * 100).toInt()
            if (progress - progressToReport >= 1) {
                progressToReport = progress
                onProgress?.invoke(progress)
            }
        }
    }
    return file
}