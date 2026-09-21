package com.noduq.app

object AuthLinks {
    const val CONFIRM = "https://noduq.app/confirmar"
    const val RECOVER = "https://noduq.app/recuperar"
}

data class EmailAuthPayload(
    val accessToken: String?,
    val refreshToken: String?,
    val type: String,
    val email: String?,
)
