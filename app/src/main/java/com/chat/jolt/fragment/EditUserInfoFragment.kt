package com.chat.jolt.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.R
import com.chat.jolt.adapter.TurnsOnsAdapter
import com.chat.jolt.adapter.UserPictureAdapter
import com.chat.jolt.data.PictureData
import com.chat.jolt.data.ProfessionData
import com.chat.jolt.data.SocialAimData
import com.chat.jolt.data.TagData
import com.chat.jolt.data.UploadPictureData
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.databinding.FraEditUserInfoBinding
import com.chat.jolt.databinding.ItemEditInfoInterstBinding
import com.chat.jolt.dialog.EditWheelDialog
import com.chat.jolt.dialog.EditAboutMeDialog
import com.chat.jolt.dialog.EditAvatarDialog
import com.chat.jolt.dialog.EditBirthDialog
import com.chat.jolt.dialog.EditInterestDialog
import com.chat.jolt.dialog.EditNameDialog
import com.chat.jolt.dialog.EditTurnOnsDialog
import com.chat.jolt.dialog.PictureDetailDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.bus.FlowBus.observe
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.util.CompressUtil.isValidMedia
import com.chat.lib_common.util.OSSUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.dateFormat
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.replaceEmoji
import com.chat.lib_common.util.showShort
import com.chat.lib_common.util.toZodiac
import com.chat.lib_common.util.viewVisibility
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue


