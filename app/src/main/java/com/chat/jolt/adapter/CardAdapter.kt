package com.chat.jolt.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils.getColor
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.VibrateUtils
import com.chat.jolt.R
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.databinding.ItemCardBinding
import com.chat.jolt.databinding.ItemCardHobbyBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.mTranslation
import com.chat.lib_common.util.replaceEmoji
import com.chat.lib_common.util.viewVisibility
import com.youth.banner.config.IndicatorConfig
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.isNullOrEmpty
import kotlin.collections.take
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceAtMost
import kotlin.text.isNullOrEmpty

class CardAdapter() :
    BaseRecyclerAdapter<ModelUserData, ItemCardBinding>(ItemCardBinding::inflate) {


    var sharedPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()

    private var mObjectAnimator: ObjectAnimator? = null


    private var startNotice = getCache(AppConstant.Constant.START_NOTICE, false)

    private val mTranslationDuration = 300L

    private val mTranslationX = 10f

    private val mVibrateDuration = 20L

    private val mIndicatorHeight = 4f.dip2px(Utils.getApp())

    private val mBannerRound = 15f.dip2px(Utils.getApp()).toFloat()

    private val mAvatarWidth = 56f.dip2px(Utils.getApp())

    private val mSpaceWidth = 80f.dip2px(Utils.getApp())


    override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemCardBinding>) {
        super.onInitViewHolder(holder)

        changePicture(holder)

    }


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemCardBinding>,
        itemView: ItemCardBinding,
        item: ModelUserData,
        position: Int
    ) {
        itemView.clContainer.rotation = 0f
        itemView.clContainer.translationX = 0f
        holder.itemView.rotation = 0f
        holder.itemView.translationX = 0f

        itemView.apply {

            tvName.text = "${item.nickname}·${item.age}"

//            viewVisibility(View.VISIBLE,tvContent,tv0)

            hobbyFlexbox.visibility = View.GONE

            if (!item.aim.isNullOrEmpty()) {


                tvContent.text = item.aim.replaceEmoji()

                sllContainer.visibility = View.VISIBLE
            } else {
                sllContainer.visibility = View.INVISIBLE
            }

            ivAvatar.loadImage(context,item.headPic,mAvatarWidth,mAvatarWidth)

            stvTurn.tag = if (item.turnOnsTags.isNullOrEmpty()) "empty" else "full"

            if (UserInfoHold.isReview || UserInfoHold.isOrganic){

                stvTurn.visibility = View.GONE

                tvContent.visibility = View.VISIBLE
                tv0.visibility = View.VISIBLE

            }else{

                if (item.turnOnsTags.isNullOrEmpty()){

                    stvTurn.visibility = View.GONE

                    tvContent.visibility = View.VISIBLE
                    tv0.visibility = View.VISIBLE
                }else{

                    stvTurn.visibility = View.VISIBLE

                    tvContent.visibility = View.GONE
                    tv0.visibility = View.GONE

                    stvTurn.text = "${item.turnOnsTags?.size?:0} Turn-ons"
                }

            }


            hobbyFlexbox.removeAllViews()

            if (item.commonHobbyTags.isNullOrEmpty()){

                item.hobbyTags.take(3).forEach {

                    ItemCardHobbyBinding.inflate(LayoutInflater.from(context), hobbyFlexbox, true)
                        .apply {

                            tvTitle.text = it
                        }

                }
            }else{

                val commonTake = item.commonHobbyTags.take(3)

                commonTake.forEach {

                    ItemCardHobbyBinding.inflate(LayoutInflater.from(context), hobbyFlexbox, true)
                        .apply {

                            tvTitle.text = it
                            tvTitle.isEnabled = false
                        }

                }

                if (commonTake.size < 3){

                    val hobbyTake = item.hobbyTags.take(3 - commonTake.size)

                    hobbyTake.forEach {

                        ItemCardHobbyBinding.inflate(LayoutInflater.from(context), hobbyFlexbox, true)
                            .apply {

                                tvTitle.text = it
                            }

                    }
                }

            }



            if (position == 0) {


                ivNoticePicChange.visibility = if (startNotice) View.GONE else View.VISIBLE
            } else {

                ivNoticePicChange.visibility = View.GONE
            }


            if (item.onlineStatus == "Online") {

                tvOnline.text = "Online"

            } else {

                tvOnline.text = "Active"

            }

            if (item.nearby == "True") {

                tvAddress.text = "Nearby"

                viewVisibility(View.VISIBLE, sllAddress)

            } else {

                viewVisibility(View.GONE, sllAddress)
            }

            initBanner(holder, item.coverPics)

        }
    }


    private fun changePicture(holder: BaseRecyclerViewHolder<ItemCardBinding>) {

        holder.binding.apply {

            ivNoticePicChange.setOnClickListener {

                startNotice = true

                ivNoticePicChange.visibility = View.GONE

                FlowBus.with(AppConstant.EventConstant.EVENT_SHOW_SWIP_GUIDE).postValue(true)
            }


            viewLeft.setOnClickListener {

                try {

                    val currentItem = banner.currentItem

                    if (currentItem == 0) {

                        mObjectAnimator = null

                        mObjectAnimator =
                            clContainer.mTranslation("X", 0f, -mTranslationX, mTranslationX, 0f)

                        mObjectAnimator?.apply {

                            setDuration(mTranslationDuration)

                            start()
                        }

                        VibrateUtils.vibrate(mVibrateDuration)

                        return@setOnClickListener
                    }

                    banner.currentItem = (currentItem - 1).coerceAtLeast(0)


                    if (UserInfoHold.isOrganic || stvTurn.tag == "empty" || UserInfoHold.isReview){

                        if (banner.currentItem == 0) {
                            viewVisibility(View.VISIBLE,tvContent,tv0)
                            viewVisibility(View.GONE,stvTurn,hobbyFlexbox)
                        }
                        else {
                            hobbyFlexbox.visibility = View.VISIBLE
                            viewVisibility(View.GONE,tvContent,tv0,stvTurn)
                        }
                    }else{
                        if (banner.currentItem == 0) {
                            viewVisibility(View.GONE,tvContent,tv0,hobbyFlexbox)
                            stvTurn.visibility = View.VISIBLE

                        }else if (banner.currentItem == 1) {
                            viewVisibility(View.VISIBLE,tvContent,tv0)
                            viewVisibility(View.GONE,stvTurn,hobbyFlexbox)
                        }
                        else {
                            hobbyFlexbox.visibility = View.VISIBLE
                            viewVisibility(View.GONE,tvContent,tv0,stvTurn)
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            viewRight.setOnClickListener {

                try {

                    val currentItem = banner.currentItem

                    if (currentItem == banner.itemCount - 1) {

                        mObjectAnimator = null

                        mObjectAnimator =
                            clContainer.mTranslation("X", 0f, -mTranslationX, mTranslationX, 0f)

                        mObjectAnimator?.apply {

                            setDuration(mTranslationDuration)

                            start()
                        }

                        VibrateUtils.vibrate(mVibrateDuration)

                        return@setOnClickListener
                    }

                    banner.currentItem = (currentItem + 1).coerceAtMost(
                        banner.itemCount - 1
                    )

//                    scrollView.scrollBy(mIndicatorWidth,0)

                    if (UserInfoHold.isOrganic || stvTurn.tag == "empty" || UserInfoHold.isReview){

                        if (banner.currentItem == 0) {
                            viewVisibility(View.VISIBLE,tvContent,tv0)
                            viewVisibility(View.GONE,stvTurn,hobbyFlexbox)
                        }
                        else {
                            hobbyFlexbox.visibility = View.VISIBLE
                            viewVisibility(View.GONE,tvContent,tv0,stvTurn)
                        }
                    }else{
                        if (banner.currentItem == 0) {
                            viewVisibility(View.GONE,tvContent,tv0,hobbyFlexbox)
                            stvTurn.visibility = View.VISIBLE

                        }else if (banner.currentItem == 1) {
                            viewVisibility(View.VISIBLE,tvContent,tv0)
                            viewVisibility(View.GONE,stvTurn,hobbyFlexbox)
                        }
                        else {
                            hobbyFlexbox.visibility = View.VISIBLE
                            viewVisibility(View.GONE,tvContent,tv0,stvTurn)
                        }
                    }


                } catch (e: Exception) {

                    e.printStackTrace()
                }


            }
        }

    }


    private fun initBanner(
        holder: BaseRecyclerViewHolder<ItemCardBinding>,
        coverPics: MutableList<String>?
    ) {

        holder.binding.apply {

            val size = coverPics?.size?:1


            llIndicator.post {

               val mIndicatorWidth = (llIndicator.width-mSpaceWidth)/size

               val mCardImageAdapter = CardImageAdapter(mutableListOf())

               banner.setUserInputEnabled(false)
                   .setAdapter(mCardImageAdapter, false)
                   .isAutoLoop(false)
                   .setBannerRound(mBannerRound)
                   .setIndicator(mIndicator, false)
                   .setIndicatorSelectedColor(getColor(R.color.white))
                   .setIndicatorNormalColor(getColor(R.color.white_20))
                   .setIndicatorHeight(mIndicatorHeight)
                   .setIndicatorMargins(IndicatorConfig.Margins(20,0,20,0))
                   .setIndicatorWidth(mIndicatorWidth, mIndicatorWidth)
               mCardImageAdapter.setDatas(coverPics)
            }



        }
    }


}