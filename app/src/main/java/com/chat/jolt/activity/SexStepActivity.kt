package com.chat.jolt.activity

import android.animation.ValueAnimator
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.R
import com.chat.jolt.data.FigureData
import com.chat.jolt.databinding.ActSexStepBinding
import com.chat.jolt.viewmodel.MainViewModel
import  com.chat.jolt.data.UpdateUserInfoData
import com.chat.jolt.databinding.ItemFigureBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.tracking.reportRegisterStep
import com.chat.lib_common.tracking.updateSolarEngineUser
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.startLoadingAnimation
import com.chat.lib_common.util.viewVisibility
import kotlin.jvm.java


class SexStepActivity : BaseActivity<ActSexStepBinding, MainViewModel>(ActSexStepBinding::inflate) {

    private var isFirst = true

    private var currentSex = 0

    private var currentFigure = -1

    private var loadingAnimator: ValueAnimator? = null

    private lateinit var mFigureAdapter: BaseRecyclerAdapter<FigureData, ItemFigureBinding>

    private val manList = mutableListOf<FigureData>()

    private val womanList = mutableListOf<FigureData>()


    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()




        mViewBinding.apply {




            llMale.setOnClickListener {

                setSex(0)
            }


            llFemale.setOnClickListener {

                setSex(1)
            }


            llOther.setOnClickListener {

                setSex(2)
            }


            stvNext.click {

                if (loadingAnimator?.isRunning == true) return@click

                val updateUserInfoData = UpdateUserInfoData()

                updateUserInfoData.sex = when (currentSex) {
                    0 -> "Male"
                    1 -> "Female"
                    else -> "Other"
                }

                loadingAnimator = stvNext.startLoadingAnimation("Next")

                mViewModel.updateUserInfo(updateUserInfoData)

                updateSolarEngineUser("gender",updateUserInfoData.sex)

            }

            initRecyclerView()

            setSex(0)
        }


    }

    override fun initViewModel() {


        mViewModel.mUpdateUserInfoStatus.observe(this) {

            loadingAnimator?.cancel()



            reportRegisterStep(1,false, UserInfoHold.isOrganic)

            createIntent(IWantStepActivity::class.java).startActivity(this@SexStepActivity, true)
        }

        mViewModel.requestFailEvent.observe(this) {

            loadingAnimator?.cancel()



        }

    }


    override fun initData() {

    }


    private fun setSex(index: Int) {

        if (!isFirst && currentSex == index) return

        isFirst = false

        currentSex = index

        mFigureAdapter.getItem(currentFigure)?.isCheck = false

        currentFigure = -1

        withViewBinding {


            when (index) {

                0 -> {

                    tvMale.setTextColor(getColor(R.color.firstTextColor))
                    tvFemale.setTextColor(getColor(R.color.white))
                    tvOther.setTextColor(getColor(R.color.white))

                    ivMale.setImageResource(R.drawable.iv_select_media_check)
                    ivFemale.setImageResource(R.drawable.iv_sex_normal)
                    ivOther.setImageResource(R.drawable.iv_sex_normal)

                    llMale.shapeDrawableBuilder.setSolidColor(getColor(R.color.color_FDDBFF))
                        .setStrokeColor(getColor(R.color.transparent)).intoBackground()
                    llFemale.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                        .setStrokeColor(getColor(R.color.white)).intoBackground()
                    llOther.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                        .setStrokeColor(getColor(R.color.white)).intoBackground()


                    mFigureAdapter.submitList(womanList)

                }

                1 -> {

                    tvMale.setTextColor(getColor(R.color.white))
                    tvFemale.setTextColor(getColor(R.color.firstTextColor))
                    tvOther.setTextColor(getColor(R.color.white))

                    ivMale.setImageResource(R.drawable.iv_sex_normal)
                    ivFemale.setImageResource(R.drawable.iv_select_media_check)
                    ivOther.setImageResource(R.drawable.iv_sex_normal)

                    llMale.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                        .setStrokeColor(getColor(R.color.white)).intoBackground()
                    llFemale.shapeDrawableBuilder.setSolidColor(getColor(R.color.color_FDDBFF))
                        .setStrokeColor(getColor(R.color.transparent)).intoBackground()
                    llOther.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                        .setStrokeColor(getColor(R.color.white)).intoBackground()



                    mFigureAdapter.submitList(manList)
                }

                2 -> {

                    tvMale.setTextColor(getColor(R.color.white))
                    tvFemale.setTextColor(getColor(R.color.white))
                    tvOther.setTextColor(getColor(R.color.firstTextColor))

                    ivMale.setImageResource(R.drawable.iv_sex_normal)
                    ivFemale.setImageResource(R.drawable.iv_sex_normal)
                    ivOther.setImageResource(R.drawable.iv_select_media_check)


                    llMale.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                        .setStrokeColor(getColor(R.color.white)).intoBackground()
                    llFemale.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                        .setStrokeColor(getColor(R.color.white)).intoBackground()
                    llOther.shapeDrawableBuilder.setSolidColor(getColor(R.color.color_FDDBFF))
                        .setStrokeColor(getColor(R.color.transparent)).intoBackground()

                    mFigureAdapter.submitList(null)
                }

            }

        }

    }


    private fun initFigureData() {


        manList.add(FigureData("Short", R.drawable.iv_short_male))
        manList.add(FigureData("Average", R.drawable.iv_average_male))
        manList.add(FigureData("Stocky", R.drawable.iv_stocky_male))
        manList.add(FigureData("Muscular", R.drawable.iv_muscular_male))



        womanList.add(FigureData("Slim", R.drawable.iv_slim))
        womanList.add(FigureData("Athletic", R.drawable.iv_athletic))
        womanList.add(FigureData("Average", R.drawable.iv_average_female))
        womanList.add(FigureData("Curvy", R.drawable.iv_curvy))
        womanList.add(FigureData("Stocky", R.drawable.iv_stocky_female))
        womanList.add(FigureData("Full-Figured", R.drawable.iv_full_figured))
        womanList.add(FigureData("Fit", R.drawable.iv_fit))
        womanList.add(FigureData("Muscular", R.drawable.iv_muscular_female))
        womanList.add(FigureData("Doll", R.drawable.iv_doll))
        womanList.add(FigureData("Tall", R.drawable.iv_tall))
        womanList.add(FigureData("Short", R.drawable.iv_short_female))


    }


    private fun initRecyclerView() {

        initFigureData()

        mFigureAdapter = object :
            BaseRecyclerAdapter<FigureData, ItemFigureBinding>(ItemFigureBinding::inflate) {
            override fun convert(
                holder: BaseRecyclerViewHolder<ItemFigureBinding>,
                itemView: ItemFigureBinding,
                item: FigureData,
                position: Int
            ) {

                itemView.tvFigure.text = item.title

                itemView.ivImage.setImageResource(item.resourcesId)

                if (item.isCheck) {

                    viewVisibility(View.VISIBLE, itemView.ivFigureCheck)

                    itemView.tvFigure.shapeDrawableBuilder.setSolidColor(getColor(R.color.color_EAA82B))
                        .intoBackground()

                } else {

                    viewVisibility(View.GONE, itemView.ivFigureCheck)

                    itemView.tvFigure.shapeDrawableBuilder.setSolidColor(getColor(R.color.black_30))
                        .intoBackground()
                }

            }


        }

        mViewBinding.recyclerView.adapter = mFigureAdapter

        mViewBinding.recyclerView.layoutManager = GridLayoutManager(this, 2)


        mFigureAdapter.setOnItemClickListener { _, _, position ->

            val lastItem = mFigureAdapter.getItem(currentFigure)

            if (null != lastItem) {

                lastItem.isCheck = false

                mFigureAdapter.notifyItemChanged(currentFigure,false)

            }

            val item = mFigureAdapter.getItem(position) ?: return@setOnItemClickListener

            item.isCheck = true

            currentFigure = position

            mFigureAdapter.notifyItemChanged(currentFigure,false)
        }


        if (UserInfoHold.isReview || UserInfoHold.isOrganic){

            viewVisibility(View.GONE,mViewBinding.tv0,mViewBinding.tv1,mViewBinding.recyclerView)

        }else{
            viewVisibility(View.VISIBLE,mViewBinding.tv0,mViewBinding.tv1,mViewBinding.recyclerView)
        }


    }


    override fun onDestroy() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        super.onDestroy()
    }

    override fun onBackPressed() {

        if (false) {
            super.onBackPressed()
        }

    }

}