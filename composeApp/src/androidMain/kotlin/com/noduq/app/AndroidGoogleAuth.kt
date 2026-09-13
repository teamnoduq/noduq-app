package com.noduq.app

import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CompletableDeferred
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class AndroidGoogleAuth(
    private val supabase: SupabaseAuthApi,
    private val config: AppConfig,
) : GoogleAuth {
    private var host = WeakReference<ComponentActivity>(null)
    private var pendingRedirect: CompletableDeferred<Uri>? = null
    private var pendingVerifier: String? = null

    fun attach(activity: ComponentActivity) {
        host = WeakReference(activity)
        activity.intent?.let { onIntent(it) }
    }

    fun detach(activity: ComponentActivity) {
        if (host.get() === activity) host = WeakReference(null)
    }

    fun onIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme != REDIRECT_SCHEME || uri.host != REDIRECT_HOST) return
        val pending = pendingRedirect ?: return
        pendingRedirect = null
        pending.complete(uri)
    }

    override suspend fun signIn(): SupabaseSession {
        if (config.googleWebClientId.isNotBlank()) {
            try {
                return signInNative()
            } catch (cause: AuthCancelledException) {
                throw cause
            } catch (_: GetCredentialException) {
                // Play Services missing, no account, or console mismatch.
            }
        }
        return signInWithBrowser()
    }

    private suspend fun signInNative(): SupabaseSession {
        val activity = host.get() ?: throw ApiException(0, "AUTH", "Abre NODUQ de nuevo para entrar con Google.")
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = sha256Hex(rawNonce)
        val manager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(config.googleWebClientId)
            .setNonce(hashedNonce)
            .build()
        val oneTapOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(config.googleWebClientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()
        val result = try {
            manager.getCredential(
                context = activity,
                request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build(),
            )
        } catch (cause: GetCredentialCancellationException) {
            throw AuthCancelledException()
        } catch (_: GetCredentialException) {
            try {
                manager.getCredential(
                    context = activity,
                    request = GetCredentialRequest.Builder().addCredentialOption(oneTapOption).build(),
                )
            } catch (inner: GetCredentialCancellationException) {
                throw AuthCancelledException()
            }
        }
        val credential = result.credential
        val token = if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw ApiException(0, "AUTH", "Google no devolvió el token.")
        }
        return supabase.signInWithGoogleIdToken(token, rawNonce)
    }

    private suspend fun signInWithBrowser(): SupabaseSession {
        val activity = host.get() ?: throw ApiException(0, "AUTH", "Abre NODUQ de nuevo para entrar con Google.")
        pendingRedirect?.cancel()
        val verifier = pkceVerifier()
        pendingVerifier = verifier
        val deferred = CompletableDeferred<Uri>()
        pendingRedirect = deferred
        val url = authorizeUrl(pkceChallenge(verifier))
        CustomTabsIntent.Builder().build().launchUrl(activity, Uri.parse(url))
        val uri = try {
            deferred.await()
        } catch (_: kotlinx.coroutines.CancellationException) {
            throw AuthCancelledException()
        } finally {
            if (pendingRedirect === deferred) pendingRedirect = null
        }
        val error = uri.getQueryParameter("error")
        if (!error.isNullOrBlank()) {
            if (error == "access_denied") throw AuthCancelledException()
            val description = uri.getQueryParameter("error_description").orEmpty()
            throw ApiException(401, error, description.ifBlank { "No se pudo entrar con Google." })
        }
        val code = uri.getQueryParameter("code")
            ?: throw ApiException(401, "AUTH", "Google no devolvió el código.")
        val usedVerifier = pendingVerifier ?: verifier
        pendingVerifier = null
        return supabase.exchangePkce(code, usedVerifier)
    }

    private fun authorizeUrl(challenge: String): String {
        val redirect = URLEncoder.encode(REDIRECT, StandardCharsets.UTF_8.name())
        val encodedChallenge = URLEncoder.encode(challenge, StandardCharsets.UTF_8.name())
        return buildString {
            append(config.supabaseUrl.trimEnd('/'))
            append("/auth/v1/authorize?provider=google")
            append("&redirect_to=").append(redirect)
            append("&code_challenge=").append(encodedChallenge)
            append("&code_challenge_method=s256")
            append("&prompt=select_account")
        }
    }

    companion object {
        const val REDIRECT_SCHEME = "com.noduq.app"
        const val REDIRECT_HOST = "login-callback"
        const val REDIRECT = "$REDIRECT_SCHEME://$REDIRECT_HOST"
    }
}

private fun pkceVerifier(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return base64Url(bytes)
}

private fun pkceChallenge(verifier: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(verifier.toByteArray(StandardCharsets.US_ASCII))
    return base64Url(digest)
}

private fun sha256Hex(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

private fun base64Url(bytes: ByteArray): String =
    Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
