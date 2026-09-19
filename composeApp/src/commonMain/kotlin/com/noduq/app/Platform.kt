package com.noduq.app

interface AppConfig {
    val apiBaseUrl: String
    val supabaseUrl: String
    val supabaseAnonKey: String
    val googleWebClientId: String
    val revenueCatApiKey: String
}

interface GoogleAuth {
    suspend fun signIn(): SupabaseSession
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

interface LinkOpener {
    fun open(url: String)
}

/** The address this phone answers to for push. Null when the phone cannot reach Firebase. */
interface PushTokens {
    suspend fun current(): String?
}

enum class PermissionState {
    Granted,

    /** Asked and refused, but asking again is still allowed. */
    Denied,

    /** Refused for good. Only the system settings can turn it back on. */
    Blocked,

    /** This Android version does not ask for it. */
    NotNeeded,
}

interface DevicePermissions {
    fun notifications(): PermissionState
    fun sms(): PermissionState
    suspend fun requestNotifications(): PermissionState
    suspend fun requestSms(): PermissionState
    fun openSettings()
}

/**
 * Bank messages that could not be handed to the server yet. Losing one means losing a
 * payment, so they wait on disk until the next chance.
 */
interface PendingSmsStore {
    fun keep(sender: String, message: String, sentAtMillis: Long)

    /** Reads without removing, so nothing is lost if the delivery attempt dies halfway. */
    fun waiting(): List<PendingSms>
    fun forget(entries: List<PendingSms>)
}

@kotlinx.serialization.Serializable
data class PendingSms(
    val sender: String,
    val message: String,
    val sentAtMillis: Long,
)

object AppGraph {
    lateinit var config: AppConfig
    lateinit var tokens: TokenStore
    lateinit var api: NoduqApi
    lateinit var supabase: SupabaseAuthApi
    lateinit var googleAuth: GoogleAuth
    lateinit var clipboard: Clipboard
    lateinit var links: LinkOpener
    lateinit var push: PushTokens
    lateinit var permissions: DevicePermissions
    lateinit var pendingSms: PendingSmsStore
    lateinit var bankSms: BankSmsForwarder
    lateinit var billing: ShopBilling
}

expect fun createHttpClient(): io.ktor.client.HttpClient

expect fun isoInstant(millis: Long): String

/** "3:11 p. m." for a payment time, or an empty string when the stamp cannot be read. */
expect fun clockLabel(iso: String?): String

/** "hoy", "ayer" or "12 sep" for the day a payment landed on. */
expect fun dayLabel(iso: String?): String

/** Inclusive start of a local calendar day as ISO-8601 instant. 0 = today, 1 = yesterday. */
expect fun localDayStartIso(daysAgo: Int): String

/** Exclusive end (start of next day) of a local calendar day. */
expect fun localDayEndExclusiveIso(daysAgo: Int): String

/** Monday 00:00 local, this week. */
expect fun localWeekStartIso(): String

/** Start of the local day that contains this UTC epoch-milli (DatePicker). */
expect fun dayStartIsoFromUtcMillis(utcMillis: Long): String

/** Exclusive next local day from a DatePicker UTC milli. */
expect fun nextDayStartIsoFromUtcMillis(utcMillis: Long): String

/** "19 sep 2026" for a filter field. */
expect fun filterDateLabel(iso: String?): String

@androidx.compose.runtime.Composable
expect fun BackNavigation(enabled: Boolean = true, onBack: () -> Unit)
