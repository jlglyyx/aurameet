package com.chat.jolt.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.chat.jolt.R
import com.chat.jolt.adapter.HobbyTagAdapter
import com.chat.jolt.data.HobbyTagData
import com.chat.jolt.databinding.DialogFilterCardBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.toJson
import com.google.android.flexbox.FlexboxLayoutManager
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.findLast
import kotlin.collections.forEach
import kotlin.collections.indexOfFirst
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.toMutableList
import kotlin.collections.toSet
import kotlin.let
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty

class FilterCardDialog: BaseDialog<DialogFilterCardBinding>(DialogFilterCardBinding::inflate) {

    private lateinit var mSexAdapter : HobbyTagAdapter

    private lateinit var mInterestAdapter : HobbyTagAdapter

    private var distant:Int = 100

    private var minAge:Int = 18

    private var maxAge:Int = 35

    private var sexType:String = ""


    private var currentSex = 2

    var onConfirm:(Int,Int,Int,String,MutableList<String>) ->Unit = { _,_,_,_,_ ->}

    private var mHobbyTagList:MutableList<HobbyTagData> = mutableListOf()

    companion object {
        fun newInstance(distant: Int,minAge:Int,maxAge:Int,sexType:String,hobbyTags:MutableList<String>): FilterCardDialog {
            return FilterCardDialog().apply {
                arguments = Bundle().apply {
                    putInt("distant", distant)
                    putInt("minAge", minAge)
                    putInt("maxAge", maxAge)
                    putString("sexType", sexType)
                    putString("hobbyTags", hobbyTags.toJson())
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setStyle(STYLE_NORMAL, R.style.Theme_Dialog)

        return super.onCreateDialog(savedInstanceState)
    }


    override fun initView() {

       withViewBinding {

           root.edgeToEdgeAll()


           ivConfirmFilter.click {


               onConfirm(distant,minAge,maxAge,sexType,mInterestAdapter.items.filter { it.isCheck }.map { it.hobbyTagName }.toMutableList())

               dismissAllowingStateLoss()
           }

           ivCloseFilter.click {

               dismissAllowingStateLoss()
           }




       }

    }



    override fun initData() {

        val mHobbyTag = getCache(AppConstant.Constant.HOBBY_TAG, "")

        mHobbyTagList = if (mHobbyTag.isEmpty()){

            mutableListOf()
        }else{

            mHobbyTag.formatListJson()
        }


        arguments?.let {

            distant = it.getInt("distant", distant)
            minAge = it.getInt("minAge", minAge)
            maxAge = it.getInt("maxAge", maxAge)
            sexType = it.getString("sexType", sexType)
           val hobbyTag = it.getString("hobbyTags", "")

            if (hobbyTag.isNotEmpty()){

               val list = hobbyTag.formatListJson<String>().toSet()

                if (list.isNotEmpty()){
                    mHobbyTagList.forEach { item ->

                        item.isCheck = list.contains(item.hobbyTagName)
                    }
                }

            }


            withViewBinding {

                rsDistant.setValues(distant.toFloat())

                tvDistant.text = "${distant}km"

                tvDistant.text = if (distant >= 100) "∞" else "${distant}km"

                rsAge.setValues(minAge.toFloat(),maxAge.toFloat())

                tvAge.text = "${minAge}-${maxAge}"


                rsDistant.addOnChangeListener { slider, value, fromUser ->

                    distant = value.toInt()

                    tvDistant.text = if (distant >= 100) "∞" else "${distant}km"

                }
                rsAge.addOnChangeListener { slider, value, fromUser ->

                    val values = slider.values

                    minAge = values[0].toInt()

                    maxAge = values[1].toInt()

                    tvAge.text = "${minAge}-${maxAge}"
                }

                initSexRecyclerView()

                initInterestRecyclerView()
            }

        }


        mInterestAdapter.submitList(mHobbyTagList)


    }

    private fun initSexRecyclerView(){

        mSexAdapter = HobbyTagAdapter()

        withViewBinding {

            recyclerView.adapter = mSexAdapter

            recyclerView.layoutManager = FlexboxLayoutManager(requireContext())

            mSexAdapter.setOnItemClickListener{ _, _, position ->

                val item = mSexAdapter.getItem(position)

                item?.let {

                    val lastItem = mSexAdapter.getItem(currentSex)

                    if (null != lastItem){
                        lastItem.isCheck = false

                        mSexAdapter.notifyItemChanged(currentSex,false)
                    }

                    item.isCheck = true

                    currentSex = position

                    sexType = item.hobbyTag

                    mSexAdapter.notifyItemChanged(currentSex,false)

                }

            }

            val list = mutableListOf<HobbyTagData>().apply {

                add(HobbyTagData("Male", "Male"))
                add(HobbyTagData("Female", "Female"))
                add(HobbyTagData("", "All"))
            }

            val indexOfFirst = list.indexOfFirst { it.hobbyTag == sexType }

            if (indexOfFirst != -1){

                currentSex = indexOfFirst
            }

            list.findLast { it.hobbyTag ==  sexType}?.isCheck = true

            mSexAdapter.submitList(list)


        }

    }
    private fun initInterestRecyclerView(){

        withViewBinding {

            mInterestAdapter = HobbyTagAdapter()

            interestRecyclerView.adapter = mInterestAdapter

            interestRecyclerView.layoutManager = FlexboxLayoutManager(requireContext())


            mInterestAdapter.setOnItemClickListener{ _, _, position ->

                val item = mInterestAdapter.getItem(position)

                item?.let {

                    it.isCheck = !it.isCheck


                    mInterestAdapter.notifyItemChanged(position,false)

                }

            }

            mInterestAdapter.submitList(mHobbyTagList)
        }

    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun setDialogGravity(): Int {
        return Gravity.TOP
    }

}