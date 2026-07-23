package com.chat.jolt.activity

import android.animation.ValueAnimator
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.jolt.R
import com.chat.jolt.adapter.TurnsOnsAdapter
import com.chat.jolt.databinding.ActIWantStepBinding
import com.chat.jolt.databinding.ItemIWantStepBinding
import com.chat.jolt.viewmodel.MainViewModel
import  com.chat.jolt.data.SocialAimData
import  com.chat.jolt.data.UpdateUserInfoData
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.tracking.reportRegisterStep
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.replaceEmoji
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.startLoadingAnimation
import com.chat.lib_common.util.viewVisibility
import kotlin.jvm.java


class IWantStepActivity : BaseActivity<ActIWantStepBinding, MainViewModel>(ActIWantStepBinding::inflate) {


    private var currentSocial = 0

    private var loadingAnimator: ValueAnimator? = null

    private lateinit var mAdapter: BaseRecyclerAdapter<SocialAimData, ItemIWantStepBinding>

    private val mTurnsOnsAdapter by lazy{ TurnsOnsAdapter() }


    private val wantList = mutableListOf(R.drawable.iv_want_0,R.drawable.iv_want_1,R.drawable.iv_want_2,R.drawable.iv_want_3)

    private val unWantList = mutableListOf(R.drawable.iv_want_0_un,R.drawable.iv_want_1_un,R.drawable.iv_want_2_un,R.drawable.iv_want_3_un)


    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()


        mViewBinding.apply {

            if (UserInfoHold.isReview || UserInfoHold.isOrganic){

                viewVisibility(View.GONE,tv0,turnsOnsRecyclerView)
            }else{
                viewVisibility(View.VISIBLE,tv0,turnsOnsRecyclerView)
            }


            initRecyclerView()

            initTurnsOnsRecyclerView()

            stvNext.click {

                if (loadingAnimator?.isRunning == true) return@click

                val updateUserInfoData = UpdateUserInfoData()

                updateUserInfoData.socialAim = mAdapter.getItem(currentSocial)?.socialAim

                updateUserInfoData.turnOnsTags = mTurnsOnsAdapter.items.filter { it.isCheck }.map { it.userTag }.toMutableList()

                loadingAnimator = stvNext.startLoadingAnimation("Next")

                mViewModel.updateUserInfo(updateUserInfoData)


            }

        }


    }

    override fun initViewModel() {


        mViewModel.mSocialAimData.observe(this) {

            if (it.isNotEmpty()){
                it[0].isCheck = true
            }
            mAdapter.submitList(it)
        }

        mViewModel.mUpdateUserInfoStatus.observe(this) {

            loadingAnimator?.cancel()


            reportRegisterStep(2,false, UserInfoHold.isOrganic)

            createIntent(HobbyStepActivity::class.java).startActivity(this@IWantStepActivity,true)
        }

        mViewModel.requestFailEvent.observe(this) {

            loadingAnimator?.cancel()


        }
        mViewModel.mTurnTagData.observe(this) {

            mTurnsOnsAdapter.submitList(it)
        }

    }




    override fun initData() {

        mViewModel.initSocialAim()

        if (mViewModel.getTurnOnsData().isEmpty()){
            mViewModel.initTag2()
        }
    }



    private fun initRecyclerView() {

        withViewBinding {

            mAdapter = object :
                BaseRecyclerAdapter<SocialAimData, ItemIWantStepBinding>(ItemIWantStepBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemIWantStepBinding>,
                    itemView: ItemIWantStepBinding,
                    item: SocialAimData,
                    position: Int
                ) {
                    itemView.tvTitle.text = item.socialAimName.replaceEmoji()

                    itemView.tvTitle.isEnabled = item.isCheck

                    if(item.isCheck){

                        itemView.siv0.setImageResource(wantList[position])

                        itemView.ivImage.visibility = View.VISIBLE

                        itemView.sllContainer.shapeDrawableBuilder.setSolidColor(getColor(R.color.color_FDDBFF))
                            .setStrokeColor(getColor(R.color.color_FDDBFF)).intoBackground()


                    }else{

                        itemView.siv0.setImageResource(unWantList[position])
                        itemView.ivImage.visibility = View.GONE
                        itemView.sllContainer.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                            .setStrokeColor(getColor(R.color.transparent)).intoBackground()
                    }

                }

            }
            recyclerView.adapter = mAdapter
            recyclerView.layoutManager = GridLayoutManager(this@IWantStepActivity,2)

            mAdapter.setOnItemClickListener{ _,_,position ->

                val item = mAdapter.getItem(position)

                item?.let {

                    val lastItem = mAdapter.getItem(currentSocial)

                    if (null != lastItem){

                        lastItem.isCheck = false

                        mAdapter.notifyItemChanged(currentSocial,false)
                    }

                    currentSocial = position

                    item.isCheck = true

                    mAdapter.notifyItemChanged(currentSocial,false)

                }

            }
        }

    }


    private fun initTurnsOnsRecyclerView(){

        val list = mViewModel.getTurnOnsData().map {

            it.value
        }

        withViewBinding {

            turnsOnsRecyclerView.adapter = mTurnsOnsAdapter

            turnsOnsRecyclerView.layoutManager = GridLayoutManager(this@IWantStepActivity,2)

            mTurnsOnsAdapter.submitList(list)
        }



        mTurnsOnsAdapter.setOnItemClickListener { _, _, position ->

            val item = mTurnsOnsAdapter.getItem(position) ?: return@setOnItemClickListener

            item.isCheck = !item.isCheck

            mTurnsOnsAdapter.notifyItemChanged(position,false)
        }
    }



    override fun onDestroy() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        super.onDestroy()
    }


}