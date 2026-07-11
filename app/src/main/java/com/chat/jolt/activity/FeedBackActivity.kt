package com.chat.jolt.activity

import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.chat.jolt.databinding.ActFeedbackBinding
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.showShort
import kotlin.text.contains
import kotlin.text.isNotEmpty
import kotlin.toString


class FeedBackActivity : BaseActivity<ActFeedbackBinding, UserViewModel>(ActFeedbackBinding::inflate) {


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            setText.doAfterTextChanged {

                mViewBinding.tvCount.text = "${mViewBinding.setText.text.toString().length}/500"

                mViewBinding.stvNext.isEnabled = (mViewBinding.setText as TextView).text.toString().isNotEmpty()

            }

            stvNext.click {

                val textEmail = mViewBinding.setEmails.text.toString()

                if (textEmail.isNotEmpty() && !textEmail.contains("@")) {
                    showShort("Email format error")
                    return@click
                }

                mViewModel.feedback(mViewBinding.setText.text.toString(),textEmail)

            }
        }




    }

    override fun initData() {

    }

    override fun initViewModel() {

        mViewModel.mFeedBackStatus.observe(this) {

            showShort("FeedBack Success")

            finish()
        }

    }



}