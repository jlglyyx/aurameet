package com.chat.jolt.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.R
import com.chat.jolt.adapter.OpenMediaAdapter
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.databinding.DialogSendMediaBinding
import com.chat.jolt.databinding.ViewSendMediaTabBinding
import com.chat.jolt.viewmodel.ChatViewModel
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.CompressUtil.isValidMedia
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.formatWithSymbol
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.viewVisibility
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendMediaDialog : BaseDialog<DialogSendMediaBinding>(DialogSendMediaBinding::inflate) {

    private lateinit var mOpenMediaAdapter: OpenMediaAdapter

    private var isCanSend: Boolean = true

    private var currentTabPosition = 0

    private var lastTabPosition = 0

    private var targetId: String? = null

    private var titles = arrayOf("Private Photo", "Private Video")

    private var maxSelect = 9

    private var isVideo = false

    private lateinit var mViewModel: ChatViewModel
//    private val mViewModel: ChatViewModel by sharedViewModels<ChatViewModel>()

    var onSendMessage: (MutableList<ModelMediaData>, String) -> Unit = { item, sendType ->

    }

    private var sendType = ""


    val list = mutableListOf<ModelMediaData>()

    private lateinit var mModelMediaData: ModelMediaData


    private var mSelectMediaDialog: SelectMediaDialog? = null

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPhotoPicked(uri)
    }

    private val pickLegacy = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            onPhotoPicked(uri)
        }
    }


    companion object {

        fun newInstance(
            targetId: String,
            isCanSend: Boolean = true
        ): SendMediaDialog {

            return SendMediaDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.TARGET_ID, targetId)
                    putBoolean(AppConstant.Constant.IS_CAN_SEND, isCanSend)
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun initView() {

        mViewModel = ViewModelProvider(this)[ChatViewModel::class]

        withViewBinding {

            appToolBar.mToolbarBinding.ivBack.setOnClickListener {

                dismissAllowingStateLoss()
            }

            root.edgeToEdgeBottom()

            tvDelete.click {

                val selectItem = mOpenMediaAdapter.items.filter { it.isCheck == true }

                val albumIdList = mutableListOf<String>()

                selectItem.forEach {

                    if (it.albumId.isNullOrEmpty()) {

                        list.remove(it)

                        mOpenMediaAdapter.remove(it)

                    } else {
                        it.albumId?.let { albumId ->

                            albumIdList.add(albumId)
                        }
                    }

                }

                if (albumIdList.isEmpty()) return@click

                mViewModel.deleteMedia(albumIdList)
            }



            tvCommit.click {


                val selectItem = mOpenMediaAdapter.items.filter { it.isCheck == true }

                val albumList = mutableListOf<ModelMediaData>()

                selectItem.forEach {
                    if (!it.albumId.isNullOrEmpty()) {

                        albumList.add(it)
                    }
                }

                if (albumList.isEmpty()) return@click

                if (!AppConstant.Constant.ppvsEnable) {
                    if (albumList.size != 1 && albumList.size != 4 && albumList.size != 9) {

                        showShort("Only 1, 4, or 9 photos or video can be sent at a time")

                        return@click
                    }
                }


                if (sendType == AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_MSG) {

                    if (albumList.size > 1) {
                        sendType = AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_S_MSG
                    }
                }
                if (sendType == AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_MSG) {

                    if (albumList.size > 1) {
                        sendType = AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_S_MSG
                    }
                }


                val albumIds =
                    albumList.map { it.albumId.toString() }.toMutableList().formatWithSymbol(",")

                albumList[0].content = albumIds

                onSendMessage(albumList, sendType)

                dismissAllowingStateLoss()

            }


            initTabLayout()

            initRecyclerView()
        }


    }

    override fun initData() {

        arguments?.let {
            targetId = it.getString(AppConstant.Constant.TARGET_ID)

            isCanSend = it.getBoolean(AppConstant.Constant.IS_CAN_SEND, isCanSend)
        }

        targetId?.let {
            mViewModel.queryMediaPhoto(
                it,
            )

            mViewModel.queryMediaVideo(
                it,
            )
        }

        if (isCanSend) {
            viewVisibility(View.VISIBLE, mDialogBinding.tvDelete, mDialogBinding.tvCommit)
        } else {
            viewVisibility(View.GONE, mDialogBinding.tvCommit)
        }


    }


    override fun initViewModel() {
        super.initViewModel()

        mViewModel.mModelMediaPhotoData.observe(this) {

            if (null == it.data) {

                it.data = mutableListOf()
            }

            handleData(it.data ?: mutableListOf())

            showMedia(0)

            setMediaCount(0)

        }
        mViewModel.mModelMediaVideoData.observe(this) {

            if (null == it.data) {

                it.data = mutableListOf()
            }

            handleData(it.data ?: mutableListOf())

            setMediaCount(1)

        }


        mViewModel.mOssData.observe(this) {

            if (it.uploadType == OSSUtil.VIDEO) {
                OSSUtil.uploadVideoWithCover(
                    it,
                    it.uploadUri,
                    onSuccess = { videoPath, imagePath, uploadId, videoDuration ->

                        if (isVisible) {
                        if (videoPath.isNullOrEmpty()) {

                            handleUploadStatus(it.uploadId, OSSUtil.UPLOAD_STATUS_ERROR)

                            return@uploadVideoWithCover
                        }

                        Log.i(
                            TAG,
                            "initViewModel: $isVisible  ${isDetached}  $isHidden $isCancelable"
                        )


                            mViewModel.addMedia(
                                uploadId,
                                "Video",
                                videoPath,
                                videoDuration.toInt(),
                                imagePath
                            )
                        }
                    },
                    onError = { uploadId ->

                        if (isVisible) {

                            handleUploadStatus(it.uploadId, OSSUtil.UPLOAD_STATUS_ERROR)
                        }

                    })

            } else {
                OSSUtil.uploadPicture(
                    it,
                    it.uploadType,
                    it.uploadUri,
                    onSuccess = { path, uploadId ->

                        if (isVisible) {

                        if (path.isNullOrEmpty()) {

                            handleUploadStatus(it.uploadId, OSSUtil.UPLOAD_STATUS_ERROR)

                            return@uploadPicture
                        }

                            mViewModel.addMedia(uploadId, "Photo", path)
                        }
                    },
                    onError = { uploadId ->

                        if (isVisible) {
                            handleUploadStatus(it.uploadId, OSSUtil.UPLOAD_STATUS_ERROR)
                        }
                    })

            }

        }


        mViewModel.requestFailEvent.observe(this) {

            if (it is String) {

                handleUploadStatus(it, OSSUtil.UPLOAD_STATUS_ERROR)

            }

        }

        mViewModel.mAddModelMediaData.observe(this) {

            handleUploadStatus(it.uploadId, OSSUtil.UPLOAD_STATUS_SUCCESS, it)

            setMediaCount(currentTabPosition)

        }

        mViewModel.mDeleteModelMediaStatus.observe(this) {

            try {
                it.forEach { id ->

                    val item =
                        mOpenMediaAdapter.items.findLast { findLast -> id == findLast.albumId }

                    item?.let {
                        mOpenMediaAdapter.remove(item)
                    }

                }
                list.clear()

                setMediaCount(currentTabPosition)

                setSendCount()
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


    }


    private fun handleData(data: MutableList<ModelMediaData>) {

        val mModelMediaData = "{}".fromJson<ModelMediaData>().apply {

            itemType = OpenMediaAdapter.ITEM_ADD_MEDIA
        }

        if (data.isEmpty()) {

            data.add(0, mModelMediaData)

        } else {
            if (data.size < 100) {
                data.add(0, mModelMediaData)
            }
            data.forEach { item -> item.uploadStatus = OSSUtil.UPLOAD_STATUS_SUCCESS }
        }

    }


    private fun initTabLayout() {

        titles.forEachIndexed { index, s ->

            mDialogBinding.tabLayout.addTab(mDialogBinding.tabLayout.newTab().apply {

                val mViewSendMediaTabBinding =
                    ViewSendMediaTabBinding.inflate(LayoutInflater.from(context))

                mViewSendMediaTabBinding.tvTitle.text = s

                if (index == 0) {

                    mViewSendMediaTabBinding.tvTitle.setTextColor(requireContext().getColor(R.color.white))
                    mViewSendMediaTabBinding.tvCount.setTextColor(requireContext().getColor(R.color.white))
                } else {
                    mViewSendMediaTabBinding.tvTitle.setTextColor(requireContext().getColor(R.color.color_999999))
                    mViewSendMediaTabBinding.tvCount.setTextColor(requireContext().getColor(R.color.color_999999))

                }
                customView = mViewSendMediaTabBinding.root
            })

        }

        val tabStrip = mDialogBinding.tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabStrip.childCount) {
            val tabView = tabStrip.getChildAt(i)
            tabView.setPadding(0)
        }


        mDialogBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewSendMediaTabBinding = ViewSendMediaTabBinding.bind(it)

                    mViewSendMediaTabBinding.tvTitle.setTextColor(getColor(R.color.white))
                    mViewSendMediaTabBinding.tvCount.setTextColor(getColor(R.color.white))

                }

                if (list.isEmpty()) {

                    lastTabPosition = tab.position
                }

                currentTabPosition = tab.position

                showMedia(currentTabPosition)

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewSendMediaTabBinding = ViewSendMediaTabBinding.bind(it)

                    mViewSendMediaTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))
                    mViewSendMediaTabBinding.tvCount.setTextColor(getColor(R.color.color_999999))

                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }


    private fun initRecyclerView() {

        mOpenMediaAdapter = OpenMediaAdapter(list)

        mDialogBinding.recyclerView.adapter = mOpenMediaAdapter

        mDialogBinding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        mDialogBinding.recyclerView.itemAnimator = null


        mOpenMediaAdapter.setOnItemClickListener { _, _, position ->

            val item = mOpenMediaAdapter.getItem(position) ?: return@setOnItemClickListener

            if (item.itemType == OpenMediaAdapter.ITEM_ADD_MEDIA) {


                pickImage()
            } else {

                if (item.uploadStatus != OSSUtil.UPLOAD_STATUS_SUCCESS) return@setOnItemClickListener

                val list = mOpenMediaAdapter.items.filter {
                    it.itemType != OpenMediaAdapter.ITEM_ADD_MEDIA
                }.filter {
                    it.uploadStatus == OSSUtil.UPLOAD_STATUS_SUCCESS
                }.toMutableList()

                val count = mOpenMediaAdapter.items.filter {
                    it.itemType != OpenMediaAdapter.ITEM_ADD_MEDIA
                }.count {
                    it.uploadStatus != OSSUtil.UPLOAD_STATUS_SUCCESS
                }


                val realPosition =
                    if (list.size == mOpenMediaAdapter.itemCount) position else (position - count-1).coerceAtLeast(
                        0
                    )

                initSelectMediaDialog(list, realPosition)
            }
        }


        mOpenMediaAdapter.addOnItemChildClickListener(R.id.cl_num) { _, _, position ->

            val item = mOpenMediaAdapter.getItem(position) ?: return@addOnItemChildClickListener

            item.let {

                if (it.isCheck == true) {

                    item.isCheck = false

                    list.remove(it)

                    for (i in 0 until list.size) {
                        val pos = mOpenMediaAdapter.items.indexOf(list[i])
                        if (pos != -1) mOpenMediaAdapter.notifyItemChanged(pos, false)
                    }

                } else {


                    if (list.size >= maxSelect) {

                        showShort("Maximum of $maxSelect  allowed")

                        return@addOnItemChildClickListener
                    }


                    item.isCheck = true

                    list.add(it)

                }

                mOpenMediaAdapter.notifyItemChanged(position, false)

                setSendCount()

            }

        }

    }


    private fun showMedia(type: Int) {

        list.clear()

        when (type) {

            0 -> {
                sendType = AppConstant.RIMConstant.RC_SEND_PRIVATE_IMAGE_MSG

                isVideo = false

                mOpenMediaAdapter.submitList(mViewModel.mModelMediaPhotoData.currentValue()?.data)
            }

            1 -> {
                sendType = AppConstant.RIMConstant.RC_SEND_PRIVATE_VIDEO_MSG

                isVideo = true

                mOpenMediaAdapter.submitList(mViewModel.mModelMediaVideoData.currentValue()?.data)
            }


        }

        currentTabPosition = type

        setSendCount()
    }


    private fun setSendCount() {

        mDialogBinding.tvCommit.isEnabled = list.isNotEmpty()

    }


    private fun onPhotoPicked(uri: Uri?): Uri? {

        if (null == uri) return null


        if (!isValidMedia(requireContext(), uri, !isVideo)) {

            showShort("too large, cannot upload")

            return null
        }


        val mLocalMessageId = "ID${System.currentTimeMillis()}"

        mModelMediaData = "{}".fromJson<ModelMediaData>().apply {

            isLocal = true

            id = mLocalMessageId

            uploadStatus = OSSUtil.UPLOAD_STATUS_LOADING

            this.uri = uri

            this.albumStatus = "Pass"

        }

        if (mOpenMediaAdapter.itemCount > 0) {

            lifecycleScope.launch(Dispatchers.Main) {
                if (mOpenMediaAdapter.items.first().itemType == OpenMediaAdapter.ITEM_ADD_MEDIA) {

                    mOpenMediaAdapter.add(1, mModelMediaData)
                } else {
                    mOpenMediaAdapter.add(0, mModelMediaData)
                }

            }

            mViewModel.ossAuth(mLocalMessageId, if (isVideo) OSSUtil.VIDEO else OSSUtil.ALBUM, uri)
        }


        return uri
    }


    private fun handleUploadStatus(
        uploadId: String?,
        status: Int,
        mModelMediaData: ModelMediaData? = null
    ) {

        lifecycleScope.launch(Dispatchers.Main) {

            try {

                val index =
                    mOpenMediaAdapter.items.indexOfFirst { it.id == uploadId }

                if (index != -1) {

                    val currentItem = mOpenMediaAdapter.getItem(index) ?: return@launch

                    currentItem.uploadStatus = status

                    if (null != mModelMediaData) {

                        currentItem.albumId = mModelMediaData.albumId

                        currentItem.albumUrl = mModelMediaData.albumUrl

                        currentItem.videoCover = mModelMediaData.videoCover

                        currentItem.duration = mModelMediaData.duration

                        currentItem.videoSeconds = mModelMediaData.videoSeconds

                        currentItem.albumType = mModelMediaData.albumType

                    }

                    mOpenMediaAdapter.notifyItemChanged(index, false)
                } else {

                    if (currentTabPosition == 0) {

                        val data =
                            mViewModel.mModelMediaVideoData.currentValue()?.data ?: return@launch

                        val index = data.indexOfFirst { it.id == uploadId }

                        if (index != -1) {

                            val currentItem = data[index]

                            currentItem.uploadStatus = status

                            if (null != mModelMediaData) {

                                currentItem.albumId = mModelMediaData.albumId

                                currentItem.albumUrl = mModelMediaData.albumUrl

                                currentItem.videoCover = mModelMediaData.videoCover

                                currentItem.duration = mModelMediaData.duration

                                currentItem.videoSeconds = mModelMediaData.videoSeconds

                                currentItem.albumType = mModelMediaData.albumType

                            }

                            setMediaCount(1)
                        }
                    } else {
                        val data =
                            mViewModel.mModelMediaPhotoData.currentValue()?.data ?: return@launch

                        val index = data.indexOfFirst { it.id == uploadId }

                        if (index != -1) {

                            val currentItem = data[index]

                            currentItem.uploadStatus = status

                            if (null != mModelMediaData) {

                                currentItem.albumId = mModelMediaData.albumId

                                currentItem.albumUrl = mModelMediaData.albumUrl

                                currentItem.videoCover = mModelMediaData.videoCover

                                currentItem.duration = mModelMediaData.duration

                                currentItem.videoSeconds = mModelMediaData.videoSeconds

                                currentItem.albumType = mModelMediaData.albumType

                            }

                            setMediaCount(0)
                        }
                    }

                }
            } catch (e: Exception) {

                e.printStackTrace()
            }


        }


    }

    private fun setMediaCount(index: Int) {

        val tabView = mDialogBinding.tabLayout.getTabAt(index) ?: return

        tabView.customView?.let {

            val mViewSendMediaTabBinding = ViewSendMediaTabBinding.bind(it)

            if (index == 0) {
                val currentPhoto = mViewModel.mModelMediaPhotoData.currentValue()?.data

                if (null == currentPhoto) {

                    mViewSendMediaTabBinding.tvCount.text = "(0/100)"

                    return
                }
                val item = currentPhoto.findLast {
                    it.itemType == OpenMediaAdapter.ITEM_ADD_MEDIA
                }
                if (null == item) {
                    mViewSendMediaTabBinding.tvCount.text = "(${currentPhoto.size}/100)"
                } else {
                    mViewSendMediaTabBinding.tvCount.text =
                        "(${(currentPhoto.size - 1).coerceAtLeast(0)}/100)"
                }

            } else {
                val currentVideo = mViewModel.mModelMediaVideoData.currentValue()?.data

                if (null == currentVideo) {

                    mViewSendMediaTabBinding.tvCount.text = "(0/100)"

                    return
                }
                val item = currentVideo.findLast {
                    it.itemType == OpenMediaAdapter.ITEM_ADD_MEDIA
                }
                if (null == item) {
                    mViewSendMediaTabBinding.tvCount.text = "(${currentVideo.size}/100)"
                } else {
                    mViewSendMediaTabBinding.tvCount.text =
                        "(${(currentVideo.size - 1).coerceAtLeast(0)}/100)"
                }
            }


        }

    }


    private fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMedia.launch(PickVisualMediaRequest(if (isVideo) ActivityResultContracts.PickVisualMedia.VideoOnly else ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = if (isVideo) "video/*" else "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickLegacy.launch(Intent.createChooser(intent, "Select Picture"))
        }
    }


    private fun initSelectMediaDialog(data: MutableList<ModelMediaData>, position: Int) {

        mSelectMediaDialog?.dismissAllowingStateLoss()

        data.forEach {

            it.uri = null
        }

        mSelectMediaDialog = SelectMediaDialog.newInstance(
            data,
            position,
            maxSelect,
            isCanSend
        ).apply {


            onSelect = {

                chooseItem(it)
            }

            onSendMessage = {

                this@SendMediaDialog.mDialogBinding.tvCommit.performClick()
            }
        }


        mSelectMediaDialog?.show(childFragmentManager)

    }


    private fun chooseItem(position: Int) {

        try {

            val realPosition =
                if (list.size == mOpenMediaAdapter.itemCount) position else (position + 1).coerceAtMost(
                    mOpenMediaAdapter.items.lastIndex
                )

            val item = mOpenMediaAdapter.getItem(realPosition) ?: return

            item.let {

                if (it.isCheck == true) {

                    item.isCheck = false

                    list.remove(it)

                    for (i in 0 until list.size) {
                        val pos = mOpenMediaAdapter.items.indexOf(list[i])
                        if (pos != -1) mOpenMediaAdapter.notifyItemChanged(pos, false)
                    }

                } else {


                    if (list.size >= maxSelect) {

                        showShort("Maximum of $maxSelect  allowed")

                        return
                    }


                    item.isCheck = true

                    list.add(it)

                }

                mOpenMediaAdapter.notifyItemChanged(realPosition, false)

                setSendCount()

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

}