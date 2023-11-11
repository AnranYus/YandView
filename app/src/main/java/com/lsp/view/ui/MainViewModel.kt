package com.lsp.view.ui

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.view.ui.fragment.adapter.PostAdapter
import com.lsp.view.repository.network.PostRepository
import androidx.savedstate.SavedStateRegistryOwner
import com.lsp.view.YandViewApplication
import com.lsp.view.bean.YandPost
import com.lsp.view.repository.datasource.CollectDatabase
import com.lsp.view.repository.datasource.CollectRepository
import com.lsp.view.repository.datasource.CollectRepositoryImpl
import com.lsp.view.repository.datasource.model.Collect
import com.lsp.view.repository.exception.NetworkErrorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PostRepository, context: Context):ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState:StateFlow<UiState>
        get() =  _uiState.asStateFlow()
    private var fetchJob: Job? = null
    private val TAG = this::class.java.simpleName
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage
        get() = _toastMessage as LiveData<String>

    private val _postList = MutableLiveData<ArrayList<YandPost>>()
    val postList get() = _postList as LiveData<ArrayList<YandPost>>
    val adapter by lazy {
        fetchPostByRefresh()
        PostAdapter(context)
    }
    private val _collectList:MutableLiveData<List<Collect>> = MutableLiveData()
    val collectList:LiveData<List<Collect>>
        get() = _collectList

    private val collectRepository: CollectRepository by lazy {
        CollectRepositoryImpl(CollectDatabase.getDatabase(context).collectDao())
    }

    @Volatile private var isProcessing = false

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
            it.apply {
                it.nowSourceName.postValue(configSp.getString("source_name",null))
            }

            it.copy(isSafe = configSp.getBoolean("safe_mode",true))
        }

    }

    private fun fetchNewPost(){
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.isRefreshing.postValue(true)
            try {
                _postList.postValue(repository.fetchPostData(
                    _uiState.value.nowSearchText.value,
                    _uiState.value.isSafe,
                    _uiState.value.nowPage
                ))

            }catch (e:NetworkErrorException){
                postNewToast(e.message.toString())
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

    fun fetchPostBySearch(searchTarget:String){
        _uiState.update {
            it.apply {
                it.nowSearchText.postValue(searchTarget)
            }
        }

        fetchNewPost()
    }

    fun fetchMore(){
        _uiState.value.nowPage++
        fetchNewPost()
    }

    fun updateSafeMode(mode:Boolean){
        _uiState.update {
            it.copy(isSafe = mode)
        }
        fetchNewPost()
    }

    fun updateNowSource(source:String){
        _uiState.value.nowSourceName.postValue(source)
        fetchPostByRefresh()
    }

    fun getCollectList(){
        viewModelScope.launch {
            _collectList.postValue(collectRepository.getAllCollect())
        }
    }

    fun addCollect(collect: Collect){
        if (!isProcessing){
            viewModelScope.launch {
                collectRepository.addNewCollect(collect)

            }
        }
    }

    fun removeCollect(collect:Collect){

        if (!isProcessing){
            viewModelScope.launch {
                collectRepository.removeCollect(collect)

            }
        }

    }

    suspend fun getCollectByPostId(postId:String) = viewModelScope.async {
        val type = YandViewApplication.context?.getSharedPreferences("com.lsp.view_preferences",Context.MODE_PRIVATE)?.getString("source_name", "yande.re")
        collectRepository.getCollectByPostId(postId,type!!)
    }

    fun postNewToast(content:String){
        _toastMessage.postValue(content)
    }

}