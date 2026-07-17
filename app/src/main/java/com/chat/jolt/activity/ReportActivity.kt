package com.chat.jolt.activity

import androidx.recyclerview.widget.GridLayoutManager
import com.chat.jolt.R
import com.chat.jolt.data.ReportData
import com.chat.jolt.databinding.ActReportBinding
import com.chat.jolt.databinding.ItemReportBinding
import com.chat.jolt.viewmodel.ChatViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.ScreenChangeUtil
import com.chat.lib_common.util.click
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.showShort


class ReportActivity : BaseActivity<ActReportBinding, ChatViewModel>(ActReportBinding::inflate){


    private var modelId : String? = null

    private lateinit var mReportAdapter: BaseRecyclerAdapter<ReportData, ItemReportBinding>

    private var currentPosition = -1


    override fun initView() {

        mViewBinding.root.edgeToEdgeBottom()

        ScreenChangeUtil().onScreenChange(mViewBinding.root, true){ _, _ ->

        }



        mViewBinding.stvNext.click {

            if (modelId.isNullOrEmpty()) return@click

           val list =  mReportAdapter.items.filter { it.isCheck }.map { it.reportType }.toMutableList()

            if (list.isEmpty()) return@click

            mViewModel.doReport(list,modelId!!,mViewBinding.setEmails.text.toString().trim())


        }

        initRecyclerView()

    }

    override fun initData() {


        modelId =  intent.getStringExtra(AppConstant.Constant.ID)

        mViewModel.getReportType()

    }

    override fun initViewModel() {

        mViewModel.mReportStatus.observe(this){

            showShort("Report successful")

            finish()

        }

        mViewModel.mReportData.observe(this){

            mReportAdapter.submitList(it)

        }

    }



    private fun initRecyclerView(){

        mReportAdapter = object : BaseRecyclerAdapter<ReportData, ItemReportBinding>(ItemReportBinding::inflate){
            override fun convert(
                holder: BaseRecyclerViewHolder<ItemReportBinding>,
                itemView: ItemReportBinding,
                item: ReportData,
                position: Int
            ) {
                itemView.tvContent.text = item.reportTypeName

                itemView.tvContent.isSelected = item.isCheck


            }

        }
        mViewBinding.recyclerView.adapter = mReportAdapter
        mViewBinding.recyclerView.layoutManager = GridLayoutManager(this,2)
        mReportAdapter.setOnItemClickListener{ adapter, view, position ->

            val item = mReportAdapter.getItem(position)
            currentPosition = position
            item?.let {
                it.isCheck = !it.isCheck
            }
            mReportAdapter.notifyItemChanged(position,false)

            mViewBinding.stvNext.isEnabled = mReportAdapter.items.findLast { it.isCheck } != null

        }

    }




}