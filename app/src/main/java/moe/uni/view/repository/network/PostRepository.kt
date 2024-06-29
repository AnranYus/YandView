package moe.uni.view.repository.network

import moe.uni.view.bean.YandPost
import moe.uni.view.common.Config


class PostRepository {
    private val dataSource = PostDataSource()

    //获取post
    suspend fun fetchPostData(searchTarget :String?,page:Int): ArrayList<YandPost> {
        var target = searchTarget
        var isNum = true

        //如果检索目标是纯数字，则按照id搜索
        try {
            target?.toLong()
        }catch (e:NumberFormatException){
            isNum = false
        }

        if (isNum){
            target = "id:$searchTarget"
        }

        val load = Load.builder(target,page)
        val dataList = dataSource.fetchNewPost(load)

        return if (Config.getSafeMode()){
            ArrayList(dataList.filter { it.rating == "s" })
        }else{
            dataList
        }



    }
}