package com.lsp.view.activity.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.lsp.view.R
import com.lsp.view.activity.pic.PicActivity
import com.lsp.view.repository.bean.YandPost


class PostAdapter(val context: Context):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    val TAG = this::class.java.simpleName
    private val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"

    private var postList = ArrayList<YandPost>()


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picImage: ImageView = view.findViewById(R.id.picImgae)

    }

    fun pushNewData(list: ArrayList<YandPost>) {
        val oldSize = postList.size
        postList.clear()
        notifyItemRangeRemoved(0,oldSize)
        postList.addAll(list)
        notifyItemRangeInserted(0, list.size)
    }

    fun appendDate(list: ArrayList<YandPost>){
        val pos = postList.size
        postList.addAll(list)
        notifyItemRangeInserted(pos, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.img_item_layout, parent, false)
        val viewHolder = ViewHolder(view)

        viewHolder.picImage.setOnClickListener {
            val position = viewHolder.adapterPosition
//            Log.w("position", position.toString())
//            Log.w("url", postList.value?.get(position)?.sample_url)
//            Log.w("rating", postList[position].rating)
            var file_ext = postList[position].file_ext

            if (file_ext == null){
                val strarr = postList[position].sample_url.split(".")
                file_ext = strarr[strarr.lastIndex]
            }

            PicActivity.actionStartActivity(
                context,
                postList[position].id,
                postList[position].sample_url,
                postList[position].file_url,
                postList[position].tags,
                file_ext,
                postList[position].file_size,
                postList[position].md5,
                postList[position].sample_height,
                postList[position].sample_width)
        }


        return viewHolder
    }

    private lateinit var mScrollToBottom: OnScrollToBottom

    interface OnScrollToBottom {
        fun event(position: Int)
    }

    fun setLoadMoreListener(mLoadMoreListener: OnScrollToBottom) {
        this.mScrollToBottom = mLoadMoreListener
    }

    private fun preLoad(p:Int){
        if (p%20==0){
            //执行预加载
            val last:Int = if (postList.size-p<20){
                postList.size-1
            }else{
                p+19
            }
            for(index in  p..last){
                if (p>6) {
                    val source: String = postList[index].sample_url
                    val glideUrl = GlideUrl(
                        source,
                        LazyHeaders.Builder().addHeader("User-Agent", UA)
                            .build()
                    )
                    Glide.with(context).download(glideUrl).preload()
                }
            }
        }


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        preLoad(position)

        val post = postList[position]

        val source: String = post.sample_url

        val height = postList[position].sample_height
        val width = postList[position].sample_width

        //图片过长则缩减高度以保证显示完整
        if (width > height){
            holder.picImage.layoutParams.height = height / 2
        }else {
            holder.picImage.layoutParams.height = height
        }


        val glideUrl = GlideUrl(
            source,
            LazyHeaders.Builder().addHeader("User-Agent", UA)
                .build()
        )
        Glide.with(context).load(glideUrl).into(holder.picImage)

        if (position == postList.size - 1 && postList.size > 6) {
            //到达底部
            mScrollToBottom.event(position)
        }


    }

    override fun getItemCount(): Int {
        val size = postList.size
        return size
    }


}
