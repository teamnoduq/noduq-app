package com.noduq.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
    val msg: String? = null,
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
)

class ApiException(
    val status: Int,
    val code: String,
    override val message: String,
) : Exception(message) {
    val notProvisioned: Boolean get() = code == "NOT_PROVISIONED"
}

@Serializable
data class ProfileDto(val id: String, val displayName: String)

@Serializable
data class OrganizationDto(
    val id: String,
    val name: String,
    val smsPhone: String? = null,
    val merchantLast4: String? = null,
)

@Serializable
data class BranchDto(val id: String, val name: String)

@Serializable
data class WorkspaceDto(
    val profile: ProfileDto,
    val organization: OrganizationDto,
    val role: String,
    val branches: List<BranchDto> = emptyList(),
    val plan: PlanDto? = null,
)

@Serializable
data class PlanDto(
    val entitlement: String? = null,
    val status: String = "none",
    val periodEndsAt: String? = null,
    val active: Boolean = false,
)

fun WorkspaceDto.planActive(): Boolean = plan?.active == true

/** Pagado y renovando: la tarjeta verde de plan activo. */
fun WorkspaceDto.planRenewing(): Boolean = plan?.renewing() == true

/** Canceló, pero el ciclo actual sigue abierto. */
fun WorkspaceDto.planCancelling(): Boolean = plan?.cancelling() == true

fun PlanDto.renewing(): Boolean = active && !status.equals("cancelled", ignoreCase = true)

fun PlanDto.cancelling(): Boolean = active && status.equals("cancelled", ignoreCase = true)

@Serializable
data class EmployeeDto(
    val id: String,
    val branchId: String,
    val displayName: String,
    val username: String,
    val active: Boolean,
    val lookbackDays: Int = 1,
    val createdAt: String? = null,
)

@Serializable
data class CreatedEmployeeDto(
    val id: String,
    val branchId: String,
    val displayName: String,
    val username: String,
    val active: Boolean,
    val lookbackDays: Int = 1,
    val code: String,
)

@Serializable
data class EmployeeSessionDto(
    val token: String,
    val expiresAt: String,
    val employee: EmployeeDto,
    val organization: OrganizationDto,
    val branch: BranchDto,
)

/**
 * A payment the bank told NODUQ about. Show [amountLabel]; [amount] is only there for
 * anything that needs to compare values.
 */
@Serializable
data class PaymentNoticeDto(
    val id: String,
    val source: String,
    val payerName: String? = null,
    val amount: Double? = null,
    val amountLabel: String? = null,
    val currency: String = "COP",
    val occurredAt: String? = null,
    val receivedAt: String,
    val readable: Boolean = false,
    val confirmedByEmail: Boolean = false,
)

@Serializable
data class PaymentFeedDto(val notices: List<PaymentNoticeDto> = emptyList())

@Serializable
data class RegisterDeviceRequest(
    val pushToken: String,
    val platform: String = "android",
    val smsReader: Boolean = false,
)

@Serializable
data class RegisterEmployeeDeviceRequest(
    val pushToken: String,
    val platform: String = "android",
)

@Serializable
data class ForgetDeviceRequest(val pushToken: String)

@Serializable
data class DeviceDto(
    val id: String,
    val platform: String,
    val smsReader: Boolean,
    val lastSeenAt: String,
)

@Serializable
data class SmsIngestRequest(
    val sender: String,
    val message: String,
    val sentAt: String? = null,
)

/** [outcome] is `stored`, `duplicate`, `ignored_sender` or `ignored_other_account`. */
@Serializable
data class SmsIngestResponseDto(
    val outcome: String,
    val notice: PaymentNoticeDto? = null,
)

@Serializable
data class HistoryStatusDto(
    val status: String = "available",
    val windowFrom: String? = null,
    val windowUntil: String? = null,
    val total: Int = 0,
    val processed: Int = 0,
    val stored: Int = 0,
    val percent: Int = 0,
    val finishedAt: String? = null,
)

@Serializable
data class GmailStatusDto(
    val configured: Boolean = false,
    val connected: Boolean = false,
    val address: String? = null,
)

@Serializable
data class GmailConnectDto(val authorizationUrl: String)

@Serializable
data class BootstrapRequest(
    val displayName: String? = null,
    val organizationName: String,
)

@Serializable
data class PatchNameRequest(val displayName: String)

@Serializable
data class PatchOrganizationRequest(val name: String)

@Serializable
data class CreateEmployeeRequest(
    val displayName: String,
    val username: String? = null,
    val branchId: String? = null,
)

@Serializable
data class PatchEmployeeRequest(
    val displayName: String? = null,
    val username: String? = null,
    val active: Boolean? = null,
    val lookbackDays: Int? = null,
)

@Serializable
data class EmployeeLoginRequest(val username: String, val code: String)

@Serializable
data class SupabaseRecoverRequest(
    val email: String,
)

@Serializable
data class SupabaseResendRequest(
    val type: String,
    val email: String,
)

@Serializable
data class SupabasePasswordGrant(
    val email: String,
    val password: String,
)

@Serializable
data class SupabaseIdTokenGrant(
    val provider: String = "google",
    @SerialName("id_token") val idToken: String,
    val nonce: String? = null,
)

@Serializable
data class SupabasePkceGrant(
    @SerialName("auth_code") val authCode: String,
    @SerialName("code_verifier") val codeVerifier: String,
)

@Serializable
data class SupabaseRefreshGrant(
    @SerialName("refresh_token") val refreshToken: String,
)

class AuthCancelledException : Exception()

@Serializable
data class SupabaseUser(
    val id: String? = null,
    val email: String? = null,
)

@Serializable
data class SupabaseSession(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: SupabaseUser? = null,
)
