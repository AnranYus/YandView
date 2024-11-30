package moe.uni.view.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import moe.uni.view.bean.Post
import moe.uni.view.ui.compose.screen.DetailScreen
import moe.uni.view.ui.compose.theme.LspViewTheme

class DetailActivity: ComponentActivity() {
    private val viewModel: DetailViewModel by viewModels<DetailViewModel>()


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
                        DetailScreen(post = it, viewModel = viewModel, onBackAction = {onBackPressedDispatcher.onBackPressed()}, onImageDownloaded = {
                            Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        }
    }
}