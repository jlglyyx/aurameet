package com.chat.jolt.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.chat.jolt.R
import com.chat.jolt.data.PreViewMediaData
import com.chat.jolt.databinding.DialogPreviewMediaBinding
import com.chat.jolt.databinding.ItemImageSelectMediaBinding
import com.chat.jolt.databinding.ItemPreviewMediaBinding
import com.chat.jolt.manager.ScopedVideoManager
import com.chat.jolt.widget.CustomVideoPlay
import com.chat.jolt.widget.GridImageView
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getRealUrl
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.isVideo
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.toJson
import com.chat.lib_common.util.viewVisibility
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class PreviewMediaDialog :
    BaseDialog<DialogPreviewMediaBinding>(DialogPreviewMediaBinding::inflate) {

    private var data: MutableList<PreViewMediaData> = mutableListOf()

    private var position: Int = 0

    private var currentPosition: Int = 0

    private var lastRecyclerPosition = -1

    val width: Int = getScreenPx(BaseApplication.mApplication)[0] / 2

    val height: Int = getScreenPx(BaseApplication.mApplication)[1] / 2

    val recyclerViewHeight: Int = 95f.dip2px(BaseApplication.mApplication)

    private lateinit var mImageViewPagerAdapter: BaseRecyclerAdapter<PreViewMediaData, ItemPreviewMediaBinding>

    private lateinit var mImageRecyclerViewAdapter: BaseRecyclerAdapter<PreViewMediaData, ItemImageSelectMediaBinding>


    private var mScopedVideoManager: ScopedVideoManager? = null

    private var isMe = false

    private var isPrivate = true

    private var isVideo = false

    private var isHide = false

    private var top = 0

    companion object {
        fun newInstance(
            data: MutableList<PreViewMediaData>,
            position: Int,
            isMe: Boolean,
            isPrivate: Boolean
        ): PreviewMediaDialog {
            return PreviewMediaDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                    putInt(AppConstant.Constant.POSITION, position)
                    putBoolean(AppConstant.Constant.IS_ME, isMe)
                    putBoolean(AppConstant.Constant.IS_PRIVATE, isPrivate)
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {


            ViewCompat.setOnApplyWindowInsetsListener(appToolBar) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                top = systemBars.top
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }

            root.edgeToEdgeBottom()

            ivBack.setOnClickListener {

                dismissAllowingStateLoss()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.navigationBarColor = Color.BLACK
            it.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun initData() {

        arguments?.let {
            data = it.getString(AppConstant.Constant.DATA)?.formatListJson() ?: mutableListOf()
            position = it.getInt(AppConstant.Constant.POSITION)
            isMe = it.getBoolean(AppConstant.Constant.IS_ME, isMe)
            isPrivate = it.getBoolean(AppConstant.Constant.IS_PRIVATE, isPrivate)
        }

        isVideo = data[0].url.isVideo()

        mScopedVideoManager = ScopedVideoManager()

        currentPosition = position

        lastRecyclerPosition = position

        initRecyclerView()

        initViewPager()

        setTitle(position)

        initTimer()
    }


    private fun initViewPager() {

        val mRecyclerView = mDialogBinding.viewPager.getChildAt(0) as RecyclerView

        mRecyclerView.itemAnimator = null

        mRecyclerView.setItemViewCacheSize(0)


        mDialogBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(mPosition: Int) {
                super.onPageSelected(mPosition)

                val item = mImageViewPagerAdapter.getItem(mPosition) ?: return

                currentPosition = mPosition

                setCurrentPage(data[mPosition])

                if (item.url.isVideo()) {
                    mRecyclerView.post {
                        val findViewByPosition =
                            mRecyclerView.layoutManager?.findViewByPosition(mPosition)

                        findViewByPosition?.let {
                            val mPlayerView =
                                findViewByPosition.findViewById<CustomVideoPlay>(R.id.play_view)
                                    ?: return@post
                            autoPlayCurrent(item.url, mPlayerView)

                        }

                    }
                }

                setTitle(mPosition)

            }

        })


        mImageViewPagerAdapter = object :
            BaseRecyclerAdapter<PreViewMediaData, ItemPreviewMediaBinding>(ItemPreviewMediaBinding::inflate) {


            override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemPreviewMediaBinding>) {
                super.onInitViewHolder(holder)

                holder.binding.playView.mBinding.llMenu.updatePaddingRelative(bottom = recyclerViewHeight)


                holder.binding.ivImage.setOnClickListener {

                    setPreviewStatus()
                }

                if (isVideo) {

                    holder.binding.playView.needClickPlay = false

                    holder.binding.playView.onOtherClick = { v1, v2 ->

                        setPreviewStatus(v1, v2)
                    }
                }


            }

            @OptIn(UnstableApi::class)
            override fun convert(
                holder: BaseRecyclerViewHolder<ItemPreviewMediaBinding>,
                itemView: ItemPreviewMediaBinding,
                item: PreViewMediaData,
                position: Int
            ) {
                try {

                    itemView.flTime.updatePadding(top = top)

                    when (item.status) {

                        GridImageView.LOCK_STATUS -> {

                            if (item.url.isVideo()) {
                                viewVisibility(
                                    View.GONE,
                                    itemView.stvTime,
                                    itemView.llDestroyContainer,
                                    itemView.ivImage
                                )

                                viewVisibility(
                                    View.VISIBLE,
                                    itemView.playView
                                )
                                itemView.playView.loadCover(item.cover)


                            } else {
                                viewVisibility(
                                    View.GONE,
                                    itemView.llDestroyContainer,
                                    itemView.stvTime,
                                    itemView.playView
                                )
                                viewVisibility(View.VISIBLE, itemView.ivImage)
                                itemView.ivImage.loadImage(
                                    itemView.ivImage.context,
                                    item.url,
                                    width,
                                    height
                                )
                            }
                        }

                        GridImageView.DESTROY_STATUS -> {

                            viewVisibility(
                                View.GONE,
                                itemView.stvTime,
                                itemView.ivImage,
                                itemView.playView
                            )

                            viewVisibility(
                                View.VISIBLE,
                                itemView.llDestroyContainer
                            )

                        }

                        else -> {
                            if (item.time > 0) {


                                if (item.url.isVideo()) {
                                    viewVisibility(
                                        View.GONE,
                                        itemView.llDestroyContainer,
                                        itemView.ivImage
                                    )

                                    viewVisibility(
                                        View.VISIBLE,
                                        itemView.stvTime,
                                        itemView.playView
                                    )
                                    itemView.playView.loadCover(item.cover)
                                } else {
                                    viewVisibility(
                                        View.GONE,
                                        itemView.llDestroyContainer,
                                        itemView.stvTime,
                                        itemView.playView
                                    )
                                    viewVisibility(
                                        View.VISIBLE,
                                        itemView.stvTime,
                                        itemView.ivImage
                                    )
                                    itemView.ivImage.loadImage(
                                        itemView.ivImage.context,
                                        item.url,
                                        width,
                                        height
                                    )
                                }

                                itemView.stvTime.text = "${getTimeSecond(item.time)}"
                            } else {
                                viewVisibility(View.VISIBLE, itemView.llDestroyContainer)

                                viewVisibility(
                                    View.GONE,
                                    itemView.stvTime,
                                    itemView.playView,
                                    itemView.ivImage
                                )
                            }


                        }

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        mDialogBinding.viewPager.adapter = mImageViewPagerAdapter

        mImageViewPagerAdapter.submitList(data)

        mDialogBinding.viewPager.offscreenPageLimit = 1

        mDialogBinding.viewPager[0].overScrollMode = View.OVER_SCROLL_NEVER

        mDialogBinding.viewPager.setCurrentItem(position, false)

        mDialogBinding.recyclerView.scrollToPosition(position)

    }

    private fun initRecyclerView() {

        mImageRecyclerViewAdapter = object :
            BaseRecyclerAdapter<PreViewMediaData, ItemImageSelectMediaBinding>(
                ItemImageSelectMediaBinding::inflate
            ) {

            override fun convert(
                holder: BaseRecyclerViewHolder<ItemImageSelectMediaBinding>,
                itemView: ItemImageSelectMediaBinding,
                item: PreViewMediaData,
                position: Int
            ) {

                val url = if (item.url.isVideo()) item.cover else item.url


                viewVisibility(View.GONE, itemView.llDestroyContainer)

                viewVisibility(
                    View.VISIBLE,
                    itemView.ivImage
                )

                when (item.status) {

                    GridImageView.LOCK_STATUS -> {

                        itemView.ivImage.loadImage(
                            context,
                            url,
                            recyclerViewHeight,
                            recyclerViewHeight
                        )
                    }

                    else -> {
                        if (item.time > 0) {

                            itemView.ivImage.loadImage(
                                context,
                                url,
                                recyclerViewHeight,
                                recyclerViewHeight
                            )

                        } else {
                            viewVisibility(View.VISIBLE, itemView.llDestroyContainer)

                            viewVisibility(
                                View.INVISIBLE,
                                itemView.ivImage
                            )
                        }

                    }
                }

                if (item.isCheck) {


                    itemView.sclContainer.shapeDrawableBuilder.setStrokeColor(context.getColor(R.color.white))
                        .intoBackground()
                } else {

                    itemView.sclContainer.shapeDrawableBuilder.setStrokeColor(context.getColor(R.color.transparent))
                        .intoBackground()

                }
            }

        }
        mDialogBinding.recyclerView.adapter = mImageRecyclerViewAdapter

        mImageRecyclerViewAdapter.setOnItemClickListener { _, _, position ->

            val item = mImageRecyclerViewAdapter.getItem(position)

            item?.let {

                setCurrentPage(it)

                val index = data.indexOfLast { index -> index.id == it.id }

                if (index != -1) {
                    mDialogBinding.viewPager.setCurrentItem(index, false)
                }

            }

        }

        mImageRecyclerViewAdapter.submitList(data)

    }

    private fun initTimer() {

        lifecycleScope.launch {

            try {

                val mRecyclerView = mDialogBinding.viewPager.getChildAt(0) as RecyclerView

                while (isActive) {

                    val cancel = data.all { it.time <= 0 }

                    if (cancel) {

                        mScopedVideoManager?.release()

                        this.cancel()
                    }

                    data.forEachIndexed { index, it ->

                        if (it.status == GridImageView.NORMAL_STATUS && it.time > 0) {

                            it.time = it.time - 1

                            if (it.time <= 0){
                                mImageViewPagerAdapter.notifyItemChanged(index, false)
                            }else{

                                val findViewByPosition =
                                    mRecyclerView.layoutManager?.findViewByPosition(index)

                                if (null != findViewByPosition){

                                    val stvTime = findViewByPosition.findViewById<ShapeTextView>(R.id.stv_time)

                                    if (null != stvTime){
                                        stvTime.text = "${getTimeSecond(it.time)}"
                                    }

                                }

                            }

                            mImageRecyclerViewAdapter.notifyItemChanged(index, false)


                        }
                    }

                    delay(1000)
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }

        }

    }


    private fun setCurrentPage(currentItem: PreViewMediaData) {

        val index =
            mImageRecyclerViewAdapter.items.indexOfLast { it.id == currentItem.id }

        if (index == -1) {

            return
        }

        if (lastRecyclerPosition != -1) {

            val lastItem = mImageRecyclerViewAdapter.getItem(lastRecyclerPosition)

            if (null != lastItem) {

                lastItem.isCheck = false

                mImageRecyclerViewAdapter.notifyItemChanged(lastRecyclerPosition, false)
            }

            lastRecyclerPosition = -1
        }

        val item = mImageRecyclerViewAdapter.getItem(index)

        item?.let {

            item.isCheck = true

            lastRecyclerPosition = index

            mImageRecyclerViewAdapter.notifyItemChanged(index, false)

            mDialogBinding.recyclerView.scrollToPosition(index)
        }

    }


    private fun setPreviewStatus(v1: View? = null, v2: View? = null) {
        try {
        withViewBinding {

            isHide = !isHide

            if (isHide) {
                viewVisibility(View.GONE, sllContainer, tvCount)
                if (null != v1 && null != v2) {
                    viewVisibility(View.GONE, v1, v2)
                }

            } else {

                viewVisibility(View.VISIBLE, sllContainer, tvCount)

                if (null != v1 && null != v2) {
                    viewVisibility(View.VISIBLE, v1)
                }
            }
        }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }


    fun autoPlayCurrent(url: String, mCustomVideoPlay: CustomVideoPlay) {

        val url = getRealUrl(url).toString()

        if (url.isEmpty() || !url.isVideo()) {

            return
        }

        mScopedVideoManager?.play(mCustomVideoPlay, url)


    }


    private fun setTitle(position: Int) {

        if (isPrivate) {
            if (isVideo) {

                mDialogBinding.tvCount.text = "Private Videos (${position + 1}/${data.size})"
            } else {
                mDialogBinding.tvCount.text = "Private Photos (${position + 1}/${data.size})"
            }
        } else {
            mDialogBinding.tvCount.text = "${position + 1}/${data.size}"
        }


    }

    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun onDismiss(dialog: DialogInterface) {

        mScopedVideoManager?.release()

        mScopedVideoManager = null

        super.onDismiss(dialog)
    }

}