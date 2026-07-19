package com.chat.jolt.adapter

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.request.RequestOptions
import com.chat.jolt.data.ILikeImageData
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.databinding.ItemILikeBinding
import com.chat.jolt.databinding.ItemILikeBinding.inflate
import com.chat.jolt.databinding.ItemLikeBinding
import com.chat.jolt.databinding.ItemWlmHobbyBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.util.replaceEmoji
import com.chat.lib_common.widget.BlurTransformation



class LikeItemAdapter : BaseRecyclerAdapter<ModelUserData, ItemILikeBinding>(ItemILikeBinding::inflate) {


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


//class LikeItemAdapter : BaseRecyclerAdapter<ModelUserData, ItemLikeBinding>(ItemLikeBinding::inflate) {
//
//
//    private val pictureBlurWidth = 40f.dip2px(BaseApplication.mApplication)
//
//    private val pictureBlurHeight = pictureBlurWidth
//
//    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0]/3
//
//    private val pictureHeight = pictureWidth
//
//    private val requestOptions = RequestOptions.bitmapTransform(BlurTransformation(10))
//
//
//    private var isVip = UserInfoHold.isVip
//
//
//
//
//    override fun convert(
//        holder: BaseRecyclerViewHolder<ItemLikeBinding>,
//        itemView: ItemLikeBinding,
//        item: ModelUserData,
//        position: Int
//    ) {
//
//        itemView.apply {
//
//            try {
//
//
//
//                if (UserInfoHold.isVip){
//                    ivImage.loadImage(
//                        context,
//                        item.headPic,
//                        pictureWidth,
//                        pictureHeight
//                    )
//
//                    sclContainer.updatePadding(bottom = 0)
//
//                }else{
//                    ivImage.loadOptionImage(
//                        context,
//                        item.headPic,
//                        requestOptions,
//                        pictureBlurWidth,
//                        pictureBlurHeight
//                    )
//
//                    if (position == items.lastIndex){
//
//                        sclContainer.updatePadding(bottom = 80f.dip2px(Utils.getApp()))
//                    }else{
//                        sclContainer.updatePadding(bottom = 0)
//                    }
//                }
//
//
//
//
//                tvName.text = "${item.nickname}·${item.age}"
//
//                tvLocation.text = "${item.distance}"
//
//                tvOnline.text = if (item.onlineStatus == "Online") "Online" else "Active"
//
//                hobbyFlexbox.removeAllViews()
//
//                if (item.turnOnsTags.isNullOrEmpty() || UserInfoHold.isOrganic){
//
//
//                    if (item.commonHobbyTags.isNullOrEmpty()){
//
//                        item.hobbyTags.take(2).forEach {
//
//                            ItemWlmHobbyBinding.inflate(LayoutInflater.from(context), hobbyFlexbox, true)
//                                .apply {
//
//                                    tvTitle.text = it
//                                }
//
//                        }
//                    }else{
//
//                        val commonTake = item.commonHobbyTags.take(2)
//
//                        commonTake.forEach {
//
//                            ItemWlmHobbyBinding.inflate(LayoutInflater.from(context), hobbyFlexbox, true)
//                                .apply {
//
//                                    tvTitle.text = it
//                                    tvTitle.isEnabled = false
//                                }
//
//                        }
//
//                        if (commonTake.size < 2){
//
//                            val hobbyTake = item.hobbyTags.take(2 - commonTake.size)
//
//                            hobbyTake.forEach {
//
//                                ItemWlmHobbyBinding.inflate(LayoutInflater.from(context), hobbyFlexbox, true)
//                                    .apply {
//
//                                        tvTitle.text = it
//                                    }
//
//                            }
//                        }
//
//                    }
//
//                    stvTurn.visibility = View.GONE
//                    hobbyFlexbox.visibility = View.VISIBLE
//                }else{
//                    stvTurn.text = "${item.turnOnsTags?.size?:0} Turn-ons"
//                    stvTurn.visibility = View.VISIBLE
//                    hobbyFlexbox.visibility = View.GONE
//                }
//
//
//
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//    }
//
//
//    fun openAllImage(){
//
//        val currentVipStatus = UserInfoHold.isVip
//
//        if (isVip == currentVipStatus) return
//
//        isVip = currentVipStatus
//
//        notifyItemRangeChanged(0,itemCount,false)
//    }
//
//
//}