package moe.uni.view.repository.datasource

import moe.uni.view.repository.datasource.dao.CollectDao
import moe.uni.view.repository.datasource.model.Collect

class CollectRepositoryImpl(private val collectDao:CollectDao):CollectRepository {
    override suspend fun addNewCollect(collect: Collect) {
        collectDao.insert(collect)
    }

    override suspend fun getAllCollect(): List<Collect> {
        return collectDao.getAllCollect()
    }

    override suspend fun getCollectByPostId(postId: String,type:String): Collect? {
        return collectDao.getCollectByPostId(postId,type)
    }

    override suspend fun removeCollect(collect: Collect) {
        collectDao.delete(collect.postId,collect.type)

    }
}