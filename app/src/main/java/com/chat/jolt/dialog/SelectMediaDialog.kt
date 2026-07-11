package com.chat.jolt.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.core.view.get
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.chat.jolt.R
import com.chat.jolt.data.ModelMediaData
import com.chat.jolt.databinding.DialogSelectMediaBinding
import com.chat.jolt.databinding.ItemImageSelectSendMediaBinding
import com.chat.jolt.databinding.ItemPictureBinding
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.VideoPlayerUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.edgeToEdgeTop
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getRealUrl
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.isVideo
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.toJson
import com.chat.lib_common.util.viewVisibility
import com.github.chrisbanes.photoview.PhotoView


class SelectMediaDialog :
    BaseDialog<DialogSelectMediaBinding>(DialogSelectMediaBinding::inflate) {

    private var data: MutableList<ModelMediaData> = mutableListOf()

    private var selectData = mutableListOf<ModelMediaData>()

    private var currentMaxSelect: Int = 0

    private var currentPosition: Int = 0

    private var lastRecyclerPosition = -1


    private var canSelect = true

    val width: Int = getScreenPx(BaseApplication.mApplication)[0] / 2

    val height: Int = getScreenPx(BaseApplication.mApplication)[1] / 3


    private lateinit var mImageRecyclerViewAdapter: BaseRecyclerAdapter<ModelMediaData, ItemImageSelectSendMediaBinding>

    private lateinit var mImageViewPagerAdapter: RecyclerView.Adapter<ImageViewPagerViewHolder>

    inner class ImageViewPagerViewHolder(val mItemPictureBinding: ItemPictureBinding) :
        RecyclerView.ViewHolder(mItemPictureBinding.root)



    var onSelect: (Int) -> Unit = {

    }

    var onSendMessage: () -> Unit = {

    }


    companion object {
        fun newInstance(
            data: MutableList<ModelMediaData>,
            position: Int,
            currentMaxSelect: Int,
            canSelect: Boolean
        ): SelectMediaDialog {
            return SelectMediaDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data.toJson())
                    putInt(AppConstant.Constant.POSITION, position)
                    putInt("currentMaxSelect", currentMaxSelect)
                    putBoolean("canSelect", canSelect)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.navigationBarColor = Color.BLACK
        }
    }

    override fun initData() {

        arguments?.let {
            data = it.getString(AppConstant.Constant.DATA)?.formatListJson() ?: mutableListOf()
            currentPosition = it.getInt(AppConstant.Constant.POSITION)
            currentMaxSelect = it.getInt("currentMaxSelect")
            canSelect = it.getBoolean("canSelect", canSelect)

            mDialogBinding.tvCount.text = "1/${data.size}"

            if (canSelect) {
                viewVisibility(View.VISIBLE, mDialogBinding.setSend)
            } else {
                viewVisibility(View.GONE,  mDialogBinding.setSend)
            }
        }


        selectData.addAll(data.filter { it.isCheck == true })

        initViewPager()

        initRecyclerView()

        VideoPlayerUtil.init(requireContext())


    }


    override fun initView() {





        mDialogBinding.apply {

            appToolBar.edgeToEdgeTop()

            root.edgeToEdgeBottom()

            ivRight.setOnClickListener {


                onSelect(currentPosition)


                if (null == data[currentPosition].isCheck || data[currentPosition].isCheck == false) {


                    if ( mImageRecyclerViewAdapter.itemCount >= currentMaxSelect) {

                        return@setOnClickListener
                    }


                    data[currentPosition].isCheck = true


                    mImageRecyclerViewAdapter.add(data[currentPosition])

                    mDialogBinding.ivRight.setImageResource(R.drawable.iv_select_media_check)

                    setCurrentPage(data[currentPosition])

                } else {

                    data[currentPosition].isCheck = false

                    val index =
                        mImageRecyclerViewAdapter.items.indexOfLast { it.albumId == data[currentPosition].albumId }

                    if (index != -1) {

                        mImageRecyclerViewAdapter.removeAt(index)
                    }
                    mDialogBinding.ivRight.setImageResource(R.drawable.iv_select_media_normal)
                }

                showRecyclerView()


            }

            ivBack.click {

                dismissAllowingStateLoss()
            }

            setSend.click {

                onSendMessage()

                dismissAllowingStateLoss()

            }
        }




    }


    private fun initViewPager() {


        val mRecyclerView = mDialogBinding.viewPager.getChildAt(0) as RecyclerView

        mRecyclerView.itemAnimator = null

        mRecyclerView.setItemViewCacheSize(0)


        mDialogBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(mPosition: Int) {
                super.onPageSelected(mPosition)

                if (data[mPosition].albumUrl.isVideo()) {
                    mRecyclerView.post {
                        val findViewByPosition =
                            mRecyclerView.layoutManager?.findViewByPosition(mPosition)

                        findViewByPosition?.let {
                            val mPlayerView =
                                findViewByPosition.findViewById<PlayerView>(R.id.play_view)
                            val imageView =
                                findViewByPosition.findViewById<PhotoView>(R.id.iv_image)

                            VideoPlayerUtil.onReady = {

                                imageView.visibility = View.GONE
                            }

                            autoPlayCurrent(mPosition, mPlayerView)
                        }


                    }


                }


                mRecyclerView.post {

                    try {


                        if (data[mPosition].isCheck == true) {
                            mDialogBinding.ivRight.setImageResource(R.drawable.iv_select_media_check)
                        } else {
                            mDialogBinding.ivRight.setImageResource(R.drawable.iv_select_media_normal)
                        }



                        setCurrentPage(data[mPosition])

                        currentPosition = mPosition

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                mDialogBinding.tvCount.text = "${mPosition + 1}/${data.size}"

            }

        })


        mImageViewPagerAdapter = object : RecyclerView.Adapter<ImageViewPagerViewHolder>() {

            override fun onViewRecycled(holder: ImageViewPagerViewHolder) {


                Glide.with(holder.itemView.context).clear(holder.mItemPictureBinding.ivImage)
                holder.mItemPictureBinding.ivImage.setImageDrawable(null)
                super.onViewRecycled(holder)
            }


            @OptIn(UnstableApi::class)
            override fun onBindViewHolder(holder: ImageViewPagerViewHolder, position: Int) {

                holder.itemView.tag = position

                Glide.with(holder.itemView.context).clear(holder.mItemPictureBinding.ivImage)

                if (data[position].albumUrl.isVideo()) {
                    holder.mItemPictureBinding.ivImage.visibility = View.VISIBLE
                    holder.mItemPictureBinding.playView.visibility = View.VISIBLE

                    holder.mItemPictureBinding.ivImage.loadImage(
                        holder.mItemPictureBinding.ivImage.context,
                        data[position].videoCover,
                        width, height,
                    )

                    try {
                        holder.mItemPictureBinding.playView.controllerAutoShow = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {

                    holder.mItemPictureBinding.playView.visibility = View.GONE
                    holder.mItemPictureBinding.ivImage.visibility = View.VISIBLE

                    holder.mItemPictureBinding.ivImage.loadImage(
                        holder.mItemPictureBinding.ivImage.context,
                        data[position].albumUrl,
                        width, height,
                    )

                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ImageViewPagerViewHolder {
                return ImageViewPagerViewHolder(
                    ItemPictureBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }

            override fun getItemCount(): Int {

                return data.size
            }


        }


        mDialogBinding.viewPager.adapter = mImageViewPagerAdapter

        mDialogBinding.viewPager.offscreenPageLimit = 1

        mDialogBinding.viewPager.setCurrentItem(currentPosition, false)

        mDialogBinding.viewPager[0].overScrollMode = View.OVER_SCROLL_NEVER

    }


    private fun initRecyclerView() {

        mImageRecyclerViewAdapter = object :
            BaseRecyclerAdapter<ModelMediaData, ItemImageSelectSendMediaBinding>(
                ItemImageSelectSendMediaBinding::inflate
            ) {

            override fun convert(
                holder: BaseRecyclerViewHolder<ItemImageSelectSendMediaBinding>,
                itemView: ItemImageSelectSendMediaBinding,
                item: ModelMediaData,
                position: Int
            ) {

                if (item.albumUrl.isVideo()) {

                    itemView.ivImage.loadImage(context, item.videoCover, 100, 100)

                } else {
                    itemView.ivImage.loadImage(context, item.albumUrl, 100, 100)
                }


//                if (item.isSelect == true) {
//                    mBindView.ivImage.strokeColor =
//                        ColorStateList.valueOf(context.getColor(R.color.color_FF76BB))
//                } else {
//                    mBindView.ivImage.strokeColor =
//                        ColorStateList.valueOf(context.getColor(R.color.transparent))
//                }
            }

        }
        mDialogBinding.recyclerView.adapter = mImageRecyclerViewAdapter


        mImageRecyclerViewAdapter.setOnItemClickListener { _, _, position ->


            val item = mImageRecyclerViewAdapter.getItem(position)

            item?.let {

                setCurrentPage(it)

                val index = data.indexOfLast { index -> index.albumId == it.albumId }

                if (index != -1) {
                    mDialogBinding.viewPager.setCurrentItem(index, false)
                }

            }

        }

        mImageRecyclerViewAdapter.submitList(selectData)

        showRecyclerView()
    }


    private fun setCurrentPage(currentItem: ModelMediaData) {

        val index =
            mImageRecyclerViewAdapter.items.indexOfLast { it.albumId == currentItem.albumId }


        if (lastRecyclerPosition != -1) {

            val lastItem = mImageRecyclerViewAdapter.getItem(lastRecyclerPosition)

            if (null != lastItem) {

                lastItem.isSelect = false

                mImageRecyclerViewAdapter.notifyItemChanged(lastRecyclerPosition, false)
            }

            lastRecyclerPosition = -1
        }

        if (index == -1) {

            return

        }

        val item = mImageRecyclerViewAdapter.getItem(index)


        item?.let {

            item.isSelect = true

            lastRecyclerPosition = index

            mImageRecyclerViewAdapter.notifyItemChanged(index, false)
        }

    }


    private fun autoPlayCurrent(
        position: Int, mPlayerView: PlayerView
    ) {

        val proxy = BaseApplication.mApplication.getProxy()

        val proxyUrl = proxy.getProxyUrl(getRealUrl(data[position].albumUrl).toString() ?: "")

        VideoPlayerUtil.play(proxyUrl, mPlayerView)

    }

    private fun showRecyclerView() {

        if (mImageRecyclerViewAdapter.itemCount == 0) {
            mDialogBinding.slChat.visibility = View.GONE
        } else {
            mDialogBinding.slChat.visibility = View.VISIBLE
        }


    }




    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun onDismiss(dialog: DialogInterface) {
        try {

            VideoPlayerUtil.releaseAll()


        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDismiss(dialog)
    }

}