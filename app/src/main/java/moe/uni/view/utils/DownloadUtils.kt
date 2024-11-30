package moe.uni.view.utils

import android.media.MediaScannerConnection
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import moe.uni.view.YandViewApplication
import moe.uni.view.repository.network.retrofit.ServiceCreator
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object DownloadUtils {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    val IMAGE_PATH =
        "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/"

    fun downloadImage(fileUrl: String): Deferred<Result> {
        return scope.async {
            val split = fileUrl.split("/")
            val fileName = split[split.size - 1]
            val file = obtainImageFile(fileName)
            if (file.exists()) {
                return@async Result.failure("File exists")
            }
            FileOutputStream(file).use {
                val request = Request.Builder()
                    .url(fileUrl)
                    .build()
                val response = ServiceCreator.CLIENT.newCall(request).execute()
                val responseData = response.body?.bytes()
                return@async if (response.code == 200) {
                    try {
                        it.write(responseData)
                        //通知媒体更新
                        MediaScannerConnection.scanFile(
                            YandViewApplication.context, arrayOf(file.path),
                            null, null
                        )
                        Result.success("File download successfully")
                    } catch (e: Exception) {
                        file.delete()
                        Result.failure(exception = e)
                    }

                } else {
                    file.delete()
                    Result.failure("Connection timed out")
                }
            }
            return@async Result.failure(message = "Unknown error")
        }
    }

}

fun obtainImageFile(fileName: String): File {
    val dir = File(DownloadUtils.IMAGE_PATH)
    if (dir.exists()) {
        return File(DownloadUtils.IMAGE_PATH + fileName)
    } else {
        dir.mkdirs()
        return File(DownloadUtils.IMAGE_PATH + fileName)
    }
}

class Result(val success: Boolean, val message: String, val exception: Exception?) {
    companion object {
        fun success(message: String): Result {
            return Result(true, message, null)
        }

        fun failure(message: String = "", exception: Exception? = null): Result {
            return Result(false, message, exception)
        }
    }

}

