package moe.uni.view.ui.compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import moe.uni.view.service.Result

class DetailViewModel:ViewModel() {
    val downloadAction = MutableLiveData<String>()
    val downloadResult = MutableLiveData<Result>()

    fun startDownload(url:String){
        downloadAction.postValue(url)
    }

    fun setDownloadResult(result: Result){
        downloadResult.postValue(result)
    }
}