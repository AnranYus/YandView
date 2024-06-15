package com.lsp.view.common

import android.content.Context
import com.lsp.view.YandViewApplication

object PreUtils {
    private val PRE = YandViewApplication.context!!.getSharedPreferences(Pre.NAME,Context.MODE_PRIVATE)
    fun getString(key:String,default:String):String{
        return PRE.getString(key,default)!!
    }

    fun putString(key: String,value:String){
        PRE.edit().putString(key,value).apply()
    }

    fun getBool(key: String,default:Boolean):Boolean{
        return PRE.getBoolean(key,default)
    }

    fun putBool(key: String,value: Boolean){
        PRE.edit().putBoolean(key, value).apply()
    }
}