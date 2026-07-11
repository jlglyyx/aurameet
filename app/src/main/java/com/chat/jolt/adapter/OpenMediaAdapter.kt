package com.chat.jolt.adapter

import android.view.View
import com.chat.jolt.R
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.databinding.ItemAddMediaBinding
import com.chat.jolt.databinding.ItemSendMediaBinding
import com.chat.lib_common.adapter.BaseMultiItemAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.loadImage
import kotlin.apply
import kotlin.let
import kotlin.text.isNullOrEmpty


class OpenMediaAdapter(val selectList: MutableList<ModelMediaData>) :
    BaseMultiItemAdapter<ModelMediaData>() {

    val width: Int = (getScreenPx(BaseApplication.mApplication)[0] / 3) - 10f.dip2px(
        BaseApplication.mApplication
    )

    companion object {
        const val ITEM_OPEN_MEDIA = 0
        const val ITEM_ADD_MEDIA = 1
    }


    init {
        addItemType(
            ITEM_ADD_MEDIA,
            object : BaseMultiItemViewHolder<ModelMediaData, ItemAddMediaBinding>(
                ItemAddMediaBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemAddMediaBinding>,
                    position: Int,
                    item: ModelMediaData?
                ) {

                }

            })
        addItemType(
            ITEM_OPEN_MEDIA,
            object : BaseMultiItemViewHolder<ModelMediaData, ItemSendMediaBinding>(
                ItemSendMediaBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemSendMediaBinding>,
                    position: Int,
                    item: ModelMediaData?
                ) {

                    item?.apply {

                        try {
                            item.let {


                                if (!item.albumId.isNullOrEmpty() || item.uploadStatus == OSSUtil.UPLOAD_STATUS_SUCCESS) {

                                    holder.viewBinding.clNum.visibility = View.VISIBLE
                                } else {

                                    holder.viewBinding.clNum.visibility = View.GONE
                                }


                                when (item.uploadStatus) {
                                    OSSUtil.UPLOAD_STATUS_LOADING -> {
                                        holder.viewBinding.ivLoad.visibility = View.VISIBLE
                                        holder.viewBinding.ivError.visibility = View.GONE
                                        holder.viewBinding.svCover.alpha = 0.5f
                                        holder.viewBinding.stvSendStatus.visibility = View.GONE
                                    }

                                    OSSUtil.UPLOAD_STATUS_ERROR -> {
                                        holder.viewBinding.ivLoad.visibility = View.GONE
                                        holder.viewBinding.ivError.visibility = View.VISIBLE
                                        holder.viewBinding.stvSendStatus.visibility = View.VISIBLE
                                        holder.viewBinding.ivLoad.stop()
                                        holder.viewBinding.svCover.alpha = 0.5f
                                    }

                                    else -> {
                                        holder.viewBinding.ivLoad.visibility = View.GONE
                                        holder.viewBinding.ivError.visibility = View.GONE
                                        holder.viewBinding.ivLoad.stop()

                                        if (item.albumStatus == "Pass" || item.albumStatus == "Wait") {

                                            holder.viewBinding.stvSendStatus.visibility = View.GONE

                                            if (item.albumType == "Video") {

                                                holder.viewBinding.svCover.alpha = 0.15f
                                            }else{
                                                holder.viewBinding.svCover.alpha = 0f
                                            }


                                        } else {

                                            holder.viewBinding.stvSendStatus.visibility =
                                                View.VISIBLE
                                            holder.viewBinding.svCover.alpha = 0.5f

                                        }

                                    }
                                }

                                if (item.albumType == "Video") {
                                    holder.viewBinding.ivPlay.visibility = View.VISIBLE
                                    holder.viewBinding.tvTime.visibility = View.VISIBLE
                                    holder.viewBinding.ivImage.loadImage(
                                        context,
                                        if (null == item.uri) item.videoCover else item.uri,
                                        width,
                                        width
                                    )
                                    holder.viewBinding.tvTime.text =
                                        getTimeSecond(item.videoSeconds)
                                } else {
                                    holder.viewBinding.ivPlay.visibility = View.GONE
                                    holder.viewBinding.tvTime.visibility = View.GONE
                                    holder.viewBinding.ivImage.loadImage(
                                        context,
                                        if (null == item.uri) item.albumUrl else item.uri,
                                        width,
                                        width
                                    )
                                }


                                val selectedIndex = selectList.indexOf(it)

                                if (selectedIndex != -1) {
                                    holder.viewBinding.stvNum.shapeDrawableBuilder.setSolidColor(
                                        context.getColor(R.color.color_button)
                                    ).intoBackground()
                                    holder.viewBinding.stvNum.text = "${selectedIndex + 1}"
                                } else {
                                    holder.viewBinding.stvNum.shapeDrawableBuilder.setSolidColor(
                                        context.getColor(R.color.transparent)
                                    ).intoBackground()
                                    holder.viewBinding.stvNum.text = ""
                                }

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                }

            })


        onItemViewType { position, list ->

            if (position == 0) {
                ITEM_ADD_MEDIA
            } else {
                ITEM_OPEN_MEDIA
            }


        }
    }


}