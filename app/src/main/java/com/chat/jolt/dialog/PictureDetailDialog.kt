package com.chat.jolt.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.core.view.get
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import androidx.core.net.toUri
import com.chat.jolt.R
import com.chat.jolt.data.PictureData
import com.chat.jolt.databinding.DialogPictureDetailBinding
import com.chat.jolt.databinding.ItemPictureBinding
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.dialog.BaseDialog
import com.chat.lib_common.util.VideoPlayerUtil
import com.chat.lib_common.util.formatListJson
import com.chat.lib_common.util.getRealUrl
import com.chat.lib_common.util.getScreenPx
import com.chat.lib_common.util.isVideo
import com.chat.lib_common.util.loadImage
import com.chat.lib_common.util.toJson
import kotlin.apply
import kotlin.let


class PictureDetailDialog :
    BaseDialog<DialogPictureDetailBinding>(DialogPictureDetailBinding::inflate) {


    private var data: MutableList<PictureData> = mutableListOf()

    private var position: Int = 0

    val width: Int = getScreenPx(BaseApplication.mApplication)[0]/2

    val height: Int = getScreenPx(BaseApplication.mApplication)[1]/2


    companion object {
        fun newInstance(data: MutableList<PictureData>, position: Int): PictureDetailDialog {
            return PictureDetailDialog().apply {
                arguments = Bundle().apply {
                    putString("data", data.toJson())
                    putInt("position", position)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.navigationBarColor = Color.BLACK

            it.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun initData() {

        arguments?.let {
            data = it.getString("data")?.formatListJson() ?: mutableListOf()
            position = it.getInt("position")
        }

        VideoPlayerUtil.init(requireContext())

        mDialogBinding.appToolBar.mToolbarBinding.tvTitle.text = "1/${data.size}"

        mDialogBinding.viewPager.setCurrentItem(position, false)

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val onCreateDialog = super.onCreateDialog(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.PictureDetailDialog)

        return onCreateDialog

    }


    private fun autoPlayCurrent(
        position:Int,mPlayerView:PlayerView
    ) {

        val proxy = BaseApplication.mApplication.getProxy()

        val proxyUrl = proxy.getProxyUrl(getRealUrl(data[position].url).toString())

        VideoPlayerUtil.play(proxyUrl,mPlayerView)

    }



    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {



        mDialogBinding.appToolBar.mToolbarBinding.ivBack.imageTintList = ColorStateList.valueOf(
            requireContext().getColor(
                R.color.white
            )
        )

        mDialogBinding.appToolBar.mToolbarBinding.viewLine.visibility = View.GONE

        val mRecyclerView = mDialogBinding.viewPager.getChildAt(0) as RecyclerView

        mRecyclerView.itemAnimator = null

        mRecyclerView.setItemViewCacheSize(0)


        mDialogBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(mPosition: Int) {
                super.onPageSelected(mPosition)

                if (data[mPosition].url.isVideo()){
                    mRecyclerView.post {
                        val findViewByPosition =
                            mRecyclerView.layoutManager?.findViewByPosition(mPosition)

                        findViewByPosition?.let {
                            val mPlayerView =
                                findViewByPosition.findViewById<PlayerView>(R.id.play_view)?:return@post

                            val imageView =
                                findViewByPosition.findViewById<ImageView>(R.id.iv_cover)?:return@post

                            autoPlayCurrent(mPosition,mPlayerView)

                            VideoPlayerUtil.onReady = {

                                imageView.visibility = View.GONE
                            }
                        }

                    }
                }
                mDialogBinding.appToolBar.mToolbarBinding.tvTitle.text = "${mPosition + 1}/${data.size}"

            }

        })

        mDialogBinding.viewPager.adapter = object : RecyclerView.Adapter<ImageViewPagerViewHolder>() {

            override fun onViewRecycled(holder: ImageViewPagerViewHolder) {


                Glide.with(holder.itemView.context).clear(holder.mItemPictureBinding.ivImage)
                holder.mItemPictureBinding.ivImage.setImageDrawable(null)

                super.onViewRecycled(holder)
            }


            @OptIn(UnstableApi::class) override fun onBindViewHolder(holder: ImageViewPagerViewHolder, position: Int) {

                holder.itemView.tag = position

                Glide.with(holder.itemView.context).clear(holder.mItemPictureBinding.ivImage)

                if (data[position].url.isVideo()) {
                    holder.mItemPictureBinding.ivImage.visibility = View.GONE
                    holder.mItemPictureBinding.ivCover.visibility = View.VISIBLE
                    holder.mItemPictureBinding.playView.visibility = View.VISIBLE

                    try {

                        context?.let {
                            holder.mItemPictureBinding.ivCover.loadImage(
                                requireContext(),
                                data[position].cover,
                                width, height,
                            )
                        }

                        holder.mItemPictureBinding.playView.controllerAutoShow = false
                    }catch (e: Exception){
                        e.printStackTrace()
                    }


                } else {

                    holder.mItemPictureBinding.playView.visibility = View.GONE
                    holder.mItemPictureBinding.ivImage.visibility = View.VISIBLE
                    holder.mItemPictureBinding.ivCover.visibility = View.GONE

                    context?.let {
                        (holder.mItemPictureBinding.ivImage as ImageView).loadImage(
                            requireContext(),
                            if (null != data[position].uri) data[position].uri?.toUri() else data[position].url,
                            width,height,
                        )
                    }

                    holder.mItemPictureBinding.ivImage.setOnClickListener {
                        this@PictureDetailDialog.dismissAllowingStateLoss()
                    }

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

        mDialogBinding.viewPager.offscreenPageLimit = 1

        mDialogBinding.viewPager[0].overScrollMode = View.OVER_SCROLL_NEVER

        mDialogBinding.appToolBar.mToolbarBinding.ivBack.setOnClickListener {
           dismissAllowingStateLoss()
        }


    }


    inner class ImageViewPagerViewHolder(val mItemPictureBinding: ItemPictureBinding) : RecyclerView.ViewHolder(mItemPictureBinding.root)


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