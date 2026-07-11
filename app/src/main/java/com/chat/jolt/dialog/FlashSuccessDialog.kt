package com.chat.jolt.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chat.jolt.R
import com.chat.jolt.data.ModelUserData
import com.chat.jolt.databinding.DialogFlashSuccessBinding
import com.chat.jolt.databinding.ItemMatchTextBinding
import com.chat.jolt.helper.UserInfoHold
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.tracking.MESSAGE_CHAT_KEY
import com.chat.lib_common.tracking.reportEvent
import com.chat.lib_common.util.click
import com.chat.lib_common.util.dip2px
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.fromJson
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.toJson
import com.google.android.material.imageview.ShapeableImageView
import com.youth.banner.adapter.BannerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlashSuccessDialog :
    BaseDialog<DialogFlashSuccessBinding>(DialogFlashSuccessBinding::inflate) {


    var onConfirm:(String) ->Unit = { _->}

    private var mModelUserData: ModelUserData? = null

    private lateinit var mBannerAdapter: BannerAdapter<String, RecyclerView.ViewHolder>

    private lateinit var mAdapter: BaseRecyclerAdapter<String, ItemMatchTextBinding>

    private lateinit var mAdapter2: BaseRecyclerAdapter<String, ItemMatchTextBinding>

    private val width = 30f.dip2px(BaseApplication.mApplication)

    private val mIndicatorWidth = 20f.dip2px(BaseApplication.mApplication)


    companion object {
        fun newInstance(
            data: ModelUserData,
        ): FlashSuccessDialog {
            return FlashSuccessDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            llClose.edgeToEdgeTop()

            root.edgeToEdgeBottom()

            initRecyclerView()

            initRecyclerView2()

            ivClose.click {

                dismissAllowingStateLoss()
            }

            stvSend.click {

                onConfirm("")

                reportEvent(MESSAGE_CHAT_KEY[5],true)

//                dismissAllowingStateLoss()
            }

        }

    }

    override fun initData() {

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA, "")

            if (!data.isNullOrEmpty()) {

                mModelUserData = data.fromJson()

                withViewBinding {

                    mModelUserData?.let { mModelUserData ->
                        sivUser.loadImage(requireContext(), mModelUserData.headPic, width, width)

                        tvOnline.text = if (mModelUserData.onlineStatus == "Online") "Online" else "Active"

                        tvName.text = "${mModelUserData.nickname},${mModelUserData.age}"

                        val list =  mModelUserData.coverPics?:mutableListOf()

                        if (UserInfoHold.isOrganic || UserInfoHold.isReview){

                        }else{

                            if (!mModelUserData.publicPic.isNullOrEmpty()){
                                list.add(0,mModelUserData.publicPic)
                            }

                        }

                        initBanner(list)
                    }

                }

            }

        }

        reportEvent(MESSAGE_CHAT_KEY[4],true)
    }


    private fun initBanner(list: List<String>? = mutableListOf()) {

        if (list.isNullOrEmpty()) return

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
                    }
                }

            }

            banner.setAdapter(mBannerAdapter).addBannerLifecycleObserver(viewLifecycleOwner)
                .isAutoLoop(true)
                .setIndicator(mRectangleIndicator, false)
                .setIndicatorSelectedColor(getColor(R.color.color_EAA82B))
                .setIndicatorNormalColor(getColor(R.color.white_30))
                .setIndicatorRadius(5f.dip2px(requireContext()))
                .setIndicatorHeight(6f.dip2px(requireContext()))
                .setIndicatorWidth(
                    mIndicatorWidth,
                    mIndicatorWidth
                )


        }

    }


    private fun initRecyclerView() {


        val list = mutableListOf(
            getString(R.string.say_hi_1),
            getString(R.string.say_hi_2),
            getString(R.string.say_hi_3),
            getString(R.string.say_hi_4),
            getString(R.string.say_hi_5),
            getString(R.string.say_hi_6),

            )

        val data = List(3) { list }.flatten()

        withViewBinding {

            mAdapter = object :
                BaseRecyclerAdapter<String, ItemMatchTextBinding>(ItemMatchTextBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemMatchTextBinding>,
                    itemView: ItemMatchTextBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item
                }


            }

            recyclerView.adapter = mAdapter

            val linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            recyclerView.layoutManager =
                linearLayoutManager

            mAdapter.setOnDebouncedItemClick { _, _, position ->

                val item = mAdapter.getItem(position) ?: return@setOnDebouncedItemClick

                onConfirm(item)

            }

            mAdapter.submitList(data)

            recyclerView.post {
                val middle = mAdapter.itemCount / 2
                linearLayoutManager.scrollToPositionWithOffset(middle, 0)
            }


            lifecycleScope.launch {

                while (isActive) {
                    recyclerView.scrollBy(-2, 0)
                    val first = linearLayoutManager.findFirstVisibleItemPosition()
                    if (first != RecyclerView.NO_POSITION) {
                        val originSize = mAdapter.itemCount / 3
                        val total = mAdapter.itemCount
                        if (first < originSize) {
                            val center = first + originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                        if (first >= originSize * 2) {
                            val center = first - originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                    }
                    delay(16)
                }
            }


            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                    val first = linearLayoutManager.findFirstVisibleItemPosition()

                    val originSize = list.size

                    if (originSize == 0) return

                    if (first < originSize) {
                        val center = first + originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                    else if (first >= originSize * 2) {
                        val center = first - originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                }
            })


        }

    }

    private fun initRecyclerView2() {


        val list2 = mutableListOf(
            getString(R.string.say_hi_7),
            getString(R.string.say_hi_8),
            getString(R.string.say_hi_9),
            getString(R.string.say_hi_10),
            getString(R.string.say_hi_11),
            getString(R.string.say_hi_12)
        )

        val data = List(3) { list2 }.flatten()

        withViewBinding {

            mAdapter2 = object :
                BaseRecyclerAdapter<String, ItemMatchTextBinding>(ItemMatchTextBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemMatchTextBinding>,
                    itemView: ItemMatchTextBinding,
                    item: String,
                    position: Int
                ) {
                    itemView.stvContent.text = item
                }


            }

            recyclerView2.adapter = mAdapter2

            val linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            recyclerView2.layoutManager =
                linearLayoutManager


            mAdapter2.setOnItemClickListener { _, _, position ->

                val item = mAdapter2.getItem(position) ?: return@setOnItemClickListener

                onConfirm(item)

            }

            mAdapter2.submitList(data)

            recyclerView2.post {
                val middle = mAdapter2.itemCount / 2
                linearLayoutManager.scrollToPositionWithOffset(middle, 0)
            }


            lifecycleScope.launch {

                while (isActive) {
                    recyclerView2.scrollBy(2, 0)
                    val first = linearLayoutManager.findFirstVisibleItemPosition()
                    if (first != RecyclerView.NO_POSITION) {
                        val originSize = mAdapter2.itemCount / 3
                        if (first < originSize) {
                            val center = first + originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                        if (first >= originSize * 2) {
                            val center = first - originSize
                            linearLayoutManager.scrollToPositionWithOffset(center, 0)
                        }
                    }
                    delay(16)

                }
            }


            recyclerView2.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                    val first = linearLayoutManager.findFirstVisibleItemPosition()

                    val originSize = list2.size

                    if (originSize == 0) return

                    if (first < originSize) {
                        val center = first + originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                    else if (first >= originSize * 2) {
                        val center = first - originSize
                        linearLayoutManager.scrollToPositionWithOffset(center, 0)
                    }
                }
            })

        }

    }




    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

}