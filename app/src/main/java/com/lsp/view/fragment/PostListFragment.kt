package com.lsp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.lsp.view.fragment.adapter.PostAdapter
import com.lsp.view.R
import com.lsp.view.activity.MainActivity
import com.lsp.view.bean.Post
import com.lsp.view.model.MainViewModel

class PostListFragment:Fragment() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity(),MainViewModel.provideFactory(activityContext.repository,requireContext(),this)).get(MainViewModel::class.java)
    }
    private lateinit var activityContext: MainActivity



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityContext = requireActivity() as MainActivity
        val searchBar = view.findViewById<SearchBar>(R.id.search_bar)

        val searchView = view.findViewById<SearchView>(R.id.search_view)
        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.fetchPostBySearch(searchView.text.toString())
                searchBar.setText(searchView.text)
                searchView.hide()
            }

            return@setOnEditorActionListener false
        }

        viewModel.uiState.value.nowSearchText.observe(requireActivity()){
            searchView.setText(it)
            searchBar.setText(it)
        }

        //刷新
        val refresh =
            view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        viewModel.uiState.value.isRefreshing.observe(activityContext) {
            refresh.isRefreshing = it
        }
        refresh.setOnRefreshListener {
            viewModel.fetchPostByRefresh()
        }

        viewModel.postList.observe(activityContext){
            viewModel.adapter.pushNewData(it)
        }

        viewModel.adapter.apply {
            setLoadMoreListener(object : PostAdapter.OnScrollToBottom {
                override fun event(position: Int) {
                    viewModel.fetchMore()
                }
            })
        }

        view.findViewById<RecyclerView>(R.id.recyclerview).apply {
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.layoutManager = layoutManager
            this.adapter = viewModel.adapter
        }

        viewModel.adapter.setOnListItemClick(object : PostAdapter.OnListItemClick {
            override fun setOnListItemClick(post: Post) {
                val bundle = Bundle()
                bundle.putParcelable("post",post)
                Navigation.findNavController(view).navigate(R.id.action_postListFragment_to_imageFragment,bundle)
                val slideOutAnimation = AnimationUtils.loadAnimation(context,
                    R.anim.slide_out_bottom
                )
                activityContext.bottomNav.startAnimation(slideOutAnimation)
                activityContext.bottomNav.visibility = View.GONE
            }

        })



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