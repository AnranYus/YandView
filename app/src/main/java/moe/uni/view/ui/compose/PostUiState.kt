package moe.uni.view.ui.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import moe.uni.view.bean.Post

data class PostUiState(
    var refresh:MutableState<Boolean> = mutableStateOf(true),
    var searchTarget: MutableState<String> = mutableStateOf(""),
    var page:Int = 1,
    var selectPost: Post? = null
)