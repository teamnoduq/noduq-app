package com.noduq.app

interface ShopBilling {
    val configured: Boolean

    fun attach(activity: Any)

    fun detach()

    suspend fun logIn(appUserId: String)

    /** True when Test Store / Play accepted the monthly NODUQ SMS+correo package. */
    suspend fun purchaseSmsMonthly(): Boolean
}

class MissingShopBilling : ShopBilling {
    override val configured: Boolean = false

    override fun attach(activity: Any) = Unit

    override fun detach() = Unit

    override suspend fun logIn(appUserId: String) = Unit

    override suspend fun purchaseSmsMonthly(): Boolean {
        throw ApiException(0, "BILLING", "Falta la clave de RevenueCat en esta instalación.")
    }
}
