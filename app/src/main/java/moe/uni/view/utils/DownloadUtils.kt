package moe.uni.view.utils

import android.media.MediaScannerConnection
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import moe.uni.view.YandViewApplication
import moe.uni.view.repository.exception.NetworkErrorException
import moe.uni.view.repository.network.retrofit.ServiceCreator
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.UnknownHostException

object DownloadUtils {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    val IMAGE_PATH =
        "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/"

    fun downloadImage(fileUrl: String): Deferred<Result> {
        return scope.async {
            val split = fileUrl.split("/")
            val fileName = split[split.size - 1]
            val file = obtainImageFile(fileName)
            FileOutputStream(file).use {
                val request = Request.Builder()
                    .url(fileUrl)
                    .build()
                try {
                    val response = ServiceCreator.CLIENT.newCall(request).execute()
                    val responseData = response.body?.bytes()
                    return@async if (response.code == 200) {
                        it.write(responseData)
                        //通知媒体更新
                        MediaScannerConnection.scanFile(
                            YandViewApplication.context, arrayOf(file.path),
                            null, null
                        )
                        Result.success("File download successfully")
                    } else {
                        throw NetworkErrorException("Download code error")
                    }
                }catch (e: Exception){
                    file.delete()
                    if (e is UnknownHostException){
                        Result.failure("Network error")
                    }else{
                        Result.failure(exception = e)

                    }
                }
            }
        }
    }

}

fun obtainImageFile(fileName: String): File {
    val dir = File(DownloadUtils.IMAGE_PATH)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return File(DownloadUtils.IMAGE_PATH + fileName)

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

