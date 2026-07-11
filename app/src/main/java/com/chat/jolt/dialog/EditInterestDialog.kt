package com.chat.jolt.dialog

import android.os.Bundle
import android.view.Gravity
import com.chat.jolt.adapter.HobbyTagAdapter
import com.chat.jolt.data.HobbyTagData
import com.chat.jolt.databinding.DialogEditInterestBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.toJson

import com.google.android.flexbox.FlexboxLayoutManager
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNullOrEmpty
import kotlin.collections.toMutableList
import kotlin.collections.toSet
import kotlin.let
import kotlin.text.isNotEmpty

class EditInterestDialog : BaseDialog<DialogEditInterestBinding>(DialogEditInterestBinding::inflate) {

    var onConfirm: (MutableList<HobbyTagData>) -> Unit = {}

    private lateinit var mHobbyTagAdapter: HobbyTagAdapter

    private var list:MutableList<String> = mutableListOf()

    private var mStartList:MutableList<String> = mutableListOf()


    companion object {
        fun newInstance(data: MutableList<String>): EditInterestDialog {
            return EditInterestDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {


            root.edgeToEdgeBottom()

            ivClose.click {

                dismissAllowingStateLoss()
            }

            tvSave.click {

               val list =  mHobbyTagAdapter.items.filter { it.isCheck }.toMutableList()

                onConfirm(list)

                dismissAllowingStateLoss()

            }

        }



    }

    override fun initData() {

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")

            if (data.isNotEmpty()){

                list = data.formatListJson<String>()

                mStartList = list
            }

            initRecyclerView()
        }

    }


    private fun initRecyclerView() {

        withViewBinding {

            mHobbyTagAdapter = HobbyTagAdapter()

            recyclerView.layoutManager = FlexboxLayoutManager(requireContext())

            recyclerView.adapter = mHobbyTagAdapter

            mHobbyTagAdapter.setOnItemClickListener{ _, _, position ->

                val item = mHobbyTagAdapter.getItem(position)

                item?.let {

                    item.isCheck = !item.isCheck

                    mHobbyTagAdapter.notifyItemChanged(position,false)

                    val toSet = mHobbyTagAdapter.items.filter { it.isCheck }.map {
                        it.hobbyTagName
                    }.toSet()

                    val toSet1 = mStartList.toSet()

                    tvSave.isEnabled = toSet != toSet1

                }

            }




            val cacheHobbyTag = mHobbyTagAdapter.getCacheHobbyTag()

            if (!cacheHobbyTag.isNullOrEmpty()){

                val stringSet = list.toSet()

                cacheHobbyTag.forEach { item ->

                    if (stringSet.contains(item.hobbyTagName)){

                        item.isCheck = true
                    }else{
                        item.isCheck = false
                    }
                }
                mHobbyTagAdapter.submitList(cacheHobbyTag)

            }




        }

    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}