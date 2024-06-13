package com.lsp.view.ui.compose

data class PostUiState(
    var refresh:Boolean = false,
    var searchTarget: String = "",
    val source: String = "",
    val safeModel: Boolean = false, //安全模式
    var page:Int = 1,
)