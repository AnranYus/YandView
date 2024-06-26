package moe.uni.view.repository.datasource.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import moe.uni.view.YandViewApplication
import moe.uni.view.bean.Post

@Entity(tableName = "collect")
data class Collect(
    @PrimaryKey(autoGenerate = true) var id:Int = 0,
    @ColumnInfo(name = "post_id")override val postId: String,
    @ColumnInfo(name = "sample_url")override val sampleUrl:String,
    @ColumnInfo(name = "rating")override val rating:String,
    @ColumnInfo(name = "file_url")override val fileUrl:String,
    @ColumnInfo(name = "sampleHeight") override val sampleHeight:Int,
    @ColumnInfo(name = "sampleWeight") override val sampleWidth:Int,
    @ColumnInfo(name = "type")val type:String,
    @ColumnInfo(name = "tags") override val tags:String?
):Post(postId,rating,sampleUrl,fileUrl,sampleHeight,sampleWidth,tags){
    constructor(
        postId: String,
        sampleUrl:String,
        rating:String,
        fileUrl:String,
        sampleHeight:Int,
        sampleWeight:Int,
        tags:String?
    ): this(0, postId, sampleUrl, rating, fileUrl, sampleHeight, sampleWeight,
        YandViewApplication.context?.getSharedPreferences("moe.uni.view_preferences",Context.MODE_PRIVATE)?.getString("source_name", "yande.re")!!,tags)
    companion object{
        fun Post.toCollect():Collect = Collect(this.postId,this.sampleUrl,this.rating,this.fileUrl,this.sampleHeight,this.sampleWidth ,this.tags)

    }
}

