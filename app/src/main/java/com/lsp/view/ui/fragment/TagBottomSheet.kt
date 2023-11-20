package com.lsp.view.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lsp.view.R
import com.lsp.view.ui.activity.MainActivity
import com.lsp.view.ui.fragment.adapter.TagsAdapter

class TagBottomSheet(val tags:String?): BottomSheetDialogFragment() {
    private val activityContext : MainActivity by lazy {
        requireActivity() as MainActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tagList = view.findViewById<RecyclerView>(R.id.tag_list)
//        val layoutManager = LinearLayoutManager(activityContext)
        val flManager = FlexboxLayoutManager(activityContext)
        flManager.flexWrap = FlexWrap.WRAP
        flManager.flexDirection = FlexDirection.ROW

        Log.e(TAG,tags.toString())
        val split = tags?.split(" ")?.toList()
        if (split!=null){
            val adapter = TagsAdapter(split)
            tagList.layoutManager = flManager
            tagList.adapter = adapter
        }

    }
    companion object {
        const val TAG = "TagBottomSheet"
    }
}