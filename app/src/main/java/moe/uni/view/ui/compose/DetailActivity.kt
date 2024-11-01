package moe.uni.view.ui.compose

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.uni.view.bean.Post
import moe.uni.view.service.DownloadService
import moe.uni.view.service.Result
import moe.uni.view.ui.compose.screen.DetailScreen
import moe.uni.view.ui.compose.theme.LspViewTheme

class DetailActivity: ComponentActivity() {
    private val viewModel: DetailViewModel by viewModels<DetailViewModel>()
    private var downloadBinder: DownloadService.DownloadBinder? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            downloadBinder = iBinder as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            downloadBinder = null
        }
    }


    companion object {
        const val POST_EXTRA = "post"

        fun start(context:Context,post:Post){
            val intent = Intent(context,DetailActivity::class.java)
            intent.putExtra(POST_EXTRA,post)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(POST_EXTRA,Post::class.java)
        } else {
            intent.getSerializableExtra(POST_EXTRA) as Post?
        }
        post?.let {
            setContent {
                LspViewTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                    ) {
                        DetailScreen(post = it, viewModel = viewModel){
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
            initObserver()
            val intent = Intent(this,DownloadService::class.java)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    fun initObserver(){
        viewModel.downloadAction.observe(this) {
            CoroutineScope(Dispatchers.IO).launch {
                val result =
                    downloadBinder?.downloadImage(it)
                        ?.await()
                result?.let {
                    viewModel.setDownloadResult(it)
                }?:let {
                    viewModel.setDownloadResult(Result.failure("Download failure"))
                }
            }
        }

        viewModel.downloadResult.observe(this) {
            val message = it.message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}