class EditUserInfoFragment :
    BaseFragment<FraEditUserInfoBinding, UserViewModel>(FraEditUserInfoBinding::inflate) {

    private lateinit var mUserPictureAdapter: UserPictureAdapter

    private var mOtherUserInfo: UserInfoData? = null

    private lateinit var mHobbyTagAdapter: BaseRecyclerAdapter<String, ItemEditInfoInterstBinding>

    private val mTurnsOnsAdapter by lazy { TurnsOnsAdapter(false) }

    private var mUploadAvatarPath = ""

    private var mEditAvatarDialog: EditAvatarDialog? = null

    private var mPictureDetailDialog: PictureDetailDialog? = null

    private val mParentMainViewModel by activityViewModels<UserViewModel>()

    private var isTurnOns = false

    private var mCurrentUserInfoData: UserInfoData? = null

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


    companion object {

        fun newInstance(isTurnOns: Boolean): EditUserInfoFragment {
            return EditUserInfoFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(AppConstant.Constant.IS_TURN_ONS, isTurnOns)
                }
            }
        }
    }

    override fun initView() {


        withViewBinding {

            if (UserInfoHold.isOrganic || UserInfoHold.isReview) {

                viewVisibility(View.GONE, llTurnsOns, turnsOnsRecyclerView)
            } else {
                viewVisibility(View.VISIBLE, llTurnsOns, turnsOnsRecyclerView)
            }

            initPictureRecyclerView()

            initLabelRecyclerView()

            ivtName.click {

                EditNameDialog.newInstance(ivtName.getContent()).apply {

                    onConfirm = {

                        ivtName.setContent(it)

                        previewUserInfo { info ->

                            info.nickname = it
                        }


                    }

                }.show(parentFragmentManager)

            }
            ivtAvatar.click {

                initAvatarDialog()

            }
            ivtBirth.click {

                EditBirthDialog.newInstance(ivtBirth.getContent()).apply {

                    onConfirm = { it, age ->

                        if (null != it) {
                            ivtBirth.setContent(it.dateFormat("MM/dd/yyyy", Locale.US))

                            ivtZodiac.setContent(it.toZodiac(this.requireContext()))

                            previewUserInfo { info ->

                                info.birthDay = ivtBirth.getContent()
                            }

                        }

                    }

                }.show(parentFragmentManager)

            }
            ivtWeight.click {

                EditWheelDialog.newInstance(ivtWeight.getContent(), 0).apply {

                    onConfirm = { it, _ ->

                        ivtWeight.setContent(it)

                        val number = it.takeWhile { take -> take.isDigit() }

                        previewUserInfo { info ->

                            info.weight = number
                        }
                    }

                }.show(parentFragmentManager)

            }
            ivtHeight.click {

                EditWheelDialog.newInstance(ivtHeight.getContent(), 1).apply {

                    onConfirm = { it, _ ->

                        ivtHeight.setContent(it)

                        val number = it.takeWhile { take -> take.isDigit() }

                        previewUserInfo { info ->

                            info.height = number
                        }

                    }

                }.show(parentFragmentManager)

            }
            ivtCareer.click {

                EditWheelDialog.newInstance(ivtCareer.getContent(), 2).apply {

                    onConfirm = { it, key ->

                        ivtCareer.setContent(it)

                        previewUserInfo { info ->

                            info.profession = key
                        }

                    }

                }.show(parentFragmentManager)

            }
            ivtIntroduction.click {

                val title = ivtIntroduction.getTitle()

                EditAboutMeDialog.newInstance(if (title == "Self Introduction") "" else title)
                    .apply {

                        onConfirm = {

                            ivtIntroduction.setTitle(it)

                            previewUserInfo { info ->

                                info.mySign = it
                            }
                        }

                    }.show(parentFragmentManager)

            }

            ivtWant.click {

                EditWheelDialog.newInstance(ivtWant.getTitle(), 3).apply {

                    onConfirm = { it, key ->

                        ivtWant.setTitle(it)

                        previewUserInfo { info ->

                            info.socialAim = key
                        }
                    }

                }.show(parentFragmentManager)

            }

            ivAddInterest.click {

                EditInterestDialog.newInstance(mHobbyTagAdapter.items.toMutableList()).apply {

                    onConfirm = { it ->

                        mHobbyTagAdapter.submitList(it.map { it.hobbyTagName })

                        val hobbyTags = it.map { it.hobbyTag }.toMutableList()

                        previewUserInfo { info ->

                            info.hobbyTags = hobbyTags
                            info.hobbyTagContents = hobbyTags
                        }

                    }

                }.show(parentFragmentManager)

            }
            ivAddTurn.click {

                val list = mTurnsOnsAdapter.items.map { it.userTag }.toMutableList()

                EditTurnOnsDialog.newInstance(list).apply {

                    onConfirm = { list ->

                        mTurnsOnsAdapter.submitList(list)

                        val changeList = list.map { it.userTag }.toMutableList()

                        previewUserInfo { info ->

                            info.turnOnsTags = changeList
                        }
                    }

                }.show(parentFragmentManager)


            }


        }


    }

    override fun initData() {


        isTurnOns = arguments?.getBoolean(AppConstant.Constant.IS_TURN_ONS) ?: isTurnOns

        mViewModel.getDataInfo()


    }

    override fun initViewModel() {

        mViewModel.mPreviewUserInfoData.observe(this) {

            createData(it.coverPics, it.coverStatus)

            mOtherUserInfo = it

            initUserInfo(it)

        }

        mParentMainViewModel.mUpdateUserInfoError.observe(this) {


            if (it.errorCode == 1105) {

                mViewBinding.ivtName.setContent(UserInfoHold.userInfo?.nickname)

                previewUserInfo { info ->

                    info.nickname = UserInfoHold.userInfo?.nickname
                }

            } else if (it.errorCode == 1106) {

                mViewBinding.ivtIntroduction.setTitle(
                    UserInfoHold.userInfo?.mySign ?: "Self Introduction"
                )

                previewUserInfo { info ->

                    info.mySign = UserInfoHold.userInfo?.mySign
                }

            } else if (it.errorCode == 1013) {

                mViewBinding.ivtAvatar.setImage(UserInfoHold.userInfo?.headPic)

                previewUserInfo { info ->

                    info.headPic = UserInfoHold.userInfo?.headPic
                }

            }


        }

        mViewModel.mHasChangeInfo.observe(this) {

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





        mViewModel.requestFailEvent.observe(this) {

            if (it is String) {

                handleUploadStatus(it, OSSUtil.UPLOAD_STATUS_ERROR, null)
            }
        }


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

                        previewUserInfo { info ->

                            val coverPics = info.coverPics ?: mutableListOf()

                            info.coverPics = coverPics.apply { add(path ?: "") }
                        }

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


    private fun initUserInfo(mUserInfoData: UserInfoData) {

        mCurrentUserInfoData = mUserInfoData
        withViewBinding {

            ivtName.setContent(mUserInfoData.nickname)
            ivtAvatar.setImage(mUserInfoData.headPic)
            ivtBirth.setContent(mUserInfoData.birthDay)
            ivtZodiac.setContent(mUserInfoData.constellation)
            ivtWeight.setContent(if (mUserInfoData.weight.isNullOrEmpty()) "" else "${mUserInfoData.weight}kg")
            ivtHeight.setContent(if (mUserInfoData.height.isNullOrEmpty()) "" else "${mUserInfoData.height}cm")


            val mProfessionCache = getCache(AppConstant.Constant.PROFESSION, "")

            if (mProfessionCache.isNotEmpty()) {

                val mProfessionData = mProfessionCache.formatListJson<ProfessionData>()

                val item = mProfessionData.findLast { it.profession == mUserInfoData.profession }

                item?.let {

                    ivtCareer.setContent(item.professionName)
                }

            }


            val mSocialAimCache = getCache(AppConstant.Constant.SOCIAL_AIM, "")

            if (mSocialAimCache.isNotEmpty()) {

                val mSocialAimData = mSocialAimCache.formatListJson<SocialAimData>()

                val item = mSocialAimData.findLast { it.socialAim == mUserInfoData.socialAim }

                item?.let {

                    ivtWant.setTitle(item.socialAimName.replaceEmoji())
                }


            }



            ivtIntroduction.setTitle(mUserInfoData.mySign ?: "Self Introduction")


            mHobbyTagAdapter.submitList(mUserInfoData.hobbyTagContents)

            initTurnsOnsRecyclerView(mUserInfoData.turnOnsTags ?: mutableListOf())

        }

        initProgress(mUserInfoData)

    }


    private fun initPictureRecyclerView() {


        withViewBinding {

            mUserPictureAdapter = UserPictureAdapter()

            pictureRecyclerView.adapter = mUserPictureAdapter

            pictureRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)


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

                previewUserInfo { info ->

                    val coverPics = info.coverPics ?: mutableListOf()

                    val indexOfFirst = coverPics.indexOfFirst { it == item.httpUrl }

                    if (indexOfFirst != -1) {

                        coverPics.removeAt(indexOfFirst)

                        info.coverPics = coverPics
                    }


                }


                item.status = OSSUtil.UPLOAD_STATUS_NORMAL

                item.url = null

                item.httpUrl = null

                mUserPictureAdapter.notifyItemChanged(position, false)


            }

        }

    }


    private fun createData(list: MutableList<String>?, coverStatus: MutableList<String>?) {

        val data = mutableListOf<UploadPictureData>()

        if (!list.isNullOrEmpty()) {
            data.addAll(list.mapIndexed { index, s ->
                UploadPictureData().apply {
                    url = s
                    httpUrl = s
                    status = OSSUtil.UPLOAD_STATUS_SUCCESS
                    albumStatus = coverStatus?.get(index) ?: ""
                }
            })
        }

        for (i in 0 until 9 - data.size) {

            data.add(UploadPictureData())
        }

        mUserPictureAdapter.submitList(data)
    }


    private fun onPhotoPicked(uri: Uri?): Uri? {

        if (null == uri) return null

        if (!isValidMedia(requireContext(), uri, true)) {

            showShort("too large, cannot upload")

            return null
        }

        val index = mUserPictureAdapter.items.indexOfFirst { it.status == 0 }

        if (index != -1) {

            val item = mUserPictureAdapter.getItem(index) ?: return null

            item.status = OSSUtil.UPLOAD_STATUS_LOADING

            item.url = uri

            mUserPictureAdapter.notifyItemChanged(index, false)

            mViewModel.ossAuth(item.id, OSSUtil.COVER, uri)
        }

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


    private fun initLabelRecyclerView() {


        withViewBinding {

            mHobbyTagAdapter = object :
                BaseRecyclerAdapter<String, ItemEditInfoInterstBinding>(ItemEditInfoInterstBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemEditInfoInterstBinding>,
                    itemView: ItemEditInfoInterstBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item
                }


            }

            labelRecyclerView.adapter = mHobbyTagAdapter

            labelRecyclerView.layoutManager = FlexboxLayoutManager(requireContext())


        }

    }


    private fun initTurnsOnsRecyclerView(turnOnsTags: MutableList<String>) {

        val toMutableSet = turnOnsTags.toMutableSet()

        val list = mViewModel.getTurnOnsData().filter { toMutableSet.contains(it.value.userTag) }
            .map { it.value }


        withViewBinding {

            turnsOnsRecyclerView.adapter = mTurnsOnsAdapter

            turnsOnsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

            mTurnsOnsAdapter.submitList(list)

        }



        if (isTurnOns) {
            lifecycleScope.launch {
                delay(200)
                val targetLocation = IntArray(2)
                mViewBinding.llTurnsOns.getLocationInWindow(targetLocation)
                val targetYInWindow = targetLocation[1] - 200
                mViewBinding.mNestedScrollView.smoothScrollTo(0, targetYInWindow)
            }
        }
    }


    private fun initPictureDetailDialog(data: MutableList<PictureData>) {

        mPictureDetailDialog?.dismissAllowingStateLoss()

        mPictureDetailDialog = PictureDetailDialog.newInstance(data, 0)

        mPictureDetailDialog?.show(parentFragmentManager)
    }


    private fun initAvatarDialog() {

        mEditAvatarDialog?.dismissAllowingStateLoss()

        val avatar = mUploadAvatarPath.ifEmpty { mOtherUserInfo?.headPic ?: "" }

        mEditAvatarDialog = EditAvatarDialog.newInstance(avatar)

        mEditAvatarDialog?.apply {

            onConfirm = { remotePath, localUrl ->

                if (remotePath.isNotEmpty()) {

                    mUploadAvatarPath = remotePath

                    mViewBinding.ivtAvatar.setImage(localUrl)

                    previewUserInfo { info ->

                        info.headPic = remotePath
                    }

                    mEditAvatarDialog?.dismissAllowingStateLoss()
                }

            }

        }


        mEditAvatarDialog?.show(parentFragmentManager)
    }


    private fun previewUserInfo(block: (UserInfoData) -> Unit) {

        val currentValue = mParentMainViewModel.mPreviewUserInfoData.currentValue()

        currentValue?.let {

            block(it)

            initProgress(it)

            mParentMainViewModel.mHasChangeInfo.postValue(true)

            mParentMainViewModel.mPreviewUserInfoData.postValue(it)


        }
    }


    private fun initProgress(mUserInfoData: UserInfoData) {

        var progress = 0

        withViewBinding {

            progress = 27 - if (mUserInfoData.coverPics.isNullOrEmpty()) {
                0
            } else {
                mUserInfoData.coverPics!!.size * 3
            }

            tvPhotoProgress.visibility = if (progress <= 0) View.GONE else View.VISIBLE

            tvPhotoProgress.text = "+${progress}%"

            progress = 50

            progress -= if (mUserInfoData.nickname.isNullOrEmpty()) 0 else 10
            progress -= if (mUserInfoData.headPic.isNullOrEmpty()) 0 else 10
            progress -= if (mUserInfoData.height.isNullOrEmpty()) 0 else 10
            progress -= if (mUserInfoData.weight.isNullOrEmpty()) 0 else 10
            progress -= if (mUserInfoData.profession.isNullOrEmpty()) 0 else 10

            tvBasicProgress.visibility = if (progress <= 0) View.GONE else View.VISIBLE

            tvBasicProgress.text = "+${progress}%"

            progress = if (mUserInfoData.mySign.isNullOrEmpty()) 13 else 0

            tvAboutMeProgress.visibility = if (progress <= 0) View.GONE else View.VISIBLE

            tvAboutMeProgress.text = "+${progress}%"

            progress = if (mUserInfoData.hobbyTags.isNullOrEmpty()) 10 else 0

            tvInterestsProgress.visibility = if (progress <= 0) View.INVISIBLE else View.VISIBLE

            tvInterestsProgress.text = "+${progress}%"
        }

    }


    override fun onDestroyView() {
        mUserPictureAdapter.destroyViews()
        super.onDestroyView()
    }
}