package moe.uni.view.repository.network.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {

    val CLIENT = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }).build()

    /**
     * @param serviceClass:所属类的Service接口
     * @param source:加载根地址
     *
     */
    fun <T> create(serviceClass: Class<T>, source: String): T {


        val retrofit = Retrofit.Builder()
            .baseUrl(source)
            .client(CLIENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(serviceClass)
    }

    /**
     * 提供更优的函数调用方式
     */
    inline fun <reified T> create(source: String): T = create(T::class.java, source)
}