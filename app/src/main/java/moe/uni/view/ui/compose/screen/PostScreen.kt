package moe.uni.view.ui.compose.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import moe.uni.view.bean.Post
import moe.uni.view.common.Config
import moe.uni.view.ui.compose.PostViewModel
import moe.uni.view.ui.compose.widget.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.uni.view.ui.compose.DetailActivity
import moe.uni.view.ui.compose.widget.SettingItem
import moe.uni.view.ui.compose.widget.SettingType

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun PostListScreen(context: Context, viewModel: PostViewModel) {
    val postList by viewModel.postData.collectAsState()
    val listState = rememberLazyStaggeredGridState()
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

    var refreshing by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
        scope.launch(Dispatchers.IO) {
            viewModel.refresh()
        }
    })


    LaunchedEffect(listState) {
        val scrollThreshold = 10
        snapshotFlow { listState.layoutInfo }.map { layoutInfo ->
            val offsetDifference = listState.firstVisibleItemScrollOffset - lastScrollOffset
            val positionDifference = listState.firstVisibleItemIndex - lastScrollPosition
            if (offsetDifference > scrollThreshold || positionDifference > 0) {
                // 向上滚动
                scrollDirectionState = -1
            } else if (offsetDifference < -scrollThreshold || positionDifference < 0) {
                // 向下滚动
                scrollDirectionState = 1
            }

            lastScrollOffset = listState.firstVisibleItemScrollOffset
            lastScrollPosition = listState.firstVisibleItemIndex

            layoutInfo.visibleItemsInfo.lastOrNull()?.index == postList.lastIndex
        }.distinctUntilChanged().collect { reachedEnd ->
            //避免初次加载数据时，list未绘制导致被判断为到达底部
            if (reachedEnd && listState.firstVisibleItemIndex != 0) {
                viewModel.loadData()
            }
        }
    }

    LaunchedEffect(postList) {
        refreshing = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            state = listState,
            columns = StaggeredGridCells.Adaptive(200.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                //空置一行显示search bar
                item(key = 0, span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .height(searchBarHeightSize + searchBarPadding)
                            .fillMaxWidth()
                    )
                }

                items(items = postList.distinctBy { it.postId }, key = {
                    it.postId//提供key以保持列表顺序稳定
                }) {
                    Row(Modifier.animateItemPlacement(tween(durationMillis = 250))) {
                        PostItem(it, clickable = {
                            DetailActivity.start(context,it)
                        })
                    }

                }
            },
            modifier = Modifier
                .background(Color.Transparent)
                .pullRefresh(pullRefreshState)
                .padding(horizontal = 4.dp)

        )

        PullRefreshIndicator(
            refreshing, pullRefreshState,
            Modifier
                .align(Alignment.TopCenter)
                .padding(searchBarHeightSize + searchBarPadding)
        )

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
                searchEvent = {
                    scope.launch(Dispatchers.IO) {
                        viewModel.search(it)
                        listState.animateScrollToItem(index = 0)
                    }
                }, menuButtonAction = {
                    showBottomSheet = true
                }
            )
        }

        if (showBottomSheet) {
            Setting(onDismissRequest = {showBottomSheet = false}, onSettingChanged = {
                refreshing = true
                viewModel.refresh()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Setting(onDismissRequest:() -> Unit,onSettingChanged:() -> Unit){
    var safeModel by remember {
        mutableStateOf(Config.getSafeMode())
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.height(400.dp)
    ) {
        SettingItem(SettingType.Switch(
            "Safe mode", safeModel
        ) {
            safeModel = it
            Config.setSafeMode(it)
            onSettingChanged.invoke()
        })
        MaterialTheme.shapes.large
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