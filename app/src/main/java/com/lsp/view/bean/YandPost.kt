package com.lsp.view.bean

import com.google.gson.annotations.SerializedName

class YandPost(
    @SerializedName("file_url")  override val fileUrl: String,
    @SerializedName("sample_url") override val sampleUrl: String,
    @SerializedName("sample_height") override val sampleHeight : Int,
    @SerializedName("sample_width") override val sampleWidth: Int,
    @SerializedName("file_ext") val fileExt: String,
    @SerializedName("file_size") val fileSize: String,
    @SerializedName("id") override val postId: String,
    rating: String
):Post(postId, rating, sampleUrl,fileUrl,sampleHeight,sampleWidth)