package moe.uni.view.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import moe.uni.view.bean.YandPost
import moe.uni.view.repository.exception.NetworkErrorException
import moe.uni.view.repository.network.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel:ViewModel() {
    val postData:MutableStateFlow<List<YandPost>> = MutableStateFlow(arrayListOf())
    private var searchKeyword:String = ""
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val repository by lazy {
        PostRepository()
    }
    private var nowPage = 1

    init {
        initData()
    }

    private fun initData(){
        viewModelScope.launch(Dispatchers.IO) {
            fetchPost("",true)
        }
    }

    fun refresh(onRefreshComplete:()-> Unit = {}){
        viewModelScope.launch(Dispatchers.IO) {
            fetchPost(searchKeyword,refresh = true,onRefreshComplete)
        }
    }

    fun search(keyword:String){
        viewModelScope.launch(Dispatchers.IO) {
            searchKeyword = keyword
            fetchPost(searchKeyword,true)
        }
    }

    fun loadData(){
        viewModelScope.launch(Dispatchers.IO) {
            fetchPost(searchKeyword,false)
        }
    }

    private suspend fun fetchPost(keyword:String,refresh:Boolean,onFetchComplete:()-> Unit = {}){
        try {
            _loading.value = true
            if (refresh){
                nowPage = 1
            }
            val data = repository.fetchPostData(
                keyword,
                nowPage
            )
            postData.value  =  if (refresh){
                data
            }else{
                postData.value + data
            }
            nowPage ++
            _loading.value = false
            onFetchComplete.invoke()
        } catch (e: NetworkErrorException) {
            //todo network error
        }
    }

}