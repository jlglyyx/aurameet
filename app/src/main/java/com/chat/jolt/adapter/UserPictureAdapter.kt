package com.chat.jolt.adapter

import android.view.View
import com.chat.jolt.databinding.ItemCoverPhotoBinding
import  com.chat.jolt.data.UploadPictureData
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.viewVisibility
import kotlin.apply

class UserPictureAdapter :
    BaseRecyclerAdapter<UploadPictureData, ItemCoverPhotoBinding>(ItemCoverPhotoBinding::inflate) {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0] / 3

    private val pictureHeight = pictureWidth * 4 / 3

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemCoverPhotoBinding>,
        itemView: ItemCoverPhotoBinding,
        item: UploadPictureData,
        position: Int
    ) {

        itemView.apply {

            when (item.status) {

                OSSUtil.UPLOAD_STATUS_NORMAL -> {
                    viewVisibility(View.VISIBLE, ivAdd)
                    viewVisibility(View.GONE, ivImage, ivLoad, ivDelete, ivError,svCover,stvSendStatus)
                }

                OSSUtil.UPLOAD_STATUS_LOADING -> {
                    viewVisibility(View.VISIBLE, ivImage, ivLoad)
                    viewVisibility(View.GONE, ivAdd, ivDelete, ivError,svCover,stvSendStatus)
                }

                OSSUtil.UPLOAD_STATUS_SUCCESS -> {
                    viewVisibility(View.VISIBLE, ivImage, ivDelete)

                    viewVisibility(View.GONE, ivAdd, ivLoad, ivError,stvSendStatus)

                    if (item.albumStatus == "Pass" || item.albumStatus == "Wait") {

                        stvSendStatus.visibility = View.GONE

                        svCover.visibility = View.GONE

                    } else {

                        stvSendStatus.visibility = View.VISIBLE

                        svCover.visibility = View.VISIBLE

                    }

                }

                OSSUtil.UPLOAD_STATUS_ERROR -> {
                    viewVisibility(View.VISIBLE, ivImage, ivError, ivDelete, svCover)
                    viewVisibility(View.GONE, ivAdd, ivLoad,stvSendStatus)
                }

                else -> {
                    viewVisibility(View.GONE, ivImage, ivLoad, ivDelete, ivError)
                    viewVisibility(View.VISIBLE, ivAdd,svCover,stvSendStatus)
                }
            }


        }

        itemView.ivImage.loadImage(context, item.url, pictureWidth, pictureHeight)
    }

    fun destroyViews() {
        try {
            val recyclerView = recyclerView ?: return
            for (i in 0 until recyclerView.childCount) {
                val view = recyclerView.getChildAt(i)
                val binding = ItemCoverPhotoBinding.bind(view)
                binding.ivLoad.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}