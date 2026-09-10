package com.noduq.app

interface AppConfig {
    val apiBaseUrl: String
    val supabaseUrl: String
    val supabaseAnonKey: String
}

interface TokenStore {
    fun ownerAccessToken(): String?
    fun ownerRefreshToken(): String?
    fun ownerEmail(): String?
    fun saveOwner(accessToken: String, refreshToken: String?, email: String?)
    fun employeeToken(): String?
    fun saveEmployee(token: String)
    fun clear()
}

interface Clipboard {
    fun copy(text: String): Boolean
}

object AppGraph {
    lateinit var config: AppConfig
    lateinit var tokens: TokenStore
    lateinit var api: NoduqApi
    lateinit var supabase: SupabaseAuthApi
    lateinit var clipboard: Clipboard
}

expect fun createHttpClient(): io.ktor.client.HttpClient

@androidx.compose.runtime.Composable
expect fun BackNavigation(enabled: Boolean = true, onBack: () -> Unit)
