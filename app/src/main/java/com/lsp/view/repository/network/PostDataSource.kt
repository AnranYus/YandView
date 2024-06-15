package com.lsp.view.repository.network

import com.lsp.view.R
import com.lsp.view.YandViewApplication
import com.lsp.view.repository.exception.UnableConstructObjectException
import com.lsp.view.repository.network.api.PostApi
import com.lsp.view.repository.network.api.PostApiImpl
import com.lsp.view.bean.YandPost
import com.lsp.view.common.Config
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
    val safe:Boolean,
    val source:String
){
    companion object{
        fun builder(tags: String?, page: Int): Load {
            val config = Config.getConfig()
            return  Load(tags,page,config.safeMode,config.source)
        }
    }

}