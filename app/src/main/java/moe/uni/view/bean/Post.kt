package moe.uni.view.bean

import java.io.Serializable

open class Post(
    open val postId: String,
    open val rating: String,
    open val sampleUrl: String,
    open val fileUrl: String,
    open val sampleHeight: Int,
    open val sampleWidth: Int,
    open val tags: String?
) : Serializable