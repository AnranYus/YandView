package com.lsp.view.activity.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.YandViewApplication
import com.lsp.view.R
import com.lsp.view.activity.favtag.FavTagActivity
import com.lsp.view.activity.setting.SettingsActivity
import com.lsp.view.model.MainViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var search: EditText
    private var shortAnnotationDuration: Int = 0
    private var username: String? = ""
    private var nowSourceName: String? = null
    val TAG = javaClass.simpleName
    private var safeMode: Boolean = true //安全模式
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewModel = MainViewModel.provideFactory((application as YandViewApplication).repository, this,this).create(MainViewModel::class.java)
        //刷新
        val refresh =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        viewModel.uiState.value.isRefreshing.observe(this) {
            refresh.isRefreshing = it
        }
        viewModel.errorMessage.observe(this){
            Snackbar.make(refresh,it,Snackbar.LENGTH_SHORT).show()
        }

//        val appbar = findViewById<AppBarLayout>(R.id.appbar)
//        val nowHeight = appbar.layoutParams.height
//        appbar.layoutParams.height = (application as YandViewApplication).statusBarHeight()+nowHeight


        refresh.setOnRefreshListener {
            viewModel.fetchPostByRefresh()
        }

        viewModel.adapter.apply {
            setLoadMoreListener(object :PostAdapter.OnScrollToBottom{
                override fun event(position: Int) {
                    viewModel.appendPost()
                }

            })
        }

        findViewById<RecyclerView>(R.id.recyclerview).apply {
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.layoutManager = layoutManager
            this.adapter = viewModel.adapter
        }


        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }

        search = findViewById(R.id.search)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.fetchNewPostBySearch(search.text.toString())
            }
            return@setOnEditorActionListener false
        }



        //侧边栏
        val sp = getSharedPreferences("username", 0)
        username = sp.getString("username", null)

        val nav = findViewById<NavigationView>(R.id.nav)

        //加载导航栏列表
        nav.setCheckedItem(R.id.photo)
        //设置侧边栏点击逻辑
        nav.setNavigationItemSelectedListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
            when (it.itemId) {
                //画廊
                R.id.photo -> {
                    viewModel.fetchPostByRefresh()
                    drawerLayout.closeDrawers()
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
        val configSp = getSharedPreferences("com.lsp.view_preferences", 0)
        if (nowSourceName != configSp.getString("source_name",null)){
            viewModel.fetchPostByRefresh()
            nowSourceName = configSp.getString("source_name",null)
        }

        if (safeMode != configSp.getBoolean("safe_mode",true)){
            viewModel.fetchPostByRefresh()
            safeMode = configSp.getBoolean("safe_mode",true)
        }

//        search.setText("")

    }

    //隐藏搜索栏
    private fun hiddenSearchBar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        search.animate()
            .alpha(0f)
            .setDuration(shortAnnotationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    search.visibility = View.GONE
                }
            })
        hideIm()
        viewModel.uiState.value.switchShowSearchBar()
    }

    //现实搜索栏
    private fun showSearchBar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        search.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(null)
        }
        viewModel.uiState.value.switchShowSearchBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (viewModel.uiState.value.showSearchBar){
                    hiddenSearchBar()
                }else {
                    val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            R.id.search_nav -> {
                if (viewModel.uiState.value.showSearchBar) {
                    viewModel.fetchNewPostBySearch(search.text.toString())
                    hideIm()

                }

                if (search.visibility == View.GONE) {
                    showSearchBar()
                }
            }
        }
        return true
    }


    private fun hideIm() {
        WindowCompat.getInsetsController(this.window,window.decorView).hide(ime())
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }






}