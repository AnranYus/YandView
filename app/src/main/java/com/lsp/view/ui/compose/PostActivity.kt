package com.lsp.view.ui.compose

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lsp.view.ui.compose.theme.LspViewTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lsp.view.service.DownloadService
import com.lsp.view.ui.compose.screen.DetailScreen
import com.lsp.view.ui.compose.screen.PostListScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "Compose_PostActivity"

class PostActivity : ComponentActivity() {
    lateinit var downloadBinder: DownloadService.DownloadBinder
    val viewModel: PostViewModel by viewModels<PostViewModel>()

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            downloadBinder = iBinder as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LspViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }

        viewModel.downloadAction.observe(this) {
            CoroutineScope(Dispatchers.IO).launch {
                val result =
                    downloadBinder.downloadImage(viewModel.uiState.value.selectPost?.fileUrl ?: "")
                        .await()
                viewModel.setDownloadResult(result)
            }
        }

        viewModel.downloadResult.observe(this){
            val message = it.message
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }

        val serviceIntent = Intent(this, DownloadService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}

const val NAV_ROUTE_POSTLIST_SCREEN = "NAV_ROUTE_POSTLIST_SCREEN"
const val NAV_ROUTE_DETAIL_SCREEN = "NAV_ROUTE_DETAIL_SCREEN"

@Composable
fun App(viewModel: PostViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = NAV_ROUTE_POSTLIST_SCREEN) {
        composable(NAV_ROUTE_POSTLIST_SCREEN) {
            PostListScreen(onNavigateToDetail = {
            }, navController = navController, viewModel = viewModel)
        }

        composable(NAV_ROUTE_DETAIL_SCREEN) {
            DetailScreen(navController = navController, viewModel = viewModel)
        }
    }

}