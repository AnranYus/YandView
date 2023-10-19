package com.lsp.view.activity.model

import android.os.Handler
import android.os.Message
import com.lsp.view.bean.Post
import com.lsp.view.util.CallBackStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseModel {
    /**
     * @param service ArrayList接收边界为Post的泛型，若直接使用Post作为泛型参数，需要强制类型转换。
     */
    fun <T : Post> request(service: Call<ArrayList<T>>, safeMode: Boolean, handler: Handler){

        service.enqueue(object : Callback<ArrayList<T>> {
            override fun onResponse(call: Call<ArrayList<T>>, response: Response<ArrayList<T>>) {
                val getValue:ArrayList<T>? = response.body()

                if (getValue!=null) {
                    if (getValue.size==0){
                        callBack(handler, CallBackStatus.DATAISNULL)
                        return
                    }
                    if (safeMode) {
                        val iter = getValue.iterator()
                        while (iter.hasNext()) {
                            val post = iter.next()
                            if (post.rating != "s")
                                iter.remove()
                        }
                    }
                    callBack(handler, CallBackStatus.OK,getValue)

                }

            }

            override fun onFailure(call: Call<ArrayList<T>>, t: Throwable) {
                callBack(handler, CallBackStatus.NETWORKERROR)
            }

        })
    }

    fun <T:Post> callBack(handler: Handler,status: CallBackStatus,value:ArrayList<T>){
        val msg = Message.obtain()
        msg.what = status.ordinal
        msg.obj = value
        handler.sendMessage(msg)
    }

    fun callBack(handler: Handler,status: CallBackStatus){
        val msg = Message.obtain()
        msg.what = status.ordinal
        handler.sendMessage(msg)
    }
}