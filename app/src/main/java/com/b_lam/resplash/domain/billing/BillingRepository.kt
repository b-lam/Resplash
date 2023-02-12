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
import com.b_lam.resplash.data.billing.Sku
import com.b_lam.resplash.data.billing.Sku.INAPP_PRODUCTS
import com.b_lam.resplash.data.billing.model.*
import com.b_lam.resplash.util.debug
import com.b_lam.resplash.util.error
import com.b_lam.resplash.util.livedata.Event
import com.b_lam.resplash.util.warn
import kotlinx.coroutines.*
import java.lang.Long.min

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes

class BillingRepository(
    private val application: Application
) : PurchasesUpdatedListener, BillingClientStateListener {

    // How long before the data source tries to reconnect to Google Play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    private lateinit var billingClient: BillingClient

    private lateinit var localCacheBillingClient: LocalBillingDatabase

    val productsWithProductDetails = MutableLiveData<Map<String, ProductDetails>>()

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

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
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
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDatabase.getInstance(application)
    }

    fun endDataSourceConnections() {
        billingClient.endConnection()
    }

    private fun instantiateAndConnectToPlayBillingService() {
        billingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases()
            .setListener(this).build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
            return true
        }
        return false
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
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
                        productsWithProductDetails.postValue(
                            productDetailsList.associateBy { it.productId }
                        )
                        debug("productDetailsList: $productDetailsList")
                    } else {
                        productsWithProductDetails.postValue(emptyMap())
                        error("queryProductDetailsAsync response was empty")
                    }
                }
                else -> error(billingResult.debugMessage)
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase) = BillingSecurity.verifyPurchase(
        BillingSecurity.BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature
    )

    private fun processPurchases(purchases: Set<Purchase>) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val validPurchases = HashSet<Purchase>(purchases.size)
            debug("processPurchases newBatch content $purchases")
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (isSignatureValid(purchase)) {
                        validPurchases.add(purchase)
                    } else {
                        _billingMessageLiveData.postValue(Event("Unable to validate purchase"))
                    }
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    debug("Received a pending purchase of SKUs: ${purchase.products}")
                    _billingMessageLiveData.postValue(Event("Purchase is pending"))
                } else {
                    debug("Received an UNSPECIFIED_STATE purchase: $purchase")
                }
            }
            // Purchases cannot contain a mixture of consumable and non-consumable items
            val (consumables, nonConsumables) = validPurchases.partition { purchase ->
                purchase.products.any { Sku.CONSUMABLE_PRODUCTS.contains(it) }
            }
            debug("processPurchases consumables content $consumables")
            debug("processPurchases non-consumables content $nonConsumables")
            handleConsumablePurchasesAsync(consumables)
            acknowledgeNonConsumablePurchasesAsync(nonConsumables)
        }

    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        consumables.forEach {
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
        nonConsumables.forEach { purchase ->
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
                        Sku.COFFEE -> updateDonations(Donation(LEVEL_COFFEE))
                        Sku.SMOOTHIE -> updateDonations(Donation(LEVEL_SMOOTHIE))
                        Sku.PIZZA -> updateDonations(Donation(LEVEL_PIZZA))
                        Sku.FANCY_MEAL -> updateDonations(Donation(LEVEL_FANCY_MEAL))
                    }
                }
            }
            _purchaseCompleteLiveData.postValue(Event(purchase))
        }

    private fun disburseNonConsumableEntitlement(purchase: Purchase) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            for (product in purchase.products) {
                when (product) {
                    Sku.RESPLASH_PRO -> insert(ResplashPro(true))
                }
            }
        }
    }

    @WorkerThread
    suspend fun updateDonations(donation: Donation) = withContext(Dispatchers.IO) {
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
    }

    @WorkerThread
    private suspend fun insert(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())
    }
}
