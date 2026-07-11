package com.chat.jolt.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.R
import com.chat.jolt.adapter.HobbyTagAdapter
import com.chat.jolt.adapter.UserPictureAdapter
import com.chat.jolt.data.PictureData
import com.chat.jolt.databinding.ActHobbyStepBinding
import com.chat.jolt.dialog.PictureDetailDialog
import com.chat.jolt.viewmodel.MainViewModel
import  com.chat.jolt.data.UpdateUserInfoData
import  com.chat.jolt.data.UploadPictureData
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.tracking.reportRegisterStep
import com.chat.lib_common.util.CompressUtil
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeAll
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.startLoadingAnimation
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.collections.isNullOrEmpty
import kotlin.collections.map
import kotlin.collections.toMutableList
import kotlin.jvm.java
import kotlin.let


class HobbyStepActivity :
    BaseActivity<ActHobbyStepBinding, MainViewModel>(ActHobbyStepBinding::inflate) {


    private lateinit var mHobbyTagAdapter: HobbyTagAdapter

    private lateinit var mUserPictureAdapter: UserPictureAdapter

    private var loadingAnimator: ValueAnimator? = null

    private var mPictureDetailDialog: PictureDetailDialog? = null


    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPhotoPicked(uri)
    }

    private val pickLegacy = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            onPhotoPicked(uri)
        }
    }


    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()

        mViewBinding.apply {


            initRecyclerView()

            initPictureRecyclerView()

            tvSkip.click {

                reportRegisterStep(3,true, UserInfoHold.isOrganic)

                createIntent(MainActivity::class.java).startActivity(
                    this@HobbyStepActivity,
                    true
                )
            }

            stvNext.click {

                val mHobbyList = mHobbyTagAdapter.items.filter { it.isCheck }.map {
                    it.hobbyTag
                }.toMutableList()

                val mPictureList = mUserPictureAdapter.items.filter { filter -> filter.httpUrl != null }
                    .map { map -> map.httpUrl.toString() }.toMutableList()

                if (mPictureList.isEmpty() && mHobbyList.isEmpty()) {

                    createIntent(MainActivity::class.java).startActivity(
                        this@HobbyStepActivity,
                        true
                    )

                } else {
                    if (loadingAnimator?.isRunning == true) return@click

                    val mUpdateUserInfoData = UpdateUserInfoData()

                    mUpdateUserInfoData.coverPics = mPictureList

                    mUpdateUserInfoData.hobbyTags = mHobbyList

                    loadingAnimator = stvNext.startLoadingAnimation("Done")

                    mViewModel.updateUserInfo(mUpdateUserInfoData)
                }

            }
        }


    }

    override fun initViewModel() {


        mViewModel.mHobbyTagData.observe(this) {

            if (it.isNotEmpty()) {
                it.first().isCheck = true
            }
            mHobbyTagAdapter.submitList(it)
        }

        mViewModel.mOssData.observe(this) {

            OSSUtil.uploadPicture(
                it,
                it.uploadType,
                it.uploadUri,
                onSuccess = { path, uploadId ->

                    handleUploadStatus(uploadId, OSSUtil.UPLOAD_STATUS_SUCCESS, path)

                },
                onError = { uploadId ->

                    handleUploadStatus(uploadId, OSSUtil.UPLOAD_STATUS_ERROR, null)

                })


        }

        mViewModel.mUpdateUserInfoStatus.observe(this) {

            loadingAnimator?.cancel()



            reportRegisterStep(3,false, UserInfoHold.isOrganic)

            createIntent(MainActivity::class.java).startActivity(this@HobbyStepActivity, true)

        }

        mViewModel.requestFailEvent.observe(this) {

            loadingAnimator?.cancel()



        }

    }


    override fun initData() {

        createData()

    }

    private fun initPictureRecyclerView() {


        withViewBinding {

            mUserPictureAdapter = UserPictureAdapter()

            pictureRecyclerView.adapter = mUserPictureAdapter

            pictureRecyclerView.layoutManager = GridLayoutManager(this@HobbyStepActivity, 3)


            mUserPictureAdapter.setOnItemClickListener { _, _, position ->

                val item = mUserPictureAdapter.getItem(position) ?: return@setOnItemClickListener

                when (item.status) {

                    OSSUtil.UPLOAD_STATUS_NORMAL -> {

                        pickImage()

                    }

                    else -> {

                        val data = if (item.url is Uri) {
                            PictureData("", null, item.url.toString())
                        } else {
                            PictureData(item.url.toString(), null, null)
                        }
                        initPictureDetailDialog(mutableListOf(data))
                    }
                }

            }

            mUserPictureAdapter.addOnItemChildClickListener(R.id.iv_delete) { _, _, position ->

                val item =
                    mUserPictureAdapter.getItem(position) ?: return@addOnItemChildClickListener

                item.status = 0

                item.url = null

                item.httpUrl = null

                mUserPictureAdapter.notifyItemChanged(position, false)
            }

        }

    }


    private fun initRecyclerView() {

        withViewBinding {

            mHobbyTagAdapter = HobbyTagAdapter()

            recyclerView.layoutManager = FlexboxLayoutManager(this@HobbyStepActivity)

            recyclerView.adapter = mHobbyTagAdapter

            mHobbyTagAdapter.setOnItemClickListener { _, _, position ->

                val item = mHobbyTagAdapter.getItem(position)

                item?.let {

                    item.isCheck = !item.isCheck

                    mHobbyTagAdapter.notifyItemChanged(position, false)

                }

            }

            val cacheHobbyTag = mHobbyTagAdapter.getCacheHobbyTag()

            if (cacheHobbyTag.isNullOrEmpty()) {

                mViewModel.initTag()

            } else {

                if (cacheHobbyTag.isNotEmpty()) {
                    cacheHobbyTag.first().isCheck = true
                }

                mHobbyTagAdapter.submitList(cacheHobbyTag)
            }


        }

    }

    private fun createData() {

        val data = MutableList(6){
            UploadPictureData()
        }
        mUserPictureAdapter.submitList(data)
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

    private fun onPhotoPicked(uri: Uri?): Uri? {

        if (null == uri) return null

        if (!CompressUtil.isValidMedia(this, uri, true)) {

            showShort("too large, cannot upload")

            return null
        }


        val index = mUserPictureAdapter.items.indexOfFirst { it.status == OSSUtil.UPLOAD_STATUS_NORMAL }

        if (index != -1) {

            val item = mUserPictureAdapter.getItem(index) ?: return null

            item.status = OSSUtil.UPLOAD_STATUS_LOADING

            item.url = uri

            mUserPictureAdapter.notifyItemChanged(index, false)

            mViewModel.ossAuth(item.id, OSSUtil.COVER, uri)
        }

        return uri
    }


    private fun handleUploadStatus(uploadId: String?, status: Int, path: String? = null) {

        lifecycleScope.launch {

            try {

                if (!isActive) return@launch

                if (uploadId.isNullOrEmpty()) return@launch

                val index = mUserPictureAdapter.items.indexOfFirst { find -> find.id == uploadId }

                if (index != -1) {

                    val item = mUserPictureAdapter.getItem(index) ?: return@launch

                    item.status = status

                    if (status == OSSUtil.UPLOAD_STATUS_SUCCESS) {

                        item.httpUrl = path

                    } else if (status == OSSUtil.UPLOAD_STATUS_ERROR) {

                        showShort("upload error")
                    }

                    mUserPictureAdapter.notifyItemChanged(index, false)

                }

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }

    }

    private fun initPictureDetailDialog(data: MutableList<PictureData>) {

        mPictureDetailDialog?.dismissAllowingStateLoss()

        mPictureDetailDialog = PictureDetailDialog.newInstance(data, 0)

        mPictureDetailDialog?.show(supportFragmentManager)
    }


    override fun onDestroy() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        super.onDestroy()
    }

    override fun onBackPressed() {

        if (false) {
            super.onBackPressed()
        }

    }
}