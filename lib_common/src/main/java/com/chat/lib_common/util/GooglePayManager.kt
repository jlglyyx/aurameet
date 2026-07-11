package com.chat.lib_common.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.chat.lib_common.constant.AppConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

object GooglePayManager {

    private var coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var purchasesUpdatedListener: PurchasesUpdatedListener

    private lateinit var billingClient: BillingClient

    private const val TAG = "GooglePayManager"

    private var type = ""

    private var currentName = ""

    private val listeners = mutableMapOf<String, GooglePayListener>()

    interface GooglePayListener {

        fun onError(code:Int,data:String,orderId: String? = null)

        fun onClientSuccess()

        fun onPaySuccess(mPurchase:Purchase,type:String,lastOrderId:Int?)

        fun onHandlePurchaseSuccess(mPurchase:Purchase,type:String)
    }

    fun addListener(name:String,listener: GooglePayListener) {
        listeners[name] = listener
    }

    fun removeListener(name:String) {

        listeners.remove(name)
    }


    fun initGooglePay(context: Context) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    // Process the purchase as described in the next section.

                    if (purchase.purchaseState == PurchaseState.PURCHASED){

                        listeners[currentName]?.onPaySuccess(purchase, type,null)
                    }else{

                        listeners[currentName]?.onError(billingResult.responseCode,"pay success but status error :${purchase.purchaseState}")
                    }
//                    handlePurchase(purchase)
                    Log.i(TAG, "pay success: $purchase")
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user canceling the purchase flow.

