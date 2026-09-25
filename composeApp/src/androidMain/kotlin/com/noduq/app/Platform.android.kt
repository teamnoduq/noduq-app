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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
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

actual fun motionEnabled(): Boolean = android.animation.ValueAnimator.areAnimatorsEnabled()

class AndroidClipboard(private val context: Context) : Clipboard {
    override fun copy(text: String): Boolean {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("NODUQ", text))
        return true
    }
}

class AndroidLinkOpener(private val context: Context) : LinkOpener {
    override fun open(url: String) {
        val parsed = android.net.Uri.parse(url)
        val tabs = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
        tabs.intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            tabs.launchUrl(context, parsed)
        }.onFailure {
            context.startActivity(
                android.content.Intent(android.content.Intent.ACTION_VIEW, parsed)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}

actual fun isoInstant(millis: Long): String = Instant.ofEpochMilli(millis).toString()

actual fun epochMillis(iso: String?): Long = readInstant(iso)?.toEpochMilli() ?: -1L

actual fun clockLabel(iso: String?): String {
    val moment = readInstant(iso) ?: return ""
    return CLOCK.format(moment.atZone(ZoneId.systemDefault()))
}

actual fun dayLabel(iso: String?): String {
    val moment = readInstant(iso) ?: return ""
    val day = moment.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now(ZoneId.systemDefault())
    return when (day) {
        today -> "hoy"
        today.minusDays(1) -> "ayer"
        else -> DAY.format(day)
    }
}

actual fun localDayStartIso(daysAgo: Int): String =
    LocalDate.now(ZONE).minusDays(daysAgo.toLong()).atStartOfDay(ZONE).toInstant().toString()

actual fun localDayEndExclusiveIso(daysAgo: Int): String =
    LocalDate.now(ZONE).minusDays(daysAgo.toLong()).plusDays(1).atStartOfDay(ZONE).toInstant().toString()

actual fun localWeekStartIso(): String {
    val today = LocalDate.now(ZONE)
    val monday = today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
    return monday.atStartOfDay(ZONE).toInstant().toString()
}

actual fun dayStartIsoFromUtcMillis(utcMillis: Long): String {
    val day = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    return day.atStartOfDay(ZONE).toInstant().toString()
}

actual fun nextDayStartIsoFromUtcMillis(utcMillis: Long): String {
    val day = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    return day.plusDays(1).atStartOfDay(ZONE).toInstant().toString()
}

actual fun filterDateLabel(iso: String?): String {
    val moment = readInstant(iso) ?: return "Elegir"
    return FILTER_DAY.format(moment.atZone(ZONE).toLocalDate())
}

actual fun longDateLabel(iso: String?): String {
    val moment = readInstant(iso) ?: return ""
    return LONG_DAY.format(moment.atZone(ZONE).toLocalDate())
}

private val ZONE: ZoneId = ZoneId.systemDefault()
private val SPANISH = Locale("es", "CO")
private val CLOCK = DateTimeFormatter.ofPattern("h:mm a", SPANISH)
private val DAY = DateTimeFormatter.ofPattern("d MMM", SPANISH)
private val FILTER_DAY = DateTimeFormatter.ofPattern("d MMM yyyy", SPANISH)
private val LONG_DAY = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", SPANISH)

/** Tolerant on purpose: the server may spell instants as text or as epoch seconds. */
private fun readInstant(iso: String?): Instant? {
    val raw = iso?.trim().orEmpty()
    if (raw.isEmpty()) return null
    runCatching { return Instant.parse(raw) }
    runCatching { return OffsetDateTime.parse(raw).toInstant() }
    runCatching { return Instant.ofEpochMilli((raw.toDouble() * 1000).toLong()) }
    return null
}

internal fun securePrefs(context: Context, file: String): SharedPreferences {
    return try {
        val master = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            file,
            master,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (_: Exception) {
        context.getSharedPreferences(file + "_fallback", Context.MODE_PRIVATE)
    }
}

/**
 * Bank texts that have not reached the server yet, kept encrypted because they name the
 * customer who paid.
 */
class AndroidPendingSmsStore(context: Context) : PendingSmsStore {
    private val prefs = securePrefs(context, "noduq_pending_sms")

    override fun keep(sender: String, message: String, sentAtMillis: Long) {
        val next = (waiting() + PendingSms(sender, message, sentAtMillis)).takeLast(MAX)
        write(next)
    }

    override fun waiting(): List<PendingSms> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(PendingSms.serializer()), raw) }
            .getOrDefault(emptyList())
    }

    override fun forget(entries: List<PendingSms>) {
        val gone = entries.toSet()
        write(waiting().filterNot { it in gone })
    }

    private fun write(entries: List<PendingSms>) {
        if (entries.isEmpty()) {
            prefs.edit().remove(KEY).apply()
        } else {
            prefs.edit()
                .putString(KEY, json.encodeToString(ListSerializer(PendingSms.serializer()), entries))
                .apply()
        }
    }

    private companion object {
        const val KEY = "waiting"
        const val MAX = 50
        val json = Json { ignoreUnknownKeys = true }
    }
}

class AndroidTokenStore(context: Context) : TokenStore {
    private val prefs: SharedPreferences = securePrefs(context, FILE)

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

    override fun onboardingStep(): Int? {
        if (!prefs.contains(ONBOARDING)) return null
        return prefs.getInt(ONBOARDING, 0)
    }

    override fun setOnboardingStep(step: Int?) {
        if (step == null) {
            prefs.edit()
                .remove(ONBOARDING)
                .remove(ONBOARD_SHOP)
                .remove(ONBOARD_NAME)
                .apply()
        } else {
            prefs.edit().putInt(ONBOARDING, step).apply()
        }
    }

    override fun onboardShop(): String = prefs.getString(ONBOARD_SHOP, "") ?: ""

    override fun setOnboardShop(value: String) {
        prefs.edit().putString(ONBOARD_SHOP, value).apply()
    }

    override fun onboardName(): String = prefs.getString(ONBOARD_NAME, "") ?: ""

    override fun setOnboardName(value: String) {
        prefs.edit().putString(ONBOARD_NAME, value).apply()
    }

    private companion object {
        const val FILE = "noduq_secure"
        const val OWNER_ACCESS = "owner_access_token"
        const val OWNER_REFRESH = "owner_refresh_token"
        const val OWNER_EMAIL = "owner_email"
        const val EMPLOYEE = "employee_token"
        const val ONBOARDING = "onboarding_step"
        const val ONBOARD_SHOP = "onboard_shop"
        const val ONBOARD_NAME = "onboard_name"
    }
}
