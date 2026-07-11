package com.chat.jolt.dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chat.jolt.R
import com.chat.jolt.data.UnlockAlbums
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.DialogPreviewCrazyBinding
import com.chat.jolt.databinding.ItemPreviewCrazyBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.manager.ScopedVideoManager
import com.chat.jolt.viewmodel.PublicViewModel
import com.chat.jolt.widget.CustomVideoPlay
import com.chat.jolt.widget.GridImageView
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.getRealUrl
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.getTimeSecond
import com.chat.lib_common.util.isVideo
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.loadOptionImage
import com.chat.lib_common.util.toJson
import com.chat.lib_common.util.viewVisibility
import com.chat.lib_common.widget.BlurTransformation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class PreviewCrazyDialog :
    BaseDialog<DialogPreviewCrazyBinding>(DialogPreviewCrazyBinding::inflate) {

    private val mViewModel by sharedViewModels<PublicViewModel>()

    private var data: MutableList<UnlockAlbums> = mutableListOf()

    private var mUserInfoData: UserInfoData? = null

    private var position: Int = 0

    private var currentPosition: Int = 0

    val width: Int = getScreenPx(BaseApplication.mApplication)[0] / 2

    val height: Int = getScreenPx(BaseApplication.mApplication)[1] / 2

    private lateinit var mImageViewPagerAdapter: BaseRecyclerAdapter<UnlockAlbums, ItemPreviewCrazyBinding>


    private var showFlash = false

    private val requestOptions =
        RequestOptions.bitmapTransform(BlurTransformation(AppConstant.Constant.PPV_BLUR_RADIUS, 2))

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mBuyRightDialog: BuyRightDialog? = null

    var onFlushChat = {}

    var onUnlock: (UnlockAlbums) -> Unit = {}

    private var mScopedVideoManager: ScopedVideoManager? = null

    private var top = 0


    companion object {
        fun newInstance(
            data: MutableList<UnlockAlbums>,
            position: Int,
            mUserInfoData: UserInfoData, showFlash: Boolean = false
        ): PreviewCrazyDialog {
            return PreviewCrazyDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                    putString(AppConstant.Constant.USER_INFO, mUserInfoData.toJson())
                    putInt(AppConstant.Constant.POSITION, position)
                    putBoolean(AppConstant.Constant.SHOW_FLASH, showFlash)
                }
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
            mUserInfoData = it.getString(AppConstant.Constant.USER_INFO)?.fromJson()
            position = it.getInt(AppConstant.Constant.POSITION)
            showFlash = it.getBoolean(AppConstant.Constant.SHOW_FLASH, showFlash)
        }

        mScopedVideoManager = ScopedVideoManager()

        initViewPager()


        if (showFlash) {
            mDialogBinding.ivCrazyFlushChat.visibility = View.VISIBLE
        } else {
            mDialogBinding.ivCrazyFlushChat.visibility = View.GONE
        }

        mDialogBinding.tvCount.text = "${position + 1}/${data.size}"

        mDialogBinding.viewPager.setCurrentItem(position, false)

        currentPosition = position

        initUserInfo()

        initTimer()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {


        withViewBinding {


            ViewCompat.setOnApplyWindowInsetsListener(appToolBar) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                top = systemBars.top
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }

            clFlashChat.edgeToEdgeBottom()
            viewPager.edgeToEdgeBottom()

            ivCrazyFlushChat.click {


                onFlushChat()

                dismissAllowingStateLoss()
            }

            ivBack.click {

                dismissAllowingStateLoss()
            }
        }


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

                if (item.status == GridImageView.NORMAL_STATUS + 10) {

                    if (showFlash) {
                        mDialogBinding.ivCrazyFlushChat.visibility = View.VISIBLE
                    }
                    mDialogBinding.clFlashChat.visibility = View.VISIBLE
                } else {
                    mDialogBinding.clFlashChat.visibility = View.GONE
                    mDialogBinding.ivCrazyFlushChat.visibility = View.GONE
                }


                if (item.albumUrl.isVideo()) {
                    mRecyclerView.post {
                        val findViewByPosition =
                            mRecyclerView.layoutManager?.findViewByPosition(mPosition)

                        findViewByPosition?.let {
                            val mPlayerView =
                                findViewByPosition.findViewById<CustomVideoPlay>(R.id.play_view)
                                    ?: return@post
                            autoPlayCurrent(item.albumUrl, mPlayerView)

                        }

                    }
                } else {
                    mScopedVideoManager?.release()
                }

                currentPosition = mPosition

                mDialogBinding.tvCount.text = "${mPosition + 1}/${data.size}"

            }

        })


        mImageViewPagerAdapter = object :
            BaseRecyclerAdapter<UnlockAlbums, ItemPreviewCrazyBinding>(ItemPreviewCrazyBinding::inflate) {

            override fun convert(
                holder: BaseRecyclerViewHolder<ItemPreviewCrazyBinding>,
                itemView: ItemPreviewCrazyBinding,
                item: UnlockAlbums,
                position: Int
            ) {

                try {

                    itemView.flTime.updatePadding(top = top)


                    when (item.status) {

                        GridImageView.LOCK_STATUS -> {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                                itemView.blurView.setupWith(itemView.target)
                                    .setBlurRadius(20f)
                            } else {

                                itemView.ivLockCover.loadOptionImage(
                                    itemView.ivLockCover.context,
                                    item.albumUrl,
                                    requestOptions,
                                    width,
                                    height
                                )

                            }

                            viewVisibility(View.VISIBLE, itemView.clLock, itemView.playView)

                            if (UserInfoHold.isVip) {
                                viewVisibility(View.GONE, itemView.tvTitle, itemView.tvDesc)
                                itemView.tvGetVip.text = "Private Video"
                                itemView.ivType.setImageResource(R.drawable.iv_devil)
                            } else {
                                viewVisibility(View.VISIBLE, itemView.tvTitle, itemView.tvDesc)
                                itemView.tvGetVip.text = "Get Premium"
                                itemView.ivType.setImageResource(R.drawable.iv_open_like_vip)
                            }

                            viewVisibility(
                                View.GONE,
                                itemView.stvTime,
                                itemView.llDestroyContainer,
                                itemView.ivImage,
                                itemView.playView.mBinding.llMenu
                            )
                        }

                        GridImageView.NORMAL_STATUS -> {


                            if (item.ttl > 0) {
                                viewVisibility(
                                    View.GONE,
                                    itemView.blurView,
                                    itemView.clLock,
                                    itemView.llDestroyContainer,
                                    itemView.ivImage
                                )

                                viewVisibility(View.VISIBLE, itemView.stvTime, itemView.playView, itemView.playView.mBinding.llMenu)
                                itemView.stvTime.text = "${getTimeSecond(item.ttl)}"


                            } else {
                                viewVisibility(View.VISIBLE, itemView.llDestroyContainer)

                                viewVisibility(
                                    View.GONE,
                                    itemView.blurView,
                                    itemView.stvTime,
                                    itemView.clLock,
                                    itemView.playView,
                                    itemView.ivImage
                                )
                            }

                        }

                        GridImageView.NORMAL_STATUS + 10 -> {

                            viewVisibility(
                                View.GONE,
                                itemView.clLock,
                                itemView.blurView,
                                itemView.llDestroyContainer,
                                itemView.stvTime,
                                itemView.playView
                            )
                            viewVisibility(View.VISIBLE, itemView.ivImage)
                            itemView.ivImage.loadImage(
                                itemView.ivImage.context,
                                item.albumUrl,
                                width,
                                height
                            )
                        }

                        else -> {

                            viewVisibility(
                                View.GONE,
                                itemView.blurView,
                                itemView.clLock,
                                itemView.stvTime,
                                itemView.ivImage,
                                itemView.playView
                            )

                            viewVisibility(
                                View.VISIBLE,
                                itemView.llDestroyContainer
                            )
                        }

                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        mDialogBinding.viewPager.adapter = mImageViewPagerAdapter

        data.forEach { item ->

            val status = if (item.albumType == "PUBLIC_PIC") {
                GridImageView.NORMAL_STATUS + 10
            } else {
                if (item.albumStatus == "Sent") {
                    GridImageView.LOCK_STATUS
                } else {
                    if (item.albumStatus == "Unlock" && item.ttl <= 0) {
                        GridImageView.DESTROY_STATUS
                    } else {
                        GridImageView.NORMAL_STATUS
                    }
                }
            }

            item.status = status
        }

        mImageViewPagerAdapter.submitList(data)

        mDialogBinding.viewPager.offscreenPageLimit = 5

        mDialogBinding.viewPager[0].overScrollMode = View.OVER_SCROLL_NEVER



        mImageViewPagerAdapter.addOnDebouncedChildClick(R.id.scl_open_vip) { _, _, position ->

            val item = mImageViewPagerAdapter.getItem(position) ?: return@addOnDebouncedChildClick

            mViewModel.unlockCrazyAlbum(item.id, item, position)

        }

    }

    override fun initViewModel() {
        super.initViewModel()

        mViewModel.mUnlockCrazyAlbumStatus.observe(this) {

            if (it.position != -1) {

                onUnlock(it)

                mImageViewPagerAdapter.notifyItemChanged(it.position, false)
            }

        }


        mViewModel.mVipData.observe(this) {

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {

                    initVipDialog(it)
                }

                else -> {
                    initBuyRightDialog(it)
                }

            }

        }

    }


    fun autoPlayCurrent(url: String, mCustomVideoPlay: CustomVideoPlay) {

        val url = getRealUrl(url).toString()

        if (url.isEmpty() || !url.isVideo()) {

            return
        }

        mScopedVideoManager?.play(mCustomVideoPlay, url)


    }


    private fun initTimer() {

        lifecycleScope.launch {

            try {
                while (isActive) {

                    val cancel = data.all { it.ttl == 0 }

                    if (cancel) {

                        this.cancel()
                    }

                    val start = data.all { it.albumStatus == "Sent" }

                    if (!start) {

                        data.forEachIndexed { index, it ->

                            if (it.albumStatus != "Sent" && it.ttl > 0) {

                                it.ttl = it.ttl - 1

                                mImageViewPagerAdapter.notifyItemChanged(index, false)
                            }
                        }
                    }

                    delay(1000)
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }

        }

    }


    private fun initUserInfo() {

        mUserInfoData?.let {

            withViewBinding {

                tvName.text = "${it.nickname}·${it.age}"

                if (it.city.isNullOrEmpty()) {
                    viewVisibility(View.GONE, sllAddress)
                } else {
                    tvAddress.text = it.city
                    viewVisibility(View.VISIBLE, sllAddress)
                }
                if (it.onlineStatus == "Online") {

                    tvOnline.text = "Online"

                } else {

                    tvOnline.text = "Active"
                }
            }

        }


    }


    private fun initVipDialog(it: VipData) {

        if (null != mBuyVipDialog && mBuyVipDialog?.isVisible == true) {

            mBuyVipDialog?.resetData(it)

            return
        }

        mBuyVipDialog = BuyVipDialog.newInstance(it).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mBuyVipDialog = null
                }
            })
        }

        mBuyVipDialog?.show(parentFragmentManager)

    }

    private fun initBuyRightDialog(it: VipData) {

        if (null != mBuyRightDialog && mBuyRightDialog?.isVisible == true) return

        mBuyRightDialog = BuyRightDialog.newInstance(it).apply {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mBuyRightDialog = null
                }
            })
        }

        mBuyRightDialog?.show(parentFragmentManager)


    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    private fun release() {

        mScopedVideoManager?.release()

        mScopedVideoManager = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        try {
            release()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDismiss(dialog)
    }

}