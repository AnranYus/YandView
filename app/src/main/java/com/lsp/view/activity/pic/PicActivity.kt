package com.lsp.view.activity.pic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.*
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.flexbox.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.R
import com.lsp.view.activity.main.MainActivity
import com.lsp.view.repository.bean.ID
import com.lsp.view.repository.bean.Size
import com.lsp.view.repository.bean.Tags
import com.lsp.view.util.Util
import kotlin.properties.Delegates


class PicActivity : AppCompatActivity() {


    private val idList = ArrayList<ID>()
    private val sizeList = ArrayList<Size>()
    private lateinit var image: ImageView
    private lateinit var photoView: PhotoView
    private var shortAnnotationDuration by Delegates.notNull<Int>()
    private var md5 : String?= null


    companion object{
        fun actionStartActivity(context: Context,id:String,sample_url:String,file_url:String,tags:String,
                                file_ext:String,file_size:String,md5:String,sample_height:Int,sample_width:Int){
            val intent=Intent(context,PicActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("sample_url", sample_url)
            intent.putExtra("file_url", file_url)
            intent.putExtra("tags", tags)
            intent.putExtra("file_ext", file_ext)
            intent.putExtra("file_size", file_size)
            intent.putExtra("md5", md5)
            intent.putExtra("sample_height",sample_height)
            intent.putExtra("sample_width",sample_width)
            context.startActivity(intent)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pic)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        image = findViewById(R.id.titleImage)

        val sample_height = intent.getIntExtra("sample_height",-1)
        val sample_width = intent.getIntExtra("sample_width",-1)

        if (sample_width > sample_height){
            image.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val intent = intent
        val tags = intent.getStringExtra("tags")
        if (tags != null) {
            loadTags(tags, "tags")
        }

        val file_size = intent.getStringExtra("file_size")
        if (file_size != null) {
            loadTags(file_size, "size")
        }
        md5 = intent.getStringExtra("md5")

        val id = intent.getStringExtra("id")
        if (id != null) {
            loadTags(id, "id")
        }
        val sample_url = intent.getStringExtra("sample_url")
        sample_url?.let {
            loadPic(it)
        }

        val file_url = intent.getStringExtra("file_url")
        val file_ext = intent.getStringExtra("file_ext")
        val download =
            findViewById<FloatingActionButton>(
                R.id.download
            )

        photoView = findViewById(R.id.photoView)

        image.setOnClickListener {
            photoView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(shortAnnotationDuration.toLong())
                    .setListener(null)
            }
            Glide.with(this).load(sample_url).into(photoView)


        }
        photoView.setOnClickListener {
            photoView.animate()
                .alpha(0f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        photoView.visibility = View.GONE
                    }
                })
        }
        val share = findViewById<FloatingActionButton>(R.id.share)
        share.setOnClickListener {
            if (sample_url != null) {
                Util.share(sample_url,this)
            }

        }

        download.setOnClickListener {
            Util.download(file_url, file_ext, md5)
        }

        val f_btn = findViewById<FloatingActionButton>(R.id.float_btn)
        val ctrl = findViewById<LinearLayout>(R.id.ctrl)


        f_btn.setOnClickListener {
            if (ctrl.visibility == View.VISIBLE)
                ctrl.visibility = View.GONE
            else
                ctrl.visibility = View.VISIBLE
        }

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }

    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        onBackPressedDispatcher.addCallback{
            if (photoView.visibility != View.GONE) {
                photoView.animate()
                    .alpha(0f)
                    .setDuration(shortAnnotationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            photoView.visibility = View.GONE
                        }
                    })
            }else{
                finish()
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun loadPic(url: String) {

        val glideUrl = GlideUrl(
            url,
            LazyHeaders.Builder().addHeader(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36"
            ).build()
        )
        Glide.with(this).load(glideUrl).listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                e?.printStackTrace()
                val back = findViewById<ImageView>(R.id.back)
                Snackbar.make(back, R.string.toast_load_fail, Snackbar.LENGTH_LONG).setAction(R.string.button_check) {
                    AlertDialog.Builder(this@PicActivity).apply {
                        setTitle("Log")
                        if (e != null) {
                            setMessage(e.stackTraceToString())
                        }
                        setNegativeButton(R.string.button_ok, null)
                        create()
                        show()
                    }
                }.show()

                return false

            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false


            }

        }).into(image)

    }

    private fun layoutManager(): FlexboxLayoutManager {
        val manager = object : FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        manager.alignItems = AlignItems.CENTER
        manager.justifyContent = JustifyContent.FLEX_START

        return manager
    }

    //设置tags列表
    //这写的很烂 得改
    private fun loadTags(tags: String, type: String) {
        val tagList = ArrayList<Tags>()

        when (type) {
            "tags" -> {

                tagList.add(Tags("Tag"))
                val list = tags.split(" ")
                val tagRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tagRecyclerView)
                tagRecyclerView.layoutManager = layoutManager()
                for (tag in list) run {
                    tagList.add(Tags(tag))
                }
                val adapter = TagAdapter(tagList, this)
                adapter.setOnItemClickListener(object : TagAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val intent = Intent(this@PicActivity, MainActivity::class.java)
                        intent.putExtra("searchTag", tagList[position].tag)
                        startActivity(intent)
                    }

                })
                tagRecyclerView.adapter = adapter
            }
            "id" -> {
                idList.add(ID("ID"))
                idList.add(ID(tags))
                val idRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.idRecyclerView)
                idRecyclerView.layoutManager = layoutManager()
                val adapter = IdAdapter(idList,this)
                idRecyclerView.adapter = adapter
            }
            "size" -> {
                sizeList.add(Size("Size"))
                sizeList.add(Size(tags))
                val sizeRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.sizeRecyclerView)
                sizeRecyclerView.layoutManager = layoutManager()
                val adapter = SizeAdapter(sizeList)
                sizeRecyclerView.adapter = adapter

            }
        }
    }

}