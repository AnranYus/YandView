package com.lsp.view.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lsp.view.R
import com.lsp.view.model.MainViewModel
import com.lsp.view.repository.network.PostRepository


class MainActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName
    lateinit var viewModel: MainViewModel
    lateinit var bottomNav:BottomNavigationView
    val repository = PostRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = MainViewModel.provideFactory(repository, this,this).create(MainViewModel::class.java)

        bottomNav = findViewById(R.id.bottom_navigation)

        viewModel.toastMessage.observe(this){
            Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
        }

        //接收来自PicActivity的快捷搜索Tag
        val stringExtra = intent.getStringExtra("searchTag")
        if (stringExtra!=null){
            viewModel.uiState.value.nowSearchText.value = stringExtra
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNav.setupWithNavController(navController)

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