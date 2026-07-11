package com.chat.jolt.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.chat.jolt.databinding.DialogEditAvatarBinding
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.CompressUtil.isValidMedia
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.showShort
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.getValue
import kotlin.let
import kotlin.toString

class EditAvatarDialog : BaseDialog<DialogEditAvatarBinding>(DialogEditAvatarBinding::inflate) {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0] / 2

    private val pictureHeight = pictureWidth * 4 / 3

    var onConfirm: (String, Uri?) -> Unit = { _, _ -> }

    var onPickMedia: (Uri) -> Unit = {}

    private var mFileUri: Uri? = null

    private var url: String = ""


    private val mViewModel by activityViewModels<UserViewModel>()

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        mFileUri = onPhotoPicked(uri)
    }

    private val pickLegacy = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            mFileUri = onPhotoPicked(uri)
        } else {
            mFileUri = onPhotoPicked(null)
        }
    }


    companion object {
        fun newInstance(data: String): EditAvatarDialog {
            return EditAvatarDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.URL, data)
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

                if (null != mFileUri) {

                    onConfirm(url, mFileUri)

                } else {

                    dismissAllowingStateLoss()
                }

            }

            stvEdit.click {

                pickImage()
            }

        }

    }

    override fun initData() {

        arguments?.let {

            url = it.getString(AppConstant.Constant.URL) ?: url

            mDialogBinding.ivImage.loadImage(requireContext(), url, pictureWidth, pictureHeight)
        }

    }

    override fun initViewModel() {
        super.initViewModel()

        mViewModel.mOssData.observe(this) {

            OSSUtil.uploadPicture(
                it,
                it.uploadType,
                it.uploadUri,
                onSuccess = { path, uploadId ->

                    lifecycleScope.launch {

                        if (isActive){
                            if (isVisible) {
                                withViewBinding {

                                    url = path.toString()

                                    ivLoad.visibility = View.GONE

                                    tvSave.isEnabled = true

                                    ivLoad.stop()
                                }

                            }
                        }

                    }


                },
                onError = { uploadId ->
                    if (isVisible) {
                        withViewBinding {

                            ivLoad.visibility = View.GONE

                            ivLoad.stop()

                        }
                    }

                })


        }
    }


    private fun onPhotoPicked(uri: Uri?): Uri? {

        if (null == uri) return null


        if (!isValidMedia(requireContext(), uri, true)) {

            showShort("too large, cannot upload")

            return null
        }

        withViewBinding {

            ivImage.loadImage(requireContext(), uri)

            ivLoad.visibility = View.VISIBLE
        }

        val id = "ID${System.currentTimeMillis()}${(Math.random() * 1000).toInt()}"

        onPickMedia(uri)

        mViewModel.ossAuth(id, OSSUtil.AVATAR, uri)

        return uri
    }


    private fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickLegacy.launch(Intent.createChooser(intent, "Select Picture"))
        }
    }

    fun uploadSuccess() {

        withViewBinding {

            ivLoad.visibility = View.GONE
        }
    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}