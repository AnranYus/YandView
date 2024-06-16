package com.lsp.view.ui.compose

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
import com.lsp.view.common.Config
import com.lsp.view.common.share
import com.lsp.view.service.DownloadService
import com.lsp.view.ui.compose.widget.SearchBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

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
                viewModel.uiState.value.selectPost = it
                navController.navigate(NAV_ROUTE_DETAIL_SCREEN)
            }, navController = navController, viewModel = viewModel)
        }

        composable(NAV_ROUTE_DETAIL_SCREEN) {
            DetailScreen(navController = navController, viewModel = viewModel)
        }
    }

}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun PostListScreen(
    navController: NavController, onNavigateToDetail: (Post) -> Unit, viewModel: PostViewModel
) {
    val postList by viewModel.postData.collectAsState()
    val listState = rememberLazyStaggeredGridState()
    val uiState by viewModel.uiState.collectAsState()
    var lastScrollOffset by remember {
        mutableIntStateOf(0)
    }
    var lastScrollPosition by remember {
        mutableIntStateOf(0)
    }

    /**
     * 1 scroll up
     * -1 scroll down
     * 0 default
     */
    var scrollDirectionState by remember {
        mutableIntStateOf(0)
    }

    val scope = rememberCoroutineScope()
    var searchBarHeightSize by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    val searchBarPadding by remember {
        mutableStateOf(24.dp)
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    val refreshing by remember {
        uiState.refresh
    }
    
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
        scope.launch(Dispatchers.IO) {
            viewModel.fetchPost(refresh = true)
        }
    })


    LaunchedEffect(listState) {

        snapshotFlow { listState.layoutInfo }.map { layoutInfo ->
            if (lastScrollOffset < listState.firstVisibleItemScrollOffset && lastScrollPosition < listState.firstVisibleItemIndex) {
                scrollDirectionState = -1
            }
            if (lastScrollOffset > listState.firstVisibleItemScrollOffset && lastScrollPosition > listState.firstVisibleItemIndex) {
                scrollDirectionState = 1
            }
            lastScrollOffset = listState.firstVisibleItemScrollOffset
            lastScrollPosition = listState.firstVisibleItemIndex

            layoutInfo.visibleItemsInfo.lastOrNull()?.index == postList.lastIndex
        }.distinctUntilChanged().collect { reachedEnd ->
            Log.d(TAG, reachedEnd.toString())
            //避免初次加载数据时，list未绘制导致被判断为到达底部
            if (reachedEnd && listState.firstVisibleItemIndex != 0) {
                withContext(Dispatchers.IO) {
                    viewModel.fetchPost()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            state = listState,
            columns = StaggeredGridCells.Adaptive(200.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                //放置两个item将下面的内容顶下来
                items(count = 2, key = {
                    Random.nextInt()
                }) {
                    Box(modifier = Modifier
                        .height(searchBarHeightSize + searchBarPadding)
                        .fillMaxWidth())
                }

                items(items = postList, key = {
                    it.postId//提供key以保持列表顺序稳定
                }) {
                    Row(Modifier.animateItemPlacement(tween(durationMillis = 250))) {
                        PostItem(it, clickable = {
                            onNavigateToDetail.invoke(it)
                        })
                    }

                }
            },
            modifier = Modifier
                .background(Color.Transparent)
                .pullRefresh(pullRefreshState)

        )

        PullRefreshIndicator(refreshing, pullRefreshState,
            Modifier
                .align(Alignment.TopCenter)
                .padding(searchBarHeightSize + searchBarPadding))

        AnimatedVisibility(
            visible = scrollDirectionState != -1,
            enter = slideInVertically(
                // 进入动画，从顶部滑下
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                // 退出动画，向上滑动消失
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            )
        ) {

            SearchBar(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = searchBarPadding / 2)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onGloballyPositioned {
                        searchBarHeightSize = with(density) {
                            it.size.height.toDp()
                        }
                    },
                searchTarget = uiState.searchTarget.value,
                searchEvent = {
                    scope.launch(Dispatchers.IO) {
                        viewModel.fetchPost(it, refresh = true)
                        listState.animateScrollToItem(index = 0)
                    }
                }, menuButtonAction = {
                    showBottomSheet = true
                }
            )
        }

        if (showBottomSheet){
            var safeModel by remember {
                mutableStateOf(Config.getSafeMode())
            }
            ModalBottomSheet(onDismissRequest ={showBottomSheet = false}, modifier = Modifier.height(400.dp)){
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Safe mode", modifier = Modifier.wrapContentSize(), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = safeModel, onCheckedChange = {
                        safeModel = it
                        Config.setSafeMode(it)
                    })
                }
                MaterialTheme.shapes.large
            }

        }

    }


}

@Composable
fun PostItem(post: Post, clickable: (Post) -> Unit) {
    Card {
        SubcomposeAsyncImage(model = post.sampleUrl,
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
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { clickable.invoke(post) })
    }

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
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {
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
                    IconButton(onClick = {
                        viewModel.actionDownload()
                        Toast.makeText(context,"Start download",Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twotone_arrow_downward_24),
                            contentDescription = "Download"
                        )
                    }
                    IconButton(onClick = {
                        uiState.selectPost?.let {
                            share(it.sampleUrl,context)
                        }
                    }) {
                        androidx.compose.material.Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
            )
        }
    }

}