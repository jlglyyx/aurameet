package com.chat.jolt.dialog

import android.os.Bundle
import android.view.Gravity
import com.chat.jolt.databinding.DialogEditBirthBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.click
import com.chat.lib_common.util.dateFormat
import com.chat.lib_common.util.edgeToEdgeBottom

import java.util.Date
import kotlin.apply
import kotlin.let
import kotlin.text.isNotEmpty


class EditBirthDialog : BaseDialog<DialogEditBirthBinding>(DialogEditBirthBinding::inflate) {

    var onConfirm: (Date?,Int) -> Unit = {_,_ ->}


    private var mCurrentDate: Date? = null

    private var mStartDate: Date? = null




    companion object {
        fun newInstance(data: String): EditBirthDialog {
            return EditBirthDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()


            arguments?.let {

                val data = it.getString(AppConstant.Constant.DATA, "")

                if (data.isNotEmpty()) {
                    mTimePickView.setCurrentTime(data)
                }
            }

            tvAge.text = "You're ${mTimePickView.getCurrentAge()}"

            mCurrentDate = mTimePickView.getCurrentDate()

            mStartDate = mCurrentDate

            mTimePickView.onTimeChange = { date, age ->

                tvAge.text = "You're $age"

                mCurrentDate = date

                tvCommit.isEnabled = mCurrentDate?.dateFormat("yyyy.MM.dd") != mStartDate?.dateFormat("yyyy.MM.dd")
            }


            tvCommit.click {


                onConfirm(mCurrentDate,mTimePickView.getCurrentAge())

                dismissAllowingStateLoss()


            }

        }

    }

    override fun initData() {


    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}