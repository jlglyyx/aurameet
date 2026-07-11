package com.chat.jolt.adapter

import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.request.RequestOptions
import com.chat.jolt.R
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.databinding.ItemVisitorBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.widget.BlurTransformation

class VisitorAdapter :
    BaseRecyclerAdapter<ModelUserData, ItemVisitorBinding>(ItemVisitorBinding::inflate) {


    private val mAvatarWidth = 50f.dip2px(Utils.getApp())

    private val requestOptions = RequestOptions.bitmapTransform(BlurTransformation(20,2))


    private var isVip = UserInfoHold.isVip

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemVisitorBinding>,
        itemView: ItemVisitorBinding,
        item: ModelUserData,
        position: Int
    ) {

        itemView.apply {


            if (UserInfoHold.isVip){
                ivAvatar.loadImage(context, item.headPic, mAvatarWidth, mAvatarWidth)

            }else{

                if (position < 2){
                    ivAvatar.loadImage(context, item.headPic, mAvatarWidth, mAvatarWidth)
                }else{
                    ivAvatar.loadOptionImage(context, item.headPic, requestOptions,mAvatarWidth, mAvatarWidth)
                }

            }
            tvName.text = "${item.nickname},${item.age}"

            SpanUtils.with(tvMessage).append("Viewed you: ").append("${item.visitorTimes}Times").setForegroundColor(context.getColor(
                R.color.color_E70614)).create()

            itemView.stvAline.visibility =
                if (item.onlineStatus == "Online") View.VISIBLE else View.GONE

        }


    }

    fun openAllImage(){

        val currentVipStatus = UserInfoHold.isVip

        if (isVip == currentVipStatus) return

        isVip = currentVipStatus

        notifyItemRangeChanged(0,itemCount,false)
    }
}