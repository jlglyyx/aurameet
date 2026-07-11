package com.chat.jolt.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.request.RequestOptions
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.databinding.ItemCrazyImageBinding
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionVideo
import com.chat.lib_common.widget.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CrazyAdapter(val scope:LifecycleCoroutineScope) :
    BaseRecyclerAdapter<ModelMediaData, ItemCrazyImageBinding>(ItemCrazyImageBinding::inflate) {

    private val pictureWidth = 120f.dip2px(BaseApplication.mApplication)

    private val pictureHeight = (pictureWidth * 1.3f).toInt()

    private val requestOptions = RequestOptions.bitmapTransform(BlurTransformation(AppConstant.Constant.PPV_BLUR_RADIUS, 2))


    init {

        initTimer()
    }


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemCrazyImageBinding>,
        itemView: ItemCrazyImageBinding,
        item: ModelMediaData,
        position: Int
    ) {

        if (item.albumType == "PUBLIC_PIC"){

            itemView.stvImage.loadImage(
                itemView.stvImage.context,
                item.albumUrl,pictureWidth,pictureHeight
            )

            itemView.ivVideo.visibility = GONE

            itemView.sllTime.visibility = GONE

            itemView.llDestroyContainer.visibility = GONE
        }else{

            itemView.ivVideo.visibility = VISIBLE


            when (item.albumStatus) {
                "Sent" -> {
                    itemView.sllTime.visibility = GONE
                    itemView.llDestroyContainer.visibility = GONE

                    itemView.stvImage.loadOptionVideo(
                        itemView.stvImage.context,
                        item.albumUrl,requestOptions,pictureWidth,pictureHeight
                    )
                }
                else -> {
                    val time = item.ttl
                    if (time > 0) {
                        itemView.tvTime.text = "${getTimeSecond(time)}"
                        itemView.sllTime.visibility = VISIBLE
                        itemView.llDestroyContainer.visibility = GONE

                        itemView.stvImage.loadImage(
                            itemView.stvImage.context,
                            item.albumUrl,pictureWidth,pictureHeight
                        )

                    } else {
                        itemView.sllTime.visibility = GONE
                        itemView.llDestroyContainer.visibility = VISIBLE
                    }

                }
            }

        }
    }



    private fun initTimer() {

        scope.launch(Dispatchers.Main) {

            try {
                while (isActive) {

                    val cancel = this@CrazyAdapter.items.all { it.ttl == 0 }

                    if (cancel){

                        this.cancel()

                    }

                    val start = this@CrazyAdapter.items.all { it.albumStatus == "Sent" }

                    if (!start){

                        this@CrazyAdapter.items.forEachIndexed { index, it ->

                            if (it.albumStatus != "Sent" &&it.ttl > 0){

                                it.ttl = it.ttl - 1

                                this@CrazyAdapter.notifyItemChanged(index,false)
                            }
                        }
                    }

                    delay(1000)
                }
            }catch (e: Exception){

                e.printStackTrace()
            }

        }

    }


}