package moe.uni.view.repository.exception

import android.util.Log

open class LoggerException(override val message: String?):Exception() {
    init {
        logMessage()
    }

    private fun logMessage(){
        Log.e(this::class.java.simpleName,message.toString())
    }
}