package com.lsp.view.ui.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.lsp.view.bean.Post

data class PostUiState(
    var refresh:Boolean = false,
    var searchTarget: MutableState<String> = mutableStateOf(""),
    val source: String = "",
    val safeModel: Boolean = false, //安全模式
    var page:Int = 1,
    var selectPost: Post? = null
)