package com.chat.jolt.activity


import android.R.attr.banner
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.adapter.CrazyAdapter
import com.chat.jolt.adapter.TurnsOnsAdapter
import com.chat.jolt.data.ModelImageData
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.data.PictureData
import com.chat.jolt.data.UnlockAlbums
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.ActUserInfoBinding
import com.chat.jolt.databinding.ItemImageRecyclerPagerBinding
import com.chat.jolt.databinding.ItemInfoInterstBinding
import com.chat.jolt.databinding.ViewSplashNoNetBinding
import com.chat.jolt.dialog.BuyRightDialog
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.dialog.ChatMoreDialog
import com.chat.jolt.dialog.FlashSuccessDialog
import com.chat.jolt.dialog.PictureDetailDialog
import com.chat.jolt.dialog.PreviewCrazyDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.tracking.mMessageUserKey
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.replaceEmoji
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.viewVisibility
import com.chat.lib_common.widget.ErrorReLoadView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.listener.OnPageChangeListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class UserInfoActivity :
    BaseActivity<ActUserInfoBinding, UserViewModel>(ActUserInfoBinding::inflate) {

    private lateinit var mBannerAdapter: BannerAdapter<String, RecyclerView.ViewHolder>

    private lateinit var mHobbyTagAdapter: BaseRecyclerAdapter<String, ItemInfoInterstBinding>

    private lateinit var mImageRecyclerViewAdapter: BaseRecyclerAdapter<ModelImageData, ItemImageRecyclerPagerBinding>

    private val mTurnsOnsAdapter by lazy { TurnsOnsAdapter() }

    private val mCrazyAdapter by lazy { CrazyAdapter(lifecycleScope) }

    private var currentPosition = 0

    private var targetUserId = ""

    private var showFlash = false

    private var isTurnOns = false

    private var intoPage = ""

    private var mFlashSuccessDialog: FlashSuccessDialog? = null

    private var mBuyVipDialog: BuyVipDialog? = null

    private var mBuyRightDialog: BuyRightDialog? = null

    private var mModelUserData: ModelUserData? = null

    override fun initView() {

        withViewBinding {

            llToolbar.edgeToEdgeTop()

            flashContainer.edgeToEdgeBottom()

            ivClose.click {

                if (targetUserId == UserInfoHold.userId) {
                    createIntent(EditUserInfoActivity::class.java)
                        .startActivity(this@UserInfoActivity)
                } else {
                    finish()
                }

            }

            ivMore.click {

                initReportDialog()
            }


            ivDislike.click {

                handleMenu(0)


            }

            ivFlashChat.click {

                handleMenu(1)


            }

            ivLike.click {

                handleMenu(2)

            }


        }


    }


    override fun initData() {

        targetUserId = intent.getStringExtra(AppConstant.Constant.ID) ?: targetUserId

        isTurnOns = intent.getBooleanExtra(AppConstant.Constant.IS_TURN_ONS, isTurnOns)

        intoPage = intent.getStringExtra(AppConstant.Constant.PAGE) ?: intoPage

        val mModelData = intent.getStringExtra(AppConstant.Constant.MODEL_DATA)

        if (!mModelData.isNullOrEmpty()) {
            mModelUserData = mModelData.fromJson()
        }


        if (targetUserId == UserInfoHold.userId) {
            viewVisibility(View.GONE, mViewBinding.flashContainer, mViewBinding.ivMore)
        } else {
            viewVisibility(View.VISIBLE, mViewBinding.flashContainer, mViewBinding.ivMore)

        }




        isShowFlash()



        mViewModel.getHomePage(targetUserId)


        mViewBinding.errorReLoadView.addNoNetView { viewGroup ->
            ViewSplashNoNetBinding.inflate(
                LayoutInflater.from(this@UserInfoActivity),
                viewGroup,
                true
            )
                .apply {

                    stvNext.click {

                        mViewModel.getHomePage(targetUserId)
                    }

                }
        }


    }

    override fun initViewModel() {

        mViewModel.mUserInfoData.observe(this) {

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NORMAL)

            initUserInfo(it)
        }
        mViewModel.mUserInfoDataStatus.observe(this) {

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)
        }

        mViewModel.mLikeStatusData.observe(this) {

            when (intoPage) {

                "chat" -> {

                }

                "ILike" -> {

                    FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_I_LIKE).postValue(0)
                }

                "Visitor" -> {

                    FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_VISITOR).postValue(0)
                }

                "card" -> {


                }

                else -> {

                }

            }
            finish()
        }

        mViewModel.mFlashChatStatusData.observe(this) {

            mFlashSuccessDialog?.dismissAllowingStateLoss()

            when (intoPage) {

                "chat" -> {

                }

                "ILike" -> {

                    FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_I_LIKE).postValue(1)
                }

                "Visitor" -> {

                    FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_VISITOR).postValue(1)
                }

                "card" -> {

                    FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_CARD).postValue(1)

                }

                else -> {

                }

            }
            finish()

        }


        mViewModel.mVipData.observe(this) {

            dismissLoading()

            it.userId2 = mModelUserData?.userId
            it.name2 = mModelUserData?.nickname

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {
                    initVipDialog(it)
                }

                AppConstant.Constant.PAY_FLASH_CHAT -> {
                    initBuyRightDialog(it)
                }
            }

        }

    }


    private fun initUserInfo(mUserInfoData: UserInfoData?) {

        if (null == mUserInfoData) {

            return
        }

        withViewBinding {

            tvTitle.text = "${mUserInfoData.nickname}·${mUserInfoData.age}"



            if (mUserInfoData.mySign.isNullOrEmpty()) {
                viewVisibility(View.GONE, sclAboutMe)
            } else {
                tvIntroductions.text = mUserInfoData.mySign
                viewVisibility(View.VISIBLE, sclAboutMe)
            }


            if (mUserInfoData.socialAim.isNullOrEmpty()) {
                viewVisibility(View.GONE, sclNiw)
            } else {


                val findSex = when (mUserInfoData.sex) {

                    "Female" -> {
                        "Male"
                    }

                    "Male" -> {
                        "Female"
                    }

                    "Other" -> {
                        ""
                    }
                    else -> {
                        ""
                    }

                }
                SpanUtils.with(tvSocialAim).append("I'm ").append("${mUserInfoData.sex}\n").setForegroundColor(getColor(R.color.color_EAA82B)).append("I'm seeking for ").append("${findSex}\n").setForegroundColor(getColor(R.color.color_EAA82B)).append("I'm ").append(
                    mUserInfoData.socialAim.replaceEmoji()
                ).setForegroundColor(getColor(R.color.color_EAA82B)).create()

                viewVisibility(View.VISIBLE, sclNiw)

            }



            if (mUserInfoData.city.isNullOrEmpty()) {
                viewVisibility(View.GONE, sllAddress)
            } else {
                tvAddress.text = mUserInfoData.city
                viewVisibility(View.VISIBLE, sllAddress)
            }
            if (mUserInfoData.onlineStatus == "Online") {

                tvOnline.text = "Online"

            } else {

                tvOnline.text = "Active"

            }

            if (mUserInfoData.height.isNullOrEmpty()) {

                viewVisibility(View.GONE, stvHeight)
            } else {
                stvHeight.text = "${mUserInfoData.height}"
                viewVisibility(View.VISIBLE, stvHeight)
            }


            if (mUserInfoData.weight.isNullOrEmpty()) {

                viewVisibility(View.GONE, stvWeight)
            } else {
                stvWeight.text = "${mUserInfoData.weight}"
                viewVisibility(View.VISIBLE, stvWeight)
            }

            if (mUserInfoData.height.isNullOrEmpty() && mUserInfoData.weight.isNullOrEmpty() && mUserInfoData.profession.isNullOrEmpty()) {

                viewVisibility(View.GONE, sclBasic)
            } else {

                viewVisibility(View.VISIBLE, sclBasic)
            }





            if (mUserInfoData.profession.isNullOrEmpty()) {

                viewVisibility(View.GONE, stvProfession)
            } else {
                stvProfession.text = mUserInfoData.profession
                viewVisibility(View.VISIBLE, stvProfession)
            }

            initBanner(mUserInfoData.coverPics)

            initImageRecyclerView(mUserInfoData.coverPics?.map {

                ModelImageData(it).apply { isSelect = false }
            }?.toMutableList())

            if (mUserInfoData.hobbyTagContents.isNullOrEmpty()) {

                viewVisibility(View.GONE, sclInterests)
            } else {
                initRecyclerView(
                    mUserInfoData.hobbyTagContents,
                    mUserInfoData.hobbyTags ?: mutableListOf(),
                    mUserInfoData.commonHobbyTags ?: mutableListOf()
                )
                viewVisibility(View.VISIBLE, sclInterests)
            }

            if (mUserInfoData.turnOnsTags.isNullOrEmpty()) {

                viewVisibility(View.GONE, sclTurn)
            } else {
                initTurnsOnsRecyclerView(mUserInfoData.turnOnsTags ?: mutableListOf())
                viewVisibility(View.VISIBLE, sclTurn)
            }

            if (mUserInfoData.privateAlbums.isNullOrEmpty()) {

                viewVisibility(View.GONE, sclCrazy)
            } else {

                tvCrazy.text = "${mUserInfoData.nickname}'s crazy hole"

                val mutableListOf = mutableListOf<ModelMediaData>()
                mutableListOf.addAll(mUserInfoData.publicAlbums ?: mutableListOf())
                mutableListOf.addAll(mUserInfoData.privateAlbums ?: mutableListOf())
                initCrazyRecyclerView(mutableListOf, mUserInfoData)
                viewVisibility(View.VISIBLE, sclCrazy)
            }

            if (intoPage.isNotEmpty()) {

                val params = mutableMapOf<String, Any?>()
                params["page"] = intoPage
                params["model_id"] = mUserInfoData.userId
                params["model_name"] = mUserInfoData.nickname
                reportEvent(mMessageUserKey[0], params)
            }

        }

    }


    private fun initBanner(list: List<String>? = mutableListOf()) {


        if (list.isNullOrEmpty()) return


        val result = list.map { map -> PictureData(map) }.toMutableList()

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
                    imageView.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCorners(
                        CornerFamily.ROUNDED, 10f.dip2px(this@UserInfoActivity).toFloat()
                    ).build()

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

                        it.itemView.click {

                            PictureDetailDialog.newInstance(result, position)
                                .show(supportFragmentManager)

                        }

                    }


                }

            }

            banner.setAdapter(mBannerAdapter).addBannerLifecycleObserver(this@UserInfoActivity)
                .isAutoLoop(false)
                .addOnPageChangeListener(object : OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageSelected(position: Int) {

                        val item = mImageRecyclerViewAdapter.getItem(position)

                        val lastItem = mImageRecyclerViewAdapter.getItem(currentPosition)

                        if (null != lastItem) {

                            lastItem.isSelect = false

                            mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)
                        }

                        item?.let {

                            item.isSelect = true

                            currentPosition = position

                            mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)

                            imageRecyclerview.smoothScrollToPosition(currentPosition)
                        }

                        currentPosition = position
                    }

                    override fun onPageScrollStateChanged(state: Int) {

                    }

                })


        }

    }


    private fun initImageRecyclerView(data: MutableList<ModelImageData>?) {

        if (!data.isNullOrEmpty()) {
            data[0].isSelect = true
        }

        mImageRecyclerViewAdapter = object :
            BaseRecyclerAdapter<ModelImageData, ItemImageRecyclerPagerBinding>(
                ItemImageRecyclerPagerBinding::inflate
            ) {

            override fun convert(
                holder: BaseRecyclerViewHolder<ItemImageRecyclerPagerBinding>,
                itemView: ItemImageRecyclerPagerBinding,
                item: ModelImageData,
                position: Int
            ) {


                itemView.ivImage.loadImage(itemView.ivImage.context, item.data)

                if (item.isSelect) {
                    itemView.ivImage.strokeColor =
                        ColorStateList.valueOf(getColor(R.color.white))
                } else {
                    itemView.ivImage.strokeColor =
                        ColorStateList.valueOf(getColor(R.color.transparent))
                }
            }

        }
        mViewBinding.imageRecyclerview.adapter = mImageRecyclerViewAdapter

        mImageRecyclerViewAdapter.submitList(data)




        mImageRecyclerViewAdapter.setOnItemClickListener { _, _, position ->

            val item = mImageRecyclerViewAdapter.getItem(position)

            val lastItem = mImageRecyclerViewAdapter.getItem(currentPosition)

            if (null != lastItem) {

                lastItem.isSelect = false

                mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)
            }

            item?.let {

                item.isSelect = true

                currentPosition = position

                mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)
            }

            mViewBinding.banner.setCurrentItem(position + 1, false)

        }

    }


    private fun initRecyclerView(
        list: MutableList<String>? = mutableListOf(),
        hobbyList: MutableList<String>,
        commonList: MutableList<String>
    ) {

        if (list.isNullOrEmpty()) return


        withViewBinding {

            mHobbyTagAdapter = object :
                BaseRecyclerAdapter<String, ItemInfoInterstBinding>(ItemInfoInterstBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemInfoInterstBinding>,
                    itemView: ItemInfoInterstBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item


                    if (position < hobbyList.size) {
                        itemView.stvContent.isEnabled = !commonList.contains(hobbyList[position])
                    } else {
                        itemView.stvContent.isEnabled = true
                    }

                }


            }

            recyclerView.adapter = mHobbyTagAdapter

            recyclerView.layoutManager = FlexboxLayoutManager(this@UserInfoActivity)

            mHobbyTagAdapter.submitList(list)

        }

    }


    private fun initTurnsOnsRecyclerView(turnOnsTags: MutableList<String>) {

        val list = turnOnsTags.map {
            mViewModel.getTurnOnsData()[it]!!
        }

        withViewBinding {

            turnsOnsRecyclerView.adapter = mTurnsOnsAdapter

            turnsOnsRecyclerView.layoutManager = GridLayoutManager(this@UserInfoActivity, 2)

            mTurnsOnsAdapter.submitList(list)

            if (isTurnOns) {
                lifecycleScope.launch {
                    delay(200)
                    val targetLocation = IntArray(2)
                    mViewBinding.sclTurn.getLocationInWindow(targetLocation)
                    val targetYInWindow = targetLocation[1] - 200
                    mViewBinding.mNestedScrollView.smoothScrollTo(0, targetYInWindow)
                }
            }

        }
    }

    private fun initCrazyRecyclerView(
        mModelMediaData: MutableList<ModelMediaData>,
        mUserInfoData: UserInfoData
    ) {


        withViewBinding {

            crazyRecyclerView.adapter = mCrazyAdapter

            crazyRecyclerView.layoutManager = GridLayoutManager(this@UserInfoActivity, 3)

            mCrazyAdapter.submitList(mModelMediaData)

            mCrazyAdapter.setOnDebouncedItemClick { adapter, view, position ->

                val item = mCrazyAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                if (item.albumStatus == "Unlock" && item.ttl <= 0) return@setOnDebouncedItemClick


                val data =
                    mCrazyAdapter.items.filterNot { filter -> filter.albumStatus == "Unlock" && filter.ttl <= 0 }
                        .toMutableList()

                val realPosition =
                    data.indexOfLast { index -> index.albumId == item.albumId }
                        .coerceAtLeast(0)

                val list = data.map { map ->

                    UnlockAlbums(
                        map.albumId.toString(),
                        map.albumStatus ?: "Sent",
                        map.albumType,
                        map.albumUrl.toString(),
                        map.albumId.toString(),
                        map.ttl
                    )

                }.toMutableList()


                PreviewCrazyDialog.newInstance(list, realPosition, mUserInfoData, showFlash).apply {

                    onFlushChat = {
                        mViewBinding.ivFlashChat.performClick()
                    }
                    onUnlock = {
                        val mIndex =
                            mCrazyAdapter.items.indexOfLast { find -> it.albumId == find.albumId }

                        if (mIndex != -1) {

                            val mItem = mCrazyAdapter.getItem(mIndex)

                            if (null != mItem) {

                                mItem.albumStatus = it.albumStatus

                                mItem.ttl = it.ttl

                                mCrazyAdapter.notifyItemChanged(mIndex, false)
                            }


                        }
                    }
                }.show(supportFragmentManager)


            }


        }
    }


    private fun initReportDialog() {

        ChatMoreDialog().apply {

            initView = { dialog, mViewBinding ->

                mViewBinding.tvBlock.visibility = View.GONE

                mViewBinding.tvRetort.click {

                    if (targetUserId.isEmpty()) {

                        return@click
                    }

                    createIntent(ReportActivity::class.java)
                        .putExtra(AppConstant.Constant.ID, targetUserId)
                        .startActivity(this@UserInfoActivity)

                    dialog.dismissAllowingStateLoss()
                }

            }

        }.show(supportFragmentManager)
    }


    private fun isShowFlash() {

        when (intoPage) {

            "chat" -> {

                viewVisibility(View.GONE, mViewBinding.flashContainer, mViewBinding.svCover)

                showFlash = false

            }

            "ILike" -> {

                viewVisibility(View.VISIBLE, mViewBinding.flashContainer, mViewBinding.svCover)
                viewVisibility(View.GONE, mViewBinding.ivDislike, mViewBinding.ivLike)

                showFlash = true
            }

            "Visitor" -> {

                viewVisibility(View.VISIBLE, mViewBinding.flashContainer, mViewBinding.svCover)
                viewVisibility(View.GONE, mViewBinding.ivDislike, mViewBinding.ivLike)

                showFlash = true

            }

            "card" -> {

                viewVisibility(View.VISIBLE, mViewBinding.flashContainer, mViewBinding.svCover)

                showFlash = true

            }

            else -> {

                showFlash = false

                viewVisibility(View.GONE, mViewBinding.flashContainer, mViewBinding.svCover)
            }

        }


    }


    /**
     *
     * 0 dislike 1 flash chat 2 like
     */
    private fun handleMenu(type: Int) {

        when (intoPage) {

            "chat" -> {
                when (type) {

                    0 -> {

                    }

                    1 -> {

                    }

                    2 -> {

                    }
                }

            }

            "Visitor", "ILike" -> {
                when (type) {

                    0 -> {

                        likeAnimate(false)


                    }

                    1 -> {
                        flashChat()


                    }

                    2 -> {

                        likeAnimate(true)

                    }
                }

            }

            "card" -> {

                when (type) {

                    0 -> {
                        FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_CARD).postValue(0)

                        finish()
                    }

                    1 -> {
                        flashChat()


                    }

                    2 -> {
                        FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_CARD).postValue(2)

                        finish()
                    }
                }

            }

            else -> {


            }

        }


    }


    private fun flashChat() {


        mModelUserData?.let {

            val currentValue = mViewModel.mUserInfoData.currentValue()

            if (null == currentValue) return@let

            if (it.coverPics.isNullOrEmpty()) {

                it.coverPics = currentValue.coverPics
            }

            if (it.publicPic.isNullOrEmpty()) {

                if (!currentValue.publicAlbums.isNullOrEmpty()) {
                    it.publicPic = currentValue.publicAlbums[0].albumUrl.toString()
                }

            }

            initFlashDialog(it)
        }


    }


    private fun likeAnimate(like: Boolean) {

        if (UserInfoHold.isLowUse) {

            mViewModel.getVipInfo(
                AppConstant.Constant.PAY_VIP,
                mVipShowValue[7],
                "MoreSwipe"
            )

            return
        }

        if (like) {

            mModelUserData?.let {
                mViewModel.likeModel(it.userId, "UserInfo", "Click")
            }
        } else {

            mModelUserData?.let {
                mViewModel.dislikeModel(it.userId, "UserInfo", "Click")

            }
        }
    }

    private fun initFlashDialog(
        mModelUserData: ModelUserData,
    ) {

        if (null != mFlashSuccessDialog && mFlashSuccessDialog?.isVisible == true) return

        mFlashSuccessDialog = FlashSuccessDialog.newInstance(mModelUserData).apply {

            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    mFlashSuccessDialog = null
                }
            })
        }


        mFlashSuccessDialog?.onConfirm = {

            mViewModel.flashChat(mModelUserData.userId, "UserInfo", it)

        }

        mFlashSuccessDialog?.show(supportFragmentManager)

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

        mBuyVipDialog?.show(supportFragmentManager)


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

        mBuyRightDialog?.show(supportFragmentManager)


    }

}