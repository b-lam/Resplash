package com.b_lam.resplash.domain.billing

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.b_lam.resplash.data.billing.LocalBillingDatabase
import com.b_lam.resplash.data.billing.model.*
import com.b_lam.resplash.domain.billing.BillingRepository.Sku.INAPP_PRODUCTS
import com.b_lam.resplash.util.debug
import com.b_lam.resplash.util.error
import com.b_lam.resplash.util.livedata.Event
import com.b_lam.resplash.util.warn
import kotlinx.coroutines.*
import java.lang.Long.min
import java.util.*

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes

class BillingRepository(
    private val application: Application
) : PurchasesUpdatedListener, BillingClientStateListener {

    // How long before the data source tries to reconnect to Google Play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    private lateinit var billingClient: BillingClient

    private lateinit var localCacheBillingClient: LocalBillingDatabase

    val consumableSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDatabase.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getSkuDetailsLiveDataInList(Sku.CONSUMABLE_PRODUCTS)
    }

    val resplashProSkuDetailsLiveData: LiveData<AugmentedSkuDetails?> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDatabase.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getSkuDetailsLiveDataById(Sku.RESPLASH_PRO)
    }

    private val _purchaseCompleteLiveData = MutableLiveData<Event<Purchase>>()
    val purchaseCompleteLiveData: LiveData<Event<Purchase>> = _purchaseCompleteLiveData

    private val _billingMessageLiveData = MutableLiveData<Event<String>>()
    val billingMessageLiveData: LiveData<Event<String>> = _billingMessageLiveData

    private val _billingErrorLiveData = MutableLiveData<Event<BillingResult>>()
    val billingErrorLiveData: LiveData<Event<BillingResult>> = _billingErrorLiveData

    val donationLiveData: LiveData<Donation?> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDatabase.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getDonation()
    }

    val resplashProLiveData: LiveData<ResplashPro?> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDatabase.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getResplashPro()
    }

    fun launchBillingFlow(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) =
        launchBillingFlow(activity, SkuDetails(augmentedSkuDetails.originalJson!!))

    private fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        billingClient.launchBillingFlow(activity, purchaseParams)
    }

    private fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun startDataSourceConnections() {
        debug("startDataSourceConnections")
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDatabase.getInstance(application)
    }

    fun endDataSourceConnections() {
        debug("endDataSourceConnections")
        billingClient.endConnection()
    }

    private fun instantiateAndConnectToPlayBillingService() {
        billingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases()
            .setListener(this).build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        debug("connectToPlayBillingService")
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
            return true
        }
        return false
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                debug("onBillingSetupFinished successfully")
                queryProductDetailsAsync()
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                debug(billingResult.debugMessage)
            else -> debug(billingResult.debugMessage)
        }
    }

    /**
     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
     * self-upgrades or is force closed.
     */
    override fun onBillingServiceDisconnected() {
        debug("onBillingServiceDisconnected")
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(
            { billingClient.startConnection(this@BillingRepository) },
            reconnectMilliseconds
        )
        reconnectMilliseconds = min(
            reconnectMilliseconds * 2,
            RECONNECT_TIMER_MAX_TIME_MILLISECONDS
        )
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.apply { processPurchases(this.toSet()) }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchasesAsync()
            else -> debug("${billingResult.responseCode}: ${billingResult.debugMessage}")
        }
    }

    fun queryPurchasesAsync(restore: Boolean = false) {
        debug("queryPurchasesAsync called")
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    debug("queryPurchasesAsync INAPP results: ${purchasesList.size}")
                    if (restore && purchasesList.isEmpty()) {
                        _billingMessageLiveData.postValue(Event("No purchases found"))
                    }
                    processPurchases(purchasesList.toSet())
                }
                else -> error(billingResult.debugMessage)
            }
        }
    }

    private fun queryProductDetailsAsync() {
        debug("querySkuDetailsAsync")
        val productList = INAPP_PRODUCTS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (productDetailsList.isNotEmpty()) {
                        productDetailsList.forEach {
                            CoroutineScope(Job() + Dispatchers.IO).launch {
                                localCacheBillingClient.skuDetailsDao().insertOrUpdate(it)
                            }
                        }
                    }
                }
                else -> error(billingResult.debugMessage)
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase) = BillingSecurity.verifyPurchase(
        BillingSecurity.BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature
    )

    private fun processPurchases(purchasesResult: Set<Purchase>) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            debug("processPurchases called")
            val validPurchases = HashSet<Purchase>(purchasesResult.size)
            debug("processPurchases newBatch content $purchasesResult")
            purchasesResult.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (isSignatureValid(purchase)) {
                        validPurchases.add(purchase)
                    } else {
                        _billingMessageLiveData.postValue(Event("Unable to validate purchase"))
                    }
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    debug("Received a pending purchase of SKU: ${purchase.sku}")
                    _billingMessageLiveData.postValue(Event("Purchase is pending"))
                } else {
                    debug("Received an UNSPECIFIED_STATE purchase: $purchase")
                }
            }
            val (consumables, nonConsumables) = validPurchases.partition {
                Sku.CONSUMABLE_PRODUCTS.contains(it.sku)
            }
            debug("processPurchases consumables content $consumables")
            debug("processPurchases non-consumables content $nonConsumables")

            val testing = localCacheBillingClient.purchaseDao().getPurchases()
            debug("processPurchases purchases in the lcl db ${testing.size}")
            localCacheBillingClient.purchaseDao().insert(*validPurchases.toTypedArray())
            handleConsumablePurchasesAsync(consumables)
            acknowledgeNonConsumablePurchasesAsync(nonConsumables)
        }

    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        debug("handleConsumablePurchasesAsync called")
        consumables.forEach {
            debug("handleConsumablePurchasesAsync foreach it is $it")
            val params = ConsumeParams.newBuilder().setPurchaseToken(it.purchaseToken).build()
            billingClient.consumeAsync(params) { billingResult, _ ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> disburseConsumableEntitlements(it)
                    else -> {
                        val message = "${billingResult.responseCode}: ${billingResult.debugMessage}"
                        warn(message)
                        _billingMessageLiveData.postValue(Event(message))
                        _billingErrorLiveData.postValue(Event(billingResult))
                    }
                }
            }
        }
    }

    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        debug("acknowledgeNonConsumablePurchasesAsync called")
        nonConsumables.forEach { purchase ->
            debug("handleConsumablePurchasesAsync foreach purchase is $purchase")
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { billingResult ->
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK ->
                            disburseNonConsumableEntitlement(purchase)
                        else -> {
                            val message = "${billingResult.responseCode}: ${billingResult.debugMessage}"
                            warn(message)
                            _billingMessageLiveData.postValue(Event(message))
                            _billingErrorLiveData.postValue(Event(billingResult))
                        }
                    }
                }
            } else {
                disburseNonConsumableEntitlement(purchase)
            }
        }
    }

    private fun disburseConsumableEntitlements(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            for (product in purchase.products) {
                if (Sku.CONSUMABLE_PRODUCTS.contains(product)) {
                    when (product) {
                        Sku.COFFEE -> updateDonations(product, Donation(LEVEL_COFFEE))
                        Sku.SMOOTHIE -> updateDonations(product, Donation(LEVEL_SMOOTHIE))
                        Sku.PIZZA -> updateDonations(product, Donation(LEVEL_PIZZA))
                        Sku.FANCY_MEAL -> updateDonations(product, Donation(LEVEL_FANCY_MEAL))
                    }
                }
            }
            _purchaseCompleteLiveData.postValue(Event(purchase))
            localCacheBillingClient.purchaseDao().delete(purchase)
        }

    private fun disburseNonConsumableEntitlement(purchase: Purchase) {
        debug("disburseNonConsumableEntitlement")
        CoroutineScope(Job() + Dispatchers.IO).launch {
            for (product in purchase.products) {
                when (product) {
                    Sku.RESPLASH_PRO -> {
                        val resplashPro = ResplashPro(true)
                        insert(resplashPro)
                        localCacheBillingClient.skuDetailsDao()
                            .insertOrUpdate(product, resplashPro.mayPurchase())
                    }
                }
            }
            localCacheBillingClient.purchaseDao().delete(purchase)
        }
    }

    @WorkerThread
    suspend fun updateDonations(product: String, donation: Donation) = withContext(Dispatchers.IO) {
        debug("updateDonations")
        var update = donation
        donationLiveData.value?.apply {
            synchronized(this) {
                if (this != donation) { //new purchase
                    update = Donation(level + donation.level)
                }
                debug("New purchase level is ${donation.level}; existing level is ${level}; " +
                        "so the final result is ${update.level}")
                localCacheBillingClient.entitlementsDao().update(update)
            }
        }
        if (donationLiveData.value == null) {
            localCacheBillingClient.entitlementsDao().insert(update)
            debug("We just added from null donation with level: ${donation.level}")
        }
        localCacheBillingClient.skuDetailsDao().insertOrUpdate(product, update.mayPurchase())
        debug("Updated AugmentedSkuDetails as well")
    }

    @WorkerThread
    private suspend fun insert(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    private object Sku {

        const val RESPLASH_PRO = "pro"
        const val COFFEE = "coffee"
        const val SMOOTHIE = "smoothie"
        const val PIZZA = "pizza"
        const val FANCY_MEAL = "meal"

        val INAPP_PRODUCTS = listOf(RESPLASH_PRO, COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
        val CONSUMABLE_PRODUCTS = listOf(COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())
    }
}
