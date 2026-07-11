package com.chat.jolt.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.databinding.DialogMatchSuccessBinding
import com.chat.jolt.databinding.ItemMatchTextBinding
import com.chat.jolt.viewmodel.ChatViewModel
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.im.RIMClient.PUSH_CONTENT
import com.chat.lib_common.im.RIMClient.PUSH_TITLE
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.toJson
import com.google.android.material.imageview.ShapeableImageView
import com.youth.banner.adapter.BannerAdapter
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.MessageContent
import io.rong.imlib.model.MessagePushConfig
import io.rong.message.TextMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.TimeZone

class MatchSuccessDialog :
    BaseDialog<DialogMatchSuccessBinding>(DialogMatchSuccessBinding::inflate) {

    private val mViewModel: ChatViewModel by activityViewModels()

    private var mUserInfoData: UserInfoData? = null

    private var mCustomMessageExtraData: CustomMessageExtraData? = null

    private lateinit var mBannerAdapter: BannerAdapter<String, RecyclerView.ViewHolder>

    private lateinit var mAdapter: BaseRecyclerAdapter<String, ItemMatchTextBinding>

    private lateinit var mAdapter2: BaseRecyclerAdapter<String, ItemMatchTextBinding>

    private val width = 30f.dip2px(BaseApplication.mApplication)

    private val mIndicatorWidth = 20f.dip2px(BaseApplication.mApplication)



    companion object {
        fun newInstance(
            data: UserInfoData,
            mCustomMessageExtraData: CustomMessageExtraData
        ): MatchSuccessDialog {
            return MatchSuccessDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.EXTRA_DATA, mCustomMessageExtraData.toJson())
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            llClose.edgeToEdgeTop()

            root.edgeToEdgeBottom()

            flViewContainer.edgeToEdgeBottom()


            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

                flContainer.updatePadding(
                    bottom = imeHeight.takeIf { it > 0 } ?: navHeight
                )

                if (imeHeight > 100){
                    ivMatch.visibility = View.GONE
                }else{
                    ivMatch.visibility = View.VISIBLE
                }

                insets
            }

            initRecyclerView()

            initRecyclerView2()

            ivClose.click {

                dismissAllowingStateLoss()
            }

            tv1.setOnClickListener {
                setText.append(tv1.text)
                setText.setSelection(setText.text.toString().length)
            }
            tv2.setOnClickListener {
                setText.append(tv2.text)
                setText.setSelection(setText.text.toString().length)
            }
            tv3.setOnClickListener {
                setText.append(tv3.text)
                setText.setSelection(setText.text.toString().length)
            }
            tv4.setOnClickListener {
                setText.append(tv4.text)
                setText.setSelection(setText.text.toString().length)
            }
            tv5.setOnClickListener {
                setText.append(tv5.text)
                setText.setSelection(setText.text.toString().length)
            }

            ivSend.click {

                val text = setText.text.toString().ifBlank { setText.hint.toString() }

                sendTextMessage(text)

                dismissAllowingStateLoss()
            }

        }

    }

    override fun initData() {

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")

            val extraData = it.getString(AppConstant.Constant.EXTRA_DATA, "")

            if (!extraData.isNullOrEmpty()) {

                mCustomMessageExtraData = extraData.fromJson()

                mDialogBinding.setText.setHint(mCustomMessageExtraData?.firstChat)
            }

            if (!data.isNullOrEmpty()) {

                mUserInfoData = data.fromJson()

                withViewBinding {

                    mUserInfoData?.let {
                        sivUser.loadImage(requireContext(), it.headPic, width, width)

                        tvOnline.text = if (it.onlineStatus == "Online") "Online" else "Active"

                        tvName.text = "${it.nickname},${it.age}"

                        initBanner(it.coverPics)
                    }

                }

            }

        }

    }


    private fun initBanner(list: List<String>? = mutableListOf()) {

        if (list.isNullOrEmpty()) return

        withViewBinding {

            mBannerAdapter = object :
                BannerAdapter<String, RecyclerView.ViewHolder>(list) {
                override fun onCreateHolder(
                    parent: ViewGroup?,
                    viewType: Int
                ): RecyclerView.ViewHolder {

                    val imageView = ShapeableImageView(parent!!.context)
                    imageView.setLayoutParams(
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)


                    return object : RecyclerView.ViewHolder(imageView) {

                    }
                }

                override fun onBindView(
                    holder: RecyclerView.ViewHolder?,
                    data: String,
                    position: Int,
                    size: Int
                ) {
                    holder?.let {
                        (it.itemView as ShapeableImageView).loadImage(it.itemView.context, data)
                    }
                }

            }

            banner.setAdapter(mBannerAdapter).addBannerLifecycleObserver(requireActivity())
                .isAutoLoop(true)
                .setIndicator(mRectangleIndicator, false)
                .setIndicatorSelectedColor(getColor(R.color.color_EAA82B))
                .setIndicatorNormalColor(getColor(R.color.white_30))
                .setIndicatorRadius(5f.dip2px(requireContext()))
                .setIndicatorHeight(6f.dip2px(requireContext()))
                .setIndicatorWidth(
                    mIndicatorWidth,
                    mIndicatorWidth
                )


        }

        reportEvent()

    }


    private fun initRecyclerView() {


        val list = mutableListOf(
            getString(R.string.say_hi_1),
            getString(R.string.say_hi_2),
            getString(R.string.say_hi_3),
            getString(R.string.say_hi_4),
            getString(R.string.say_hi_5),
            getString(R.string.say_hi_6),

            )

        val data = List(3) { list }.flatten()

        withViewBinding {

            mAdapter = object :
                BaseRecyclerAdapter<String, ItemMatchTextBinding>(ItemMatchTextBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemMatchTextBinding>,
                    itemView: ItemMatchTextBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item
                }


            }

            recyclerView.adapter = mAdapter

            val linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            recyclerView.layoutManager =
                linearLayoutManager
            mAdapter.setOnDebouncedItemClick { _, _, position ->

                val item = mAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                sendTextMessage(item)

                dismissAllowingStateLoss()
            }

            mAdapter.submitList(data)

            recyclerView.post {
                val middle = mAdapter.itemCount / 2
                linearLayoutManager.scrollToPositionWithOffset(middle, 0)
            }


            lifecycleScope.launch {

                while (isActive) {
                    recyclerView.scrollBy(-2, 0)
                    val first = linearLayoutManager.findFirstVisibleItemPosition()
                    if (first != RecyclerView.NO_POSITION) {
                        val originSize = mAdapter.itemCount / 3
                        val total = mAdapter.itemCount
                        if (first < originSize) {
                            val center = first + originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                        if (first >= originSize * 2) {
                            val center = first - originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                    }
                    delay(16)
                }
            }


            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                    val first = linearLayoutManager.findFirstVisibleItemPosition()

                    val originSize = list.size

                    if (originSize == 0) return

                    if (first < originSize) {
                        val center = first + originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                    else if (first >= originSize * 2) {
                        val center = first - originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                }
            })


        }

    }

    private fun initRecyclerView2() {


        val list2 = mutableListOf(
            getString(R.string.say_hi_7),
            getString(R.string.say_hi_8),
            getString(R.string.say_hi_9),
            getString(R.string.say_hi_10),
            getString(R.string.say_hi_11),
            getString(R.string.say_hi_12)
        )

        val data = List(3) { list2 }.flatten()

        withViewBinding {

            mAdapter2 = object :
                BaseRecyclerAdapter<String, ItemMatchTextBinding>(ItemMatchTextBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemMatchTextBinding>,
                    itemView: ItemMatchTextBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item
                }


            }

            recyclerView2.adapter = mAdapter2

            val linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            recyclerView2.layoutManager =
                linearLayoutManager


            mAdapter2.setOnItemClickListener { _, _, position ->

                val item = mAdapter2.getItem(position) ?: return@setOnItemClickListener

                sendTextMessage(item)

                dismissAllowingStateLoss()
            }

            mAdapter2.submitList(data)

            recyclerView2.post {
                val middle = mAdapter2.itemCount / 2
                linearLayoutManager.scrollToPositionWithOffset(middle, 0)
            }


            lifecycleScope.launch {

                while (isActive) {
                    recyclerView2.scrollBy(2, 0)
                    val first = linearLayoutManager.findFirstVisibleItemPosition()
                    if (first != RecyclerView.NO_POSITION) {
                        val originSize = mAdapter2.itemCount / 3
                        if (first < originSize) {
                            val center = first + originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                        if (first >= originSize * 2) {
                            val center = first - originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                    }
                    delay(16)

                }
            }


            recyclerView2.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                    val first = linearLayoutManager.findFirstVisibleItemPosition()

                    val originSize = list2.size

                    if (originSize == 0) return

                    if (first < originSize) {
                        val center = first + originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                    else if (first >= originSize * 2) {
                        val center = first - originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                }
            })



        }

    }


    private fun sendTextMessage(
        message: String,
    ) {

        if (message.isBlank()) {
            return
        }

        if (null == mCustomMessageExtraData) return

        val targetId = mCustomMessageExtraData!!.groupId

        if (targetId.isNullOrEmpty()) return


        mViewModel.sendMatchMsg(
            targetId,
            message,
            AppConstant.RIMConstant.RC_SEND_TEXT_MSG
        ) {
            val messageContent = TextMessage.obtain(message)

            val pushMapData = mutableMapOf<String, String>()

            pushMapData["touchType"] = "Chat"

            pushMapData["touchValue"] = targetId

            val message = buildMessage(mCustomMessageExtraData, messageContent, pushMapData.toJson()) ?: return@sendMatchMsg

            RIMClient.sendMessage(message, pushMapData.toJson(), onSuccess = {

                FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_MATCH_MESSAGE_ITEM)
                    .postValue(it)
            })
        }
    }


    private fun buildMessage(
        mCustomMessageExtraData: CustomMessageExtraData?,
        messageContent: MessageContent,
        pushData: String
    ): Message? {

        if (null == mCustomMessageExtraData) return null

        val message = Message.obtain(
            mCustomMessageExtraData.groupId,
            Conversation.ConversationType.GROUP,
            messageContent
        )
        message.messagePushConfig = MessagePushConfig.Builder()
            .setPushTitle(PUSH_TITLE)
            .setPushContent(PUSH_CONTENT)
            .setPushData(pushData)
            .build()

        messageContent.extra = CustomMessageExtraData().apply {
            name1 = mCustomMessageExtraData.name1 ?: ""
            headPic1 = mCustomMessageExtraData.headPic1 ?: ""
            userId1 = mCustomMessageExtraData.userId1 ?: ""
            name2 = mCustomMessageExtraData.name2 ?: ""
            headPic2 = mCustomMessageExtraData.headPic2 ?: ""
            userId2 = mCustomMessageExtraData.userId2 ?: ""
            source = mCustomMessageExtraData.source ?: ""
            tzId1 = TimeZone.getDefault().id
        }.toJson()

        message.isCanIncludeExpansion = true

        message.setExpansion(HashMap<String, String>().apply {
            this["isLocked"] = "True"
            this["unlockTimestamp"] = "0"
            this["isDestroy"] = "False"
            this["isNewConversation"] = "True"
        })

        Log.i(TAG, "buildMessage: ${message.toJson()}")

        return message
    }

    private fun reportEvent(){

        val params = mutableMapOf<String, Any?>()

        params["m_type"] = "full screen"

        reportEvent(mPopPopupDialogKey[4], params)
    }



    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

}