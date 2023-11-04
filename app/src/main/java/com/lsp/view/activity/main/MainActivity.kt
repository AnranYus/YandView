package com.lsp.view.activity.main

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.R
import com.lsp.view.YandViewApplication
import com.lsp.view.model.MainViewModel


class MainActivity : AppCompatActivity() {
    private var shortAnnotationDuration: Int = 0
    private var username: String? = ""
    val TAG = javaClass.simpleName
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = MainViewModel.provideFactory((application as YandViewApplication).repository, this,this).create(MainViewModel::class.java)

        val adapter by lazy {
            viewModel.fetchPostByRefresh()
            PostAdapter(this)
        }

        //刷新
        val refresh =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        viewModel.uiState.value.isRefreshing.observe(this) {
            refresh.isRefreshing = it
        }
        viewModel.errorMessage.observe(this){
            Snackbar.make(refresh,it,Snackbar.LENGTH_SHORT).show()
        }
        viewModel.postList.observe(this){

            if (adapter.isAppendData){
                adapter.appendDate(it)
            }else{
                adapter.pushNewData(it)
            }
        }

        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val searchBar = findViewById<SearchBar>(R.id.search_bar)
        val searchView = findViewById<SearchView>(R.id.search_view)

        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.fetchPostBySearch(searchView.text.toString())
                searchBar.setText(searchView.text)
                searchView.hide()
            }

            return@setOnEditorActionListener false
        }

        //接收来自PicActivity的快捷搜索Tag
        val stringExtra = intent.getStringExtra("searchTag")
        if (stringExtra!=null){
            viewModel.uiState.value.nowSearchText.value = stringExtra
        }
        viewModel.uiState.value.nowSearchText.observe(this){
            searchView.setText(it)
            searchBar.setText(it)
        }


        refresh.setOnRefreshListener {
            viewModel.fetchPostByRefresh()
        }

        adapter.apply {
            setLoadMoreListener(object :PostAdapter.OnScrollToBottom{
                override fun event(position: Int) {
                    adapter.isAppendData = true//设置数据状态为追加数据
                    viewModel.fetchMore()
                }
            })
        }

        findViewById<RecyclerView>(R.id.recyclerview).apply {
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.layoutManager = layoutManager
            this.adapter = adapter
        }


        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }


        //侧边栏
        val sp = getSharedPreferences("username", 0)
        username = sp.getString("username", null)

    }

    override fun onResume() {
        super.onResume()

        //加载源改变
        val configSp = getSharedPreferences("com.lsp.view_preferences", 0)

        val nowSourceName = configSp.getString("source_name", null)

        if (nowSourceName != null && viewModel.uiState.value.nowSourceName.value != nowSourceName){
            viewModel.updateNowSource(nowSourceName)
        }

        val nowMode = configSp.getBoolean("safe_mode", true)
        if (viewModel.uiState.value.isSafe != nowMode){
            viewModel.updateSafeMode(nowMode)
            Log.e(TAG,nowMode.toString())
        }

    }

}