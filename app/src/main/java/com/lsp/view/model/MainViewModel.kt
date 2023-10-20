package com.lsp.view.model

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.view.activity.main.PostAdapter
import com.lsp.view.repository.PostRepository
import androidx.savedstate.SavedStateRegistryOwner
import com.lsp.view.repository.exception.NetworkErrorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PostRepository,context: Context):ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState:StateFlow<UiState> = _uiState.asStateFlow()
    private var fetchJob: Job? = null
    val adapter = PostAdapter(context)
    private val TAG = this::class.java.simpleName
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage
        get() = _errorMessage as LiveData<String>


    companion object {
        fun provideFactory(
            repository: PostRepository,
            context: Context,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,

            ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return MainViewModel(repository, context) as T
                }
            }
    }



    init {
        //初始化写入
        val configSp = context.getSharedPreferences("com.lsp.view_preferences", 0)
        if (configSp.getString("source_name", null) == null) {
            configSp.edit().putString("source_name", "yande.re").apply()
            configSp.edit().putString("type", "0").apply()
        }

        _uiState.update {
            it.copy(nowSourceName = configSp.getString("source_name",null))
        }

    }

    private fun fetchNewPost(){
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {

            _uiState.value.isRefreshing.postValue(true)

            try {
                 val postList = repository.fetchPostData(
                    _uiState.value.nowSearchText.value,
                    _uiState.value.isSafe,
                    _uiState.value.nowPage
                )

                launch(Dispatchers.Main) {
                    if (_uiState.value.isAppend){
                        adapter.appendDate(postList)
                    }else{
                        adapter.pushNewData(postList)
                    }
                }

            }catch (e:NetworkErrorException){
                _errorMessage.postValue(e.message.toString())
            }

            _uiState.value.isRefreshing.postValue(false)

        }



    }


    fun fetchPostByRefresh(){
        _uiState.update {
            it.copy(nowPage = 1)
        }

        fetchNewPost()
    }

    fun appendPost(){

        _uiState.update {
            it.copy(isAppend = true)
        }

        fetchNewPost()

        _uiState.update {
            it.copy(isAppend = false)
        }

    }

}