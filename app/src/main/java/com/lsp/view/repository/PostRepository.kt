package com.lsp.view.repository

import com.lsp.view.repository.bean.YandPost


class PostRepository {
    private val dataSource = PostDataSource()

    //获取post
    suspend fun fetchPostData(searchTarget :String?,safe:Boolean,page:Int): List<YandPost> {
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

        val load = Load.Builder(target,page,safe)
        val dataList = dataSource.fetchNewPost(load)

        return if (safe){
            dataList.filter { it.rating == "s" }
        }else{
            dataList
        }



    }
}