package com.lsp.view.activity.model

import android.os.Handler
import com.lsp.view.retrofit.PostService
import com.lsp.view.retrofit.ServiceCreator
import com.lsp.view.bean.Post_yand
import retrofit2.Call
import kotlin.collections.ArrayList

class MainActivityModelImpl :MainActivityModel, BaseModel() {
    private val TAG = this::class.java.simpleName
    override fun requestPostList(handler: Handler, source: String, tage: String?, page: Int,safeMode: Boolean){
        val postService: PostService = ServiceCreator.create(source)
        val service: Call<ArrayList<Post_yand>> = postService.getPostData("100",tage, page)
        request(service,safeMode, handler)
    }

}