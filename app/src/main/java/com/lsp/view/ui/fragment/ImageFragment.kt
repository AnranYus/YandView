package com.lsp.view.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lsp.view.R
import com.lsp.view.YandViewApplication
import com.lsp.view.ui.activity.MainActivity
import com.lsp.view.bean.Post
import com.lsp.view.repository.datasource.model.Collect
import com.lsp.view.repository.datasource.model.Collect.Companion.toCollect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ImageFragment : Fragment() {
    private val post by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("post",Post::class.java)
        }else{
            arguments?.getParcelable("post")
        }
    }
    @Volatile private var isCollect = false
    private val activityContext : MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private lateinit var collectBtn:MenuItem
    private lateinit var collect:Collect

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomAppBar = view.findViewById<BottomAppBar>(R.id.bottomAppBar)
        collectBtn = bottomAppBar.menu.findItem(R.id.collect_menu_btn)
        collectBtn.setIcon(R.drawable.ic_bashline_unfavorite_24)
        bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.share_menu_btn ->{
                    post?.sampleUrl?.let { it1 -> share(it1,activityContext) }
                    true
                }
                R.id.collect_menu_btn ->{
                    if (!isCollect){
                        collect = post!!.toCollect()
                        collectIt(collect)
                    }else{
                        unCollectIt(collect)
                    }
                    true
                }

                else -> {false}
            }
        }

        lifecycleScope.launch {
            post?.postId?.let {
                val collectPost = activityContext.viewModel.getCollectByPostId(it)
                val result = collectPost.await()
                if (result != null){
                    collect = result
                    collectBtn.setIcon(R.drawable.ic_baseline_favorite_24)
                    isCollect = true
                }
            }
        }

        val downloadBtn = view.findViewById<FloatingActionButton>(R.id.download_btn)
        downloadBtn.setOnClickListener {
            lifecycleScope.launch {
                if (post!=null){
                    download(post!!.fileUrl)
                }
            }
        }

        if (post!=null){
            val imageContent = view.findViewById<ImageView>(R.id.image_content)
            val glideUrl = GlideUrl(
                post!!.sampleUrl,
                LazyHeaders.Builder().addHeader("User-Agent", YandViewApplication.UA)
                    .build()
            )
            Glide.with(this).load(glideUrl).into(imageContent)

        }


    }


    private fun collectIt(collect: Collect){
            activityContext.viewModel.addCollect(collect)
            collectBtn.setIcon(R.drawable.ic_baseline_favorite_24)
            isCollect = true
    }

    private fun unCollectIt(collect:Collect){
            activityContext.viewModel.removeCollect(collect)
            collectBtn.setIcon(R.drawable.ic_bashline_unfavorite_24)
            isCollect = false
    }

    private fun download(fileUrl: String){
        lifecycleScope.launch(Dispatchers.IO) {
            activityContext.viewModel.postNewToast("Start download")
            val result = (activityContext.application as YandViewApplication).downloadBinder.downloadImage(fileUrl)
            if (result.isSuccess){
                activityContext.viewModel.postNewToast("Download successfully")
            }else{
                val exception = result.exceptionOrNull()
                activityContext.viewModel.postNewToast(exception?.message.toString())
            }

        }

    }

    private fun share(url:String,context: Context){
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = Glide.with(context).asBitmap().load(url).submit().get()
            val path = File("${context.cacheDir}/image")
            val split = url.split("/")
            val fileName = split[split.size - 1]
            if (!path.exists())
                path.mkdirs()


            var file = File("${context.cacheDir}/image/$fileName")
            val downloadFile =
                File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/$fileName")

            if (downloadFile.exists()){
                file = downloadFile
            }else if (!file.exists()) {
                file.outputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                }
            }
            val imageUri = FileProvider.getUriForFile(
                context,
                "com.lsp.view.fileprovider",
                file

            )
            context.grantUriPermission(
                "com.lsp.view",
                imageUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.putExtra(Intent.EXTRA_TITLE,fileName)
            val intent = Intent.createChooser(shareIntent, R.string.title_share.toString())
            val resInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT < 33) {
                context.packageManager
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            } else {
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            }
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    imageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.startActivity(intent)
        }


    }

    companion object{
        fun navigationToImageFragment(context:MainActivity ,post:Post,view:View,fragmentId:Int){
            val bundle = Bundle()
            bundle.putParcelable("post",post)
            val tags = post.tags?.split(",")?.toList()

            context.viewModel.tagsList.value?.apply {
                clear()
                tags?.let { addAll(it) }
            }

            Navigation.findNavController(view).navigate(fragmentId,bundle)
            val slideOutAnimation = AnimationUtils.loadAnimation(context,
                R.anim.slide_out_bottom
            )
            context.bottomNav.startAnimation(slideOutAnimation)
            context.bottomNav.visibility = View.GONE
        }
    }



}