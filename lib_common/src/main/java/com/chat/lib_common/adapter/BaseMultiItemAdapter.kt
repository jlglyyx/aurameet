package com.chat.lib_common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.BaseMultiItemAdapter

abstract class BaseMultiItemAdapter<T : Any>: BaseMultiItemAdapter<T>() {

    class BaseRecyclerViewHolder<VB : ViewBinding>(val viewBinding: VB) :
        RecyclerView.ViewHolder(viewBinding.root) {

        val extra = mutableMapOf<String, Any>()
    }

    abstract class BaseMultiItemViewHolder<T,VB : ViewBinding>(private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB) :
        OnMultiItemAdapterListener<T, BaseRecyclerViewHolder<VB>> {
        override fun onCreate(
            context: Context,
            parent: ViewGroup,
            viewType: Int
        ): BaseRecyclerViewHolder<VB> {


            val baseRecyclerViewHolder = BaseRecyclerViewHolder(
                bindingInflater(LayoutInflater.from(parent.context), parent, false)
            )

            onInitViewHolder(baseRecyclerViewHolder)

            return baseRecyclerViewHolder
        }


        open fun  onInitViewHolder(holder: BaseRecyclerViewHolder<VB>){}
    }



}