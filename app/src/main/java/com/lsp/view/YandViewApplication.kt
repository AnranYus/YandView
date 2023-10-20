package com.lsp.view

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import android.content.ServiceConnection
import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import com.lsp.view.service.DownloadService.DownloadBinder
import com.google.android.material.color.DynamicColors
import android.content.Intent
import com.lsp.view.service.DownloadService
import androidx.lifecycle.LifecycleOwner
import com.lsp.view.repository.PostRepository

class YandViewApplication : Application(), DefaultLifecycleObserver {
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            downloadBinder = iBinder as DownloadBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }
    val repository = PostRepository()

    override fun onCreate() {
        super<Application>.onCreate()
        context = applicationContext
        DynamicColors.applyToActivitiesIfAvailable(this)
        val serviceIntent = Intent(this, DownloadService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unbindService(connection)
    }

    companion object {
        var context: Context? = null
            private set
        var downloadBinder: DownloadBinder? = null
            private set
    }
}