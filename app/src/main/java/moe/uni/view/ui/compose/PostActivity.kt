package moe.uni.view.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import moe.uni.view.ui.compose.screen.PostListScreen
import moe.uni.view.ui.compose.theme.LspViewTheme

class PostActivity : ComponentActivity() {
    private val viewModel: PostViewModel by viewModels<PostViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LspViewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PostListScreen(
                        this,
                        viewModel = viewModel,
                        Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}