package com.chat.jolt.dialog

import android.content.Context
import android.widget.LinearLayout
import com.chat.jolt.R
import com.chat.lib_common.util.click
import com.lxj.xpopup.core.AttachPopupView

class TextOperationDialog(context: Context,val isMe: Boolean): AttachPopupView(context) {

    var onCopy: () -> Unit = {}

    var onDelete: () -> Unit = {}

    var onReport: () -> Unit = {}

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_text_operation
    }


    override fun onCreate() {
        super.onCreate()


        if (isMe){
            findViewById<LinearLayout>(R.id.ll_report).visibility = GONE
        }else{
            findViewById<LinearLayout>(R.id.ll_report).visibility = VISIBLE
        }


        findViewById<LinearLayout>(R.id.ll_copy).click {


            onCopy()


            dismiss()
        }
        findViewById<LinearLayout>(R.id.ll_delete).click {


            onDelete()

            dismiss()
        }
        findViewById<LinearLayout>(R.id.ll_report).click {


            onReport()

            dismiss()
        }
    }

}