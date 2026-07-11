package com.chat.jolt.activity

import android.animation.ValueAnimator
import android.text.InputType
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SpanUtils
import com.chat.jolt.R
import com.chat.jolt.databinding.ActLoginEmailBinding
import com.chat.jolt.databinding.ItemEmailMenuBinding
import com.chat.jolt.dialog.NoticeDialog
import com.chat.jolt.viewmodel.MainViewModel
import com.chat.lib_common.activity.BaseActivity
import com.chat.lib_common.adapter.BaseRecyclerAdapter
import com.chat.lib_common.bus.FlowBus
import com.chat.lib_common.bus.FlowBus.postValue
import com.chat.lib_common.constant.AppConstant
import com.chat.lib_common.util.click
import com.chat.lib_common.util.copyContent
import com.chat.lib_common.util.createIntent
import com.chat.lib_common.util.edgeToEdgeBottom
import com.chat.lib_common.util.getCache
import com.chat.lib_common.util.isVpnConnected
import com.chat.lib_common.util.startActivity
import com.chat.lib_common.util.startLoadingAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

class LoginEmailActivity : BaseActivity<ActLoginEmailBinding, MainViewModel>(ActLoginEmailBinding::inflate) {

    private var isPasswordVisible = false

    private var loadingAnimator: ValueAnimator? = null


    private lateinit var mAdapter: BaseRecyclerAdapter<String, ItemEmailMenuBinding>

    private var emailMenu = arrayOf("@gmail.com", "@yahoo.com", "@outlook.com", "@hotmail.com")

    private var localPart = ""



    override fun initView() {

        mViewBinding.apply {

            root.edgeToEdgeBottom()

            initRecyclerView()

            lifecycleScope.launch {

                repeatOnLifecycle(Lifecycle.State.STARTED) {

                    etAccount.requestFocus()

                    delay(500)

                    etAccount.showSoftInputOnFocus = true

                    KeyboardUtils.showSoftInput()
                }


            }


            etAccount.doAfterTextChanged {

                tvEmailNotice.visibility =
                    if (it.toString().contains("@")) View.INVISIBLE else View.VISIBLE

                ivEmailClear.visibility =
                    if (it.toString().isNotEmpty()) View.VISIBLE else View.GONE


                createEmailMenuData(it.toString().trim())

                canLogin()
            }
            etPassword.doAfterTextChanged {

                ivPasswordEyes.visibility =
                    if (it.toString().isNotEmpty()) View.VISIBLE else View.GONE
                ivPasswordClear.visibility =
                    if (it.toString().isNotEmpty()) View.VISIBLE else View.GONE

                canLogin()
            }

            ivEmailClear.setOnClickListener {

                etAccount.setText("")
            }
            ivPasswordClear.setOnClickListener {

                etPassword.setText("")
            }


            ivPasswordEyes.setOnClickListener {

                isPasswordVisible = !isPasswordVisible

                if (isPasswordVisible) {
                    etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    ivPasswordEyes.setImageResource(R.drawable.iv_eyes)
                } else {
                    etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    ivPasswordEyes.setImageResource(R.drawable.iv_no_eyes)
                }

                etPassword.setSelection(etPassword.text?.length ?: 0)

            }


            stvLogin.click {

                if (loadingAnimator?.isRunning == true) return@click

                val email = etAccount.text.toString().trim()

                val password = etPassword.text.toString().trim()

                if (email.isBlank() || password.isBlank()) {

                    return@click
                }


                if (getCache(AppConstant.Constant.IS_VPN,"False") == "True" && isVpnConnected(this@LoginEmailActivity)){

                    initNoticeDialog()

                    return@click
                }


                loadingAnimator = stvLogin.startLoadingAnimation("Login")

                mViewModel.login(AppConstant.Constant.EMAIL, email, password)
            }
            sllGoogleLogin.click {

                finish()
            }


        }


    }

    override fun initData() {
    }


    override fun initViewModel() {


        mViewModel.mUserInfoData.observe(this) {

            loadingAnimator?.cancel()

            FlowBus.with(AppConstant.EventConstant.EVENT_FINISH).postValue(true)

            if (it.firstLogin == "True") {

                createIntent(BirthStepActivity::class.java).startActivity(this, true)

            } else {

                createIntent(MainActivity::class.java).startActivity(this, true)
            }

        }
        mViewModel.requestFailEvent.observe(this) {

            loadingAnimator?.cancel()


            mViewBinding.stvLogin.text = "Login"

            if (it is Boolean){
                initNoticeDialog()
            }

        }

    }


    private fun canLogin() {

        mViewBinding.apply {

            stvLogin.isEnabled =
                etAccount.text.toString().isNotBlank() && etPassword.text.toString().isNotBlank()

            stvLogin.alpha = if (stvLogin.isEnabled) 1f else 0.3f

        }

    }


    private fun initRecyclerView() {

        withViewBinding {

            mAdapter = object :
                BaseRecyclerAdapter<String, ItemEmailMenuBinding>(ItemEmailMenuBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemEmailMenuBinding>,
                    itemView: ItemEmailMenuBinding,
                    item: String,
                    position: Int
                ) {

                    itemView.tvEmail.text = item

                }

            }
            recyclerView.adapter = mAdapter

            mAdapter.setOnItemClickListener { _, _, position ->

                val item = mAdapter.getItem(position) ?: return@setOnItemClickListener


                etAccount.setText(item)

                etAccount.setSelection(etAccount.text.toString().length)

                createEmailMenuData("")
            }


        }

    }


    private fun createEmailMenuData(text: String) {

        try {


            if (text.isBlank()) {
                mAdapter.submitList(null)
                mViewBinding.recyclerView.visibility = View.GONE
                localPart = ""
                return
            }

            val isExactMatch = emailMenu.any { text.endsWith(it) }
            if (isExactMatch) {
                mAdapter.submitList(null)
                mViewBinding.recyclerView.visibility = View.GONE
                return
            }

            val atIndex = text.indexOf("@")

            if (atIndex == -1) {

                localPart = text
                val suggestions = emailMenu.map { localPart + it }
                mAdapter.submitList(suggestions)
                mViewBinding.recyclerView.visibility = View.VISIBLE
            } else {

                localPart = text.substring(0, atIndex)
                val domainPart = text.substring(atIndex).lowercase()


                val matchingDomains = emailMenu.filter { domain ->
                    domain.lowercase().startsWith(domainPart)
                }


                val suggestions = matchingDomains.map { localPart + it }

                if (suggestions.isNotEmpty()) {
                    mAdapter.submitList(suggestions)
                    mViewBinding.recyclerView.visibility = View.VISIBLE
                } else {

                    mAdapter.submitList(null)
                    mViewBinding.recyclerView.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initNoticeDialog() {


        NoticeDialog().apply {

            initView = { dialog, mViewBinding ->

                mViewBinding.tvTitle.visibility = View.GONE
                createNoticeText(mViewBinding.tvContent)
                mViewBinding.tvCancel.visibility = View.GONE
                mViewBinding.tvCommit.text = "OK"

            }

        }.show(supportFragmentManager)

    }

    private fun createNoticeText(text:TextView) {

        SpanUtils.with(text).append("Your account did not pass our security verification.If you believe this is a mistake, please contact us at:\n")
            .append("service@jolt-chat.com")
            .setClickSpan(getColor(R.color.color_21EACF), true) {

                this.copyContent(getString(R.string.app_name),"service@jolt-chat.com")

            }
            .create()
    }


    override fun finish() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        KeyboardUtils.hideSoftInput(this)
        super.finish()
    }
}