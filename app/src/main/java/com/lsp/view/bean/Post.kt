package com.lsp.view.bean

import android.os.Parcel
import android.os.Parcelable

open class Post(
    open val postId: String,
    open val rating: String,
    open val sampleUrl:String,
    open val fileUrl:String,
    open val sampleHeight: Int,
    open val sampleWidth: Int,
    ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(postId)
        dest.writeString(rating)
        dest.writeString(sampleUrl)
        dest.writeString(fileUrl)
        dest.writeInt(sampleHeight)
        dest.writeInt(sampleWidth)
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}