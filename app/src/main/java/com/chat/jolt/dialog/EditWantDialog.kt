package com.chat.jolt.dialog

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.R
import com.chat.jolt.data.SocialAimData
import com.chat.jolt.databinding.DialogEditWantBinding
import com.chat.jolt.databinding.ItemIWantStepBinding
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.replaceEmoji

class EditWantDialog : BaseDialog<DialogEditWantBinding>(DialogEditWantBinding::inflate) {

    var onConfirm: ((String, String) -> Unit)? = null

    private val wantList = mutableListOf(
        R.drawable.iv_want_0,
        R.drawable.iv_want_1,
        R.drawable.iv_want_2,
        R.drawable.iv_want_3
    )

    private val unWantList = mutableListOf(
        R.drawable.iv_want_0_un,
        R.drawable.iv_want_1_un,
        R.drawable.iv_want_2_un,
        R.drawable.iv_want_3_un
    )

    private lateinit var mAdapter: BaseRecyclerAdapter<SocialAimData, ItemIWantStepBinding>

    private var mCurrentItem = ""

    private var mStartItem = ""

    private var mCurrentIndex = 0

    private var keyList = mutableListOf<String>()

    companion object {
        fun newInstance(data: String): EditWantDialog {
            return EditWantDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            initRecyclerView()

            arguments?.let {

                mCurrentItem = it.getString(AppConstant.Constant.DATA, mCurrentItem)

                mStartItem = mCurrentItem

                val mSocialAimCache = getCache(AppConstant.Constant.SOCIAL_AIM, "")

                if (mSocialAimCache.isNotEmpty()) {

                    val mSocialAimData = mSocialAimCache.formatListJson<SocialAimData>()


                    mCurrentIndex = mSocialAimData.indexOfFirst { find -> find.socialAimName.contains(mStartItem) }

                    mSocialAimData.findLast { find -> find.socialAimName.contains(mStartItem) }?.isCheck = true



                    mAdapter.submitList(mSocialAimData)

                }

            }


            ViewCompat.setOnApplyWindowInsetsListener(sclContainer) { v, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom


                val offset = if (imeHeight > 0) imeHeight else navHeight

                if (v.translationY != -offset.toFloat()) {
                    v.translationY = -offset.toFloat()
                }


                insets
            }


            root.click {

                dismissAllowingStateLoss()
            }

            ivClose.click {

                dismissAllowingStateLoss()

            }

            tvSave.click {

                mCurrentItem = mAdapter.getItem(mCurrentIndex).toString()


                val key = if (keyList.isEmpty()) {
                    ""
                } else {
                    keyList[mCurrentIndex]
                }

                onConfirm?.invoke(mCurrentItem, key)

                dismissAllowingStateLoss()
            }


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

                    if (item.isCheck) {

                        itemView.siv0.setImageResource(wantList[position])

                        itemView.ivImage.visibility = View.VISIBLE

                        itemView.sllContainer.shapeDrawableBuilder.setSolidColor(getColor(R.color.color_FDDBFF))
                            .setStrokeColor(getColor(R.color.color_FDDBFF)).intoBackground()


                    } else {

                        itemView.siv0.setImageResource(unWantList[position])
                        itemView.ivImage.visibility = View.GONE
                        itemView.sllContainer.shapeDrawableBuilder.setSolidColor(getColor(R.color.transparent))
                            .setStrokeColor(getColor(R.color.transparent)).intoBackground()
                    }

                }

            }
            recyclerView.adapter = mAdapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

            mAdapter.setOnItemClickListener { _, _, position ->

                val item = mAdapter.getItem(position)

                item?.let {

                    val lastItem = mAdapter.getItem(mCurrentIndex)

                    if (null != lastItem) {

                        lastItem.isCheck = false

                        mAdapter.notifyItemChanged(mCurrentIndex, false)
                    }

                    mCurrentIndex = position

                    item.isCheck = true

                    mAdapter.notifyItemChanged(mCurrentIndex, false)


                    mDialogBinding.tvSave.isEnabled =  !item.socialAimName.contains(mStartItem)

                }

            }
        }

    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    override fun initData() {
    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


}