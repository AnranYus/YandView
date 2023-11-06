package com.lsp.view.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.lsp.view.R
import com.lsp.view.YandViewApplication
import com.lsp.view.bean.Post


class PostAdapter(val context: Context,private val postList: ArrayList<Post> = ArrayList()):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    val TAG = this::class.java.simpleName
    private var inBottom:Boolean = false//当前列表是否在底部
    @Volatile private var isProcess:Boolean = false

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picImage: ImageView = view.findViewById(R.id.image)

    }

    fun pushNewData(list: List<Post>) {
        if (!isProcess) {
            isProcess = true
            if (inBottom) {
                //在底部，则追加数据
                val pos = postList.size
                postList.addAll(list)
                notifyItemRangeInserted(pos - 1, list.size)
            } else {
                //不在则刷新数据
                val oldSize = postList.size
                postList.clear()
                notifyItemRangeRemoved(0, oldSize)
                postList.addAll(list)
                notifyItemRangeInserted(0, list.size)
            }
        }

        isProcess = false
        inBottom = false//加入新数据后不在底部，若仍在底部说明无更多数据，也不必处理

    }


    private lateinit var mOnListItemClick: OnListItemClick
    interface OnListItemClick {
        fun setOnListItemClick(post:Post)
    }
    fun setOnListItemClick(mOnListItemClick: OnListItemClick) {
        this.mOnListItemClick = mOnListItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.img_item_layout, parent, false)
        val viewHolder = ViewHolder(view)

        viewHolder.picImage.setOnClickListener {
            val position = viewHolder.adapterPosition
            mOnListItemClick.setOnListItemClick(postList[position])
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
                    val source: String = postList[index].sampleUrl
                    val glideUrl = GlideUrl(
                        source,
                        LazyHeaders.Builder().addHeader("User-Agent", YandViewApplication.UA)
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

        val source: String = post.sampleUrl

        val height = postList[position].sampleHeight
        val width = postList[position].sampleWidth

        //图片过长则缩减高度以保证显示完整
        if (width > height){
            holder.picImage.layoutParams.height = height / 2
        }else {
            holder.picImage.layoutParams.height = height
        }


        val glideUrl = GlideUrl(
            source,
            LazyHeaders.Builder().addHeader("User-Agent", YandViewApplication.UA)
                .build()
        )
        Glide.with(context).load(glideUrl).into(holder.picImage)

        if (position == postList.size - 1 && postList.size > 6 ) {
            //到达底部
            inBottom = true
            if (this::mScrollToBottom.isInitialized) {
                mScrollToBottom.event(position)
            }
        }


    }

    override fun getItemCount(): Int {
        return postList.size
    }


}
