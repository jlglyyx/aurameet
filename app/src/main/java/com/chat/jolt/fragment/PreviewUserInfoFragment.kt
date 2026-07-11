package com.chat.jolt.fragment


import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.jolt.R
import com.chat.jolt.adapter.TurnsOnsAdapter
import com.chat.jolt.data.ModelImageData
import com.chat.jolt.data.PictureData
import com.chat.jolt.data.SocialAimData
import com.chat.jolt.data.UserInfoData
import com.chat.jolt.databinding.FraPreviewUserInfoBinding
import com.chat.jolt.databinding.ItemImageRecyclerPagerBinding
import com.chat.jolt.databinding.ItemInfoInterstBinding
import com.chat.jolt.dialog.PictureDetailDialog
import com.chat.jolt.helper.UserInfoHold
import com.chat.jolt.viewmodel.UserViewModel
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.fragment.BaseFragment
import com.chat.lib_common.util.click
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.getColor
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.replaceEmoji
import com.chat.lib_common.util.viewVisibility
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.listener.OnPageChangeListener


class PreviewUserInfoFragment :
    BaseFragment<FraPreviewUserInfoBinding, UserViewModel>(FraPreviewUserInfoBinding::inflate) {

    private lateinit var mBannerAdapter: BannerAdapter<String, RecyclerView.ViewHolder>

    private lateinit var mHobbyTagAdapter: BaseRecyclerAdapter<String, ItemInfoInterstBinding>

    private lateinit var mImageRecyclerViewAdapter: BaseRecyclerAdapter<ModelImageData, ItemImageRecyclerPagerBinding>

    private var currentPosition = 0

    private var targetUserId = ""

    private val mTurnsOnsAdapter by lazy { TurnsOnsAdapter() }


    private val mSocialAimMap = mutableMapOf<String, String>()


    private val mParentMainViewModel by activityViewModels<UserViewModel>()


    override fun initView() {

        withViewBinding {

        }


    }


    override fun initData() {



    }

    override fun initViewModel() {

        mParentMainViewModel.mPreviewUserInfoData.observe(this) {

            initUserInfo(it)
        }

    }


    private fun initUserInfo(mUserInfoData: UserInfoData?) {


        if (null == mUserInfoData) {

            return
        }

        withViewBinding {


            if (mUserInfoData.mySign.isNullOrEmpty()) {
                viewVisibility(View.GONE, sclAboutMe)
            } else {
                tvIntroductions.text = mUserInfoData.mySign
                viewVisibility(View.VISIBLE, sclAboutMe)
            }


            if (mUserInfoData.socialAim.isNullOrEmpty()) {
                viewVisibility(View.GONE, sclNiw)
            } else {

                viewVisibility(View.VISIBLE, sclNiw)

                if (mSocialAimMap.isEmpty()){
                    val mSocialAimCache = getCache(AppConstant.Constant.SOCIAL_AIM, "")

                    if (mSocialAimCache.isNotEmpty()) {

                        val mSocialAimData = mSocialAimCache.formatListJson<SocialAimData>()

                        mSocialAimData.forEach {

                            mSocialAimMap.put(it.socialAim, it.socialAimName)
                        }

                    }
                }
                tvSocialAim.text = mSocialAimMap[mUserInfoData.socialAim].replaceEmoji()

            }



            if (mUserInfoData.city.isNullOrEmpty()) {
                viewVisibility(View.GONE, sllAddress)
            } else {
                tvAddress.text = mUserInfoData.city
                viewVisibility(View.VISIBLE, sllAddress)
            }


            if (mUserInfoData.height.isNullOrEmpty()) {

                viewVisibility(View.GONE, stvHeight)
            } else {
                stvHeight.text = "${mUserInfoData.height}cm"
                viewVisibility(View.VISIBLE, stvHeight)
            }


            if (mUserInfoData.weight.isNullOrEmpty()) {

                viewVisibility(View.GONE, stvWeight)
            } else {
                stvWeight.text = "${mUserInfoData.weight}kg"
                viewVisibility(View.VISIBLE, stvWeight)
            }

            if (mUserInfoData.height.isNullOrEmpty() && mUserInfoData.weight.isNullOrEmpty() && mUserInfoData.profession.isNullOrEmpty()) {

                viewVisibility(View.GONE, sclBasic)
            } else {

                viewVisibility(View.VISIBLE, sclBasic)
            }





            if (mUserInfoData.profession.isNullOrEmpty()) {

                viewVisibility(View.GONE, stvProfession)
            } else {
                stvProfession.text = mUserInfoData.profession
                viewVisibility(View.VISIBLE, stvProfession)
            }

            initBanner(mUserInfoData.coverPics)

            initImageRecyclerView(mUserInfoData.coverPics?.map {

                ModelImageData(it).apply { isSelect = false }
            }?.toMutableList(), mUserInfoData.headPic)

            if (mUserInfoData.hobbyTagContents.isNullOrEmpty()) {

                viewVisibility(View.GONE, sclInterests)
            } else {
                initRecyclerView(mUserInfoData.hobbyTagContents)
                viewVisibility(View.VISIBLE, sclInterests)
            }


            if (UserInfoHold.isOrganic || UserInfoHold.isReview) {

                viewVisibility(View.GONE, sclTurn)
            } else {
                if (mUserInfoData.turnOnsTags.isNullOrEmpty()) {

                    viewVisibility(View.GONE, sclTurn)
                } else {
                    initTurnsOnsRecyclerView(mUserInfoData.turnOnsTags ?: mutableListOf())
                    viewVisibility(View.VISIBLE, sclTurn)
                }
            }


        }

    }


    private fun initBanner(list: List<String>? = mutableListOf()) {

        currentPosition = 0

        if (list.isNullOrEmpty()) return


        val result = list.map { map -> PictureData(map) }.toMutableList()

        withViewBinding {

            mBannerAdapter = object :
                BannerAdapter<String, RecyclerView.ViewHolder>(list) {
                override fun onCreateHolder(
                    parent: ViewGroup?,
                    viewType: Int
                ): RecyclerView.ViewHolder {

                    val imageView = ShapeableImageView(parent!!.context)
                    imageView.setLayoutParams(
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
                    imageView.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCorners(
                        CornerFamily.ROUNDED, 10f.dip2px(requireContext()).toFloat()
                    ).build()

                    return object : RecyclerView.ViewHolder(imageView) {

                    }
                }

                override fun onBindView(
                    holder: RecyclerView.ViewHolder?,
                    data: String,
                    position: Int,
                    size: Int
                ) {
                    holder?.let {
                        (it.itemView as ShapeableImageView).loadImage(it.itemView.context, data)

                        it.itemView.click {

                            PictureDetailDialog.newInstance(result, position)
                                .show(parentFragmentManager)

                        }

                    }


                }

            }

            banner.setAdapter(mBannerAdapter)
                .addBannerLifecycleObserver(this@PreviewUserInfoFragment)
                .isAutoLoop(false)
                .addOnPageChangeListener(object : OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageSelected(position: Int) {

                        val item = mImageRecyclerViewAdapter.getItem(position)

                        val lastItem = mImageRecyclerViewAdapter.getItem(currentPosition)

                        if (null != lastItem) {

                            lastItem.isSelect = false

                            mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)
                        }

                        item?.let {

                            item.isSelect = true

                            currentPosition = position

                            mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)

                            imageRecyclerview.smoothScrollToPosition(currentPosition)
                        }

                        currentPosition = position
                    }

                    override fun onPageScrollStateChanged(state: Int) {

                    }

                })


        }

    }


    private fun initImageRecyclerView(data: MutableList<ModelImageData>?, headPic: String?) {

        if (data.isNullOrEmpty()) {

            mViewBinding.ivAvatar.visibility = View.VISIBLE

            mViewBinding.ivAvatar.loadImage(requireContext(), headPic)

            data?.add(ModelImageData(headPic.toString()))

        } else {
            mViewBinding.ivAvatar.visibility = View.GONE
        }

        if (!data.isNullOrEmpty()) {
            data[0].isSelect = true
        }

        mImageRecyclerViewAdapter = object :
            BaseRecyclerAdapter<ModelImageData, ItemImageRecyclerPagerBinding>(
                ItemImageRecyclerPagerBinding::inflate
            ) {

            override fun convert(
                holder: BaseRecyclerViewHolder<ItemImageRecyclerPagerBinding>,
                itemView: ItemImageRecyclerPagerBinding,
                item: ModelImageData,
                position: Int
            ) {


                itemView.ivImage.loadImage(itemView.ivImage.context, item.data)

                if (item.isSelect) {
                    itemView.ivImage.strokeColor =
                        ColorStateList.valueOf(getColor(R.color.white))
                } else {
                    itemView.ivImage.strokeColor =
                        ColorStateList.valueOf(getColor(R.color.transparent))
                }
            }

        }
        mViewBinding.imageRecyclerview.adapter = mImageRecyclerViewAdapter

        mImageRecyclerViewAdapter.submitList(data)




        mImageRecyclerViewAdapter.setOnItemClickListener { _, _, position ->

            val item = mImageRecyclerViewAdapter.getItem(position)

            val lastItem = mImageRecyclerViewAdapter.getItem(currentPosition)

            if (null != lastItem) {

                lastItem.isSelect = false

                mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)
            }

            item?.let {

                item.isSelect = true

                currentPosition = position

                mImageRecyclerViewAdapter.notifyItemChanged(currentPosition, false)
            }

            mViewBinding.banner.setCurrentItem(position + 1, false)

        }

    }


    private fun initRecyclerView(list: MutableList<String>? = mutableListOf()) {

        if (list.isNullOrEmpty()) return


        withViewBinding {

            mHobbyTagAdapter = object :
                BaseRecyclerAdapter<String, ItemInfoInterstBinding>(ItemInfoInterstBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemInfoInterstBinding>,
                    itemView: ItemInfoInterstBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item
                }


            }

            recyclerView.adapter = mHobbyTagAdapter

            recyclerView.layoutManager = FlexboxLayoutManager(requireContext())

            mHobbyTagAdapter.submitList(list)

        }

    }

    private fun initTurnsOnsRecyclerView(turnOnsTags: MutableList<String>) {

        val list = turnOnsTags.filter {
            null != mViewModel.getTurnOnsData()[it]
        }.map {
            mViewModel.getTurnOnsData()[it]!!
        }

        withViewBinding {

            turnsOnsRecyclerView.adapter = mTurnsOnsAdapter

            turnsOnsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

            mTurnsOnsAdapter.submitList(list)


        }
    }


}