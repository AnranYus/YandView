package com.lsp.view.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.view.bean.YandPost
import com.lsp.view.repository.exception.NetworkErrorException
import com.lsp.view.repository.network.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel:ViewModel() {
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState = _uiState.asStateFlow()
    val postData:MutableStateFlow<List<YandPost>> = MutableStateFlow(arrayListOf())
    private val repository by lazy {
        PostRepository()
    }

    init {
        initData()
    }

    private fun initData(){
        fetchPost()
    }

    fun fetchPost(){
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.refresh = true
            try {
                val data = repository.fetchPostData(
                    _uiState.value.searchTarget,
                    _uiState.value.safeModel,
                    _uiState.value.page
                )
                _uiState.value.page ++
                postData.value += data
            } catch (e: NetworkErrorException) {
                //todo network error
            }
            _uiState.value.refresh = false
        }
    }


}