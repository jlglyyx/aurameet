package com.chat.jolt.adapter

import android.graphics.Outline
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SpanUtils
import com.bumptech.glide.request.RequestOptions
import com.chat.jolt.R
import com.chat.jolt.data.CustomMessage
import com.chat.jolt.data.PreViewMediaData
import com.chat.jolt.data.UnlockAlbums
import com.chat.jolt.databinding.ItemChatTurnBinding
import com.chat.jolt.databinding.ItemMessageLeftBinding
import com.chat.jolt.databinding.ItemMessageLeftImageBinding
import com.chat.jolt.databinding.ItemMessageLeftImageOrVideoBinding
import com.chat.jolt.databinding.ItemMessageLeftVideoBinding
import com.chat.jolt.databinding.ItemMessageLimitBinding
import com.chat.jolt.databinding.ItemMessageMatchBinding
import com.chat.jolt.databinding.ItemMessageMatchPvBinding
import com.chat.jolt.databinding.ItemMessageRightBinding
import com.chat.jolt.databinding.ItemMessageRightImageBinding
import com.chat.jolt.databinding.ItemMessageRightImageOrVideoBinding
import com.chat.jolt.databinding.ItemMessageRightVideoBinding
import com.chat.jolt.databinding.ItemMessageSystemBinding
import com.chat.jolt.databinding.ItemMessageTimeBinding
import com.chat.jolt.databinding.ItemMessageTurnBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.getMessageExtraData
import com.chat.jolt.helper.getMessageMediaData
import com.chat.jolt.helper.getMessagePPVData
import com.chat.jolt.widget.GridImageView
import com.chat.lib_common.adapter.BaseMultiItemAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.RIMClient.updateMessageExpansion
import com.chat.lib_common.im.message.PPVMessage
import com.chat.lib_common.im.message.VideoMessage
import com.chat.lib_common.util.dateFormat
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.getFormatMessageTime
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.util.toJson
import com.chat.lib_common.util.viewVisibility
import com.chat.lib_common.widget.BlurTransformation
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import io.rong.imlib.model.Message
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.text.toLong

class ChatAdapter(private val scope: CoroutineScope) : BaseMultiItemAdapter<CustomMessage>() {


    private val maxIntervalTime = 5 * 60 * 1000

    private val jobMap = mutableMapOf<String, Job?>()

    private val enableTime = AppConstant.Constant.MEDIA_ENABLE_TIME


    private val pictureWidth = 150f.dip2px(BaseApplication.mApplication)

    private val pictureHeight = pictureWidth * 4 / 3

    private val avatarWidth = 40f.dip2px(BaseApplication.mApplication)

    private val avatarHeight = avatarWidth


    private val requestOptions =
        RequestOptions.bitmapTransform(BlurTransformation(AppConstant.Constant.PPV_BLUR_RADIUS, 2))


    private val radius = 10f.dip2px(BaseApplication.mApplication).toFloat()

    var onGridItemClick: (MutableList<PreViewMediaData>, Int, Int) -> Unit = { _, _, _ -> }

