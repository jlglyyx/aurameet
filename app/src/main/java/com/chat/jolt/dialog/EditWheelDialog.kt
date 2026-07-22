package com.chat.jolt.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.chat.jolt.R
import com.chat.jolt.data.ProfessionData
import com.chat.jolt.data.SocialAimData
import com.chat.jolt.databinding.DialogEditWheelBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.replaceEmoji
import kotlin.apply
import kotlin.collections.indexOfFirst
import kotlin.collections.map
import kotlin.collections.toMutableList
import kotlin.let
import kotlin.text.isNotEmpty

class EditWheelDialog : BaseDialog<DialogEditWheelBinding>(DialogEditWheelBinding::inflate) {

    var onConfirm: ((String,String) -> Unit)? = null


    private var list = mutableListOf<String>()

    private var keyList = mutableListOf<String>()

    private lateinit var mAdapter: ArrayWheelAdapter<String>


    private var mCurrentItem = ""

    private var mStartItem = ""

    private var mCurrentIndex = 0


    companion object {
        fun newInstance(data: String, type: Int): EditWheelDialog {
            return EditWheelDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                    putInt(AppConstant.Constant.TYPE, type)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            tvClose.click {

                dismissAllowingStateLoss()
            }

            tvSave.click {


                mCurrentItem = mAdapter.getItem(mCurrentIndex).toString()


                val key = if(keyList.isEmpty()){
                   ""
                }else{
                    keyList[mCurrentIndex]
                }

                onConfirm?.invoke(mCurrentItem,key)

                dismissAllowingStateLoss()

            }

        }




    }

    override fun initData() {

        arguments?.let {

            val type = it.getInt(AppConstant.Constant.TYPE, -1)

            mCurrentItem = it.getString(AppConstant.Constant.DATA, mCurrentItem)

            mStartItem = mCurrentItem

            list = createData(type)

            val startIndex = list.indexOfFirst { it == mStartItem }

            mAdapter = ArrayWheelAdapter(list)

            mDialogBinding.wheelItem.apply {
                adapter = mAdapter
                setCyclic(false)
                cameraDistance = 20f
                setDividerColor(Color.TRANSPARENT)
                setTextSize(20f)
                setTextColorCenter(requireContext().getColor(R.color.firstTextColor))
                setTextColorOut(requireContext().getColor(R.color.color_999999))
                currentItem = list.indexOfFirst { it == mCurrentItem }
                setOnItemSelectedListener { index ->

                    try {
                        mCurrentIndex = index

                        mDialogBinding.tvSave.isEnabled = startIndex != mCurrentIndex
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }
            }

        }


    }


    private fun createData(type: Int): MutableList<String> {
        //0 weight 1 height 2 Profession 3 i want
        return when (type) {
            0 -> {

                mDialogBinding.tvTitle.text = "Weight"

                mDialogBinding.tvUnit.text = "KG"

                (40..120).map { "${it}" }.toMutableList()



            }

            1 -> {

                mDialogBinding.tvTitle.text = "Height"

                mDialogBinding.tvUnit.text = "CM"

                (130..220).map { "${it}" }.toMutableList()
            }

            2 -> {

                mDialogBinding.tvTitle.text = "Profession"

                mDialogBinding.tvUnit.visibility = View.GONE

                val mProfessionCache = getCache(AppConstant.Constant.PROFESSION, "")

                if (mProfessionCache.isNotEmpty()) {

                    val mProfessionData = mProfessionCache.formatListJson<ProfessionData>()

                    keyList = mProfessionData.map { it.profession }.toMutableList()

                    mProfessionData.map { it.professionName }.toMutableList()

                } else {

                    mutableListOf()
                }

            }

            3 -> {

                val mSocialAimCache = getCache(AppConstant.Constant.SOCIAL_AIM, "")

                mDialogBinding.tvUnit.visibility = View.GONE

                if (mSocialAimCache.isNotEmpty()) {

                    val mSocialAimData = mSocialAimCache.formatListJson<SocialAimData>()

                    keyList = mSocialAimData.map { it.socialAim }.toMutableList()

                    mSocialAimData.map { it.socialAimName.replaceEmoji() }.toMutableList()

                } else {

                    mutableListOf()
                }
            }

            else -> {

                mutableListOf()
            }
        }


    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}