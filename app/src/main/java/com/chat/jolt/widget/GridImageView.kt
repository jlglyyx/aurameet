package com.chat.jolt.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Outline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.data.CustomLockMessageData
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.data.PreViewMediaData
import com.chat.jolt.databinding.ItemGridImageBinding
import com.chat.jolt.databinding.ViewGridImageBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.isVideo
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.util.toJson
import com.chat.lib_common.widget.BlurTransformation
import com.chat.lib_common.widget.ImageLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import io.rong.imlib.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GridImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {


    private val mViewBinding: ViewGridImageBinding by lazy {

        ViewGridImageBinding.inflate(LayoutInflater.from(context), this, true)
    }

    companion object{
        const val LOCK_STATUS = 0
        const val NORMAL_STATUS = 1
        const val DESTROY_STATUS = -1
    }

    var mAdapter: BaseRecyclerAdapter<PreViewMediaData, ItemGridImageBinding>? = null

    var onItemClick:(MutableList<PreViewMediaData>, Int) -> Unit = {_,_ ->}


    private val enableTime = AppConstant.Constant.MEDIA_ENABLE_TIME

    private val pictureWidth = 120f.dip2px(BaseApplication.mApplication)

    private val pictureHeight = (pictureWidth * 1.3f).toInt()

    private var timerJob: Job? = null

    private val radius = 10f.dip2px(context).toFloat()

    private var isMe = false

    var interceptTouch = false

    private val requestOptions = RequestOptions.bitmapTransform(BlurTransformation(AppConstant.Constant.PPV_BLUR_RADIUS, 2))


    override fun onFinishInflate() {
        super.onFinishInflate()

        mViewBinding.recyclerView.clipToOutline = true
        mViewBinding.recyclerView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
//        initRecyclerView()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return interceptTouch
    }


    private fun initAdapter() {

        if (null == mAdapter) {

            mAdapter = object :
                BaseRecyclerAdapter<PreViewMediaData, ItemGridImageBinding>(ItemGridImageBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemGridImageBinding>,
                    itemView: ItemGridImageBinding,
                    item: PreViewMediaData,
                    position: Int
                ) {

                    if (item.url.isVideo()) {

                        loadImage(
                            itemView.stvImage,
                            item.cover ?: item.url,
                            item.status
                        )

                        itemView.ivVideo.visibility = VISIBLE

                        itemView.ivError.setImageResource(R.drawable.iv_video_error)

                    } else {

                        loadImage(itemView.stvImage, item.url, item.status)

                        itemView.ivVideo.visibility = GONE

                        itemView.ivError.setImageResource(R.drawable.iv_photo_error)
                    }


                    //0 lock -1 destroy
                    when (item.status) {
                        LOCK_STATUS -> {
                            itemView.ivLock.visibility = VISIBLE
                            itemView.sllTime.visibility = GONE
                            itemView.llDestroyContainer.visibility = GONE
                        }

                        DESTROY_STATUS -> {
                            itemView.ivLock.visibility = GONE
                            itemView.sllTime.visibility = GONE
                            itemView.llDestroyContainer.visibility = VISIBLE
                        }

                        else -> {
                            itemView.ivLock.visibility = GONE

                            if (item.time > 0) {
                                itemView.tvTime.text = "${getTimeSecond(item.time)}"

                                itemView.sllTime.visibility = VISIBLE
                                itemView.llDestroyContainer.visibility = GONE
                            } else {
                                itemView.sllTime.visibility = GONE
                                itemView.llDestroyContainer.visibility = VISIBLE
                            }

                        }
                    }

                }

            }
        }

        mViewBinding.recyclerView.adapter = mAdapter



        mAdapter?.setOnDebouncedItemClick { _, _, position ->




            val data = mAdapter?.items

            data?.let {

                onItemClick(it.toMutableList(),position)

//                showPictureDetailDialog(it.toMutableList(), position)
            }
        }

    }


    fun initRecyclerView() {


        initAdapter()


//        mViewBinding.recyclerView.setRecycledViewPool(sharedPool)

        mViewBinding.recyclerView.itemAnimator = null
//
        mViewBinding.recyclerView.setItemViewCacheSize(9)

        mViewBinding.recyclerView.layoutManager = ImageLayoutManager()


    }





    private fun loadImage(imageView: ShapeableImageView, url: String?, status: Int) {


        when (status) {
            LOCK_STATUS -> {
                imageView.loadOptionImage(
                    imageView.context,
                    url,
                    requestOptions,
                    pictureWidth,
                    pictureHeight
                )

                imageView.visibility = VISIBLE
            }

            DESTROY_STATUS -> {

                imageView.setImageDrawable(null)

                imageView.visibility = GONE

            }

            else -> {

                imageView.visibility = VISIBLE

                imageView.loadImage(
                    imageView.context,
                    url,
                    pictureWidth,
                    pictureHeight
                )
            }
        }

    }


    fun setData(list: MutableList<ModelMediaData>, mMessage: Message?) {

        if (null == mMessage) return

//        initAdapter()

        val ppvUnlockStatus = mMessage.expansion["ppvUnlockStatus"]

        val unlockTimestamp: Long = mMessage.expansion["unlockTimestamp"]?.toLong() ?: 0L

        val time = (unlockTimestamp + enableTime - System.currentTimeMillis()) / 1000

        val isDestroy = mMessage.expansion["isDestroy"] == "True"

        val isLocked = mMessage.expansion["isLocked"] == "True"


        isMe = UserInfoHold.userId == mMessage.senderUserId

        val mPPVMessage = mMessage.content as PPVMessage

        val isVideo = mPPVMessage.type != AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG




        if (null != ppvUnlockStatus) {

            val mCustomLockMessageData = ppvUnlockStatus.formatListJson<CustomLockMessageData>()

            if (mCustomLockMessageData.all { it.unlockTimestamp == 0L } && !isLocked) {

                val status = if (isDestroy) {
                    DESTROY_STATUS
                } else {
                    NORMAL_STATUS
                }

                val result = list.map {

                    PreViewMediaData(it.albumId?: "ID_${System.currentTimeMillis()}",it.albumUrl.toString(), it.cover, time.toInt(), status)
                }



                mAdapter?.submitList(result)
            } else {

                val result = list.mapIndexed { index, modelMediaData ->

                    val unlockTimestamp: Long = mCustomLockMessageData[index].unlockTimestamp ?: 0L

                    val time = (unlockTimestamp + enableTime - System.currentTimeMillis()) / 1000

                    val isDestroy = mCustomLockMessageData[index].isDestroy == "True"

                    val isLocked = mCustomLockMessageData[index].isLocked == "True"

                    val status = if (isLocked) {
                        LOCK_STATUS
                    } else if (isDestroy) {
                        DESTROY_STATUS
                    } else {
                        NORMAL_STATUS
                    }

                    PreViewMediaData(modelMediaData.albumId?: "ID_${System.currentTimeMillis()}",
                        modelMediaData.albumUrl.toString(),
                        modelMediaData.cover,
                        time.toInt(),
                        status
                    )
                }
                mAdapter?.submitList(result)
            }
        } else {
            val status = if (isLocked) {
                LOCK_STATUS
            } else if (isDestroy) {
                DESTROY_STATUS
            } else {
                NORMAL_STATUS
            }

            val result = list.map {

                PreViewMediaData(it.albumId?: "ID_${System.currentTimeMillis()}",it.albumUrl.toString(), it.cover, time.toInt(), status)
            }
            mAdapter?.submitList(result)
        }

        val hasLocal = mAdapter?.items?.any { it.status == LOCK_STATUS }

        val count = mAdapter?.items?.count { it.status == LOCK_STATUS }?:0

        mViewBinding.sllCount.visibility = if (hasLocal == true) View.VISIBLE else View.GONE

        mViewBinding.tvNum.text =
            if (isVideo) "Private video ${count}" else "Private photo ${count}"
        setTime(mMessage)
    }


    private fun setTime(mMessage: Message) {


        val items = mAdapter?.items

        if (items.isNullOrEmpty()) return

        timerJob?.cancel()

        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {

                val allDestroy = items.all { it.time <= 0 }

                if (allDestroy){

                    cancel()
                }


                items.forEachIndexed { index, it ->

                    if (it.time > 0) {

                        it.time -= 1

                        if (it.time <= 0) {

                            it.status = DESTROY_STATUS

                            updateMessageExpansion(index, mMessage)
                        }

                        mAdapter?.notifyItemChanged(index, false)
                    }
                }

                delay(1000)
//                adapter.notifyItemRangeChanged(0, adapter.itemCount, false)
            }
        }


    }


    private suspend fun updateMessageExpansion(
        index: Int,
        message: Message
    ) {

        val ppvUnlockStatus = message.expansion["ppvUnlockStatus"]

        val param = HashMap<String, String>().apply {
            this["isDestroy"] = "True"
        }


        if (null != ppvUnlockStatus) {

            val mCustomLockMessageData = ppvUnlockStatus.formatListJson<CustomLockMessageData>()

            for (i in 0..index) {

                mCustomLockMessageData[i].isDestroy = "True"
            }

            param["ppvUnlockStatus"] = mCustomLockMessageData.toJson()

            message.setExpansion(param)
        }

        delay(100)


        RIMClient.updateMessageExpansion(param, message.uId, onSuccess = {
            message.setExpansion(param)
        })
    }


    fun release() {
        timerJob?.cancel()
        mViewBinding.recyclerView.adapter = null
        mAdapter = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            var act: Activity? = null
            var contextCheck = context
            while (contextCheck is ContextWrapper) {
                if (contextCheck is Activity) {
                    act = contextCheck
                    break
                }
                contextCheck = contextCheck.baseContext
            }
            if (act == null || act.isFinishing || act.isDestroyed) {
                release()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}