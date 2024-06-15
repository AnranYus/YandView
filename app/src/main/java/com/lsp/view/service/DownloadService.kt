package com.lsp.view.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.*
import com.lsp.view.repository.network.retrofit.ServiceCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class Result(val success:Boolean,val message: String,val exception: Exception?){
    companion object{
        fun success(message:String):Result{
            return Result(true,message,null)
        }

        fun failure(message: String = "",exception: Exception? = null):Result{
            return Result(false,message,exception)
        }
    }

}
class DownloadService : Service() {
    private val mBinder = DownloadBinder(this)

    class DownloadBinder(val context: Context) : Binder() {

        fun downloadImage(fileUrl: String): Deferred<Result> {
            return CoroutineScope(Dispatchers.IO).async {
                val fileDir =
                    File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/")
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }
                val split = fileUrl.split("/")
                val fileName = split[split.size - 1]
                val file =
                    File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/$fileName")
                if (file.exists()) {
                    return@async Result.failure("File exists")
                }
                val fos = FileOutputStream(file)
                try {
                    val request = Request.Builder()
                        .url(fileUrl)
                        .build()
                    val response = ServiceCreator.CLIENT.newCall(request).execute()
                    val responseData = response.body?.bytes()
                    return@async if (response.code == 200) {
                        fos.write(responseData)
                        //通知媒体更新
                        MediaScannerConnection.scanFile(
                            context, arrayOf(file.path),
                            null, null
                        )
                        Result.success("File download successfully")
                    } else {
                        file.delete()
                        Result.failure("Connection timed out")
                    }
                } catch (e: Exception) {
                    Result.failure(exception = e)
                } finally {
                    fos.close()
                }

                return@async Result.failure(message = "Unknown error")
            }
        }

    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

}





