package com.noduq.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val PAYMENT_CHANNEL = "noduq_payments"

class AndroidLocalPaymentAlerts(private val context: Context) : PaymentAlerts {
    override fun show(notice: PaymentNoticeDto) {
        val who = notice.payerName?.takeIf { it.isNotBlank() }
        val amount = notice.amountLabel
        val body = when {
            who != null && amount != null -> "$who · $amount"
            amount != null -> amount
            who != null -> who
            else -> "Llegó un aviso del banco. Ábrelo para verlo."
        }
        showPayment(context, "El pago ya llegó", body, notice.id)
    }
}

class AndroidPushTokens : PushTokens {
    override suspend fun current(): String? = suspendCancellableCoroutine { waiting ->
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (waiting.isActive) {
                    waiting.resume(if (task.isSuccessful) task.result else null)
                }
            }
        } catch (_: Exception) {
            if (waiting.isActive) waiting.resume(null)
        }
    }
}

/**
 * A loud channel: the counter is noisy and the till only moves once the phone speaks up.
 */
fun createPaymentChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        PAYMENT_CHANNEL,
        "Pagos confirmados",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Suena cuando el banco confirma un pago con QR."
        enableVibration(true)
    }
    context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
}

class NoduqMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val smsReader = AppGraph.permissions.sms() == PermissionState.Granted
        scope.launch { registerThisDevice(smsReader = smsReader) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Only reaches here while the app is open; otherwise the system draws the tray entry
        // from the notification payload on its own.
        PaymentSignals.announce()
        showPayment(
            context = this,
            title = message.notification?.title ?: message.data["title"] ?: "Pago confirmado",
            body = message.notification?.body ?: message.data["body"].orEmpty(),
            id = message.data["noticeId"],
        )
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

internal fun showPayment(context: Context, title: String, body: String, id: String?) {
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
    val open = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val notification = NotificationCompat.Builder(context, PAYMENT_CHANNEL)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_EVENT)
        .setAutoCancel(true)
        .setContentIntent(open)
        .build()
    try {
        NotificationManagerCompat.from(context).notify(id?.hashCode() ?: 1, notification)
    } catch (_: SecurityException) {
        // The permission was revoked between the check and the post. Nothing else to do.
    }
}
