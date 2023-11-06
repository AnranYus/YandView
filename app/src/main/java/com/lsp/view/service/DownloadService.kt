package com.lsp.view.service

import android.accounts.NetworkErrorException
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable

class DownloadService : Service() {
    private val mBinder = DownloadBinder(this)

    class DownloadBinder(val context: Context) : Binder() {

        fun downloadImage(fileUrl: String):Result<Serializable> {

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
                return Result.failure<Exception>(Exception("File exists"))
            }
            val fos = FileOutputStream(file)
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(fileUrl)
                    .build()
                val response = client.newCall(request).execute()
                val responseData = response.body()?.bytes()
                return if (response.code() == 200) {
                    fos.write(responseData)
                    //通知媒体更新
                    MediaScannerConnection.scanFile(
                        context, arrayOf(file.path),
                        null, null
                    )
                    Result.success("File download successfully")
                } else {
                    file.delete()
                    Result.failure<Exception>(NetworkErrorException("Connection timed out"))
                }
            } catch (e: Exception) {
                Result.failure<Exception>(e)
            } finally {
                fos.close()
            }

            return Result.failure(Exception("Unknown error"))

        }

    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

}





