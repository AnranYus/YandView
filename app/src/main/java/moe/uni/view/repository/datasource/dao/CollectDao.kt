package moe.uni.view.repository.datasource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import moe.uni.view.repository.datasource.model.Collect

@Dao
interface CollectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collect:Collect)
    @Query("select * from collect")
    suspend fun getAllCollect():List<Collect>
    @Query("delete from collect where post_id = :postId and type = :type")
    suspend fun delete(postId: String,type: String)
    @Query("select * from collect where post_id = :postId and type = :type")
    suspend fun getCollectByPostId(postId:String,type:String):Collect?
}