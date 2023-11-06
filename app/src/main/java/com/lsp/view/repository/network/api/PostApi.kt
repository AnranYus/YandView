package com.lsp.view.repository.network.api

import com.lsp.view.repository.network.Load
import com.lsp.view.bean.Post
import com.lsp.view.bean.YandPost
import com.lsp.view.repository.exception.NetworkErrorException
import retrofit2.Call

interface PostApi {
    private val TAG: String
        get() = this::class.java.simpleName

    /**
     * @param service ArrayList接收边界为Post的泛型，若直接使用Post作为泛型参数，需要强制类型转换。
     */
    @Throws(NetworkErrorException::class)
    fun <T : Post> request(service: Call<ArrayList<T>>, safeMode: Boolean):ArrayList<T> {

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