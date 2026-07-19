package com.chat.jolt.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chat.jolt.BuildConfig
import com.chat.jolt.R
import com.chat.jolt.activity.EditUserInfoActivity
import com.chat.jolt.activity.MainActivity
import com.chat.jolt.activity.UserInfoActivity
import com.chat.jolt.adapter.CardAdapter
import com.chat.jolt.adapter.CardNoTimeAdapter
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.data.VipData
import com.chat.jolt.databinding.FraMainBinding
import com.chat.jolt.databinding.ViewCardEmptyBinding
import com.chat.jolt.databinding.ViewCardNoTimeBinding
import com.chat.jolt.databinding.ViewNoNetworkBinding
import com.chat.jolt.dialog.BuyRightDialog
import com.chat.jolt.dialog.ExposureDialog
import com.chat.jolt.dialog.FlashSuccessDialog
import com.chat.jolt.dialog.QuickFlashDialog
import com.chat.jolt.dialog.BuyVipDialog
import com.chat.jolt.helper.FloatingWindowUtil
import com.chat.jolt.helper.ModelTouchHelper
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.mVipShowValue
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.mRotation
import com.chat.lib_common.util.mTranslation
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.toJson
import com.chat.lib_common.widget.CardLayoutManager
import com.chat.lib_common.widget.ErrorReLoadView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<FraMainBinding, MainViewModel>(FraMainBinding::inflate) {


    private val mCardAdapter: CardAdapter by lazy {

        CardAdapter()
    }

    private val mCardNoTimeAdapter: CardNoTimeAdapter by lazy {
        CardNoTimeAdapter()
    }

    private lateinit var mItemTouchHelper: ItemTouchHelper

    private val mModelTouchHelper: ModelTouchHelper by lazy {

        ModelTouchHelper()
    }



    private var mFlashSuccessDialog: FlashSuccessDialog? = null


    private var mBuyVipDialog: BuyVipDialog? = null



    private var distant: Int = 100

    private var minAge: Int = 18

    private var maxAge: Int = 35

    private var sexType: String = ""

    private var hobbyTags = mutableListOf<String>()

    private var isRefresh = true

    private var currentUserId = ""

    private var currentUserName = ""

    private var cardChatSource = "HomeCard"

    private var currentStateImage = -2

    private var animatorSet: AnimatorSet? = AnimatorSet()

    private lateinit var mRotationAnimator: ObjectAnimator

    private lateinit var mTranslationAnimator: ObjectAnimator

    private var isNoCardTime = false

    private var isShowSwipGuide = false

    override fun initView() {


        initRecyclerView()

        withViewBinding {


            ivLogo.setOnLongClickListener {


                if (BuildConfig.DEBUG) {
                    QuickFlashDialog().apply {

                        onConfirm = {

                            mViewModel.flashChat(it, cardChatSource)
                        }

                    }.show(parentFragmentManager)
                }

                return@setOnLongClickListener true

            }

            ivClose.setOnClickListener {

                sclTurn.visibility = View.GONE
            }
            sflEdit.click {

                createIntent(EditUserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.IS_TURN_ONS, true)
                    .startActivity(requireActivity())

                sclTurn.visibility = View.GONE
            }

        }

    }

    override fun initData() {

        mViewModel.getUserInfo()

        mViewModel.getModelCard()

    }


    override fun initViewModel() {




        mViewModel.mUserInfoData.observe(this) {

            if (UserInfoHold.isLowUse) {

                mViewModel.getVipInfo(
                    AppConstant.Constant.PAY_VIP,
                    mVipShowValue[7],
                    "PremiumBadge"
                )
            }


        }

        mViewModel.mModelCardData.observe(this) {


            if (isNoCardTime && !UserInfoHold.isVip) {

                mCardNoTimeAdapter.submitList(it.userList)

                return@observe
            }

            isNoCardTime = false

            mViewBinding.viewCardSkeleton.root.visibility = View.GONE
            mViewBinding.errorReLoadView.visibility = View.VISIBLE

            it.apply {


                if (isRefresh) {
                    mCardAdapter.submitList(this.userList)

                } else {
                    mCardAdapter.addAll(this.userList)
                }

                if (UserInfoHold.isLowUse) {

                    mModelTouchHelper.isNeedRemoveCard = false
                } else {
                    mModelTouchHelper.isNeedRemoveCard = paddleCount > 0
                }

                if (!UserInfoHold.isVip && paddleCount <= 0) {

                    mViewModel.mNoCardStatus.postValue(true)
                } else {

                }


            }


            setCurrentInfo(true)

        }



        mViewModel.mLikeStatusData.observe(this) {

            if (UserInfoHold.isLowUse) {

                mModelTouchHelper.isNeedRemoveCard = false
            } else {
                mModelTouchHelper.isNeedRemoveCard = it.paddleCount > 0
            }

            if (!UserInfoHold.isOrganic) {

                if (it.turnOnsGuide == "True") {

                    mViewBinding.sclTurn.visibility = View.VISIBLE

                    val param = mutableMapOf<String, Any?>()
                    param["method"] = "Card"
                    reportEvent(MESSAGE_CHAT_KEY[9], param)
                } else {
                    mViewBinding.sclTurn.visibility = View.GONE
                }
            }




            try {
                (activity as MainActivity).cancelNoSwipTimeJob()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mViewModel.mFlashChatStatusData.observe(this) {

            mFlashSuccessDialog?.dismissAllowingStateLoss()

            val item = mCardAdapter.items.findLast { find -> find.userId == it.userId }

            item?.let {

                mCardAdapter.remove(it)
                mCardAdapter.notifyItemChanged(0, false)
            }

        }
        mViewModel.requestFailEvent.observe(this) {

            mViewBinding.viewCardSkeleton.root.visibility = View.GONE
            mViewBinding.errorReLoadView.visibility = View.VISIBLE


            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

        }

        mViewModel.mNoCardStatus.observe(this) {


            if (it is Boolean) {

                if (isNoCardTime == it) return@observe

                isNoCardTime = it

                mViewModel.getModelCard(distant, minAge, maxAge, sexType, hobbyTags)


                mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.ERROR)
            }


        }


        mViewModel.mVipData.observe(this) {

            dismissLoading()

            it.userId2 = currentUserId
            it.name2 = currentUserName

            when (it.type) {
                AppConstant.Constant.PAY_VIP -> {
                    initVipDialog(it)
                }

                AppConstant.Constant.PAY_FLASH_CHAT -> {
                    initBuyRightDialog(it)
                }
            }

        }





        FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_CARD_LIST).observe(this) {

            isRefresh = true

            mViewModel.getModelCard(distant, minAge, maxAge, sexType, hobbyTags)
        }

        FlowBus.with(AppConstant.EventConstant.EVENT_OPERATION_CARD).observe(this) {

            if (mCardAdapter.itemCount <= 0) return@observe

            val layoutManager = mCardAdapter.recyclerView.layoutManager ?: return@observe

            val findViewByPosition = layoutManager.findViewByPosition(0) ?: return@observe

            if (it is Int) {

                cardChatSource = "UserInfo"

                when (it) {

                    0 -> {
                        val ivDisLike = findViewByPosition.findViewById<ImageView>(R.id.iv_dislike)
                            ?: return@observe
                        ivDisLike.performClick()
                    }

                    1 -> {
                        if (mCardAdapter.items.isNotEmpty()){
                            val item = mCardAdapter.items.first()
                            item.let {
                                mCardAdapter.remove(item)
                                mCardAdapter.notifyItemChanged(0, false)
                            }
                        }
                    }

                    2 -> {
                        val ivLike = findViewByPosition.findViewById<ImageView>(R.id.iv_like)
                            ?: return@observe
                        ivLike.performClick()
                    }
                }

            }
        }


        FlowBus.with(AppConstant.EventConstant.EVENT_SWIP_PAGE).observe(this) {

            if (it !is Int) return@observe

            lifecycleScope.launch {

                try {
                    repeat(it) {

                        if (!isActive) {

                            this.cancel()

                            return@launch
                        }

                        if (mCardAdapter.itemCount <= 0) return@launch

                        val layoutManager = mCardAdapter.recyclerView.layoutManager ?: return@launch

                        val findViewByPosition =
                            layoutManager.findViewByPosition(0) ?: return@launch

                        val maxSwipe = mCardAdapter.recyclerView.width * 0.25f

                        val animator = ValueAnimator.ofFloat(0f, maxSwipe, 0f)

                        animator.duration = 800

                        animator.addUpdateListener {
                            val dX = it.animatedValue as Float
                            val ratio = dX / mCardAdapter.recyclerView.width
                            findViewByPosition.translationX = dX
                            findViewByPosition.rotation = ratio * 15

                            val direction = when {
                                ratio == 0f -> 0
                                ratio > 0f -> 1
                                else -> -1
                            }

                            showStateImage(direction)
                        }

                        animator.start()

                        delay(810)

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }


        FlowBus.with(AppConstant.EventConstant.EVENT_SHOW_SWIP_GUIDE).observe(this){

            FloatingWindowUtil.showFloatView(requireContext())

            isShowSwipGuide = true
        }
    }

    private fun initRecyclerView() {


        mViewBinding.recyclerView.layoutManager = CardLayoutManager()

        mViewBinding.recyclerView.adapter = mCardAdapter

        mViewBinding.recyclerView.setItemViewCacheSize(10)

        mViewBinding.recyclerView.setRecycledViewPool(mCardAdapter.sharedPool)

        mViewBinding.recyclerView.animation = null



        mViewBinding.errorReLoadView.addEmptyView { viewGroup ->
            ViewCardEmptyBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                .apply {

                    stvConfirm.click {

                        FlowBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(1)
                    }
                }
        }
        mViewBinding.errorReLoadView.addErrorView { viewGroup ->
            ViewCardNoTimeBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                .apply {

                    recyclerView.adapter = mCardNoTimeAdapter
                    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 2)
                    mCardNoTimeAdapter.setOnItemClickListener { _, _, _ ->

                        mViewModel.getVipInfo(
                            AppConstant.Constant.PAY_VIP,
                            mVipShowValue[13],
                            "MoreSwipe"
                        )
                    }

                    stvMatch.setOnClickListener {
                        mViewModel.getVipInfo(
                            AppConstant.Constant.PAY_VIP,
                            mVipShowValue[12],
                            "MoreSwipe"
                        )
                    }
                }
        }
        mViewBinding.errorReLoadView.addNoNetView { viewGroup ->
            ViewNoNetworkBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                .apply {


                    stvConfirm.click {

                        mViewModel.getModelCard(distant, minAge, maxAge, sexType, hobbyTags)
                    }
                }
        }



        setCache(AppConstant.Constant.START_NOTICE, true)

        mCardAdapter.addOnDebouncedChildClick(R.id.cl_info) { _, _, position ->

            val item = mCardAdapter.getItem(position) ?: return@addOnDebouncedChildClick

            requireContext().createIntent(UserInfoActivity::class.java)
                .putExtra(AppConstant.Constant.ID, item.userId)
                .putExtra(AppConstant.Constant.SHOW_FLASH, true)
                .putExtra(AppConstant.Constant.PAGE, "card")
                .putExtra(AppConstant.Constant.MODEL_DATA, item.toJson())
                .startActivity(requireContext())
        }

        mCardAdapter.addOnDebouncedChildClick(R.id.stv_turn) { _, _, position ->

            val item = mCardAdapter.getItem(position) ?: return@addOnDebouncedChildClick

            requireContext().createIntent(UserInfoActivity::class.java)
                .putExtra(AppConstant.Constant.ID, item.userId)
                .putExtra(AppConstant.Constant.SHOW_FLASH, true)
                .putExtra(AppConstant.Constant.IS_TURN_ONS, true)
                .putExtra(AppConstant.Constant.PAGE, "card")
                .putExtra(AppConstant.Constant.MODEL_DATA, item.toJson())
                .startActivity(requireContext())
        }


        mCardAdapter.addOnDebouncedChildClick(R.id.iv_flash_chat) { _, _, position ->

            flashChat()

        }

        mCardAdapter.addOnDebouncedChildClick(R.id.iv_dislike) { _, _, position ->

            val item = mCardAdapter.getItem(position) ?: return@addOnDebouncedChildClick

            currentUserId = item.userId

            currentUserName = item.nickname

            likeAnimate(false)
        }

        mCardAdapter.addOnDebouncedChildClick(R.id.iv_like) { _, _, position ->

            val item = mCardAdapter.getItem(position) ?: return@addOnDebouncedChildClick

            currentUserId = item.userId

            currentUserName = item.nickname

            likeAnimate(true)
        }


        initTouchHelper()

    }


    private fun initTouchHelper() {


        mItemTouchHelper = ItemTouchHelper(mModelTouchHelper.apply {


            onShowImage = { type, dX ->


                showStateImage(type)
            }

            onMove = {


            }
            onReset = {

                if (UserInfoHold.isVip) {

                    reportNoTimeEvent()
                } else {

                    mViewModel.mNoCardStatus.postValue(true)

                    mViewModel.getVipInfo(
                        AppConstant.Constant.PAY_VIP,
                        if (UserInfoHold.isLowUse) mVipShowValue[7] else mVipShowValue[1],
                        "MoreSwipe"
                    )

                }


            }

            onSwiped = { position ->


                if (mCardAdapter.itemCount > 0) {

                    currentUserId = mCardAdapter.items.first().userId
                    currentUserName = mCardAdapter.items.first().nickname

                    mCardAdapter.removeAt(position)
                }

                setCurrentInfo()
            }

            onLike = { position ->


                likeModel()
            }

            onDisLike = { position ->


                dislikeModel()
            }

        })


        mItemTouchHelper.attachToRecyclerView(mViewBinding.recyclerView)
    }


    private fun showStateImage(it: Int) {


        if (currentStateImage == it) return

        currentStateImage = it

        val layoutManager = mCardAdapter.recyclerView.layoutManager ?: return

        val findViewByPosition = layoutManager.findViewByPosition(0) ?: return

        val ivLikeStatus = findViewByPosition.findViewById<ImageView>(R.id.iv_like_status) ?: return

        val ivDisLikeStatus =
            findViewByPosition.findViewById<ImageView>(R.id.iv_dislike_status) ?: return


        when (it) {
            0 -> {

                ivLikeStatus.visibility =
                    View.GONE
                ivDisLikeStatus.visibility =
                    View.GONE

            }

            -1 -> {
                ivLikeStatus.visibility =
                    View.GONE
                ivDisLikeStatus.visibility =
                    View.VISIBLE

            }

            else -> {
                ivLikeStatus.visibility =
                    View.VISIBLE
                ivDisLikeStatus.visibility =
                    View.GONE

            }
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


        if (mViewModel.paddleCount == 0) {

            if (!UserInfoHold.isVip) {
                reportNoTimeEvent()
                mViewModel.mNoCardStatus.postValue(true)
            }
            return
        }


        if (!mModelTouchHelper.isSwipeEnabled) {

            return
        }

        animatorSet?.cancel()

        animatorSet?.removeAllListeners()

        animatorSet = null

        animatorSet = AnimatorSet()

        val layoutManager = mCardAdapter.recyclerView.layoutManager ?: return

        val findViewByPosition = layoutManager.findViewByPosition(0) ?: return

        val group = findViewByPosition.findViewById<ConstraintLayout>(R.id.cl_container) ?: return


        val ivLikeStatus = findViewByPosition.findViewById<ImageView>(R.id.iv_like_status) ?: return

        val ivDisLikeStatus =
            findViewByPosition.findViewById<ImageView>(R.id.iv_dislike_status) ?: return

        group.let {

            it.pivotY = it.height.toFloat()

            mTranslationAnimator = it.mTranslation("X", 0f, if (like) 100f else -100f)

            mRotationAnimator = it.mRotation(0f, if (like) 30f else -30f)

            animatorSet?.setDuration(300)

            animatorSet?.playTogether(mTranslationAnimator, mRotationAnimator)



            animatorSet?.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)

                    mModelTouchHelper.isSwipeEnabled = false

                    if (like) {
                        ivLikeStatus.visibility =
                            View.VISIBLE
                    } else {
                        ivDisLikeStatus.visibility =
                            View.VISIBLE
                    }

                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)

                    mModelTouchHelper.isSwipeEnabled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    mModelTouchHelper.isSwipeEnabled = true
                    if (like) {
                        ivLikeStatus.visibility =
                            View.GONE
                        likeModel("Click")
                    } else {
                        ivDisLikeStatus.visibility =
                            View.GONE
                        dislikeModel("Click")
                    }
                    if (mCardAdapter.itemCount > 0) {
                        mCardAdapter.removeAt(0)
                    }

                    lifecycleScope.launch {

                        delay(200)

                        setCurrentInfo()
                    }

                    animatorSet?.removeAllListeners()
                }
            })

            animatorSet?.start()
        }
    }


    private fun setCurrentInfo(isFilter: Boolean = false) {

        withViewBinding {


            errorReLoadView.showSuccessView(mCardAdapter.items)

            mCardAdapter.notifyItemChanged(0, false)

            if (mCardAdapter.itemCount == 0) {

                reportEvent(isFilter)
            }

        }


    }


    private fun dislikeModel(type: String = "Swipe") {


        mViewModel.dislikeModel(currentUserId, cardChatSource, type)

        needLoadNext()
    }


    private fun likeModel(type: String = "Swipe") {

        initExposureDialog()


        mViewModel.likeModel(currentUserId, cardChatSource, type)

        needLoadNext()
    }

    private fun flashChat() {

        if (!mModelTouchHelper.isSwipeEnabled) return

        if (mCardAdapter.itemCount <= 0) return

        val data = mCardAdapter.items.first()

        currentUserId = data.userId
        currentUserName = data.nickname

        initFlashDialog(data)

        cardChatSource = "HomeCard"

        needLoadNext()
    }

    private fun needLoadNext() {

        if (mCardAdapter.itemCount == 3) {

            lifecycleScope.launch {

                delay(500)

                isRefresh = false

                mViewModel.getModelCard(distant, minAge, maxAge, sexType, hobbyTags)
            }


        }

        if (isShowSwipGuide){

            FloatingWindowUtil.remove()

            isShowSwipGuide = false
        }

    }





    private fun initExposureDialog() {

        val hasExposure = getCache(AppConstant.Constant.HAS_EXPOSURE, false)

        if (!hasExposure) {
            ExposureDialog().show(parentFragmentManager)
            setCache(AppConstant.Constant.HAS_EXPOSURE, true)
        }
    }


    private fun initVipDialog(it: VipData) {

        if (null != mBuyVipDialog && mBuyVipDialog?.isVisible == true){

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


        val mBuyRightDialog = BuyRightDialog.newInstance(it)

        mBuyRightDialog.show(parentFragmentManager)


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

            mViewModel.flashChat(mModelUserData.userId, cardChatSource, it)

        }

        mFlashSuccessDialog?.show(parentFragmentManager)

    }


    private fun reportEvent(isFilter: Boolean) {

        val params = mutableMapOf<String, Any?>()


        params["method"] = if (isFilter) "filter" else "no_filter"

        params["filter_score"] = "$distant, $minAge, $maxAge, $sexType, $hobbyTags"

        reportEvent(mPopPopupDialogKey[2], params)
    }

    private fun reportNoTimeEvent() {

        val params = mutableMapOf<String, Any?>()

        params["is_vip"] = if (UserInfoHold.isVip) "True" else "False"

        reportEvent(mPopPopupDialogKey[3], params)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mFlashSuccessDialog?.dismissAllowingStateLoss()
        mFlashSuccessDialog = null
    }

}