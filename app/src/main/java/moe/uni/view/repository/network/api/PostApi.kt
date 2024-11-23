package moe.uni.view.repository.network.api

import moe.uni.view.repository.network.Load
import moe.uni.view.bean.Post
import moe.uni.view.bean.YandPost
import moe.uni.view.repository.exception.NetworkErrorException
import retrofit2.Call

interface PostApi {

    @Throws(NetworkErrorException::class)
    fun <T> request(service: Call<ArrayList<T>>):ArrayList<T> {

        try {
            val execute = service.execute()
            return if (execute.isSuccessful) {
                if (execute.body() != null) {
                    execute.body()!!

                } else {
                    //结果为空，返回空集避免空指针
                    ArrayList()
                }
            } else {
                throw NetworkErrorException(execute.errorBody().toString())
            }

        }catch (e: Exception){
            throw NetworkErrorException(e.message)
        }
    }

    suspend fun fetchNewPost(load: Load):ArrayList<YandPost>
}