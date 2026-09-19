package com.noduq.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NoduqApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val http = createHttpClient()
        AppGraph.config = AndroidAppConfig
        AppGraph.tokens = AndroidTokenStore(this)
        AppGraph.clipboard = AndroidClipboard(this)
        AppGraph.links = AndroidLinkOpener(this)
        AppGraph.paymentAlerts = AndroidLocalPaymentAlerts(this)
        AppGraph.api = NoduqApi(http, AndroidAppConfig)
        AppGraph.supabase = SupabaseAuthApi(client = http, config = AndroidAppConfig)
        AppGraph.googleAuth = AndroidGoogleAuth(
            supabase = AppGraph.supabase,
            config = AndroidAppConfig,
        )
        AppGraph.push = AndroidPushTokens()
        AppGraph.permissions = AndroidPermissions(this)
        AppGraph.pendingSms = AndroidPendingSmsStore(this)
        AppGraph.bankSms = BankSmsForwarder(
            tokens = AppGraph.tokens,
            api = AppGraph.api,
            supabase = AppGraph.supabase,
            pending = AppGraph.pendingSms,
        )
        AppGraph.billing = if (BuildConfig.REVENUECAT_API_KEY.isNotBlank()) {
            AndroidShopBilling(this, BuildConfig.REVENUECAT_API_KEY.trim())
        } else {
            MissingShopBilling()
        }

        createPaymentChannel(this)

        // A text may have arrived while the phone was offline or the session was stale. The
        // process also wakes up here for an incoming SMS, so this is the earliest retry.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { AppGraph.bankSms.flushPending() }
        }
    }
}

object AndroidAppConfig : AppConfig {
    override val apiBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')
    override val supabaseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')
    override val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
    override val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
    override val revenueCatApiKey: String = BuildConfig.REVENUECAT_API_KEY.trim()
}
