package com.lsp.view.activity.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.hentai.yandeview.Retrofit.PostService
import com.hentai.yandeview.Retrofit.ServiceCreator
import com.lsp.view.R
import com.lsp.view.activity.favtag.FavTagActivity
import com.lsp.view.activity.pic.PicActivity
import com.lsp.view.activity.setting.SettingsActivity
import com.lsp.view.bean.Post
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NumberFormatException
import java.util.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lsp.view.activity.model.MainActivityModelImpl
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private var searchTag: String? = null
    private lateinit var search: EditText
    private lateinit var searchBar: LinearLayout
    private var shortAnnotationDuration: Int = 0
    private var nowPage = 1
    private lateinit var adapter: PostAdapter
    private var username: String? = ""
    private lateinit var sourceUrlArray: Array<String>
    private lateinit var sourceNameArray: Array<String>
    private lateinit var source: String
    private var nowSourceName: String? = null
    val TAG = javaClass.simpleName
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var safeMode: Boolean = true //安全模式
    private lateinit var recyclerView: RecyclerView
    private var barShow = false
    private var tags: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        adapter = PostAdapter(this, ArrayList<Post>())

        //到达底部，加载更多数据
        adapter.setLoadMoreListener(object : PostAdapter.OnLoadMoreListener {
            //重写接口
            override fun loadMore(position: Int) {
                loadData(tags,++nowPage,0)


            }

        })

        //初始化数据
        sourceNameArray = resources.getStringArray(R.array.pic_source)
        sourceUrlArray = resources.getStringArray(R.array.url_source)
        val configSp = getSharedPreferences("com.lsper.view_preferences", 0)
        if (configSp.getString("sourceName", null) == null) {
            configSp.edit().putString("sourceName", "yande.re").apply()
            configSp.edit().putString("type", "0").apply()
        }
        nowSourceName = configSp.getString("sourceName","yande.re")
        safeMode = configSp.getBoolean("safe_mode",true)



        //横屏逻辑
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerview)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter



        //收藏Tag

        val fbtn =
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                R.id.fbtn
            )
        val swipeRefreshLayout =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                R.id.swipeRefreshLayout
            )
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)


        search = findViewById<EditText>(R.id.search)
        //快捷搜索tag 来自PicActivity
        searchTag = intent.getStringExtra("searchTag")
        if (searchTag != null) {
            //tag按钮搜索
            this.search.setText(searchTag)
            searchAction(searchTag)
        } else {
            //初次启动
            loadData( tags,1,0)
        }

        val close = findViewById<View>(R.id.close)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }

        searchBar = findViewById<LinearLayout>(R.id.search_bar)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        close.setOnClickListener {
            hiddenSearchBar()
            hideIm(close)
        }

        search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAction(search.text.toString())
                searchTag = search.text.toString()
                hiddenSearchBar()
            }
            return@setOnEditorActionListener false
        }

        fbtn.setOnClickListener {

            if (barShow) {
                searchTag = search.text.toString()
                searchAction(searchTag)
                hiddenSearchBar()
                hideIm(search)

            }

            if (searchBar.visibility == View.GONE) {
                showSearchBar()
            }
        }

        //刷新
        swipeRefreshLayout.setOnRefreshListener {
            nowPage = 1
            loadData( searchTag, nowPage, 1)
        }

        //侧边栏
        val sp = getSharedPreferences("username", 0)
        username = sp.getString("username", null)

        val nav = findViewById<NavigationView>(R.id.nav)

        //加载导航栏列表
        nav.setCheckedItem(R.id.photo)
        //设置侧边栏点击逻辑
        nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                //收藏夹
                R.id.fav -> {
                    Log.w(TAG, username.toString())
                    if (username == null || username == "") {
                        alterEditDialog()
                    } else {
                        Log.e("username", username.toString())
                        loadData( "vote:3:$username order:vote", 1, 1)
                        drawerLayout.closeDrawers()
                    }

                    true
                }
                //画廊
                R.id.photo -> {
                    loadData( null, 1, 1)
                    drawerLayout.closeDrawers()
                    searchTag = ""
                    true
                }
                //设置
                R.id.setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false

                }
                R.id.taglist -> {
                    val intent = Intent(this, FavTagActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false
                }
                else -> false
            }
        }


    }

    override fun onResume() {
        super.onResume()
        //加载源改变
        val configSp = getSharedPreferences("com.lsper.view_preferences", 0)
        if (nowSourceName != configSp.getString("sourceName",null)){
            loadData(tags,1,1)
            nowSourceName = configSp.getString("sourceName",null)
        }

        if (safeMode != configSp.getBoolean("safe_mode",true)){
            loadData(tags,1,1)
            safeMode = configSp.getBoolean("safe_mode",true)
        }

    }


    //弹出键入用户名对话框
    private fun alterEditDialog() {
        val et = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("请输入您的yande.re用户名")
            .setView(et)
            .setPositiveButton("确定") { _, _ ->
                Log.w(TAG, et.text.toString())
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
                if (et.text.toString() != "") {
                    val sharedPreferences = getSharedPreferences("username", 0).edit()
                    sharedPreferences.putString("username", et.text.toString()).apply()
                    loadData( "vote:3:$username order:vote", 1, 1)
                    drawerLayout.closeDrawers()
                } else {
                    Snackbar.make(drawerLayout, "用户名不能为空！", Snackbar.LENGTH_SHORT).show()
                }
            }.create().show()

    }

    //隐藏搜索栏
    private fun hiddenSearchBar() {
        searchBar.animate()
            .alpha(0f)
            .setDuration(shortAnnotationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    searchBar.visibility = View.GONE
                }
            })
        barShow = false

    }

    //现实搜索栏
    private fun showSearchBar() {

        searchBar.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(null)
        }
        barShow = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }


    private fun hideIm(v: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (imm.isActive) {
            imm.hideSoftInputFromWindow(v.applicationWindowToken, 0)
        }
    }

    //执行搜索
    private fun searchAction(tags: String?) {
        Log.e("num",tags.toString())
        var tag = tags
        var isNum = true
        try {
            tags?.toInt()
        }catch (e:NumberFormatException){
            isNum = false
        }

        if (isNum){
            tag = "id:"+tags
        }

        loadData(tag, 1, 1)


    }


    //设置列表数据
    private fun loadData(tags: String?,page: Int,type:Int){
        //缓存搜索的tags
        this.tags = tags

        Log.e(TAG,"now page is $page")

        val swipeRefreshLayout =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                R.id.swipeRefreshLayout
            )
        swipeRefreshLayout.isRefreshing = true

        //读取配置
        val configSp = getSharedPreferences("com.lsper.view_preferences", 0)
        val nowSourceName: String? = configSp.getString("sourceName",null)
        var source = ""
        for ((index,sourceName) in sourceNameArray.withIndex()){
            if (sourceName == nowSourceName){
                source = sourceUrlArray[index]
            }
        }

        //接收异步信息
        val handler = object : Handler(Looper.myLooper()!!){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what){
                    0 -> {
                        if (type == 1 )
                        //刷新数据
                            adapter.refreshData(msg.obj as ArrayList<Post>)
                        else
                        //加载数据
                            adapter.addData(msg.obj as ArrayList<Post>)

                    }
                    1 -> {
                        Snackbar.make(swipeRefreshLayout,"网络连接失败",Snackbar.LENGTH_SHORT).show()
                    }
                }

                swipeRefreshLayout.isRefreshing = false

            }
        }

        MainActivityModelImpl().requestPostList(handler,source,tags,page,configSp.getBoolean("safe_mode",true))

    }

}