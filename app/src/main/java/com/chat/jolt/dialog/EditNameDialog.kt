package com.chat.jolt.dialog

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.chat.jolt.databinding.DialogEditNameBinding
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.ScreenChangeUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.hideSoftInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.let
import kotlin.ranges.coerceAtLeast
import kotlin.text.isBlank
import kotlin.text.trim
import kotlin.toString

class EditNameDialog : BaseDialog<DialogEditNameBinding>(DialogEditNameBinding::inflate) {

    var onConfirm: (String) -> Unit = {}


    private var startText = ""


    companion object {
        fun newInstance(data: String): EditNameDialog {
            return EditNameDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            arguments?.let {

                val name = it.getString(AppConstant.Constant.DATA, "")

                startText = name

                setText.setText(name)
                setText.setSelection(setText.text.toString().length)

            }


            setText.doAfterTextChanged {

                tvSave.isEnabled = it.toString() != startText

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

                val toString = setText.text.toString()

                if (toString.isBlank()) return@click

                onConfirm(toString.trim())

                dismissAllowingStateLoss()
            }


            lifecycleScope.launch {

                delay(200)

                setText.hideSoftInput(requireContext(),true)
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