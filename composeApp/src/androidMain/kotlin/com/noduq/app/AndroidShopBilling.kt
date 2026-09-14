package com.noduq.app

import android.app.Activity
import android.content.Context
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import java.lang.ref.WeakReference

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
        try {
            Purchases.sharedInstance.awaitLogIn(appUserId)
        } catch (ex: PurchasesException) {
            throw ApiException(0, "BILLING", ex.message)
        }
    }

    override suspend fun purchaseSmsMonthly(): Boolean {
        if (!configured) {
            throw ApiException(0, "BILLING", "Falta la clave de RevenueCat en esta instalación.")
        }
        val host = activity.get()
            ?: throw ApiException(0, "BILLING", "Abre la app otra vez e intenta activar el plan.")
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val pkg = offerings.current?.monthly
                ?: offerings.current?.availablePackages?.firstOrNull()
                ?: throw ApiException(0, "BILLING", "RevenueCat todavía no tiene el paquete mensual.")
            Purchases.sharedInstance.awaitPurchase(PurchaseParams.Builder(host, pkg).build())
            true
        } catch (ex: PurchasesTransactionException) {
            if (ex.userCancelled || ex.code == PurchasesErrorCode.PurchaseCancelledError) {
                false
            } else {
                throw ApiException(0, "BILLING", ex.message)
            }
        } catch (ex: PurchasesException) {
            if (ex.code == PurchasesErrorCode.PurchaseCancelledError) {
                false
            } else {
                throw ApiException(0, "BILLING", ex.message)
            }
        }
    }
}
