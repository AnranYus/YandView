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
    inline fun <reified T> create(source: String): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(source)
            .client(CLIENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(T::class.java)
    }
}