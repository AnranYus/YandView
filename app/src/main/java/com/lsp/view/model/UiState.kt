package com.lsp.view.model

import androidx.lifecycle.MutableLiveData

data class UiState(
    var showSearchBar:Boolean = false,
    val isRefreshing:MutableLiveData<Boolean> = MutableLiveData(),
    var nowSearchText:MutableLiveData<String> = MutableLiveData(""),
    val nowSourceName: MutableLiveData<String> = MutableLiveData(),
    val isSafe: Boolean = true, //安全模式
    val nowPage:Int = 1,
    val isAppend:Boolean = false
){
    fun switchShowSearchBar(){
        showSearchBar = !showSearchBar
    }
}
