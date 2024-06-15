package com.lsp.view.common

class Config(val safeMode:Boolean,val source:String) {
    companion object{
        fun setConfig(config: Config){
            PreUtils.putString(PreKV.SOURCE_NAME,config.source)
            PreUtils.putBool(PreKV.SAFE_MODE,config.safeMode)
        }

        fun getConfig():Config{
            return Config(PreUtils.getBool(PreKV.SAFE_MODE,true),PreUtils.getString(PreKV.SOURCE_NAME,SOURCE.YANDE_RE.values.first()))
        }

        fun setSafeMode(value:Boolean){
            PreUtils.putBool(PreKV.SAFE_MODE,value)
        }

        fun getSafeMode():Boolean{
            return PreUtils.getBool(PreKV.SAFE_MODE,true)
        }
    }
}

object SOURCE {
    val YANDE_RE = mapOf("yande.re" to "https://yande.re/")
    val KONACHAN = mapOf("konachan" to "https://konachan.com/")
}