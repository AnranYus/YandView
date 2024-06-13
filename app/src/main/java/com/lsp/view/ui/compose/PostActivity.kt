package com.lsp.view.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.lsp.view.bean.Post
import com.lsp.view.ui.compose.theme.LspViewTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val TAG = "Compose_PostActivity"
class PostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LspViewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PostList()
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostList(viewModel: PostViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val postList by viewModel.postData.collectAsState()
    val listState = rememberLazyStaggeredGridState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                layoutInfo.visibleItemsInfo.lastOrNull()?.index == postList.lastIndex
            }
            .distinctUntilChanged()
            .collect { reachedEnd ->
                //避免初次加载数据时，list未绘制导致被判断为到达底部
                if (reachedEnd && listState.firstVisibleItemIndex != 0) {
                    viewModel.fetchPost()
                }
            }
    }

    LazyVerticalStaggeredGrid(
        state = listState,
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(items=postList, key = {
                it.postId//提供key以保持列表顺序稳定
            }) {
                Row(Modifier.animateItemPlacement(tween(durationMillis = 250))) {
                    PostItem(it)
                }

            }
        }
    )

}

@Composable
fun PostItem(post:Post){
    SubcomposeAsyncImage(
        model = post.sampleUrl,
        contentScale = ContentScale.Crop,
        contentDescription = null,
        loading  ={
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center))
            }
        },
        success = {
            SubcomposeAsyncImageContent()
        },
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .fillMaxWidth()
            .wrapContentHeight()
    )
}