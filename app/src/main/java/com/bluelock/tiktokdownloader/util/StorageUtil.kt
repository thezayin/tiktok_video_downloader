package com.bluelock.tiktokdownloader.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.bluelock.tiktokdownloader.model.VideoModel
import java.io.File


private val rootPath =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator +
            "Tiktok Download"
val rootFile = File(rootPath)

//enum class State{
//    EXIST,
//    COMPLETE,
//    FAILED
//}
open class State {
    data class COMPLETE(val path: String = "") : State()
    object FAILED : State()
}

fun saveVideo(file: File, videoModel: VideoModel, context: Context): State {
    val currentDateTime: java.util.Date = java.util.Date()
    val current: Long = currentDateTime.time
    val saveFile = File(rootFile, videoModel.id + current.toString() + ".mp4")
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


