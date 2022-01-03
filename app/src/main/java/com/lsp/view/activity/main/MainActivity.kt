package com.lsp.view.activity.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsp.view.bean.Post
import com.hentai.yandeview.Retrofit.PostService
import com.hentai.yandeview.Retrofit.ServiceCreator
import com.lsp.view.R
import com.lsp.view.activity.favtag.FavTagActivity
import com.lsp.view.activity.favtag.FavTagAdapter
import com.lsp.view.bean.Tags
import com.lsp.view.activity.setting.SettingsActivity
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var searchTag:String? = null
    private lateinit var search:EditText
    private lateinit var searchBar:LinearLayout
    private var shortAnnotationDuration:Int = 0
    private var nowPage = 1
    private lateinit var adapter: PostAdapter
//    private var isLoading = false
    private var nowPosition =0
    private var username:String? = ""
    private lateinit var sourceUrl:Array<String>
    private lateinit var sourceName:Array<String>
    private lateinit var source:String
    private  var nowSourceName: String?=null
//    private var isRefresh=true
    val TAG = javaClass.simpleName
    private  lateinit var layoutManager: RecyclerView.LayoutManager
    private var safeMode = true //安全模式
    private lateinit var recyclerView:RecyclerView
    private var barShow = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        sourceName = resources.getStringArray(R.array.pic_source)
        sourceUrl = resources.getStringArray(R.array.url_source)
        val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
        if (configSp.getString("sourceName",null)==null){
            configSp.edit().putString("sourceName","yande.re").apply()
            configSp.edit().putString("type","0").apply()
        }

        //安全模式验证
        safeMode =configSp.getBoolean("safeMode",true)

        //横屏逻辑
        layoutManager = GridLayoutManager(this, 2)
        recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerview)
        if (getResources().getConfiguration().orientation!=Configuration.ORIENTATION_PORTRAIT){
            layoutManager = LinearLayoutManager(this)
            (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
        }

        recyclerView.layoutManager = layoutManager


            //收藏Tag








        val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fbtn
        )
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefreshLayout
        )
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)


        search = findViewById<EditText>(R.id.search)
        //快捷搜索tag 来自PicActivity
        searchTag = intent.getStringExtra("searchTag")
        if (searchTag!=null){
            //tag按钮搜索
            this.search.setText(searchTag)
            searchAction(searchTag)
        }else{
            //初次启动
            swipeRefreshLayout.isRefreshing = true
            loadPost(this, null,nowPage.toString(),null,true,true)
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
        }

        search.setOnEditorActionListener { v, actionId, event ->
            if (actionId==EditorInfo.IME_ACTION_SEARCH){
                searchAction(search.text.toString())
                searchTag = search.text.toString()
                hiddenSearchBar()
            }
            return@setOnEditorActionListener false
        }

        fbtn.setOnClickListener {

            if (barShow){
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
            loadPost(this, searchTag,nowPage.toString(),null,false,true)
        }

        //侧边栏
        val sp = getSharedPreferences("username",0)
        username = sp.getString("username",null)

        val nav = findViewById<NavigationView>(R.id.nav)

        //加载导航栏列表
        nav.setCheckedItem(R.id.photo)
        //设置侧边栏点击逻辑
        nav.setNavigationItemSelectedListener {
            when(it.itemId){
                //收藏夹
                R.id.fav -> {


                    swipeRefreshLayout.isRefreshing=true
                    Log.w(TAG,username.toString())
                    if (username == null) {
                        alterEditDialog()
                        loadPost(this,"vote:3:$username order:vote","1",null,true,true)
                        drawerLayout.closeDrawers()
                    }else{
                        loadPost(this,"vote:3:$username order:vote","1",null,true,true)
                        drawerLayout.closeDrawers()
                    }

                    true
                }
                //画廊
                R.id.photo ->{
                    swipeRefreshLayout.isRefreshing = true
                    loadPost(this,null,"1",null,false,true)
                    drawerLayout.closeDrawers()
                    true
                }
                //设置
                R.id.setting -> {
                    val intent = Intent(this,SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false

                }
                R.id.taglist -> {
                    val intent = Intent(this,FavTagActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false
                }
                else -> false
            }
        }


    }



    //弹出键入用户名对话框
    private fun alterEditDialog(){
        val et = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("请输入您的用户名")
            .setView(et)
            .setPositiveButton("确定") { _, _ ->
                Log.w(TAG,et.text.toString())
                val sharedPreferences = getSharedPreferences("username", 0).edit()
                sharedPreferences.putString("username",et.text.toString()).apply()
            }.create().show()
    }
    //隐藏搜索栏
    private fun hiddenSearchBar(){
        searchBar.animate()
            .alpha(0f)
            .setDuration(shortAnnotationDuration.toLong())
            .setListener(object :AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    searchBar.visibility = View.GONE
                }
            })
        barShow = false

    }
    //现实搜索栏
    private fun showSearchBar(){

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
        when(item.itemId){
            android.R.id.home ->  drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }


    private fun hideIm( v: View){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (imm.isActive) {
            imm.hideSoftInputFromWindow(v.applicationWindowToken,0)
        }
    }
    //执行搜索
    private fun searchAction(tags: String?) {
        if (safeMode){
            if (tags.equals("&safeMode=false")){
                val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
                configSp.edit().putBoolean("safeMode",false).apply()
                safeMode = false
                Toast.makeText(this,"打开新世界的大门",Toast.LENGTH_SHORT).show()
                return
            }

        }

        val swipeRefreshLayout =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                R.id.swipeRefreshLayout
            )
        swipeRefreshLayout.isRefreshing = true
        loadPost(this, tags, "1", null,true,true)



    }

    override fun onResume() {
        super.onResume()
        val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
        val saveName =  configSp.getString("sourceName",null)
        if (saveName!=nowSourceName) {

            val swipeRefreshLayout =
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                    R.id.swipeRefreshLayout
                )
            swipeRefreshLayout.isRefreshing = true
            loadPost(this, searchTag, nowPage.toString(),null,false,true)
        }
    }



    /**
     * 加载画廊
     * @param source 加载源
     * @param tags 标签
     * @param page 页数
     */
    private fun loadPost(context: Context, tags: String?,page:String,position:Int?,initAdapter:Boolean,isRefresh:Boolean){
        var postList:ArrayList<Post> = ArrayList()
        val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
        for ((index,name) in sourceName.withIndex() ){
            if (name == configSp.getString("sourceName",null)){
                nowSourceName = configSp.getString("sourceName",null)
                source = sourceUrl[index]
            }
        }


        val postService: PostService = if(source!=null){
            ServiceCreator.create<PostService>(source)
        }else {
            ServiceCreator.create<PostService>("https://yande.re/")
        }
        var service:Call<ArrayList<Post>>
//        if (tyep.equals("0")){
        service   =  postService.getPostData("100", tags,page)
//        }else{
//            service =  postService.getPostData_php("dapi","post","index","100",tags,"1",nowPage.toString())
//        }

        service.enqueue(object : Callback<ArrayList<Post>> {
            override fun onFailure(call: Call<ArrayList<Post>>, t: Throwable) {
                val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                    R.id.fbtn
                )
                Snackbar.make(fbtn,"请检查网络连接",Snackbar.LENGTH_LONG).show()
                val handler = Handler()
                handler.postDelayed({ loadPost(context, tags, page,null,initAdapter, isRefresh) },3000)
            }

            override fun onResponse(call: Call<ArrayList<Post>>, response: Response<ArrayList<Post>>) {
                val swipeRefreshLayout =
                    findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                        R.id.swipeRefreshLayout
                    )
                val list = response.body()

                if (list != null&&list.size>1) {
                    if (safeMode){
                        for (post in list){
                            if (post.rating!="e"){
                                postList.add(post)
                            }
                        }
                    }else{
                        postList = list
                    }
                } else {
                    swipeRefreshLayout.isRefreshing = false
                    Snackbar.make(swipeRefreshLayout,"只有这么多了哦",Snackbar.LENGTH_SHORT).show()
                    Log.w(TAG, "post is null")
                    return
                }

                if (initAdapter){
                    adapter = PostAdapter(context, postList)

                }

                recyclerView.adapter = SlideInBottomAnimationAdapter(adapter)

                if (!initAdapter){
                    adapter.notifyData(postList,isRefresh)
                    try{
                        if (position!=null) {
                            if (position > 4) {
                                recyclerView.scrollToPosition(position - 3)
                            }
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }



                }
                adapter.setLoadMoreListener(object : PostAdapter.OnLoadMoreListener{
                    override fun loadMore(position: Int) {
                        Log.w("nowP",position.toString())
                        nowPage++
                        swipeRefreshLayout.isRefreshing = true
                        loadPost(this@MainActivity,tags,nowPage.toString(),position,false, false)
                    }

                })

                swipeRefreshLayout.isRefreshing = false

            }
        })



    }

}