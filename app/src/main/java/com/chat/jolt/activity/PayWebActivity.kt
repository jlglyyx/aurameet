package com.chat.jolt.activity

import android.content.Intent
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import com.chat.jolt.R
import com.chat.jolt.databinding.ActWebBinding
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.edgeToEdgeBottom
import kotlin.apply


class PayWebActivity : BaseActivity<ActWebBinding, MainViewModel>(ActWebBinding::inflate) {


    private var url = ""

    private var title = ""

    private var bizId = -1


    override fun onResume() {
        super.onResume()
        mViewBinding.webView.onResume()
    }

    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()
        }

    }


    override fun initData() {

        url = intent.getStringExtra(AppConstant.Constant.URL) ?: url

        title = intent.getStringExtra(AppConstant.Constant.TITLE) ?: title

        bizId = intent.getIntExtra(AppConstant.Constant.BIZ_ID,bizId)

        mViewBinding.apply {

            appToolBar.mToolbarBinding.tvTitle.text = title
        }

        initWebView()

    }

    override fun initViewModel() {


    }


    private fun initWebView() {

        mViewBinding.webView.webViewClient = object : WebViewClient(){

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                when (request?.url.toString()) {
                    "https://www.mockurl.com/#/submitPay" -> {
                        setResult(RESULT_OK, Intent().apply { putExtra(AppConstant.Constant.BIZ_ID, bizId) })
                        finish()
                        return true
                    }
                    "https://www.mockUrl.com/#/cancelPay" -> {
                        finish()
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        mViewBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(p0: WebView?, p1: Int) {
                super.onProgressChanged(p0, p1)
                Log.i(TAG, "onProgressChanged: $p1")
            }
        }
        mViewBinding.webView.setBackgroundColor(getColor(R.color.transparent))
        mViewBinding.webView.setBackgroundResource(R.color.transparent)
        mViewBinding.webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            domStorageEnabled = true
            allowFileAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            defaultTextEncodingName = "utf-8"
            allowContentAccess = true
            setAllowUniversalAccessFromFileURLs(true)
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            setWebContentsDebuggingEnabled(true)
        }
        mViewBinding.webView.clearCache(true)
        mViewBinding.webView.clearHistory()
//        mViewBinding.webView.webViewClient = WebViewClient()
        mViewBinding.webView.loadUrl(url)
        mViewBinding.webView.clearCache(true)
        mViewBinding.webView.clearHistory()

    }


    override fun onBackPressed() {

        if (mViewBinding.webView.canGoBack()) {
            mViewBinding.webView.goBack()

        } else {
            super.onBackPressed()
        }

    }


    override fun onPause() {
        super.onPause()
        mViewBinding.webView.onPause()
    }

    override fun onDestroy() {

        mViewBinding.webView.destroy()
        mViewBinding.webView.webChromeClient = null

        super.onDestroy()
    }


}