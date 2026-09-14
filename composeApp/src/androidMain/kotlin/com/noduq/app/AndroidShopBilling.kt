package com.noduq.app

import android.app.Activity
import android.content.Context
import com.revenuecat.purchases.LogInCallback
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidShopBilling(context: Context, private val apiKey: String) : ShopBilling {
    private var activity: WeakReference<Activity> = WeakReference(null)

    override val configured: Boolean = apiKey.isNotBlank()

    init {
        if (configured && !Purchases.isConfigured) {
            Purchases.configure(PurchasesConfiguration.Builder(context.applicationContext, apiKey).build())
        }
    }

    override fun attach(activity: Any) {
        if (activity is Activity) {
            this.activity = WeakReference(activity)
        }
    }

    override fun detach() {
        activity.clear()
    }

    override suspend fun logIn(appUserId: String) {
        if (!configured || appUserId.isBlank()) return
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.logIn(
                appUserId,
                object : LogInCallback {
                    override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                        if (cont.isActive) cont.resume(Unit)
                    }

                    override fun onError(error: PurchasesError) {
                        if (cont.isActive) {
                            cont.resumeWithException(ApiException(0, "BILLING", error.message))
                        }
                    }
                },
            )
        }
    }

    override suspend fun purchaseSmsMonthly(): Boolean {
        if (!configured) {
            throw ApiException(0, "BILLING", "Falta la clave de RevenueCat en esta instalación.")
        }
        val host = activity.get()
            ?: throw ApiException(0, "BILLING", "Abre la app otra vez e intenta activar el plan.")
        val offerings = awaitOfferings()
        val pkg = offerings.current?.monthly
            ?: offerings.current?.availablePackages?.firstOrNull()
            ?: throw ApiException(0, "BILLING", "RevenueCat todavía no tiene el paquete mensual.")
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.purchase(
                PurchaseParams.Builder(host, pkg).build(),
                object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        if (cont.isActive) cont.resume(true)
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        if (!cont.isActive) return
                        if (userCancelled || error.code == PurchasesErrorCode.PurchaseCancelledError) {
                            cont.resume(false)
                        } else {
                            cont.resumeWithException(ApiException(0, "BILLING", error.message))
                        }
                    }
                },
            )
        }
    }

    private suspend fun awaitOfferings(): Offerings = suspendCancellableCoroutine { cont ->
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                if (cont.isActive) cont.resume(offerings)
            }

            override fun onError(error: PurchasesError) {
                if (cont.isActive) {
                    cont.resumeWithException(ApiException(0, "BILLING", error.message))
                }
            }
        })
    }
}
