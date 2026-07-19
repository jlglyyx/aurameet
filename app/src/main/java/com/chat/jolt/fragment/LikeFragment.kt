package com.chat.jolt.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chat.jolt.R
import com.chat.jolt.databinding.FraLikeBinding
import com.chat.jolt.databinding.ViewLikeTabBinding
import com.chat.jolt.viewmodel.LikeViewModel
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.util.getColor
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LikeFragment : BaseFragment<FraLikeBinding, LikeViewModel>(FraLikeBinding::inflate) {


    private val mFragments = mutableListOf<Fragment>()

    private val mTitles = mutableListOf("Who Likes you","You Liked")


    override fun initView() {


        withViewBinding {


            initViewPager()

            initTabLayout()


        }


    }

    override fun onResume() {
        super.onResume()
    }

    override fun initData() {


    }

    override fun initViewModel() {


        FlowBus.with(AppConstant.EventConstant.EVENT_SET_LIKE_PAGE).observe(this){

            if (it is Int) {

                mViewBinding.viewPager.setCurrentItem(it, false)

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

                    val mViewLikeTabBinding = ViewLikeTabBinding.bind(it)

                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.white))

                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewLikeTabBinding = ViewLikeTabBinding.bind(it)

                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))

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

        mFragments.add(LikeItemFragment())
        mFragments.add(ILikeItemFragment())

        mViewBinding.apply {


            viewPager.adapter = object : FragmentStateAdapter(this@LikeFragment) {
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
                true
            ) { tab, position ->

                val mViewLikeTabBinding =
                    ViewLikeTabBinding.inflate(LayoutInflater.from(context))

                tab.customView = mViewLikeTabBinding.root

                mViewLikeTabBinding.tvTitle.text = mTitles[position]

                if (position == 0) {
                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.white))
                } else {
                    mViewLikeTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))
                }


                tab.view.setOnLongClickListener {

                    return@setOnLongClickListener true
                }


            }.attach()

            viewPager.isUserInputEnabled = true

            viewPager.offscreenPageLimit = mFragments.size

            val tabStrip = tabLayout.getChildAt(0) as ViewGroup
            for (i in 0 until tabStrip.childCount) {
                val tabView = tabStrip.getChildAt(i)
                tabView.setPadding(0)
            }

        }
    }




}