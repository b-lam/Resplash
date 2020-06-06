package com.b_lam.resplash.domain.billing

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.b_lam.resplash.data.billing.LocalBillingDatabase
import com.b_lam.resplash.data.billing.model.*
import com.b_lam.resplash.domain.billing.BillingRepository.Sku.INAPP_SKUS
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.*
import java.util.*

class BillingRepository(
    private val application: Application
) : PurchasesUpdatedListener, BillingClientStateListener {

    private lateinit var playStoreBillingClient: BillingClient

    private lateinit var localCacheBillingClient: LocalBillingDatabase

    val consumableSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDatabase.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getSkuDetailsLiveDataInList(Sku.CONSUMABLE_SKUS)
    }

    val resplashProSkuDetailsLiveData: LiveData<AugmentedSkuDetails?> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDatabase.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getSkuDetailsLiveDataById(Sku.RESPLASH_PRO)
    }

    private val purchaseCompleteMutableLiveData = MutableLiveData<Event<Purchase>>()
    val purchaseCompleteLiveData: LiveData<Event<Purchase>> = purchaseCompleteMutableLiveData

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
        launchBillingFlow(activity, SkuDetails(augmentedSkuDetails.originalJson))

    private fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        playStoreBillingClient.launchBillingFlow(activity, purchaseParams)
    }

    fun startDataSourceConnections() {
        Log.d(TAG, "startDataSourceConnections")
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDatabase.getInstance(application)
    }

    fun endDataSourceConnections() {
        Log.d(TAG, "endDataSourceConnections")
        playStoreBillingClient.endConnection()
    }

    private fun instantiateAndConnectToPlayBillingService() {
        playStoreBillingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases()
            .setListener(this).build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        Log.d(TAG, "connectToPlayBillingService")
        if (!playStoreBillingClient.isReady) {
            playStoreBillingClient.startConnection(this)
            return true
        }
        return false
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "onBillingSetupFinished successfully")
                querySkuDetailsAsync(BillingClient.SkuType.INAPP, INAPP_SKUS)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> Log.d(TAG, billingResult.debugMessage)
            else -> Log.d(TAG, billingResult.debugMessage)
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        connectToPlayBillingService()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.apply { processPurchases(this.toSet()) }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchasesAsync()
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> connectToPlayBillingService()
            else -> Log.i(TAG, billingResult.debugMessage)
        }
    }

    fun queryPurchasesAsync() {
        Log.d(TAG, "queryPurchasesAsync called")
        val purchasesResult = HashSet<Purchase>()
        val result = playStoreBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(TAG, "queryPurchasesAsync INAPP results: ${result.purchasesList?.size}")
        result.purchasesList?.apply { purchasesResult.addAll(this) }
        processPurchases(purchasesResult)
    }

    private fun querySkuDetailsAsync(
        @BillingClient.SkuType skuType: String,
        skuList: List<String>
    ) {
        Log.d(TAG, "querySkuDetailsAsync for $skuType")
        val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(skuType).build()
        playStoreBillingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (skuDetailsList.orEmpty().isNotEmpty()) {
                        skuDetailsList.forEach {
                            CoroutineScope(Job() + Dispatchers.IO).launch {
                                localCacheBillingClient.skuDetailsDao().insertOrUpdate(it)
                            }
                        }
                    }
                }
                else -> Log.e(TAG, billingResult.debugMessage)
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase) = BillingSecurity.verifyPurchase(
        BillingSecurity.BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature
    )

    private fun processPurchases(purchasesResult: Set<Purchase>) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            Log.d(TAG, "processPurchases called")
            val validPurchases = HashSet<Purchase>(purchasesResult.size)
            Log.d(TAG, "processPurchases newBatch content $purchasesResult")
            purchasesResult.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (isSignatureValid(purchase)) {
                        validPurchases.add(purchase)
                    }
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    Log.d(TAG, "Received a pending purchase of SKU: ${purchase.sku}")
                    // handle pending purchases, e.g. confirm with users about the pending
                    // purchases, prompt them to complete it, etc.
                } else {
                    Log.d(TAG, "Received an UNSPECIFIED_STATE purchase: $purchase")
                }
            }
            val (consumables, nonConsumables) = validPurchases.partition {
                Sku.CONSUMABLE_SKUS.contains(it.sku)
            }
            Log.d(TAG, "processPurchases consumables content $consumables")
            Log.d(TAG, "processPurchases non-consumables content $nonConsumables")

            val testing = localCacheBillingClient.purchaseDao().getPurchases()
            Log.d(TAG, "processPurchases purchases in the lcl db ${testing.size}")
            localCacheBillingClient.purchaseDao().insert(*validPurchases.toTypedArray())
            handleConsumablePurchasesAsync(consumables)
            acknowledgeNonConsumablePurchasesAsync(nonConsumables)
        }

    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        Log.d(TAG, "handleConsumablePurchasesAsync called")
        consumables.forEach {
            Log.d(TAG, "handleConsumablePurchasesAsync foreach it is $it")
            val params = ConsumeParams.newBuilder().setPurchaseToken(it.purchaseToken).build()
            playStoreBillingClient.consumeAsync(params) { billingResult, purchaseToken ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> purchaseToken.apply { disburseConsumableEntitlements(it) }
                    else -> Log.w(TAG, billingResult.debugMessage)
                }
            }
        }
    }

    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        nonConsumables.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            playStoreBillingClient.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> disburseNonConsumableEntitlement(purchase)
                    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> removeNonConsumableEntitlement(purchase)
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> removeNonConsumableEntitlement(purchase)
                    else -> Log.d(TAG, "acknowledgeNonConsumablePurchasesAsync response is ${billingResult.debugMessage}")
                }
            }
        }
    }

    private fun disburseConsumableEntitlements(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            if (Sku.CONSUMABLE_SKUS.contains(purchase.sku)) {
                when (purchase.sku) {
                    Sku.COFFEE -> updateDonations(purchase.sku,
                        Donation(
                            LEVEL_COFFEE
                        )
                    )
                    Sku.SMOOTHIE -> updateDonations(purchase.sku,
                        Donation(
                            LEVEL_SMOOTHIE
                        )
                    )
                    Sku.PIZZA -> updateDonations(purchase.sku,
                        Donation(
                            LEVEL_PIZZA
                        )
                    )
                    Sku.FANCY_MEAL -> updateDonations(purchase.sku,
                        Donation(
                            LEVEL_FANCY_MEAL
                        )
                    )
                }
                purchaseCompleteMutableLiveData.postValue(Event(purchase))
            }
            localCacheBillingClient.purchaseDao().delete(purchase)
        }

    private fun disburseNonConsumableEntitlement(purchase: Purchase) {
        Log.d(TAG, "disburseNonConsumableEntitlement")
        CoroutineScope(Job() + Dispatchers.IO).launch {
            when (purchase.sku) {
                Sku.RESPLASH_PRO -> {
                    val resplashPro =
                        ResplashPro(true)
                    insert(resplashPro)
                    localCacheBillingClient.skuDetailsDao()
                        .insertOrUpdate(purchase.sku, resplashPro.mayPurchase())
                }
            }
            localCacheBillingClient.purchaseDao().delete(purchase)
        }
    }

    private fun removeNonConsumableEntitlement(purchase: Purchase) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            when (purchase.sku) {
                Sku.RESPLASH_PRO -> {
                    val resplashPro =
                        ResplashPro(false)
                    delete(resplashPro)
                    localCacheBillingClient.skuDetailsDao()
                        .insertOrUpdate(purchase.sku, resplashPro.mayPurchase())
                }
            }
        }
        handleConsumablePurchasesAsync(listOf(purchase))
    }

    @WorkerThread
    suspend fun updateDonations(sku: String, donation: Donation) = withContext(Dispatchers.IO) {
        Log.d(TAG, "updateDonations")
        var update = donation
        donationLiveData.value?.apply {
            synchronized(this) {
                if (this != donation) { //new purchase
                    update =
                        Donation(level + donation.level)
                }
                Log.d(
                    TAG,
                    "New purchase level is ${donation.level}; existing level is ${level}; so the final result is ${update.level}"
                )
                localCacheBillingClient.entitlementsDao().update(update)
            }
        }
        if (donationLiveData.value == null) {
            localCacheBillingClient.entitlementsDao().insert(update)
            Log.d(TAG, "We just added from null donation with level: ${donation.level}")
        }
        localCacheBillingClient.skuDetailsDao().insertOrUpdate(sku, update.mayPurchase())
        Log.d(TAG, "Updated AugmentedSkuDetails as well")
    }

    @WorkerThread
    private suspend fun insert(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    @WorkerThread
    private suspend fun delete(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().delete(entitlement)
    }

    companion object {

        private val TAG = BillingRepository::class.simpleName
    }

    private object Sku {

        const val RESPLASH_PRO = "pro"
        const val COFFEE = "coffee"
        const val SMOOTHIE = "smoothie"
        const val PIZZA = "pizza"
        const val FANCY_MEAL = "meal"

        val INAPP_SKUS = listOf(RESPLASH_PRO, COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
        val CONSUMABLE_SKUS = listOf(COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
    }
}