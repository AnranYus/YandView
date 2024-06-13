package com.lsp.view.repository.network

import com.lsp.view.R
import com.lsp.view.YandViewApplication
import com.lsp.view.repository.exception.UnableConstructObjectException
import com.lsp.view.repository.network.api.PostApi
import com.lsp.view.repository.network.api.PostApiImpl
import com.lsp.view.bean.YandPost
import com.lsp.view.common.Pre
import com.lsp.view.common.PreKV

class PostDataSource {
    companion object{
        const val YANDE_RE = "yande.re"
        const val KONACHAN = "konachan"
    }
    private val api: PostApi = PostApiImpl()



    //设置列表数据
    suspend fun fetchNewPost(load: Load): ArrayList<YandPost> {
        return api.fetchNewPost(load)
    }
}
data class Load(
    val tags: String?,
    val page: Int,
    val safe:Boolean
){
    lateinit var source:String

    companion object{
        fun Builder(tags: String?, page: Int, safe:Boolean): Load {
            val sourceNameArray = YandViewApplication.context!!.resources.getStringArray(R.array.pic_source)
            val sourceUrlArray = YandViewApplication.context!!.resources.getStringArray(R.array.url_source)
            val configSp = YandViewApplication.context?.getSharedPreferences(Pre.NAME, 0)
            val nowSourceName: String = configSp?.getString(PreKV.SOURCE_NAME,null) ?: PostDataSource.YANDE_RE
            val load =  Load(tags,page,safe)

            //初始化数据
            for ((index,sourceName) in sourceNameArray.withIndex()){
                if (sourceName == nowSourceName){
                    load.source = sourceUrlArray[index]
                    return load
                }
            }

            throw UnableConstructObjectException("Unable construct load object.")
        }
    }

}