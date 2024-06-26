package moe.uni.view.repository.datasource

import moe.uni.view.repository.datasource.model.Collect

interface CollectRepository {
    suspend fun addNewCollect(collect: Collect)
    suspend fun getAllCollect():List<Collect>
    suspend fun getCollectByPostId(postId:String,type:String):Collect?
    suspend fun removeCollect(collect: Collect)


}