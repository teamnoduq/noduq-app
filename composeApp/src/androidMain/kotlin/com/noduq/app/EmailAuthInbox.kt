package com.noduq.app

import android.content.Intent
import android.net.Uri

object EmailAuthInbox {
    @Volatile
    var pending: EmailAuthPayload? = null

    fun capture(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != SCHEME || uri.host != HOST) return
        val type = param(uri, "type").orEmpty()
        val access = param(uri, "access_token")
        if (access.isNullOrBlank() && type != "recovery") return
        pending = EmailAuthPayload(
            accessToken = access,
            refreshToken = param(uri, "refresh_token"),
            type = type,
            email = param(uri, "email"),
        )
    }

    private fun param(uri: Uri, name: String): String? {
        uri.getQueryParameter(name)?.takeIf { it.isNotBlank() }?.let { return it }
        val fragment = uri.fragment ?: return null
        return Uri.parse("https://noduq.invalid/?$fragment").getQueryParameter(name)?.takeIf { it.isNotBlank() }
    }

    const val SCHEME = "com.noduq.app"
    const val HOST = "email-callback"
}

actual fun takeEmailAuthPayload(): EmailAuthPayload? {
    val value = EmailAuthInbox.pending
    EmailAuthInbox.pending = null
    return value
}
