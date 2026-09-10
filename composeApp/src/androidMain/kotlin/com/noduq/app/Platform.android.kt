package com.noduq.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            writeTimeout(20, TimeUnit.SECONDS)
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 20_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 20_000
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
                explicitNulls = false
            },
        )
    }
}

@Composable
actual fun BackNavigation(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}

class AndroidClipboard(private val context: Context) : Clipboard {
    override fun copy(text: String): Boolean {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("NODUQ", text))
        return true
    }
}

class AndroidTokenStore(context: Context) : TokenStore {
    private val prefs: SharedPreferences = createPrefs(context)

    override fun ownerAccessToken(): String? = prefs.getString(OWNER_ACCESS, null)
    override fun ownerRefreshToken(): String? = prefs.getString(OWNER_REFRESH, null)
    override fun ownerEmail(): String? = prefs.getString(OWNER_EMAIL, null)
    override fun employeeToken(): String? = prefs.getString(EMPLOYEE, null)

    override fun saveOwner(accessToken: String, refreshToken: String?, email: String?) {
        prefs.edit()
            .putString(OWNER_ACCESS, accessToken)
            .putString(OWNER_REFRESH, refreshToken)
            .putString(OWNER_EMAIL, email)
            .remove(EMPLOYEE)
            .apply()
    }

    override fun saveEmployee(token: String) {
        prefs.edit()
            .putString(EMPLOYEE, token)
            .remove(OWNER_ACCESS)
            .remove(OWNER_REFRESH)
            .remove(OWNER_EMAIL)
            .apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val FILE = "noduq_secure"
        const val OWNER_ACCESS = "owner_access_token"
        const val OWNER_REFRESH = "owner_refresh_token"
        const val OWNER_EMAIL = "owner_email"
        const val EMPLOYEE = "employee_token"

        fun createPrefs(context: Context): SharedPreferences {
            return try {
                val master = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    FILE,
                    master,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (_: Exception) {
                context.getSharedPreferences("noduq_secure_fallback", Context.MODE_PRIVATE)
            }
        }
    }
}
