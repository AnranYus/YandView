package moe.uni.view.repository.network.retrofit

import moe.uni.view.bean.YandPost
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostService {
    @GET("post.json")
    fun getPostData(
        @Query("limit") limit: String,
        @Query("tags") tags: String?,
        @Query("page") page: Int
    ): Call<ArrayList<YandPost>>
}