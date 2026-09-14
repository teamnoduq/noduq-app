package com.noduq.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The bank writes to the SIM in the counter phone. This is the only thing that ever reads it,
 * it only looks at the bank's short code, and it never touches the rest of the inbox.
 */
class BankSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)?.filterNotNull().orEmpty()
        val sender = parts.firstOrNull()?.originatingAddress ?: return
        if (!isBankShortCode(sender)) return

        // A receipt this long always arrives split in two or three parts.
        val body = parts.joinToString("") { it.messageBody.orEmpty() }
        if (body.isBlank()) return
        val sentAt = parts.first().timestampMillis

        val keepAlive = goAsync()
        scope.launch {
            try {
                AppGraph.bankSms.forward(sender, body, sentAt)
            } catch (_: Exception) {
                // forward() already parks anything it could not deliver.
            } finally {
                keepAlive.finish()
            }
        }
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
