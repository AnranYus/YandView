package moe.uni.view.ui.compose.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import coil.compose.AsyncImage
import moe.uni.view.R
import moe.uni.view.common.setWallpaper
import moe.uni.view.common.share
import moe.uni.view.ui.compose.PostViewModel


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
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = R.string.description_back.toString())
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.actionDownload()
                        Toast.makeText(context, R.string.toast_download_start, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twotone_arrow_downward_24),
                            contentDescription = R.string.description_download.toString()
                        )
                    }
                    IconButton(onClick = {
                        uiState.selectPost?.let {
                            share(it.sampleUrl, context)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = R.string.description_share.toString(),
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        uiState.selectPost?.let {
                            setWallpaper(it.sampleUrl, context)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twotone_wallpaper_24),
                            contentDescription = R.string.description_use_to_wallpaper.toString(),
                            tint = Color.White
                        )
                    }
                },
            )
        }
    }

}