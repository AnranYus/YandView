package com.lsp.view.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.lsp.view.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

data class ImageInfo(val path: String, val name: String, val uri: Uri)

@SuppressLint("QueryPermissionsNeeded")
fun share(url: String, context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val imageInfo = getImageInfo(url, context)
        context.grantUriPermission(
            context.packageName,
            imageInfo.uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageInfo.uri)
        shareIntent.putExtra(Intent.EXTRA_TITLE, imageInfo.name)
        val intent = Intent.createChooser(shareIntent, R.string.title_share.toString())
        val resInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT < 33) {
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        } else {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        }
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                imageInfo.uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        context.startActivity(intent)
    }

}

fun setWallpaper(url: String,context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val imageInfo = getImageInfo(url, context)
        val intent = Intent(Intent.ACTION_ATTACH_DATA)
        intent.setDataAndType(imageInfo.uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}

suspend fun getImageInfo(url: String, context: Context): ImageInfo {

    val imageLoader = ImageLoader.Builder(context)
        .build()

    val imageRequest = ImageRequest.Builder(context).data(url).allowHardware(true).build()
    val cacheImage = imageLoader.execute(imageRequest)
    val bitmap = cacheImage.drawable?.toBitmap()

    val path = File("${context.cacheDir}/image")
    val split = url.split("/")
    val fileName = split[split.size - 1]
    if (!path.exists())
        path.mkdirs()

    var file = File("${context.cacheDir}/image/$fileName")
    val downloadFile =
        File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/$fileName")

    if (downloadFile.exists()) {
        file = downloadFile
    } else if (!file.exists()) {
        file.outputStream().apply {
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, this)
        }
    }
    val imageUri = FileProvider.getUriForFile(
        context,
        "com.lsp.view.fileprovider",
        file
    )
    return ImageInfo(path = file.path, name = fileName, uri = imageUri)
}