    companion object {
        const val ITEM_MESSAGE_MATCH = 0
        const val ITEM_MESSAGE_LEFT = 1
        const val ITEM_MESSAGE_RIGHT = 2
        const val ITEM_MESSAGE_SYSTEM = 3
        const val ITEM_MESSAGE_LEFT_IMAGE = 4
        const val ITEM_MESSAGE_RIGHT_IMAGE = 5
        const val ITEM_MESSAGE_LEFT_VIDEO = 6
        const val ITEM_MESSAGE_RIGHT_VIDEO = 7
        const val ITEM_MESSAGE_NTF_MSG = 8
        const val ITEM_MESSAGE_LEFT_IMAGE_OR_VIDEO_S = 9
        const val ITEM_MESSAGE_RIGHT_IMAGE_OR_VIDEO_S = 10
        const val ITEM_MESSAGE_MATCH_PV = 11
        const val ITEM_MESSAGE_LIMIT = 12
        const val ITEM_MESSAGE_TURN = 13
    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        try {

            val itemViewType = holder.itemViewType

            if (itemViewType == ITEM_MESSAGE_LEFT_IMAGE || itemViewType == ITEM_MESSAGE_RIGHT_IMAGE
                || itemViewType == ITEM_MESSAGE_LEFT_VIDEO || itemViewType == ITEM_MESSAGE_RIGHT_VIDEO
            ) {

                holder.itemView.findViewById<TextView>(R.id.tv_time)?.text = "00:00"

            } else if (itemViewType == ITEM_MESSAGE_LEFT_IMAGE_OR_VIDEO_S || itemViewType == ITEM_MESSAGE_RIGHT_IMAGE_OR_VIDEO_S) {

                holder.itemView.findViewById<GridImageView>(R.id.gridImageView)?.release()
            }
            val bindingAdapterPosition = holder.bindingAdapterPosition
            if (bindingAdapterPosition != -1 && bindingAdapterPosition <= items.lastIndex) {
                val item = items[bindingAdapterPosition]
                jobMap[item.message.uId]?.cancel()
                jobMap.remove(item.message.uId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    init {
        addItemType(
            ITEM_MESSAGE_MATCH,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageMatchBinding>(
                ItemMessageMatchBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageMatchBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {
                    item?.apply {

                        try {
                            val messageExtraData = getMessageExtraData(message)

                            if (null != messageExtraData) {

                                holder.viewBinding.ivUser.loadImage(
                                    context,
                                    messageExtraData.headPic2, avatarWidth, avatarHeight
                                )
                                holder.viewBinding.ivModel.loadImage(
                                    context, messageExtraData.headPic1, avatarWidth, avatarHeight
                                )


                                viewVisibility(
                                    View.VISIBLE,
                                    holder.viewBinding.llContainer,
                                    holder.viewBinding.tvMessage
                                )
                            } else {

                                viewVisibility(
                                    View.GONE,
                                    holder.viewBinding.llContainer,
                                    holder.viewBinding.tvMessage
                                )
                            }

                            holder.viewBinding.tvMessage.text =
                                "Your story began on ${Date(message.sentTime).dateFormat("MMM d,yyyy")}"

                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                    }
                }

            })
        addItemType(
            ITEM_MESSAGE_MATCH_PV,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageMatchPvBinding>(
                ItemMessageMatchPvBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageMatchPvBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {
                    item?.apply {

                        try {
                            val messageExtraData = getMessageExtraData(message)

                            if (null != messageExtraData) {

                                holder.viewBinding.tvName.text =
                                    "${messageExtraData.name2}'s Private Hub"

                            }

                            holder.viewBinding.scrollView.visibility = View.GONE

                            holder.viewBinding.ivTurnDown.setOnClickListener {

                                if (holder.viewBinding.scrollView.isVisible) {
                                    holder.viewBinding.ivTurnDown.setImageResource(R.drawable.iv_turn_down)
                                    holder.viewBinding.scrollView.visibility = View.GONE
                                } else {
                                    holder.viewBinding.ivTurnDown.setImageResource(R.drawable.iv_turn_up)
                                    holder.viewBinding.scrollView.visibility = View.VISIBLE
                                }

                            }


                            val mUserRelationData = mUserRelationData

                            holder.viewBinding.llTurn.removeAllViews()

                            if (null == mUserRelationData) {

                                viewVisibility(
                                    View.GONE,
                                    holder.viewBinding.sclTurn,
                                    holder.viewBinding.sivPhoto,
                                    holder.viewBinding.sivVideo,
                                    holder.viewBinding.systemPlayView,
                                )
                                holder.viewBinding.ivPhotoError.setImageResource(R.drawable.iv_photo_error)
                                holder.viewBinding.ivVideoError.setImageResource(R.drawable.iv_video_error)
                                holder.viewBinding.tvPhotoDesc.text = "Private Photo\nis empty"
                                holder.viewBinding.tvVideoDesc.text = "Private Video\nis empty"

                            } else {

                                mUserRelationData.unlockAlbums2?.forEach { mAlbums ->

                                    val status = if (mAlbums.albumStatus == "Sent") {
                                        0
                                    } else {
                                        if (mAlbums.albumStatus == "Unlock" && mAlbums.ttl <= 0) {
                                            2
                                        } else {
                                            1
                                        }

                                    }

                                    var time = mAlbums.ttl

                                    var textView = holder.viewBinding.tvPhotoTime

                                    var tvDesc = holder.viewBinding.tvPhotoDesc

                                    var imageView = holder.viewBinding.sivPhoto

                                    var tvDescText = "Private Photo\nhas expired"

                                    var ivErrorRes = R.drawable.iv_photo_error

                                    var errorImageView = holder.viewBinding.ivPhotoError

                                    var sllTime = holder.viewBinding.sllPhotoTime

                                    var isVideo = false

                                    if (mAlbums.albumType == "SYS_PRIVATE_PIC") {
                                        textView = holder.viewBinding.tvPhotoTime
                                        tvDesc = holder.viewBinding.tvPhotoDesc
                                        imageView = holder.viewBinding.sivPhoto
                                        errorImageView = holder.viewBinding.ivPhotoError
                                        tvDescText = "Private Photo"
                                        ivErrorRes = R.drawable.iv_photo_error
                                        sllTime = holder.viewBinding.sllPhotoTime

                                        isVideo = false

                                    } else {
                                        textView = holder.viewBinding.tvVideoTime
                                        tvDesc = holder.viewBinding.tvVideoDesc
                                        imageView = holder.viewBinding.sivVideo
                                        errorImageView = holder.viewBinding.ivVideoError
                                        tvDescText = "Private Video"
                                        ivErrorRes = R.drawable.iv_video_error
                                        sllTime = holder.viewBinding.sllVideoTime



                                        isVideo = true
                                    }

                                    loadImageByStatus(imageView, mAlbums.albumUrl, status)

                                    when (status) {

                                        0 -> {
                                            errorImageView.setImageResource(R.drawable.iv_devil)

                                            viewVisibility(
                                                View.VISIBLE,
                                                imageView,
                                                errorImageView,
                                                tvDesc
                                            )



                                            viewVisibility(View.GONE, sllTime)

                                            if (isVideo) {
                                                viewVisibility(
                                                    View.VISIBLE,
                                                    holder.viewBinding.systemPlayView,
                                                    holder.viewBinding.sivVideoCover
                                                )

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                                                        holder.viewBinding.blurView.setupWith(holder.viewBinding.systemTarget)
                                                            .setBlurRadius(20f)
                                                        holder.viewBinding.blurView.outlineProvider = object : ViewOutlineProvider() {
                                                            override fun getOutline(view: View, outline: Outline) {
                                                                outline.setRoundRect(0, 0, view.width, view.height, radius)
                                                            }
                                                        }
                                                        holder.viewBinding.blurView.setClipToOutline(true)

                                                    holder.viewBinding.sivVideoCover.loadOptionImage(
                                                        context,
                                                        mAlbums.albumUrl,requestOptions,
                                                        pictureWidth,
                                                        pictureHeight
                                                    )

                                                    viewVisibility(
                                                        View.INVISIBLE,
                                                        imageView,
                                                    )

                                                }else{

                                                    holder.viewBinding.sivVideoCover.loadImage(
                                                        context,
                                                        mAlbums.albumUrl,
                                                        pictureWidth,
                                                        pictureHeight
                                                    )

                                                    viewVisibility(
                                                        View.VISIBLE,
                                                        imageView,
                                                    )
                                                }
                                            }

                                            SpanUtils.with(tvDesc).append(tvDescText)
                                                .append("\nis filled")
                                                .setForegroundColor(context.getColor(R.color.color_76E664))
                                                .create()


                                        }

                                        1 -> {
                                            createSystemTimerJob(
                                                mAlbums.albumType,
                                                time,
                                                mAlbums,
                                                position,
                                                textView
                                            )

                                            holder.viewBinding.sivVideoCover.loadImage(
                                                context,
                                                mAlbums.albumUrl,
                                                pictureWidth,
                                                pictureHeight
                                            )


                                            viewVisibility(View.VISIBLE, sllTime)

                                            viewVisibility(View.GONE, tvDesc, errorImageView)

                                            if (isVideo) {
                                                viewVisibility(
                                                    View.VISIBLE,
                                                    holder.viewBinding.systemPlayView,
                                                    holder.viewBinding.sivVideoCover
                                                )
                                                viewVisibility(View.GONE, holder.viewBinding.blurView)
                                            } else {
                                                viewVisibility(
                                                    View.GONE,
                                                    holder.viewBinding.systemPlayView,
                                                    holder.viewBinding.sivVideoCover
                                                )
//                                                viewVisibility(View.VISIBLE,imageView)
                                            }
                                        }

                                        else -> {

                                            errorImageView.setImageResource(ivErrorRes)

                                            viewVisibility(View.VISIBLE, tvDesc, errorImageView)

                                            viewVisibility(View.GONE, imageView, sllTime)

                                            if (isVideo) {
                                                viewVisibility(
                                                    View.GONE,
                                                    holder.viewBinding.systemPlayView,
                                                    holder.viewBinding.sivVideoCover
                                                )
                                            }

                                            tvDesc.text = tvDescText + "\nhas expired"
                                        }
                                    }


                                }


                                mUserRelationData.turnOnsTags?.forEach {

                                    ItemChatTurnBinding.inflate(
                                        LayoutInflater.from(context),
                                        holder.viewBinding.llTurn,
                                        true
                                    )
                                        .apply {

                                            tvTitle.text = it
                                        }

                                }


                                val turnOnsTagsSize = mUserRelationData.turnOnsTags?.size ?: 0

                                holder.viewBinding.tvTurn.text =
                                    "$turnOnsTagsSize Turn-ons in Common"

                                holder.viewBinding.sclTurn.visibility =
                                    if (turnOnsTagsSize == 0) View.GONE else View.VISIBLE


                            }


                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                    }
                }

            })
        addItemType(
            ITEM_MESSAGE_LEFT,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageLeftBinding>(
                ItemMessageLeftBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageLeftBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                    item?.apply {

                        try {

                            val textMessage = item.message.content as TextMessage

                            holder.viewBinding.tvMessage.text = textMessage.content

                            val messageExtraData = getMessageExtraData(item.message)

                            if (null != messageExtraData) {
                                holder.viewBinding.ivAvatar.loadImage(
                                    context,
                                    messageExtraData.headPic2,
                                    avatarWidth,
                                    avatarHeight
                                )
                            }
                            showMessageTime(item, position, holder.viewBinding.llTime)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                }

            })

        addItemType(
            ITEM_MESSAGE_RIGHT,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageRightBinding>(
                ItemMessageRightBinding::inflate
            ) {

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageRightBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {
                    item?.apply {

                        try {

                            holder.viewBinding.tvErrorReason.visibility = View.GONE

                            if (item.isLocal) {
                                holder.viewBinding.tvMessage.text = item.text
                                if (item.status == 0) {
                                    holder.viewBinding.ivSendMessageLoad.visibility = View.VISIBLE
                                    holder.viewBinding.ivSendTextMessageError.visibility = View.GONE
                                } else {
                                    holder.viewBinding.tvErrorReason.text = errorReason
                                    holder.viewBinding.tvErrorReason.visibility = View.VISIBLE
                                    holder.viewBinding.ivSendMessageLoad.visibility = View.GONE
                                    holder.viewBinding.ivSendTextMessageError.visibility =
                                        View.VISIBLE
                                    holder.viewBinding.ivSendMessageLoad.stop()
                                }
                            } else {

                                val textMessage = item.message.content as TextMessage
                                holder.viewBinding.tvMessage.text = textMessage.content
                                viewVisibility(
                                    View.GONE,
                                    holder.viewBinding.ivSendTextMessageError,
                                    holder.viewBinding.ivSendMessageLoad
                                )
                                holder.viewBinding.ivSendMessageLoad.stop()
                            }

                            showMessageTime(item, position, holder.viewBinding.llTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                }

            })

        addItemType(
            ITEM_MESSAGE_LEFT_IMAGE,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageLeftImageBinding>(
                ItemMessageLeftImageBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageLeftImageBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                    item?.apply {

                        try {

                            val imageMessage = item.message.content as ImageMessage

                            val messageExtraData = getMessageExtraData(item.message)

                            if (null != messageExtraData) {

                                holder.viewBinding.ivAvatar.loadImage(
                                    context,
                                    messageExtraData.headPic2,
                                    avatarWidth,
                                    avatarHeight
                                )

                                if (messageExtraData.isPrivate == "True") {

                                    handlePrivateImage(
                                        item.message,
                                        position,
                                        holder.viewBinding.sllCount,
                                        holder.viewBinding.sllTime,
                                        holder.viewBinding.stvImage,
                                        holder.viewBinding.sllExpired,
                                        holder.viewBinding.tvTime
                                    )


                                } else {


                                    holder.viewBinding.stvImage.loadImage(
                                        context,
                                        imageMessage.remoteUri.toString(),
                                        pictureWidth,
                                        pictureHeight
                                    )
                                    viewVisibility(
                                        View.GONE,
                                        holder.viewBinding.sllExpired,
                                        holder.viewBinding.sllCount,
                                        holder.viewBinding.sllTime
                                    )
                                }
                            }

                            showMessageTime(item, position, holder.viewBinding.llTime)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                    }


                }

            })

        addItemType(
            ITEM_MESSAGE_RIGHT_IMAGE,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageRightImageBinding>(
                ItemMessageRightImageBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageRightImageBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {


                    item?.apply {

                        try {

                            if (item.isLocal) {

                                if (item.status == 0) {
                                    holder.viewBinding.ivSendMessageLoad.visibility = View.VISIBLE
                                    holder.viewBinding.ivSendMessageLoad.start()
                                    holder.viewBinding.ivSendMessageError.visibility = View.GONE
                                } else {
                                    holder.viewBinding.ivSendMessageLoad.visibility = View.GONE
                                    holder.viewBinding.ivSendMessageError.visibility = View.VISIBLE
                                    holder.viewBinding.ivSendMessageLoad.stop()
                                }

                                holder.viewBinding.stvImage.loadImage(
                                    context,
                                    item.uri,
                                    pictureWidth,
                                    pictureHeight
                                )
                                viewVisibility(
                                    View.GONE,
                                    holder.viewBinding.sllExpired,
                                    holder.viewBinding.sllCount,
                                    holder.viewBinding.sllTime
                                )
                            } else {

                                val imageMessage = item.message.content as ImageMessage

                                val messageExtraData = getMessageExtraData(item.message)

                                viewVisibility(
                                    View.GONE,
                                    holder.viewBinding.ivSendMessageError,
                                    holder.viewBinding.ivSendMessageLoad
                                )
                                holder.viewBinding.ivSendMessageLoad.stop()

                                if (null != messageExtraData) {

                                    if (messageExtraData.isPrivate == "True") {

                                        handlePrivateImage(
                                            item.message,
                                            position,
                                            holder.viewBinding.sllCount,
                                            holder.viewBinding.sllTime,
                                            holder.viewBinding.stvImage,
                                            holder.viewBinding.sllExpired,
                                            holder.viewBinding.tvTime
                                        )
                                    } else {

                                        val url = item.uri ?: imageMessage.remoteUri.toString()

                                        holder.viewBinding.stvImage.loadImage(
                                            context,
                                            url,
                                            pictureWidth,
                                            pictureHeight
                                        )
                                        viewVisibility(
                                            View.GONE,
                                            holder.viewBinding.sllExpired,
                                            holder.viewBinding.sllCount,
                                            holder.viewBinding.sllTime
                                        )
                                    }

                                }

                            }

                            showMessageTime(item, position, holder.viewBinding.llTime)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                    }

                }

            })


        addItemType(
            ITEM_MESSAGE_LEFT_VIDEO,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageLeftVideoBinding>(
                ItemMessageLeftVideoBinding::inflate
            ) { //


                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageLeftVideoBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                    item?.apply {

                        try {

                            val messageExtraData = getMessageExtraData(item.message)

                            if (null != messageExtraData) {

                                holder.viewBinding.ivAvatar.loadImage(
                                    context,
                                    messageExtraData.headPic2,
                                    avatarWidth,
                                    avatarHeight
                                )




                                if (messageExtraData.isPrivate == "True") {

                                    handlePrivateVideo(
                                        item.message,
                                        position,
                                        holder.viewBinding.sllCount,
                                        holder.viewBinding.sllTime,
                                        holder.viewBinding.stvImage,
                                        holder.viewBinding.sllExpired,
                                        holder.viewBinding.tvTime,
                                        holder.viewBinding.ivVideo,
                                        holder.viewBinding.blurView,
                                        holder.viewBinding.target,
                                        holder.viewBinding.stvImageCover,
                                        false,
                                    )


                                } else {

                                    holder.viewBinding.stvImage.loadImage(
                                        context,
                                        getVideoUrl(item.message),
                                        pictureWidth,
                                        pictureHeight
                                    )

                                    holder.viewBinding.ivVideo.visibility = View.VISIBLE
                                    viewVisibility(
                                        View.GONE,
                                        holder.viewBinding.sllExpired,
                                        holder.viewBinding.sllCount,
                                        holder.viewBinding.sllTime
                                    )
                                }
                            }

                            showMessageTime(item, position, holder.viewBinding.llTime)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                    }

                }
            })

        addItemType(
            ITEM_MESSAGE_RIGHT_VIDEO,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageRightVideoBinding>(
                ItemMessageRightVideoBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageRightVideoBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                    item?.apply {
                        try {

                            val messageExtraData = getMessageExtraData(item.message)

                            if (null != messageExtraData) {

                                if (messageExtraData.isPrivate == "True") {

                                    handlePrivateVideo(
                                        item.message,
                                        position,
                                        holder.viewBinding.sllCount,
                                        holder.viewBinding.sllTime,
                                        holder.viewBinding.stvImage,
                                        holder.viewBinding.sllExpired,
                                        holder.viewBinding.tvTime,
                                        holder.viewBinding.ivVideo, null, null, null,
                                        true
                                    )


                                } else {

                                    holder.viewBinding.stvImage.loadImage(
                                        context,
                                        getVideoUrl(item.message),
                                        pictureWidth,
                                        pictureHeight
                                    )

                                    holder.viewBinding.ivVideo.visibility = View.VISIBLE
                                    viewVisibility(
                                        View.GONE,
                                        holder.viewBinding.sllExpired,
                                        holder.viewBinding.sllCount,
                                        holder.viewBinding.sllTime
                                    )
                                }
                            }

                            showMessageTime(item, position, holder.viewBinding.llTime)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }

                    }
                }
            })




        addItemType(
            ITEM_MESSAGE_LEFT_IMAGE_OR_VIDEO_S,
            object :
                BaseMultiItemViewHolder<CustomMessage, ItemMessageLeftImageOrVideoBinding>(
                    ItemMessageLeftImageOrVideoBinding::inflate
                ) { //

                override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemMessageLeftImageOrVideoBinding>) {
                    super.onInitViewHolder(holder)

                    holder.viewBinding.gridImageView.initRecyclerView()

                    holder.viewBinding.gridImageView.onItemClick = { list, position ->

                        onGridItemClick(list, position, holder.bindingAdapterPosition)
                    }

                }

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageLeftImageOrVideoBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                    item?.apply {

                        Log.i("TAG", "onBind: sssssssssss=  \n ${item.toJson()}")


                        try {

                            val mPPVMessage = item.message.content as PPVMessage


                            val messagePPVData = getMessagePPVData(mPPVMessage)

                            val messageExtraData = getMessageExtraData(item.message)


                            if (null != messageExtraData) {

                                holder.viewBinding.ivAvatar.loadImage(
                                    context,
                                    messageExtraData.headPic2, avatarWidth, avatarHeight
                                )

                            }


                            if (!messagePPVData.isNullOrEmpty()) {

                                holder.viewBinding.gridImageView.setData(
                                    messagePPVData,
                                    item.message,
                                )


                            }
                            showMessageTime(item, position, holder.viewBinding.llTime)

                        } catch (e: Exception) {
                            e.printStackTrace()

                        }

                    }

                }


                override fun isFullSpanItem(itemType: Int): Boolean {
                    return true
                }

            })


        addItemType(
            ITEM_MESSAGE_RIGHT_IMAGE_OR_VIDEO_S,
            object :
                BaseMultiItemViewHolder<CustomMessage, ItemMessageRightImageOrVideoBinding>(
                    ItemMessageRightImageOrVideoBinding::inflate
                ) {


                override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemMessageRightImageOrVideoBinding>) {
                    super.onInitViewHolder(holder)

                    holder.viewBinding.gridImageView.initRecyclerView()

                    holder.viewBinding.gridImageView.onItemClick = { list, position ->

                        onGridItemClick(list, position, holder.bindingAdapterPosition)
                    }

                }

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageRightImageOrVideoBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {


                    item?.apply {


                        try {

                            val mPPVMessage = item.message.content as PPVMessage

                            val messageExtraData = getMessageExtraData(item.message)

                            val messagePPVData = getMessagePPVData(mPPVMessage)

                            if (null != messageExtraData) {

                                if (!messagePPVData.isNullOrEmpty()) {

                                    holder.viewBinding.gridImageView.setData(
                                        messagePPVData,
                                        item.message,
                                    )
                                }

                            }

                            showMessageTime(item, position, holder.viewBinding.llTime)

                        } catch (e: Exception) {
                            e.printStackTrace()

                        }

                    }


                }


            })







        addItemType(
            ITEM_MESSAGE_SYSTEM,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageSystemBinding>(
                ItemMessageSystemBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageSystemBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                }

            })

        addItemType(
            ITEM_MESSAGE_NTF_MSG,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageSystemBinding>(
                ItemMessageSystemBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageSystemBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                }

            })
        addItemType(
            ITEM_MESSAGE_LIMIT,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageLimitBinding>(
                ItemMessageLimitBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageLimitBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {

                }

            })
        addItemType(
            ITEM_MESSAGE_TURN,
            object : BaseMultiItemViewHolder<CustomMessage, ItemMessageTurnBinding>(
                ItemMessageTurnBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageTurnBinding>,
                    position: Int,
                    item: CustomMessage?
                ) {
                    holder.viewBinding.tv1.paint.isUnderlineText = true
                }

            })

        onItemViewType { position, list ->

            val message = list[position].message

            when (message.objectName) {

                AppConstant.RIMConstant.SYSTEM_NOTICE -> {
                    ITEM_MESSAGE_SYSTEM
                }

                AppConstant.RIMConstant.RC_TXT_MSG -> {

                    if (UserInfoHold.userId == message.senderUserId || null == message.senderUserId) {
                        ITEM_MESSAGE_RIGHT
                    } else {
                        ITEM_MESSAGE_LEFT
                    }

                }

                AppConstant.RIMConstant.RC_IMG_MSG -> {

                    if (UserInfoHold.userId == message.senderUserId) {
                        ITEM_MESSAGE_RIGHT_IMAGE
                    } else {
                        ITEM_MESSAGE_LEFT_IMAGE
                    }
                }

                AppConstant.RIMConstant.RC_IMG_VIDEO -> {


                    if (UserInfoHold.userId == message.senderUserId) {
                        ITEM_MESSAGE_RIGHT_VIDEO
                    } else {
                        ITEM_MESSAGE_LEFT_VIDEO
                    }
                }

                AppConstant.RIMConstant.RC_PP_VM_MSG -> {

                    if (UserInfoHold.userId == message.senderUserId) {
                        ITEM_MESSAGE_RIGHT_IMAGE_OR_VIDEO_S
                    } else {
                        ITEM_MESSAGE_LEFT_IMAGE_OR_VIDEO_S
                    }


                }

                AppConstant.RIMConstant.RC_NTF_MSG -> {

                    try {
                        val messageExtraData = getMessageExtraData(message)

                        if (null == messageExtraData) {
                            ITEM_MESSAGE_NTF_MSG
                        } else {
                            if (messageExtraData.eventCode == AppConstant.RIMConstant.CMD_MATCH_SUCCESS) {

                                if (UserInfoHold.isOrganic || UserInfoHold.isReview) {

                                    ITEM_MESSAGE_MATCH
                                } else {
                                    ITEM_MESSAGE_MATCH_PV
                                }


                            } else {
                                ITEM_MESSAGE_NTF_MSG
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        ITEM_MESSAGE_NTF_MSG
                    }
                }

                AppConstant.RIMConstant.RC_TURN_ONS_MSG -> {

                    ITEM_MESSAGE_TURN
                }

                AppConstant.RIMConstant.RC_LIMIT_MESSAGE_MSG -> {

                    ITEM_MESSAGE_LIMIT
                }

                else -> {

                    ITEM_MESSAGE_SYSTEM
                }

            }

        }
    }


    /**
     *
     * status 0 private 1 public 2 destroy
     */
    private fun loadImageByStatus(
        imageView: ImageView,
        url: String,
        status: Int,

    ) {

        when (status) {

            0 -> {

                imageView.visibility = View.VISIBLE
                imageView.loadOptionImage(
                    imageView.context,
                    url,
                    requestOptions,
                    pictureWidth,
                    pictureHeight
                )

            }

            1 -> {


                imageView.loadImage(
                    imageView.context,
                    url,
                    pictureWidth,
                    pictureHeight
                )


            }

            else -> {
                imageView.visibility = View.GONE
            }
        }
    }


    private fun handlePrivateImage(
        message: Message,
        position: Int,
        sllCount: ViewGroup,
        sllTime: ViewGroup,
        stvImage: ImageView,
        sllExpired: ViewGroup,
        tvTime: TextView
    ) {

        val imageMessage = message.content as ImageMessage

        if (null != message.expansion) {

            if (message.expansion["isLocked"] == "True") {

                stvImage.loadOptionImage(
                    context,
                    imageMessage.remoteUri.toString(),
                    requestOptions,
                    pictureWidth,
                    pictureHeight
                )
                sllExpired.visibility = View.GONE
                sllCount.visibility = View.VISIBLE
                sllTime.visibility = View.GONE
            } else {

                if (message.expansion["isDestroy"] == "True") {

                    sllTime.visibility = View.GONE
                    sllExpired.visibility = View.VISIBLE
                    sllCount.visibility = View.GONE

                } else {

                    stvImage.loadImage(
                        context,
                        imageMessage.remoteUri.toString(),
                        pictureWidth,
                        pictureHeight
                    )

                    sllExpired.visibility = View.GONE
                    sllTime.visibility = View.VISIBLE
                    sllCount.visibility = View.GONE

                    val unlockTimestamp = message.expansion["unlockTimestamp"]

                    val currentTime = enableTime - System.currentTimeMillis()

                    val lastTime = unlockTimestamp?.toLong()?.plus(currentTime) ?: -1

                    if (lastTime < 0) {
//                        val param = HashMap<String, String>()

                        val param = message.expansion as HashMap<String, String>

                        param["isDestroy"] = "True"
                        updateMessageExpansion(param, message, position)
                    } else {
                        createTimerJob(message, (lastTime / 1000).toInt(), position, tvTime)
                    }


                }


            }
        }
    }


    private fun handlePrivateVideo(
        message: Message,
        position: Int,
        sllCount: ViewGroup,
        sllTime: ViewGroup,
        stvImage: ImageView,
        sllExpired: ViewGroup,
        tvTime: TextView,
        ivVideo: ImageView,
        blurView: BlurView?,
        targetView: BlurTarget?,
        ivCover: ImageView?,
        isMe: Boolean,

        ) {

        if (null != message.expansion) {

            if (message.expansion["isLocked"] == "True") {

                if (isMe){
                    stvImage.loadOptionImage(
                        context,
                        getVideoUrl(message),
                        requestOptions,
                        pictureWidth,
                        pictureHeight
                    )
                    viewVisibility(View.VISIBLE, stvImage)

                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                        if (null != blurView && null != targetView) {
                            blurView.setupWith(targetView)
                                .setBlurRadius(20f)
                            blurView.outlineProvider = object : ViewOutlineProvider() {
                                override fun getOutline(view: View, outline: Outline) {
                                    outline.setRoundRect(0, 0, view.width, view.height, radius)
                                }
                            }
                            blurView.setClipToOutline(true)
                            viewVisibility(View.VISIBLE, blurView, targetView)
                        }


                        ivCover?.let {
                            ivCover.loadOptionImage(
                                context,
                                getVideoUrl(message),
                                requestOptions,
                                pictureWidth,
                                pictureHeight
                            )

                            viewVisibility(View.INVISIBLE, stvImage)
                            viewVisibility(View.VISIBLE, ivCover)
                        }

                    } else {


                        stvImage.loadOptionImage(
                            context,
                            getVideoUrl(message),
                            requestOptions,
                            pictureWidth,
                            pictureHeight
                        )
                        viewVisibility(View.VISIBLE, stvImage)
                    }
                }


                viewVisibility(View.VISIBLE, sllCount, ivVideo)
                viewVisibility(View.GONE, sllExpired, sllTime)

            } else {

                if (message.expansion["isDestroy"] == "True") {

                    viewVisibility(View.VISIBLE, sllExpired)
                    viewVisibility(View.GONE, sllCount, ivVideo, sllTime)

                } else {

                    if (isMe) {
                        stvImage.loadImage(
                            context,
                            getVideoUrl(message),
                            pictureWidth,
                            pictureHeight
                        )
                        viewVisibility(View.VISIBLE, stvImage)
                    } else {

                        ivCover?.loadImage(
                            context,
                            getVideoUrl(message),
                            pictureWidth,
                            pictureHeight
                        )
                        viewVisibility(View.INVISIBLE, stvImage)
                    }


                    viewVisibility(View.VISIBLE, sllTime, ivVideo)
                    viewVisibility(View.GONE, sllExpired, sllCount)

                    if (null != blurView && null != targetView) {
                        viewVisibility(View.GONE, blurView)
                    }

                    val unlockTimestamp = message.expansion["unlockTimestamp"]

                    val currentTime = enableTime - System.currentTimeMillis()

                    val lastTime = unlockTimestamp?.toLong()?.plus(currentTime) ?: -1

                    if (lastTime < 0) {
                        val param = HashMap<String, String>()
                        param["isDestroy"] = "True"
                        updateMessageExpansion(param, message, position)
                    } else {
                        createTimerJob(message, (lastTime / 1000).toInt(), position, tvTime)
                    }

                }

            }
        }
    }


    private fun getVideoUrl(message: Message): String? {

        val mVideoMessage = message.content as VideoMessage

        val messageMediaData = getMessageMediaData(mVideoMessage)

        return messageMediaData?.cover

    }


    private fun createTimerJob(
        message: Message,
        countTime: Int,
        position: Int,
        tvTime: TextView?,
    ) {

        if (jobMap.containsKey(message.uId)) return


        var time = 0

        val job = scope.launch(Dispatchers.Main) {

            repeat(countTime) {
                time++

                if (time >= countTime) {

                    val param = HashMap<String, String>().apply {
                        this["isDestroy"] = "True"
                    }

                    updateMessageExpansion(param, message, position)

                }

                val timeSecond = getTimeSecond(countTime - time)


//                mGridImageView?.setTime(timeSecond)

                tvTime?.text = timeSecond

                delay(1000)
            }
        }
        jobMap[message.uId] = job
    }

    private fun createSystemTimerJob(
        key: String,
        countTime: Int,
        mUnlockAlbums: UnlockAlbums,
        position: Int,
        tvTime: TextView?,
    ) {

        if (jobMap.containsKey(key)) return


        var time = 0

        val job = scope.launch(Dispatchers.Main) {

            repeat(countTime) {
                time++

                if (time >= countTime) {

                    mUnlockAlbums.albumStatus = "Unlock"

                    mUnlockAlbums.ttl = 0

                    notifyItemChanged(position, false)
                } else {
                    mUnlockAlbums.ttl = mUnlockAlbums.ttl - 1
                }

                val timeSecond = getTimeSecond(countTime - time)

                tvTime?.text = timeSecond

                delay(1000)
            }
        }
        jobMap[key] = job
    }


    private fun showMessageTime(
        message: CustomMessage?,
        position: Int,
        mBinding: ItemMessageTimeBinding
    ) {

        if (null == message) {

            return
        }

        val currentTime = Date(message.message.sentTime)

        val formattedTime = getFormatMessageTime(currentTime)

        if (position == 0) {
            mBinding.root.visibility = View.VISIBLE
        } else {
            val previousItem = getItem(position - 1)
            previousItem?.let {
                val previousTime = Date(previousItem.message.sentTime)

                val diffInMinutes = (currentTime.time - previousTime.time)

                if (diffInMinutes >= maxIntervalTime) {
                    mBinding.root.visibility = View.VISIBLE
                } else {
                    mBinding.root.visibility = View.GONE
                }
            }
        }

        mBinding.tvMessageTime.text = formattedTime

    }

    private fun updateMessageExpansion(
        param: HashMap<String, String>,
        message: Message,
        position: Int
    ) {

        updateMessageExpansion(param, message.uId, onSuccess = {
            scope.launch(Dispatchers.Main) {
                message.setExpansion(param)
                notifyItemChanged(position, false)
            }
        })
    }

}