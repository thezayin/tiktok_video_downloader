package com.bluelock.tiktokdownloader.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.bluelock.tiktokdownloader.di.model.VideoData
import java.io.File


private val rootPath =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator +
            "Tiktok"
val rootFile = File(rootPath)


fun saveVideo(file: File, videoData: VideoData, context: Context): State {
    val saveFile = File(rootFile, videoData.id + System.currentTimeMillis()+ ".mp4")
    if (!rootFile.exists()) rootFile.mkdir()


    val iStream = file.inputStream()
    val outputStream = saveFile.outputStream()
    val byteArray = iStream.readBytes()
    outputStream.write(byteArray)
    outputStream.close()
    outputStream.flush()
    iStream.close()

    MediaScannerConnection.scanFile(
        context, arrayOf(saveFile.parent), arrayOf("*/*")
    ) { _, _ ->

    }


    return if (saveFile.exists()) State.COMPLETE(saveFile.path) else State.FAILED
}


