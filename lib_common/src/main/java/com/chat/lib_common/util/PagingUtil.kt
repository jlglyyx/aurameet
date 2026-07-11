package com.chat.lib_common.util

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.chat.lib_common.adapter.BottomLoadAdapter
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.widget.ErrorReLoadView


/**
 * SwipeRefreshLayout 网络请求
 */
fun <T : Any> BaseQuickAdapter<T, *>.refreshLoadDataListener(
    refresh: SwipeRefreshLayout,
    loading: QuickAdapterHelper,
    errorReLoadView: ErrorReLoadView?,
    mData: MutableList<T>?
) {

    val data = if (mData.isNullOrEmpty()) mutableListOf() else mData

    if (refresh.isRefreshing) {

        refresh.isRefreshing = false

        if (data.isEmpty()) {
            //this.isStateViewEnable = true

        } else {
            //this.isStateViewEnable = false
            this.submitList(data)

            if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                loading.trailingLoadState = LoadState.NotLoading(true)
            } else {
                loading.trailingLoadState = LoadState.NotLoading(false)
            }
        }

    } else if (loading.trailingLoadState == LoadState.Loading) {

        if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
            if (data.isNotEmpty()) {
                this.addAll(data)
            }
            loading.trailingLoadState = LoadState.NotLoading(true)
        } else {
            this.addAll(data)
            loading.trailingLoadState = LoadState.NotLoading(false)
        }
    } else {
        if (data.isEmpty()) {
//            this.isStateViewEnable = true
        } else {
//            this.isStateViewEnable = false
            this.submitList(data)
            if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                loading.trailingLoadState = LoadState.NotLoading(true)
            } else {
                loading.trailingLoadState = LoadState.NotLoading(false)
            }
        }
    }

    errorReLoadView?.showSuccessView(this.items)

}


/**
 * SwipeRefreshLayout上拉加载 下拉刷新监听
 */
fun <T : Any> BaseQuickAdapter<T, *>.refreshLoadListener(
    refresh: SwipeRefreshLayout,
    onRefresh: () -> Unit,
    onLoad: () -> Unit
): QuickAdapterHelper {

    refresh.setOnRefreshListener {
        onRefresh()

    }

    val mQuickAdapterHelper = QuickAdapterHelper.Builder(this)
        .setTrailingLoadStateAdapter(BottomLoadAdapter().setOnLoadMoreListener(object :
            TrailingLoadStateAdapter.OnTrailingListener {
            override fun onLoad() {
                onLoad()
            }

            override fun onFailRetry() {
            }

            override fun isAllowLoading(): Boolean {
                return !refresh.isRefreshing
            }

        })).build()


    return mQuickAdapterHelper

}
