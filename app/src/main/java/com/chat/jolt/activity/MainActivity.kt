package com.chat.jolt.activity

import android.Manifest
import android.location.Address
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.chat.jolt.R
import com.chat.jolt.data.CustomMessageExtraData
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.databinding.ActMainBinding
import com.chat.jolt.databinding.ViewLimitTimeBinding
import com.chat.jolt.databinding.ViewMainTabBinding
import com.chat.jolt.dialog.FullLocationDialog
import com.chat.jolt.dialog.LimitTimeDialog
import com.chat.jolt.dialog.MatchSuccessDialog
import com.chat.jolt.dialog.SwipGuideDialog
import com.chat.jolt.dialog.UpdateVersionDialog
import com.chat.jolt.dialog.WlmNoticeDialog
import com.chat.jolt.fragment.ConversationFragment
import com.chat.jolt.fragment.LikeFragment
import com.chat.jolt.fragment.MainFragment
import com.chat.jolt.fragment.MineFragment
import com.chat.jolt.helper.FloatingWindowUtil
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.helper.getCmdMessageExtraData
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.jolt.widget.FloatingView
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.im.RIMClient
import com.chat.lib_common.im.RIMClient.getUnreadCount
import com.chat.lib_common.im.RIMDispatcher
import com.chat.lib_common.tracking.mMessageNoticeKey
import com.chat.lib_common.tracking.mPopPopupDialogKey
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.GooglePayManager
import com.chat.lib_common.util.LocationUtil
import com.chat.lib_common.util.areAllPermissionsGranted
import com.chat.lib_common.util.cancelNotification
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.isNextDay
import com.chat.lib_common.util.openPermissionDetail
import com.chat.lib_common.util.setCache
import com.chat.lib_common.util.shouldShowPermissionRationale
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.startActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.rong.imlib.RongCoreClient
import io.rong.imlib.listener.OnReceiveMessageWrapperListener
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.ReceivedProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActMainBinding, MainViewModel>(ActMainBinding::inflate) {


    private val mImages = arrayOf(
        R.drawable.iv_main,
        R.drawable.iv_wlm,
        R.drawable.iv_conversation,
        R.drawable.iv_mine
    )

    private val mSelectImages =
        arrayOf(
            R.drawable.iv_main_select,
            R.drawable.iv_wlm_select,
            R.drawable.iv_conversation_select,
            R.drawable.iv_mine_select
        )

    private val mFragments = mutableListOf<Fragment>()

    private val permission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var hasOpenLocation = false

    private var isVisibility = true

    private val locationHelper = LocationUtil(BaseApplication.mApplication)


    private var lastBackTime = 0L

    private var noSwipTime = 40

    private var noSwipTimeJob: Job? = null

    private var targetId: String = ""

    private var pushMark: String = ""

    private var touchType: String = ""

    private var mFloatingView: FloatingView? = null

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.all { it.value }) {

                startLocation()

                mViewModel.saveLocation(mAddress = null, granted = 1)


            }

        }


    private val mOnReceiveMessageWrapperListener = object :
        OnReceiveMessageWrapperListener() {
        override fun onReceivedMessage(
            message: Message,
            profile: ReceivedProfile
        ) {

            RIMDispatcher.onDispatch(message, profile)
        }

    }

    private val mMessageListener = object : RIMDispatcher.MessageListener {
        override fun onMessageReceiptResponse(
            message: Message,
            type: Conversation.ConversationType,
            targetId: String,
            mReceivedProfile: ReceivedProfile
        ) {

            val isOffline = mReceivedProfile.isOffline

            if (!isOffline) {
                showMatch(message)
            }

            getTotalUnreadCount()

            getUnreadCount()


        }


    }


    override fun onResume() {
        super.onResume()

        val isHasLocation = getCache(AppConstant.Constant.IS_HAS_LOCATION, false)

        if (areAllPermissionsGranted(this, permission)) {

            startLocation()

            if (!isHasLocation) {
                updateSolarEngineUser("location_permissions", true)
                setCache(AppConstant.Constant.IS_HAS_LOCATION, true)
            }


        } else {

            if (isHasLocation) {
                updateSolarEngineUser("location_permissions", false)
                setCache(AppConstant.Constant.IS_HAS_LOCATION, false)
            }

        }

        isVisibility = true

        getTotalUnreadCount()

    }

    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()


        initViewPager()

        initTabLayout()

        initLocationDialog()

        if (areAllPermissionsGranted(this, permission)) {
            mViewModel.saveLocation(mAddress = null, granted = 1)
        } else {
            mViewModel.saveLocation(mAddress = null, granted = 0)
        }

    }

    override fun initData() {

        targetId = intent.getStringExtra(AppConstant.Constant.TARGET_ID) ?: targetId

        pushMark = intent.getStringExtra(AppConstant.Constant.PUSH_MARK) ?: pushMark

        touchType = intent.getStringExtra(AppConstant.Constant.TOUCH_TYPE) ?: touchType


        clientRIM()

        mViewModel.findNews()

        mViewModel.configInfo()



        mViewModel.cacheAllVipInfo()

        mViewModel.online()

        GooglePayManager.initGooglePay(this)


        initSwipGuideDialog()
    }

    override fun initViewModel() {


        mViewModel.mConfigData.observe(this) {

            if (null != it.version) {

                if (it.version.updateType == "None") {

                    return@observe
                }

                if (it.version.updateType == "Force") {
                    UpdateVersionDialog.newInstance(it).show(supportFragmentManager)
                } else {

                    if (isNextDay(AppConstant.Constant.UPDATE)) {
                        UpdateVersionDialog.newInstance(it).show(supportFragmentManager)
                    }
                }

            }

        }




        mViewModel.mTargetUserInfoData.observe(this) {

            initMatchDialog(it, it.mCustomMessageExtraData)

        }




        FlowBus.with(AppConstant.EventConstant.EVENT_UPDATE_USER_INFO).observe(this) {

            try {

                val timeLimitPremiumTtl = UserInfoHold.userInfo?.timeLimitPremiumTtl ?: 0


                if (timeLimitPremiumTtl == 0) {

                    mFloatingView?.removeView()

                    return@observe
                }

                if (null == mFloatingView) {

                    mFloatingView = FloatingView(this)

                    val mViewLimitTimeBinding =
                        ViewLimitTimeBinding.inflate(LayoutInflater.from(this))


                    mViewLimitTimeBinding.stvTime.startTimer(timeLimitPremiumTtl)

                    mViewLimitTimeBinding.stvTime.onEndTime = {

                        mFloatingView?.removeView()

                    }

                    mViewLimitTimeBinding.root.click {

                        LimitTimeDialog.newInstance().show(supportFragmentManager)

                    }
                    mFloatingView?.addView(mViewLimitTimeBinding.root)

                    mFloatingView?.showOrHide(mViewBinding.viewPager.currentItem == 0 || mViewBinding.viewPager.currentItem == 1)


                }

            } catch (e: Exception) {

                e.printStackTrace()
            }


        }



        FlowBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).observe(this) {

            if (it is Int) {

                mViewBinding.viewPager.setCurrentItem(it, false)

            }
        }

        FlowBus.with(AppConstant.EventConstant.EVENT_GET_UNREAD_COUNT).observe(this) {

            getTotalUnreadCount()

        }

        FlowBus.with(AppConstant.EventConstant.APP_ENTERED_FOREGROUND).observe(this) {

            mViewModel.online()
        }

        FlowBus.with(AppConstant.EventConstant.APP_INIT_ADJUST).observe(this) {

            if (it is String) {
                mViewModel.installInfo(it)
            }

        }

    }


    private fun initTabLayout() {

        mViewBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mainTabBinding = ViewMainTabBinding.bind(it)

                    mainTabBinding.ivImage.setImageResource(mSelectImages[tab.position])

                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mainTabBinding = ViewMainTabBinding.bind(it)

                    mainTabBinding.ivImage.setImageResource(mImages[tab.position])

                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

                if (tab?.position == 2) {
                    FlowBus.with(AppConstant.EventConstant.EVENT_TO_TOP_MESSAGE).postValue(0)

                }

            }

        })

    }


    private fun initViewPager() {

        mFragments.add(MainFragment())
        mFragments.add(LikeFragment())
        mFragments.add(ConversationFragment())
        mFragments.add(MineFragment())
//        mFragments.add(WlmFragment())
//        mFragments.add(MessageFragment())
//        mFragments.add(MeFragment())

        mViewBinding.apply {


            viewPager.adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int {

                    return mFragments.size

                }

                override fun createFragment(position: Int): Fragment {

                    return mFragments[position]

                }

            }

            TabLayoutMediator(
                tabLayout,
                viewPager,
                false,
                false
            ) { tab, position ->

                val mainTabBinding =
                    ViewMainTabBinding.inflate(LayoutInflater.from(this@MainActivity))

                tab.customView = mainTabBinding.root

                if (position == 0) {
                    mainTabBinding.ivImage.setImageResource(mSelectImages[position])
                } else {
                    mainTabBinding.ivImage.setImageResource(mImages[position])
                }


                tab.view.setOnLongClickListener {

                    return@setOnLongClickListener true
                }


            }.attach()

            viewPager.isUserInputEnabled = false

            viewPager.offscreenPageLimit = mFragments.size


            viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    when (position) {

                        0, 1 -> {

                            val timeLimitPremiumTtl =
                                UserInfoHold.userInfo?.timeLimitPremiumTtl ?: 0

                            if (timeLimitPremiumTtl <= 0) {
                                mFloatingView?.showOrHide(false)
                            } else {
                                mFloatingView?.showOrHide(true)
                            }

                        }

                        else -> {
                            mFloatingView?.showOrHide(false)
                        }

                    }

                }
            })

        }
    }


    private fun clientRIM() {


        RIMDispatcher.addListener(mMessageListener)

        RIMClient.onClientRIM(onSuccess = { isHasConnect ->


            RongCoreClient.addOnReceiveMessageListener(mOnReceiveMessageWrapperListener)

            createNoticeHandle()

        })

    }


    private fun startLocation() {

        if (hasOpenLocation) return

        hasOpenLocation = true

        locationHelper.onSuccess = { latitude: Double, longitude: Double, mAddress: Address ->

            mViewModel.saveLocation(latitude, longitude, mAddress)

        }

        locationHelper.fetchLocationInfo()


    }


    private fun initMatchDialog(
        userInfoData: UserInfoData,
        mCustomMessageExtraData: CustomMessageExtraData?
    ) {

        if (AppConstant.Constant.isShowBuy) return

        if (mCustomMessageExtraData == null) return

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED).not()) return

        val mMatchSuccessDialog = MatchSuccessDialog.newInstance(userInfoData, mCustomMessageExtraData)

        mMatchSuccessDialog.show(supportFragmentManager)

    }


    private fun initWlmNoticeDialog(
        mCustomMessageExtraData: CustomMessageExtraData?
    ) {

        if (AppConstant.Constant.isShowBuy) return

        if (mCustomMessageExtraData == null) return

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED).not()) return

        val mWlmNoticeDialog = WlmNoticeDialog.newInstance(mCustomMessageExtraData)

        mWlmNoticeDialog.onConfirm = {

            mViewBinding.viewPager.setCurrentItem(1,false)
        }

        mWlmNoticeDialog.show(supportFragmentManager)

    }


    private fun showMatch(message: Message?) {

        if (!isVisibility) return

        if (null == message) return

        lifecycleScope.launch {

            try {

                if (message.objectName == AppConstant.RIMConstant.RC_CMD_MSG) {

                    if (AppConstant.Constant.isShowBuy) return@launch

                    val messageExtraData = getCmdMessageExtraData(message) ?: return@launch

                    when(messageExtraData.eventCode){

                        AppConstant.RIMConstant.CMD_MATCH_SUCCESS ->{

                            if (mViewBinding.viewPager.currentItem == 2) return@launch

                            if (messageExtraData.data?.oriSource == AppConstant.RIMConstant.CMD_FLASH_CHAT) return@launch

                            mViewModel.getTargetUserInfo(
                                messageExtraData.data?.userId2,
                                messageExtraData.data
                            )
                        }
                        AppConstant.RIMConstant.CMD_NEW_WHO_LIKE_ME ->{

                            initWlmNoticeDialog(messageExtraData)

                            FlowBus.with(AppConstant.EventConstant.EVENT_REFRESH_WLM_LIST).postValue(true)
                        }
                        else -> {

                            FloatingWindowUtil.showFloatMessage(this@MainActivity, message)
                        }

                    }

                } else {

                    if (mViewBinding.viewPager.currentItem == 2) return@launch

                    FloatingWindowUtil.showFloatMessage(this@MainActivity, message)

                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    fun getTotalUnreadCount() {
        try {

            RIMClient.getTotalUnreadCount(onSuccess = {

                val tab = mViewBinding.tabLayout.getTabAt(2) ?: return@getTotalUnreadCount

                tab.customView?.let { view ->

                    val mainTabBinding = ViewMainTabBinding.bind(view)

                    if (it > 0) {
                        mainTabBinding.stvMessageCount.visibility = View.VISIBLE
                        if (it > 999) {
                            mainTabBinding.stvMessageCount.text = "${999}+"
                        } else {
                            mainTabBinding.stvMessageCount.text = "$it"
                        }

                    } else {

                        mainTabBinding.stvMessageCount.visibility = View.GONE

                    }

                }

            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initLocationDialog() {


        if (areAllPermissionsGranted(this, permission)) return


        val mFullLocationDialog = FullLocationDialog().apply {

            onConfirm = {

                requestPermission()
            }
        }

        mFullLocationDialog.show(supportFragmentManager)

        reportEvent(mPopPopupDialogKey[0], true)

    }


    private fun requestPermission() {


        if (shouldShowPermissionRationale(this, permission)) {

            openPermissionDetail()
        } else {

            registerForActivityResult.launch(permission)
        }

    }


    private fun initSwipGuideDialog() {

        val hasSwip = getCache(AppConstant.Constant.HAS_SWIP, false)

        if (hasSwip) return

        noSwipTimeJob = lifecycleScope.launch {

            while (isActive) {

                noSwipTime--

                delay(1000)


                if (noSwipTime <= 0) {

                    SwipGuideDialog().apply {

                        onConfirm = {

                            mViewBinding.viewPager.setCurrentItem(0, false)

                            FlowBus.with(AppConstant.EventConstant.EVENT_SWIP_PAGE).postValue(2)
                        }
                    }.show(supportFragmentManager)

                    this.cancel()
                }

            }

        }
        setCache(AppConstant.Constant.HAS_SWIP, true)
    }

    fun cancelNoSwipTimeJob() {

        noSwipTimeJob?.cancel()

    }


    private fun createNoticeHandle() {

        if (touchType.isEmpty()) return

        when (touchType) {

            "Home" -> {

                val params = mutableMapOf<String, Any?>()

                when (pushMark) {

                    "Match" -> {
                        params["m_type"] = "new_match"
                    }

                    "Recall" -> {
                        params["m_type"] = "recall"
                    }

                    else -> {
                        params["m_type"] = "new_message"
                    }

                }

                reportEvent(mMessageNoticeKey[2], params)

            }

            "Wlm" -> {

                val params = mutableMapOf<String, Any?>()

                params["m_type"] = "wlm"

                reportEvent(mMessageNoticeKey[2], params)

                mViewBinding.viewPager.setCurrentItem(1, false)

            }

            "Chat" -> {


                if (targetId.isNotEmpty() && targetId != AppConstant.RIMConstant.SYSTEM_NOTICE) {

                    pushMark = when (pushMark) {

                        "Match" -> {
                            "new_match"
                        }

                        "Recall" -> {
                            "recall"
                        }

                        else -> {
                            "new_message"
                        }

                    }

                    createIntent(ChatActivity::class.java)
                        .putExtra(
                            AppConstant.Constant.TARGET_ID,
                            targetId
                        )
                        .putExtra(
                            AppConstant.Constant.PUSH_MARK,
                            pushMark
                        )
                        .putExtra(
                            AppConstant.Constant.PUSH_MARK,
                            pushMark
                        )
                        .putExtra(
                            AppConstant.Constant.IS_OFFLINE,
                            true
                        )
                        .startActivity(this@MainActivity)
                }

            }

        }


        cancelNotification(this)

    }


    override fun onBackPressed() {
        if (false) {
            super.onBackPressed()
        }
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastBackTime > 1000) {
            lastBackTime = currentTimeMillis
            showShort("Press exit again!")
        } else {
            moveTaskToBack(true)
        }
    }


    override fun onDestroy() {
        RIMDispatcher.removeListener(mMessageListener)
        RongCoreClient.removeOnReceiveMessageListener(mOnReceiveMessageWrapperListener)
        mFloatingView?.removeView()
        mFloatingView = null
        super.onDestroy()
    }
}