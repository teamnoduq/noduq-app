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
)

@Serializable
data class EmployeeDto(
    val id: String,
    val branchId: String,
    val displayName: String,
    val username: String,
    val active: Boolean,
    val createdAt: String? = null,
)

@Serializable
data class CreatedEmployeeDto(
    val id: String,
    val branchId: String,
    val displayName: String,
    val username: String,
    val active: Boolean,
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
data class DeleteAccountRequest(val confirmation: String)

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
)

@Serializable
data class EmployeeLoginRequest(val username: String, val code: String)

@Serializable
data class SupabasePasswordGrant(
    val email: String,
    val password: String,
)

@Serializable
data class SupabaseRefreshGrant(
    @SerialName("refresh_token") val refreshToken: String,
)

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
