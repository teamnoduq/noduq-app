package com.noduq.app

import android.app.Application

class NoduqApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val http = createHttpClient()
        AppGraph.config = AndroidAppConfig
        AppGraph.tokens = AndroidTokenStore(this)
        AppGraph.clipboard = AndroidClipboard(this)
        AppGraph.api = NoduqApi(http, AndroidAppConfig)
        AppGraph.supabase = SupabaseAuthApi(client = http, config = AndroidAppConfig)
    }
}

object AndroidAppConfig : AppConfig {
    override val apiBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')
    override val supabaseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')
    override val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
}