                listeners[currentName]?.onError(billingResult.responseCode,"user canceled")

            } else {
                // Handle any other error codes.

                listeners[currentName]?.onError(billingResult.responseCode,"Pay error: ${billingResult.responseCode}")
            }
        }

        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .enableAutoServiceReconnection() // Add this line to enable reconnection
            // Configure other settings.
            .build()


        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    listeners[currentName]?.onClientSuccess()

                    Log.i(TAG, "onBillingSetupFinished: $billingResult")
                } else {
                    Log.i(TAG, "onBillingSetupFinished: ${billingResult}")

                    listeners[currentName]?.onError(billingResult.responseCode,"billing setup error: $billingResult")
                }


            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

                Log.i(TAG, "onBillingServiceDisconnected: ")

                listeners[currentName]?.onError(-1,"billing service disconnected")
            }
        })

    }

    fun isClient(): Boolean {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return false

        if (!::billingClient.isInitialized) return false

        return billingClient.isReady

    }



    fun queryProduct(activity: Activity,name: String, productId: String, oldPurchaseToken: String?,userId: String?) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        if (!isClient()) {

            listeners[currentName]?.onError(-1,"billing service disconnected")

            return
        }

        currentName = name

        val isSubscription = getCache(AppConstant.Constant.IS_SUBSCRIPTION, "False")

        val type =
            if (isSubscription == "True") BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

        this.type = type

        val weakReference = WeakReference(activity)

        weakReference.get()?.let { ac ->

            coroutineScope.launch {

                try {
                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(type)
                            .build()
                    )
                    val params = QueryProductDetailsParams.newBuilder()
                    params.setProductList(productList)

                    val productDetailsResult = withContext(Dispatchers.IO) {
                        billingClient.queryProductDetails(params.build())
                    }

                    if (type == BillingClient.ProductType.INAPP) {

                        payINAPPProduct(ac, productDetailsResult.productDetailsList,userId)
                    } else {

                        paySubsProduct(
                            ac,
                            productDetailsResult.productDetailsList,
                            oldPurchaseToken,userId
                        )
                    }


                    Log.i(
                        TAG,
                        "startQuery: ${productDetailsResult.billingResult}  ${productDetailsResult.productDetailsList}"
                    )

                } catch (e: Exception) {
                    e.printStackTrace()

                    listeners[currentName]?.onError(-2,"Query Product Error : ${e.message}")
                }
            }

        }
    }


    fun payINAPPProduct(activity: Activity, mProductDetail: List<ProductDetails>?, userId: String?) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        if (mProductDetail.isNullOrEmpty()) return

        val weakReference = WeakReference(activity)

        weakReference.get()?.let {

            try {

                val productDetailsParamsList = mProductDetail.filter { filter ->

                    null != filter.oneTimePurchaseOfferDetails?.offerToken
                }.map { map ->

                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(map)
                        .setOfferToken(map.oneTimePurchaseOfferDetails?.offerToken!!)
                        .build()
                }

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .apply {
                        userId?.let { userId -> setObfuscatedAccountId(userId) }
                    }
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                // Launch the billing flow
                val billingResult = billingClient.launchBillingFlow(it, billingFlowParams)

                Log.i(TAG, "startPay: ${billingResult}")

            } catch (e: Exception) {
                e.printStackTrace()
                listeners[currentName]?.onError(-3,"Pay One Time Product Error : ${e.message}")
            }
        }

    }


    fun paySubsProduct(
        activity: Activity,
        mProductDetail: List<ProductDetails>?,
        oldPurchaseToken: String?,
        userId: String?
    ) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        if (mProductDetail.isNullOrEmpty()) return

        val weakReference = WeakReference(activity)

        weakReference.get()?.let {

            try {

                val productDetailsParamsList = mProductDetail.filter { filter ->

                    !filter.subscriptionOfferDetails.isNullOrEmpty()

                }.map { map ->

                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(map)
                        .setOfferToken(map.subscriptionOfferDetails!!.first().offerToken)
                        .build()
                }

                val newBuilder = BillingFlowParams.newBuilder()

                userId?.let { userId -> newBuilder.setObfuscatedAccountId(userId) }

                newBuilder.setProductDetailsParamsList(productDetailsParamsList)

                if (!oldPurchaseToken.isNullOrEmpty()) {
                    newBuilder.setSubscriptionUpdateParams(
                        BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                            .setOldPurchaseToken(oldPurchaseToken)
                            .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION)
                            .build()
                    )
                }

                val billingFlowParams = newBuilder.build()

                // Launch the billing flow
                val billingResult = billingClient.launchBillingFlow(it, billingFlowParams)

                Log.i(TAG, "startPay: ${billingResult}")
            } catch (e: Exception) {
                e.printStackTrace()
                listeners[currentName]?.onError(-4,"Pay Sub Product Error : ${e.message}")
            }

        }
    }


    fun handleINAPPProduct(purchase: Purchase) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        coroutineScope.launch {
            try {

                if (purchase.isAcknowledged) {
                    return@launch
                }

                val consumeParams =
                    ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()

                val consumeResult = withContext(Dispatchers.IO) {

                    billingClient.consumePurchase(consumeParams)
                }

                if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    listeners[currentName]?.onHandlePurchaseSuccess(purchase, BillingClient.ProductType.INAPP)
                }

            } catch (e: Exception) {
                e.printStackTrace()

                listeners[currentName]?.onError(-5,"Handle One Time Error: ${e.message}",purchase.orderId)
            }
        }

    }


    fun handleSunsProduct(purchase: Purchase) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        coroutineScope.launch {
            try {

                if (purchase.isAcknowledged) {
                    return@launch
                }

                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()

                withContext(Dispatchers.IO) {

                    billingClient.acknowledgePurchase(
                        acknowledgePurchaseParams
                    ) {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {


                            listeners[currentName]?.onHandlePurchaseSuccess(
                                purchase,
                                BillingClient.ProductType.SUBS
                            )

                        } else {

                            listeners[currentName]?.onError(
                                it.responseCode,
                                "Handle Sub Product Error: ${it.responseCode}",
                                purchase.orderId
                            )
                        }
                    }

                }


            } catch (e: Exception) {
                e.printStackTrace()

                listeners[currentName]?.onError(-6,"Handle Sub Product Error: ${e.message}",purchase.orderId)
            }
        }

    }


    fun queryINAPPPPurchasesAsync() {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        if (!isClient()) return

        try {
            val params =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (purchases.isNotEmpty()) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == PurchaseState.PURCHASED) {
                            Log.i(TAG, "queryPurchasesAsync: " + purchase)


                            if (purchase.isAcknowledged) {
                                return@queryPurchasesAsync
                            }

                           val lastOrderId = getCache(AppConstant.Constant.BIZ_ID+"-"+purchase.products[0],-1)

                            listeners[currentName]?.onPaySuccess(purchase, BillingClient.ProductType.INAPP,lastOrderId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun querySubsPurchasesAsync() {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        if (!isClient()) return

        try {
            val params =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (purchases.isNotEmpty()) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == PurchaseState.PURCHASED) {
                            Log.i(TAG, "queryPurchasesAsync: " + purchase)


                            if (purchase.isAcknowledged) {
                                return@queryPurchasesAsync
                            }

                            val lastOrderId = getCache(AppConstant.Constant.BIZ_ID+"-"+purchase.products[0],-1)

                            listeners[currentName]?.onPaySuccess(purchase, BillingClient.ProductType.SUBS,lastOrderId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }






    suspend fun queryProductPrice(productIds: List<String>,subscription:String): ProductDetailsResult?{

        try {

            if (!AppConstant.ClientInfo.OPEN_GOOGLE) return null

            if (!isClient()) return null

            val type =
                if (subscription == "True") BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

            val productList =productIds.map {

                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(type)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params.build())
            }

            return productDetailsResult

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }



    fun disClient() {

        listeners.clear()

    }

}