package moe.uni.view.repository.network

import moe.uni.view.repository.network.api.PostApi
import moe.uni.view.repository.network.api.PostApiImpl
import moe.uni.view.bean.YandPost
import moe.uni.view.common.Config

class PostDataSource {

    private val api: PostApi = PostApiImpl()


    //设置列表数据
    suspend fun fetchNewPost(load: Load): ArrayList<YandPost> {
        return api.fetchNewPost(load)
    }
}

data class Load(
    val tags: String?,
    val page: Int,
    val source: String
) {
    companion object {
        fun builder(tags: String?, page: Int): Load {
            val config = Config.getConfig()
            return Load(tags, page, config.source)
        }
    }

}