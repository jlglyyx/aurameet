package com.chat.jolt.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.jolt.data.ILikeImageData
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.databinding.ItemILikeBinding
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.util.replaceEmoji

class ILikeAdapter : BaseRecyclerAdapter<ModelUserData, ItemILikeBinding>(ItemILikeBinding::inflate) {


    var onImageClick:(String, Int) -> Unit = {_,_->

    }


    override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemILikeBinding>) {
        super.onInitViewHolder(holder)

        initBanner(holder)

    }

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemILikeBinding>,
        itemView: ItemILikeBinding,
        item: ModelUserData,
        position: Int
    ) {

        itemView.apply {

            try {

                tvName.text = "${item.nickname}·${item.age}"

                tvOnline.text = if (item.onlineStatus == "Online") "Online" else "Active"

                tvContent.text = item.aim.replaceEmoji()

                (holder.extra["mILikeImageAdapter"] as ILikeImageAdapter).submitList(item.coverPics?.map {
                    ILikeImageData(item.userId,it)
                })

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


    private fun initBanner(
        holder: BaseRecyclerViewHolder<ItemILikeBinding>,
    ) {

        holder.binding.apply {

            val mILikeImageAdapter = ILikeImageAdapter()

            recyclerView.adapter = mILikeImageAdapter

            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL,false)

            holder.extra["mILikeImageAdapter"] = mILikeImageAdapter


            mILikeImageAdapter.setOnItemClickListener {_,_,position->

                val item = mILikeImageAdapter.getItem(position)?:return@setOnItemClickListener

                onImageClick(item.userId,holder.absoluteAdapterPosition)
            }
        }
    }


}