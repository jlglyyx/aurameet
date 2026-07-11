package com.chat.lib_common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.BaseQuickAdapter

abstract class BaseRecyclerAdapter<T : Any,VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
):BaseQuickAdapter<T, BaseRecyclerAdapter.BaseRecyclerViewHolder<VB>>() {


    class BaseRecyclerViewHolder<VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root){

            val extra = mutableMapOf<String,Any>()
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewHolder<VB>, position: Int, item: T?) {

        item?.let {
            convert(holder,holder.binding,item,position)
        }

    }

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder<VB>,
        position: Int,
        item: T?,
        payloads: List<Any>
    ) {


        item?.let {
            convert(holder,holder.binding,item,position,payloads.toMutableList())
        }

        super.onBindViewHolder(holder, position, item, payloads)
    }

//    override fun onBindViewHolder(
//        holder: BaseRecyclerViewHolder<VB>,
//        position: Int,
//        item: T?,
//        payloads: List<Any>
//    ) {
//        super.onBindViewHolder()
//        item?.let {
//            convert(holder,holder.binding,item,position,payloads.toMutableList())
//        }
//    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): BaseRecyclerViewHolder<VB> {

        val baseRecyclerViewHolder = BaseRecyclerViewHolder(bindingInflater(LayoutInflater.from(parent.context),parent,false))

        onInitViewHolder(baseRecyclerViewHolder)

        return baseRecyclerViewHolder
    }

    open fun  onInitViewHolder(holder: BaseRecyclerViewHolder<VB>){}


    abstract fun convert(holder:BaseRecyclerViewHolder<VB>,itemView: VB, item: T, position: Int)

    open fun convert(holder:BaseRecyclerViewHolder<VB>,itemView: VB, item: T, position: Int,payloads: MutableList<Any>){

    }
}