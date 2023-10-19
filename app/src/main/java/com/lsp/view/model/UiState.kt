package com.lsp.view.model

import androidx.lifecycle.MutableLiveData

data class UiState(
    var showSearchBar:Boolean = false,
    val isRefreshing:MutableLiveData<Boolean> = MutableLiveData(),
    val nowSearchText:String = "",
    val nowSourceName: String? = null,
    val isSafe: Boolean = true, //安全模式
    val nowPage:Int = 1,
    val isAppend:Boolean = false
){
    fun switchShowSearchBar(){
        showSearchBar = !showSearchBar
    }
}
