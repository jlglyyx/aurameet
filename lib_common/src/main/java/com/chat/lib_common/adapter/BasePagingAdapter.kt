package com.chat.lib_common.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BasePagingAdapter<T : Any, VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T> = DefaultDiffCallback()
) : PagingDataAdapter<T, BasePagingAdapter.BasePagingViewHolder<VB>>(diffCallback) {

    private var itemClickListener: ((VB, Int) -> Unit)? = null
    private var itemLongClickListener: ((VB, Int) -> Unit)? = null
    private val childClickListeners = mutableMapOf<Int, (VB, Int) -> Unit>()
    private val childLongClickListeners = mutableMapOf<Int, (VB, Int) -> Unit>()

    private var _recyclerView: RecyclerView? = null

    val recyclerView: RecyclerView
        get() {
            checkNotNull(_recyclerView) {
                "Please get it after onAttachedToRecyclerView()"
            }
            return _recyclerView!!
        }

    val context: Context
        get() {
            return recyclerView.context
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasePagingViewHolder<VB> {

        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)

        return BasePagingViewHolder(binding).apply {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.invoke(binding, position)
                }
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemLongClickListener?.invoke(binding, position)
                }

                return@setOnLongClickListener false
            }

            childClickListeners.forEach { (viewId, listener) ->
                binding.root.findViewById<View>(viewId)?.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.invoke(binding, position)
                    }
                }
            }
            childLongClickListeners.forEach { (viewId, listener) ->
                binding.root.findViewById<View>(viewId)?.setOnLongClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.invoke(binding, position)
                    }

                    return@setOnLongClickListener false
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BasePagingViewHolder<VB>, position: Int) {
        getItem(position)?.let { item ->
            convert(holder.binding, item, position)
        }
    }

    abstract fun convert(mBindView: VB, item: T, position: Int)

    fun setOnItemClickListener(listener: (VB, Int) -> Unit) {
        this.itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (VB, Int) -> Unit) {
        this.itemLongClickListener = listener
    }

    fun setOnItemChildClickListener(@IdRes viewId: Int, listener: (VB, Int) -> Unit) {
        childClickListeners[viewId] = listener
    }

    fun setOnItemChildLongClickListener(@IdRes viewId: Int, listener: (VB, Int) -> Unit) {
        childClickListeners[viewId] = listener
    }

    class DefaultDiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    }

    fun indexItem(position: Int): T? {

        return getItem(position)
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        _recyclerView = recyclerView
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        _recyclerView = null
    }


    class BasePagingViewHolder<VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        val extra = mutableMapOf<String, Any>()

    }

}
