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
import android.util.Log
import com.lsp.view.service.DownloadService
import androidx.lifecycle.LifecycleOwner

class YandViewApplication : Application(), DefaultLifecycleObserver {
    lateinit var downloadBinder:DownloadBinder

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            downloadBinder = iBinder as DownloadBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    override fun onCreate() {
        super<Application>.onCreate()
        context = applicationContext
        val serviceIntent = Intent(this, DownloadService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unbindService(connection)
    }

    companion object {
        var context: Context? = null
            private set
        const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"

    }
}