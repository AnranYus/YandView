package moe.uni.view.repository.network.api

import moe.uni.view.repository.network.Load
import moe.uni.view.repository.network.retrofit.PostService
import moe.uni.view.repository.network.retrofit.ServiceCreator
import moe.uni.view.bean.YandPost
import retrofit2.Call
import kotlin.collections.ArrayList

class PostApiImpl :PostApi {
    private val TAG = this::class.java.simpleName
    private val defaultLimit = "100"
    override suspend fun fetchNewPost(load: Load):ArrayList<YandPost>{
        val postService: PostService = ServiceCreator.create(load.source)
        val service: Call<ArrayList<YandPost>> = postService.getPostData(defaultLimit,load.tags, load.page)

        return request(service, load.safe)
    }

}