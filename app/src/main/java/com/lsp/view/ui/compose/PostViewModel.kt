package com.lsp.view.ui.compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.view.bean.YandPost
import com.lsp.view.repository.exception.NetworkErrorException
import com.lsp.view.repository.network.PostRepository
import com.lsp.view.service.Result
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
    val downloadAction = MutableLiveData<Unit>()
    val downloadResult = MutableLiveData<Result>()

    init {
        initData()
    }

    private fun initData(){
        viewModelScope.launch(Dispatchers.IO) {
            fetchPost()
        }
    }

    fun actionDownload(){
        downloadAction.postValue(Unit)
    }

    suspend fun fetchPost(searchTarget: String = _uiState.value.searchTarget.value,refresh:Boolean = false){
        _uiState.value.refresh.value = true
        _uiState.value.searchTarget.value = searchTarget
        try {
            if (refresh){
                _uiState.value.page = 0
            }
            val data = repository.fetchPostData(
                searchTarget,
                _uiState.value.page
            )
            postData.value  =  if (refresh){
                data
            }else{
                _uiState.value.page ++
                postData.value + data
            }
        } catch (e: NetworkErrorException) {
            //todo network error
        }
        _uiState.value.refresh.value = false
    }

    fun setDownloadResult(result: Result){
        downloadResult.postValue(result)
    }


}