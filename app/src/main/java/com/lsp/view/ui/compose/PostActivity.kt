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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lsp.view.bean.Post
import com.lsp.view.ui.compose.theme.LspViewTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.lsp.view.R
import com.lsp.view.service.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }

        viewModel.downloadAction.observe(this){
            CoroutineScope(Dispatchers.IO).launch {
                val result =
                    downloadBinder.downloadImage(viewModel.uiState.value.selectPost?.fileUrl ?: "")
                        .await()
                launch(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(
                            this@PostActivity,
                            "Download successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@PostActivity,
                            "Download fail",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
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
                viewModel.uiState.value.selectPost = it
                navController.navigate(NAV_ROUTE_DETAIL_SCREEN)
            }, navController = navController, viewModel = viewModel)
        }

        composable(NAV_ROUTE_DETAIL_SCREEN) {
            DetailScreen(navController = navController, viewModel = viewModel)
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostListScreen(
    navController: NavController,
    onNavigateToDetail: (Post) -> Unit,
    viewModel: PostViewModel
) {
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
            items(items = postList, key = {
                it.postId//提供key以保持列表顺序稳定
            }) {
                Row(Modifier.animateItemPlacement(tween(durationMillis = 250))) {
                    PostItem(
                        it,
                        clickable = {
                            onNavigateToDetail.invoke(it)
                        }
                    )
                }

            }
        }
    )

}

@Composable
fun PostItem(post: Post, clickable: (Post) -> Unit) {
    SubcomposeAsyncImage(
        model = post.sampleUrl,
        contentScale = ContentScale.FillWidth,
        contentDescription = null,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
        },
        success = {
            SubcomposeAsyncImageContent()
        },
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { clickable.invoke(post) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, viewModel: PostViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    val state = rememberTransformableState { zoomChange, _, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
    }
    Box(modifier = Modifier.background(Color.Black).fillMaxSize()){
        AsyncImage(
            model = uiState.selectPost?.sampleUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation,
                )
                .transformable(state = state)
                .background(Color.Black)
        )
        Column {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.actionDownload()}) {
                        Icon(painter = painterResource(id = R.drawable.ic_twotone_arrow_downward_24), contentDescription = "Download")
                    }
                },
            )
        }
    }

}