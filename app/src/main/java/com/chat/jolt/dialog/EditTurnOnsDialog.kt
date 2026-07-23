package com.chat.jolt.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.adapter.HobbyTagAdapter
import com.chat.jolt.adapter.TurnsOnsAdapter
import com.chat.jolt.data.HobbyTagData
import com.chat.jolt.data.TagData
import com.chat.jolt.databinding.DialogEditTurnOnsBinding
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.toJson

import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNullOrEmpty
import kotlin.collections.toMutableList
import kotlin.collections.toSet
import kotlin.let
import kotlin.text.isNotEmpty

class EditTurnOnsDialog : BaseDialog<DialogEditTurnOnsBinding>(DialogEditTurnOnsBinding::inflate) {

    var onConfirm: (MutableList<TagData>) -> Unit = {}

    private val mTurnsOnsAdapter by lazy{ TurnsOnsAdapter() }

    private var list:MutableList<String> = mutableListOf()

    private var mStartList:MutableList<String> = mutableListOf()

    private val mViewModel by activityViewModels<UserViewModel>()


    companion object {
        fun newInstance(data: MutableList<String>): EditTurnOnsDialog {
            return EditTurnOnsDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {


            root.edgeToEdgeTop()

            ivClose.click {

                dismissAllowingStateLoss()
            }

            tvSave.click {

               val list =  mTurnsOnsAdapter.items.filter { it.isCheck }.toMutableList()

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

            initTurnsOnsRecyclerView(mStartList)
        }

    }


    private fun initTurnsOnsRecyclerView(turnOnsTags: MutableList<String>){

        val toMutableSet = turnOnsTags.toMutableSet()

        val list = mViewModel.getTurnOnsData().map {

            it.value.isCheck = toMutableSet.contains(it.value.userTag)

            it.value
        }

        withViewBinding {

            recyclerView.adapter = mTurnsOnsAdapter

            recyclerView.layoutManager = GridLayoutManager(requireContext(),2)

            mTurnsOnsAdapter.submitList(list)

        }

        mTurnsOnsAdapter.setOnItemClickListener { _, _, position ->

            val item = mTurnsOnsAdapter.getItem(position) ?: return@setOnItemClickListener

            item.isCheck = !item.isCheck

            mTurnsOnsAdapter.notifyItemChanged(position,false)




            val toSet1 = mStartList.toSet()

            val toSet =
                mTurnsOnsAdapter.items.filter { it.isCheck }.map { it.userTag }.toMutableList()
                    .toSet()
            mDialogBinding.tvSave.isEnabled = toSet != toSet1


        }


    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }
}