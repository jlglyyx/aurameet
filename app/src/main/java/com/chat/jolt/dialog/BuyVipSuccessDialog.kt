package com.chat.jolt.dialog


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter4.animation.ItemAnimator
import com.chat.jolt.R
import com.chat.jolt.data.Privilege
import com.chat.jolt.databinding.DialogBuyVipSuccessBinding
import com.chat.jolt.databinding.ItemVipSuccessBinding
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.toJson

class BuyVipSuccessDialog : BaseDialog<DialogBuyVipSuccessBinding>(DialogBuyVipSuccessBinding::inflate) {


    private lateinit var mVipAdapter: BaseRecyclerAdapter<Privilege, ItemVipSuccessBinding>


    private var imageMap = mutableMapOf(
        "UnlimitedChat" to R.drawable.iv_unlimited_chat_vip_success,
        "MoreSwipe" to R.drawable.iv_more_match_vip_success,
        "WhoLikesMe" to R.drawable.iv_who_likes_vip_success,
        "FlashChat" to R.drawable.iv_flash_chat_vip_success,
        "PremiumBadge" to R.drawable.iv_special_badge_vip_success,
        "SecretAlbum" to R.drawable.iv_private_album_vip_success,
        "SecretPhoto" to R.drawable.iv_private_photo_vip_success,
        "SecretVideo" to R.drawable.iv_private_video_vip_success,
        "MyVisitor" to R.drawable.iv_visitor_vip_success,
    )


    private var mPrivilegeList: MutableList<Privilege>? = null

    companion object {
        fun newInstance(data: List<Privilege>): BuyVipSuccessDialog {
            return BuyVipSuccessDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            initRecyclerView()


            ivClose.click {

                dismissAllowingStateLoss()
            }


            stvNext.click {


                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

        AppConstant.Constant.isShowBuy = true

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")

            if (data.isNotEmpty()) {

                mPrivilegeList = data.formatListJson()
            }

        }
        mVipAdapter.submitList(mPrivilegeList)
    }


    private fun initRecyclerView() {

        mVipAdapter = object : BaseRecyclerAdapter<Privilege, ItemVipSuccessBinding>(ItemVipSuccessBinding::inflate) {
            override fun convert(
                holder: BaseRecyclerViewHolder<ItemVipSuccessBinding>,
                itemView: ItemVipSuccessBinding,
                item: Privilege,
                position: Int
            ) {

                itemView.apply {
                    holder.itemView.tag = holder.bindingAdapterPosition
                    tvTitle.text = item.title

                    ivImage.setImageResource(imageMap[item.type] ?: R.drawable.iv_more_match_vip_success)

                }

            }

        }


        mDialogBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        mDialogBinding.recyclerView.adapter = mVipAdapter

        mVipAdapter.isAnimationFirstOnly = false

        mVipAdapter.itemAnimation = object :ItemAnimator{
            override fun animator(view: View): Animator {
                view.alpha = 0f
                val position = view.tag as? Int ?: 0
                val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.8f, 1f)
                val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.8f, 1f)
                val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
                val translationY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 100f, 0f)
                val set = AnimatorSet()
                set.playTogether(scaleX, scaleY, alpha,translationY)
                set.duration = 300L
                set.startDelay = position * 100L
                set.interpolator = DecelerateInterpolator()
                return set
            }

        }

    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        AppConstant.Constant.isShowBuy = false
    }






}