package com.noduq.app

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    explicitNulls = false
}

class NoduqApi(
    private val client: HttpClient,
    private val config: AppConfig,
) {
    suspend fun getMe(token: String): WorkspaceDto =
        request("GET", "/v1/me", token)

    suspend fun bootstrap(token: String, body: BootstrapRequest): WorkspaceDto =
        request("POST", "/v1/me/bootstrap", token, body)

    suspend fun patchMe(token: String, body: PatchNameRequest): ProfileDto =
        request("PATCH", "/v1/me", token, body)

    suspend fun deleteMe(token: String, confirmation: String) {
        request<Unit>("DELETE", "/v1/me", token, DeleteAccountRequest(confirmation), empty = true)
    }

    suspend fun getOrganization(token: String): OrganizationDto =
        request("GET", "/v1/organization", token)

    suspend fun patchOrganization(token: String, body: PatchOrganizationRequest): WorkspaceDto =
        request("PATCH", "/v1/organization", token, body)

    suspend fun listEmployees(token: String): List<EmployeeDto> =
        request("GET", "/v1/employees", token)

    suspend fun createEmployee(token: String, body: CreateEmployeeRequest): CreatedEmployeeDto =
        request("POST", "/v1/employees", token, body)

    suspend fun patchEmployee(token: String, id: String, body: PatchEmployeeRequest): EmployeeDto =
        request("PATCH", "/v1/employees/$id", token, body)

    suspend fun regenerateCode(token: String, id: String): CreatedEmployeeDto =
        request("POST", "/v1/employees/$id/code", token)

    suspend fun deleteEmployee(token: String, id: String) {
        request<Unit>("DELETE", "/v1/employees/$id", token, empty = true)
    }

    suspend fun listPayments(
        token: String,
        limit: Int = 80,
        q: String? = null,
        since: String? = null,
        until: String? = null,
    ): PaymentFeedDto {
        val query = buildList {
            add("limit=$limit")
            if (!q.isNullOrBlank()) add("q=${q.trim().encodeURLParameter()}")
            if (!since.isNullOrBlank()) add("since=${since.encodeURLParameter()}")
            if (!until.isNullOrBlank()) add("until=${until.encodeURLParameter()}")
        }.joinToString("&")
        return request("GET", "/v1/payments?$query", token)
    }

    suspend fun ingestSms(token: String, body: SmsIngestRequest): SmsIngestResponseDto =
        request("POST", "/v1/payments/sms", token, body)

    suspend fun registerDevice(token: String, body: RegisterDeviceRequest): DeviceDto =
        request("POST", "/v1/devices", token, body)

    suspend fun forgetDevice(token: String, pushToken: String) {
        request<Unit>("POST", "/v1/devices/forget", token, ForgetDeviceRequest(pushToken), empty = true)
    }

    suspend fun listEmployeePayments(token: String, limit: Int = 30): PaymentFeedDto =
        request("GET", "/v1/employee/payments?limit=$limit", token)

    suspend fun registerEmployeeDevice(token: String, body: RegisterEmployeeDeviceRequest): DeviceDto =
        request("POST", "/v1/employee/devices", token, body)

    suspend fun forgetEmployeeDevice(token: String, pushToken: String) {
        request<Unit>("POST", "/v1/employee/devices/forget", token, ForgetDeviceRequest(pushToken), empty = true)
    }

    suspend fun gmailStatus(token: String): GmailStatusDto =
        request("GET", "/v1/gmail", token)

    suspend fun gmailConnect(token: String): GmailConnectDto =
        request("GET", "/v1/gmail/connect", token)

    suspend fun gmailDisconnect(token: String) {
        request<Unit>("DELETE", "/v1/gmail", token, empty = true)
    }

    suspend fun activatePlan(token: String): PlanDto =
        request("POST", "/v1/billing/activate", token)

    suspend fun employeeLogin(username: String, code: String): EmployeeSessionDto =
        request("POST", "/v1/employee/sessions", token = null, body = EmployeeLoginRequest(username, code))

    suspend fun employeeMe(token: String): EmployeeSessionDto =
        request("GET", "/v1/employee/me", token)

    suspend fun employeeLogout(token: String) {
        request<Unit>("DELETE", "/v1/employee/sessions/me", token, empty = true)
    }

    private suspend inline fun <reified T> request(
        method: String,
        path: String,
        token: String?,
        body: Any? = null,
        empty: Boolean = false,
    ): T {
        val url = config.apiBaseUrl.trimEnd('/') + path
        val builder: HttpRequestBuilder.() -> Unit = {
            if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        val response = try {
            when (method) {
                "GET" -> client.get(url, builder)
                "POST" -> client.post(url, builder)
                "PATCH" -> client.patch(url, builder)
                "DELETE" -> client.delete(url, builder)
                else -> error("Método no soportado")
            }
        } catch (cause: Exception) {
            throw ApiException(
                0,
                "NETWORK",
                "El servidor no responde. ¿Está el API de NODUQ en ${config.apiBaseUrl}?",
            )
        }
        return parse(response, empty)
    }
}

class SupabaseAuthApi(
    private val client: HttpClient,
    private val config: AppConfig,
) {
    suspend fun signIn(email: String, password: String): SupabaseSession =
        auth("token?grant_type=password", SupabasePasswordGrant(email, password))

    suspend fun signInWithGoogleIdToken(idToken: String, nonce: String): SupabaseSession =
        auth("token?grant_type=id_token", SupabaseIdTokenGrant(idToken = idToken, nonce = nonce))

    suspend fun exchangePkce(authCode: String, codeVerifier: String): SupabaseSession =
        auth("token?grant_type=pkce", SupabasePkceGrant(authCode = authCode, codeVerifier = codeVerifier))

    suspend fun signUp(email: String, password: String): SupabaseSession =
        auth("signup", SupabasePasswordGrant(email, password))

    suspend fun recoverPassword(email: String, redirectTo: String) {
        val url = config.supabaseUrl.trimEnd('/') + "/auth/v1/recover?redirect_to=$redirectTo"
        val response = try {
            client.post(url) {
                header("apikey", config.supabaseAnonKey)
                header(HttpHeaders.Authorization, "Bearer ${config.supabaseAnonKey}")
                contentType(ContentType.Application.Json)
                setBody(SupabaseRecoverRequest(email))
            }
        } catch (_: Exception) {
            throw ApiException(0, "NETWORK", "No se pudo hablar con el inicio de sesión.")
        }
        if (!response.status.isSuccess()) {
            throw supabaseError(response)
        }
    }

    suspend fun refresh(refreshToken: String): SupabaseSession =
        auth("token?grant_type=refresh_token", SupabaseRefreshGrant(refreshToken))

    suspend fun signOut(accessToken: String) {
        val url = config.supabaseUrl.trimEnd('/') + "/auth/v1/logout"
        try {
            client.post(url) {
                header("apikey", config.supabaseAnonKey)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        } catch (_: Exception) {
            // Local sign-out still proceeds.
        }
    }

    private suspend fun auth(path: String, body: Any): SupabaseSession {
        val url = config.supabaseUrl.trimEnd('/') + "/auth/v1/$path"
        val response = try {
            client.post(url) {
                header("apikey", config.supabaseAnonKey)
                header(HttpHeaders.Authorization, "Bearer ${config.supabaseAnonKey}")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        } catch (_: Exception) {
            throw ApiException(0, "NETWORK", "No se pudo hablar con el inicio de sesión.")
        }
        if (!response.status.isSuccess()) {
            throw supabaseError(response)
        }
        val text = response.bodyAsText()
        if (text.isBlank()) return SupabaseSession()
        return json.decodeFromString(SupabaseSession.serializer(), text)
    }
}

private suspend inline fun <reified T> parse(response: HttpResponse, empty: Boolean): T {
    val text = response.bodyAsText()
    if (!response.status.isSuccess()) {
        throw apiError(response.status.value, text)
    }
    if (empty || text.isBlank() || T::class == Unit::class) {
        @Suppress("UNCHECKED_CAST")
        return Unit as T
    }
    return json.decodeFromString(text)
}

private fun apiError(status: Int, text: String): ApiException {
    val body = runCatching { json.decodeFromString(ApiErrorBody.serializer(), text) }.getOrNull()
    val code = body?.code ?: if (status == 404) "NOT_FOUND" else "ERROR"
    val message = body?.message
        ?: body?.msg
        ?: "No se pudo completar la petición ($status)."
    return ApiException(status, code, message)
}

private suspend fun supabaseError(response: HttpResponse): ApiException {
    val text = response.bodyAsText()
    val body = runCatching { json.decodeFromString(ApiErrorBody.serializer(), text) }.getOrNull()
    val code = (body?.code ?: body?.errorCode ?: body?.error ?: "").lowercase().replace("-", "_")
    val raw = (body?.message ?: body?.msg ?: body?.errorDescription ?: body?.error ?: text)
    return ApiException(response.status.value, code.ifBlank { "AUTH" }, supabaseAuthMessage(code, raw))
}

internal fun supabaseAuthMessage(code: String, raw: String): String {
    val mapped = when (code) {
        "invalid_credentials", "invalid_login_credentials", "invalid_grant" ->
            "Correo o contraseña incorrectos."
        "email_not_confirmed" ->
            "Confirma el correo antes de entrar. Revisa la bandeja."
        "user_already_registered" ->
            "Ese correo ya tiene cuenta. Entra o usa otro."
        "over_email_send_rate_limit" ->
            "Demasiados intentos. Espera un momento."
        "signup_disabled" ->
            "El registro está cerrado en Supabase."
        "weak_password" ->
            "La contraseña es demasiado débil. Prueba con una más larga."
        "bad_id_token", "invalid_id_token", "unexpected_audience" ->
            "Google no aceptó esta app. Revisa el Client ID web."
        "identity_already_exists" ->
            "Ese Google ya está ligado a otra cuenta."
        else -> null
    }
    if (mapped != null) return mapped
    val message = raw.lowercase()
    return when {
        "invalid login" in message || "invalid_credentials" in message || "invalid grant" in message ->
            "Correo o contraseña incorrectos."
        "already registered" in message || "already been registered" in message ->
            "Ese correo ya tiene cuenta. Entra o usa otro."
        "email not confirmed" in message ->
            "Confirma el correo antes de entrar. Revisa la bandeja."
        "password" in message && "6" in message ->
            "La contraseña debe tener al menos 6 caracteres."
        raw.isBlank() -> "No se pudo completar. Inténtalo de nuevo."
        else -> raw
    }
}
