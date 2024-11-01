package moe.uni.view.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import moe.uni.view.ui.compose.theme.LspViewTheme
import moe.uni.view.ui.compose.screen.PostListScreen
class PostActivity : ComponentActivity() {
    private val viewModel: PostViewModel by viewModels<PostViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LspViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    PostListScreen(this, viewModel = viewModel)
                }
            }
        }
    }
}