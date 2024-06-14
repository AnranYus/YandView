package com.lsp.view

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import android.content.Context
import com.google.android.material.color.DynamicColors

class YandViewApplication : Application(), DefaultLifecycleObserver {


    override fun onCreate() {
        super<Application>.onCreate()
        context = applicationContext

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        var context: Context? = null
            private set
        const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"

    }
}