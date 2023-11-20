package com.lsp.view.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lsp.view.ui.fragment.adapter.PostAdapter
import com.lsp.view.R
import com.lsp.view.ui.activity.MainActivity
import com.lsp.view.bean.Post

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class CollectFragment : Fragment() {
    private lateinit var activityContext: MainActivity
    private val collectAdapter by lazy {
        PostAdapter(activityContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = requireActivity() as MainActivity

        activityContext.viewModel.getCollectList()

        activityContext.viewModel.collectList.observe(this){
            collectAdapter.pushNewData(it)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //刷新
        val refresh =
            view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        activityContext.viewModel.uiState.value.isRefreshing.observe(activityContext) {
            refresh.isRefreshing = it
        }

        collectAdapter.setOnListItemClick(object : PostAdapter.OnListItemClick {
            override fun setOnListItemClick(post: Post) {
                ImageFragment.navigationToImageFragment(activityContext,post,view,R.id.action_collectFragment_to_imageFragment)
            }

        })

        view.findViewById<RecyclerView>(R.id.recyclerview).apply {
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.layoutManager = layoutManager
            this.adapter = collectAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        if (activityContext.bottomNav.visibility == View.GONE){
            val slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_on_bottom)
            activityContext.bottomNav.startAnimation(slideOutAnimation)
            activityContext.bottomNav.visibility = View.VISIBLE
        }

    }
}