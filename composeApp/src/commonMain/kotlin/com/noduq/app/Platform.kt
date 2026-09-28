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

/** Tips the phone remembers on its own. Logging out does not erase them. */
interface UiMemory {
    fun statsTipVisits(): Int
    fun recordStatsTipVisit(): Int
    fun statsTipDismissed(): Boolean
    fun dismissStatsTip()
    fun employeeOnboardDone(employeeId: String): Boolean
    fun markEmployeeOnboardDone(employeeId: String)
}

interface TokenStore {
    fun ownerAccessToken(): String?
    fun ownerRefreshToken(): String?
    fun ownerEmail(): String?
    fun saveOwner(accessToken: String, refreshToken: String?, email: String?)
    fun employeeToken(): String?
    fun saveEmployee(token: String)
    fun clear()
    fun onboardingStep(): Int?
    fun setOnboardingStep(step: Int?)
    fun onboardShop(): String
    fun setOnboardShop(value: String)
    fun onboardName(): String
    fun setOnboardName(value: String)
}

interface Clipboard {
    fun copy(text: String): Boolean
}

interface LinkOpener {
    fun open(url: String)
}

/** The till phone that caught the SMS should ring without waiting for Firebase. */
interface PaymentAlerts {
    fun show(notice: PaymentNoticeDto)
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
    lateinit var uiMemory: UiMemory
    lateinit var api: NoduqApi
    lateinit var supabase: SupabaseAuthApi
    lateinit var googleAuth: GoogleAuth
    lateinit var clipboard: Clipboard
    lateinit var links: LinkOpener
    lateinit var paymentAlerts: PaymentAlerts
    lateinit var push: PushTokens
    lateinit var permissions: DevicePermissions
    lateinit var pendingSms: PendingSmsStore
    lateinit var bankSms: BankSmsForwarder
    lateinit var billing: ShopBilling
}

expect fun takeEmailAuthPayload(): EmailAuthPayload?

expect fun createHttpClient(): io.ktor.client.HttpClient

/** A client that can stay open for the history channel. REST timeouts do not apply. */
expect fun createWebSocketClient(): io.ktor.client.HttpClient

expect fun isoInstant(millis: Long): String

/** Epoch millis for an ISO instant, or -1 when it cannot be read. */
expect fun epochMillis(iso: String?): Long

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

/** First day of the current local month, 00:00. */
expect fun localMonthStartIso(): String

/** "Septiembre" for the month filter. Follows the phone's date. */
expect fun localMonthName(): String

/** One calendar month the payments list can filter to. Newest months come first. */
data class MonthWindow(
    val name: String,
    val since: String,
    val untilExclusive: String,
)

/**
 * Months of [year], January through December, newest last when read in order.
 * Names only. [MonthWindow.untilExclusive] is the first instant of the next month.
 */
expect fun monthWindows(year: Int): List<MonthWindow>

/** "Septiembre" for the month of a payment instant. Never includes the year. */
expect fun monthHeading(iso: String?): String

/** Calendar year of [iso], or the current year when it cannot be read. */
expect fun calendarYear(iso: String?): Int

/** Month of [iso] from 1 to 12, or the current month when it cannot be read. */
expect fun calendarMonth(iso: String?): Int

/** One calendar year the payments list can filter to. Newest years come first. */
data class YearWindow(
    val label: String,
    val since: String,
    val untilExclusive: String,
)

/**
 * Current year back to the year of [earliestIso]. Only the current year when that stamp is missing.
 * [YearWindow.untilExclusive] is the first instant of the next year.
 */
expect fun yearWindows(earliestIso: String?): List<YearWindow>

/** "2026" for the year of a payment instant, or the current year when it cannot be read. */
expect fun yearHeading(iso: String?): String

/** Start of the local day that contains this UTC epoch-milli (DatePicker). */
expect fun dayStartIsoFromUtcMillis(utcMillis: Long): String

/** Exclusive next local day from a DatePicker UTC milli. */
expect fun nextDayStartIsoFromUtcMillis(utcMillis: Long): String

/** "19 sep 2026" for a filter field. */
expect fun filterDateLabel(iso: String?): String

/** "25 de septiembre de 2026" for the plan end date. */
expect fun longDateLabel(iso: String?): String

/** "1 ene. 2026" for a short history date. */
expect fun shortDayLabel(iso: String?): String

/** "27 de sep de 2026, 2:14 a. m." for when a sync finished. */
expect fun historyFinishedLabel(iso: String?): String

@androidx.compose.runtime.Composable
expect fun BackNavigation(enabled: Boolean = true, onBack: () -> Unit)

/** False when the system has animation scale at zero — skip delays that wait on motion. */
expect fun motionEnabled(): Boolean
