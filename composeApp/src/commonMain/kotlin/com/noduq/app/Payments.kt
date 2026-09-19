package com.noduq.app

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Bank messages arrive on a broadcast receiver and on a push service, neither of which owns
 * the screen. They ring this bell and whoever is showing the feed reloads it.
 */
object PaymentSignals {
    private val bell = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val arrivals: SharedFlow<Unit> = bell

    fun announce() {
        bell.tryEmit(Unit)
    }
}

/**
 * Short codes Bancolombia sends QR receipts from. The server decides for real; this only
 * keeps unrelated texts from ever leaving the phone.
 */
private val BANK_SHORT_CODES = setOf("85540")

fun isBankShortCode(sender: String?): Boolean {
    val digits = sender?.filter { it.isDigit() } ?: return false
    return BANK_SHORT_CODES.any { digits == it || digits.endsWith(it) && digits.length <= it.length + 3 }
}

/**
 * Hands bank texts to the server. A message that cannot be delivered right now waits on disk
 * rather than disappearing, because a lost text is a lost sale.
 */
class BankSmsForwarder(
    private val tokens: TokenStore,
    private val api: NoduqApi,
    private val supabase: SupabaseAuthApi,
    private val pending: PendingSmsStore,
) {
    suspend fun forward(sender: String, message: String, sentAtMillis: Long) {
        flushPending()
        if (!deliver(sender, message, sentAtMillis)) {
            pending.keep(sender, message, sentAtMillis)
        }
    }

    suspend fun flushPending() {
        val waiting = pending.waiting()
        if (waiting.isEmpty()) return
        val delivered = waiting.filter { deliver(it.sender, it.message, it.sentAtMillis) }
        if (delivered.isNotEmpty()) pending.forget(delivered)
    }

    private suspend fun deliver(sender: String, message: String, sentAtMillis: Long): Boolean {
        val token = tokens.ownerAccessToken() ?: return false
        val body = SmsIngestRequest(sender, message, isoInstant(sentAtMillis))
        val answer = try {
            api.ingestSms(token, body)
        } catch (cause: ApiException) {
            if (cause.status != 401) return false
            val refreshed = refreshOwner() ?: return false
            try {
                api.ingestSms(refreshed, body)
            } catch (_: Exception) {
                return false
            }
        } catch (_: Exception) {
            return false
        }
        if (answer.outcome == "stored") PaymentSignals.announce()
        return true
    }

    private suspend fun refreshOwner(): String? {
        val refresh = tokens.ownerRefreshToken() ?: return null
        return try {
            val session = supabase.refresh(refresh)
            val access = session.accessToken ?: return null
            tokens.saveOwner(access, session.refreshToken ?: refresh, session.user?.email ?: tokens.ownerEmail())
            access
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Tells the server where to ring this phone. Silent on failure: a phone without push still
 * shows the feed, it just does not wake up on its own.
 */
suspend fun registerThisDevice(smsReader: Boolean): Boolean {
    val pushToken = AppGraph.push.current() ?: return false
    val owner = AppGraph.tokens.ownerAccessToken()
    val employee = AppGraph.tokens.employeeToken()
    return try {
        when {
            owner != null -> {
                AppGraph.api.registerDevice(owner, RegisterDeviceRequest(pushToken, smsReader = smsReader))
                true
            }
            employee != null -> {
                AppGraph.api.registerEmployeeDevice(employee, RegisterEmployeeDeviceRequest(pushToken))
                true
            }
            else -> false
        }
    } catch (_: Exception) {
        false
    }
}

suspend fun forgetThisDevice() {
    val pushToken = AppGraph.push.current() ?: return
    val owner = AppGraph.tokens.ownerAccessToken()
    val employee = AppGraph.tokens.employeeToken()
    runCatching {
        when {
            owner != null -> AppGraph.api.forgetDevice(owner, pushToken)
            employee != null -> AppGraph.api.forgetEmployeeDevice(employee, pushToken)
        }
    }
}

fun PaymentNoticeDto.momentIso(): String = occurredAt ?: receivedAt

fun PaymentNoticeDto.whoPaid(): String = when {
    !payerName.isNullOrBlank() -> payerName
    else -> "Transferencia Bancolombia"
}

fun copLabel(amount: Double): String {
    val n = kotlin.math.round(amount).toLong()
    val grouped = n.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$$grouped"
}

fun PermissionState.needsAttention(): Boolean =
    this != PermissionState.Granted && this != PermissionState.NotNeeded

fun titledDay(iso: String?): String {
    val raw = dayLabel(iso)
    if (raw.isEmpty()) return ""
    return raw.replaceFirstChar { it.uppercaseChar() }
}
