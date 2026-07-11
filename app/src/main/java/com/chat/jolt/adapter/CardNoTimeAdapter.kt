package com.chat.jolt.adapter

import android.view.View
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.databinding.ItemNoCardBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.viewVisibility

class CardNoTimeAdapter : BaseRecyclerAdapter<ModelUserData, ItemNoCardBinding>(ItemNoCardBinding::inflate) {


    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0]/3

    private val pictureHeight = pictureWidth


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemNoCardBinding>,
        itemView: ItemNoCardBinding,
        item: ModelUserData,
        position: Int
    ) {

        itemView.apply {

            try {

                if (UserInfoHold.isOrganic){

                    if (item.coverPics.isNullOrEmpty()){
                        ivImage.loadImage(
                            context,
                            "",
                            pictureWidth,
                            pictureHeight
                        )
                    }else{
                        ivImage.loadImage(
                            context,
                            item.coverPics!![0],
                            pictureWidth,
                            pictureHeight
                        )
                    }
                }else{
                    if (item.coverPics.isNullOrEmpty()){
                        ivImage.loadImage(
                            context,
                            "",
                            pictureWidth,
                            pictureHeight
                        )
                    }else{
                        ivImage.loadImage(
                            context,
                            item.publicPic,
                            pictureWidth,
                            pictureHeight
                        )
                    }
                }



                tvName.text = "${item.nickname}·${item.age}"

                if (item.nearby == "True") {

                    tvLocation.text = "Nearby"

                    viewVisibility(View.VISIBLE, sllLocation)

                } else {

                    viewVisibility(View.GONE, sllLocation)
                }

                tvOnline.text = if (item.onlineStatus == "Online") "Online" else "Active"


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }



}