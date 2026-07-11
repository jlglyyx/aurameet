package com.chat.lib_common.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.leading.LeadingLoadStateAdapter
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter

class TopLoadAdapter :LeadingLoadStateAdapter<RecyclerView.ViewHolder>(){

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        loadState: LoadState
    ) {
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): RecyclerView.ViewHolder {

        return object :RecyclerView.ViewHolder(View(parent.context)){

        }
    }
}