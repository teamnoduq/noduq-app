package com.noduq.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class OwnerTab { Pagos, Empleados, Cuenta }

sealed interface Screen {
    data object Boot : Screen
    data object RoleGate : Screen
    data object OwnerLogin : Screen
    data object OwnerRegister : Screen
    data object OwnerSetup : Screen
    data class OwnerHome(val tab: OwnerTab = OwnerTab.Pagos) : Screen
    data object EmployeeLogin : Screen
    data object EmployeeWait : Screen
}

class AppViewModel(
    private val tokens: TokenStore,
    private val api: NoduqApi,
    private val supabase: SupabaseAuthApi,
    private val googleAuth: GoogleAuth,
) : ViewModel() {
    var screen by mutableStateOf<Screen>(Screen.Boot)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
    var info by mutableStateOf<String?>(null)

    var ownerEmail by mutableStateOf<String?>(null)
        private set
    var workspace by mutableStateOf<WorkspaceDto?>(null)
        private set
    var employeeSession by mutableStateOf<EmployeeSessionDto?>(null)
        private set
    var employees by mutableStateOf<List<EmployeeDto>>(emptyList())
        private set
    var employeesLoading by mutableStateOf(false)
    var revealed by mutableStateOf<CreatedEmployeeDto?>(null)

    val apiBaseUrl: String get() = AppGraph.config.apiBaseUrl

    fun start() {
        viewModelScope.launch { restore() }
    }

    fun go(next: Screen) {
        error = null
        info = null
        screen = next
    }

    fun back() {
        when (screen) {
            Screen.OwnerRegister -> go(Screen.OwnerLogin)
            Screen.OwnerLogin, Screen.EmployeeLogin -> go(Screen.RoleGate)
            else -> Unit
        }
    }

    fun ownerSignIn(email: String, password: String) {
        val mail = email.trim()
        if (mail.isBlank() || password.isBlank()) {
            error = "Escribe el correo y la contraseña."
            return
        }
        launchWork("Entrando…") {
            val session = supabase.signIn(mail, password)
            val access = session.accessToken ?: throw ApiException(401, "AUTH", "Correo o contraseña incorrectos.")
            tokens.saveOwner(access, session.refreshToken, session.user?.email ?: mail)
            ownerEmail = session.user?.email ?: mail
            loadOwnerWorkspace(access)
        }
    }

    fun ownerGoogle() {
        launchWork("Abriendo Google…") {
            val session = googleAuth.signIn()
            val access = session.accessToken ?: throw ApiException(401, "AUTH", "No se pudo entrar con Google.")
            val email = session.user?.email
            tokens.saveOwner(access, session.refreshToken, email)
            ownerEmail = email
            loadOwnerWorkspace(access)
        }
    }

    fun ownerSignUp(email: String, password: String, confirm: String) {
        val mail = email.trim()
        error = null
        info = null
        when {
            mail.isBlank() -> error = "Escribe un correo."
            password.length < 6 -> error = "La contraseña debe tener al menos 6 caracteres."
            password != confirm -> error = "Las contraseñas no coinciden."
            else -> launchWork("Creando…") {
                val session = supabase.signUp(mail, password)
                val access = session.accessToken
                if (access.isNullOrBlank()) {
                    info = "Revisa tu correo para confirmar la cuenta. Luego vuelve a entrar."
                    return@launchWork
                }
                tokens.saveOwner(access, session.refreshToken, session.user?.email ?: mail)
                ownerEmail = session.user?.email ?: mail
                loadOwnerWorkspace(access)
            }
        }
    }

    fun bootstrap(organizationName: String, displayName: String) {
        val name = organizationName.trim()
        if (name.length < 2 || name.length > 80) {
            error = "El nombre debe tener entre 2 y 80 caracteres."
            return
        }
        val token = tokens.ownerAccessToken()
        if (token == null) {
            error = "La sesión se cerró. Vuelve a entrar."
            go(Screen.OwnerLogin)
            return
        }
        launchWork("Abriendo…") {
            workspace = api.bootstrap(
                token,
                BootstrapRequest(
                    displayName = displayName.trim().takeIf { it.isNotBlank() },
                    organizationName = name,
                ),
            )
            screen = Screen.OwnerHome()
        }
    }

    fun saveProfile(displayName: String) {
        val name = displayName.trim()
        if (name.isBlank()) {
            error = "El nombre es obligatorio."
            return
        }
        val token = requireOwnerToken() ?: return
        launchWork {
            val profile = api.patchMe(token, PatchNameRequest(name))
            workspace = workspace?.copy(profile = profile)
            info = "Nombre actualizado."
        }
    }

    fun saveOrganization(name: String) {
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 80) {
            error = "El nombre debe tener entre 2 y 80 caracteres."
            return
        }
        val token = requireOwnerToken() ?: return
        launchWork {
            workspace = api.patchOrganization(token, PatchOrganizationRequest(trimmed))
            info = "Organización actualizada."
        }
    }

    fun loadEmployees() {
        val token = requireOwnerToken() ?: return
        viewModelScope.launch {
            employeesLoading = true
            error = null
            try {
                employees = api.listEmployees(token)
            } catch (cause: Exception) {
                error = cause.message ?: "No se pudieron cargar los empleados."
            } finally {
                employeesLoading = false
            }
        }
    }

    fun createEmployee(displayName: String, username: String, onFieldError: (name: String?, user: String?, other: String?) -> Unit) {
        val name = displayName.trim()
        val user = username.trim()
        if (name.isBlank()) {
            onFieldError("El nombre es obligatorio.", null, null)
            return
        }
        if (user.isNotBlank() && !user.matches(Regex("^[a-zA-Z0-9_]{3,32}$"))) {
            onFieldError(null, "El usuario debe tener entre 3 y 32 caracteres: letras, números o _.", null)
            return
        }
        val token = requireOwnerToken() ?: return
        launchWork {
            try {
                val created = api.createEmployee(
                    token,
                    CreateEmployeeRequest(name, user.ifBlank { null }),
                )
                upsertEmployee(created)
                revealed = created
            } catch (cause: ApiException) {
                when (cause.code) {
                    "USERNAME_TAKEN", "USERNAME_INVALID" -> onFieldError(null, cause.message, null)
                    "DISPLAY_NAME_INVALID" -> onFieldError(cause.message, null, null)
                    else -> onFieldError(null, null, cause.message)
                }
            }
        }
    }

    fun saveEmployee(id: String, displayName: String, username: String, onFieldError: (name: String?, user: String?, other: String?) -> Unit) {
        val name = displayName.trim()
        val user = username.trim()
        if (name.isBlank()) {
            onFieldError("El nombre es obligatorio.", null, null)
            return
        }
        if (!user.matches(Regex("^[a-zA-Z0-9_]{3,32}$"))) {
            onFieldError(null, "El usuario debe tener entre 3 y 32 caracteres: letras, números o _.", null)
            return
        }
        val token = requireOwnerToken() ?: return
        launchWork {
            try {
                val next = api.patchEmployee(token, id, PatchEmployeeRequest(name, user))
                employees = employees.map { if (it.id == next.id) next else it }
                info = "Datos guardados."
            } catch (cause: ApiException) {
                when (cause.code) {
                    "USERNAME_TAKEN", "USERNAME_INVALID" -> onFieldError(null, cause.message, null)
                    "DISPLAY_NAME_INVALID" -> onFieldError(cause.message, null, null)
                    else -> onFieldError(null, null, cause.message)
                }
            }
        }
    }

    fun toggleEmployee(employee: EmployeeDto) {
        val token = requireOwnerToken() ?: return
        launchWork {
            val next = api.patchEmployee(token, employee.id, PatchEmployeeRequest(active = !employee.active))
            employees = employees.map { if (it.id == next.id) next else it }
            info = if (next.active) "${next.displayName} quedó activo." else "${next.displayName} quedó inactivo."
        }
    }

    fun regenerate(employee: EmployeeDto) {
        val token = requireOwnerToken() ?: return
        launchWork {
            val created = api.regenerateCode(token, employee.id)
            upsertEmployee(created)
            revealed = created
            info = "Código nuevo. La sesión anterior ya no sirve."
        }
    }

    fun deleteEmployee(employee: EmployeeDto) {
        val token = requireOwnerToken() ?: return
        launchWork {
            api.deleteEmployee(token, employee.id)
            employees = employees.filterNot { it.id == employee.id }
            info = "${employee.displayName} se eliminó."
        }
    }

    fun employeeSignIn(username: String, code: String) {
        val user = username.trim().lowercase()
        val formatted = code.trim()
        if (user.isBlank() || formatted.isBlank()) {
            error = "Escribe el usuario y el código."
            return
        }
        launchWork("Entrando…") {
            val session = api.employeeLogin(user, formatted)
            tokens.saveEmployee(session.token)
            employeeSession = session
            screen = Screen.EmployeeWait
        }
    }

    fun ownerSignOut() {
        viewModelScope.launch {
            tokens.ownerAccessToken()?.let { runCatching { supabase.signOut(it) } }
            tokens.clear()
            resetGuest()
        }
    }

    fun deleteAccount(confirmation: String) {
        if (confirmation.isBlank()) {
            error = "Escribe el nombre de la organización para confirmar."
            return
        }
        val token = requireOwnerToken() ?: return
        launchWork {
            api.deleteMe(token, confirmation)
            runCatching { supabase.signOut(token) }
            tokens.clear()
            resetGuest()
            info = "La cuenta se borró."
        }
    }

    fun employeeSignOut() {
        viewModelScope.launch {
            tokens.employeeToken()?.let { runCatching { api.employeeLogout(it) } }
            tokens.clear()
            resetGuest()
        }
    }

    fun dismissReveal() {
        revealed = null
    }

    private suspend fun restore() {
        screen = Screen.Boot
        val owner = tokens.ownerAccessToken()
        val employee = tokens.employeeToken()
        try {
            if (!owner.isNullOrBlank()) {
                ownerEmail = tokens.ownerEmail()
                try {
                    loadOwnerWorkspace(owner)
                } catch (cause: ApiException) {
                    if (cause.status == 401) {
                        val refreshed = refreshOwner()
                        if (refreshed != null) loadOwnerWorkspace(refreshed) else resetGuest()
                    } else {
                        throw cause
                    }
                }
                return
            }
            if (!employee.isNullOrBlank()) {
                employeeSession = api.employeeMe(employee)
                screen = Screen.EmployeeWait
                return
            }
            screen = Screen.RoleGate
        } catch (cause: Exception) {
            tokens.clear()
            resetGuest()
            if (cause is ApiException && cause.code == "NETWORK") {
                error = cause.message
            }
        }
    }

    private suspend fun refreshOwner(): String? {
        val refresh = tokens.ownerRefreshToken() ?: return null
        return try {
            val session = supabase.refresh(refreshToken = refresh)
            val access = session.accessToken ?: return null
            tokens.saveOwner(access, session.refreshToken ?: refresh, session.user?.email ?: tokens.ownerEmail())
            ownerEmail = session.user?.email ?: tokens.ownerEmail()
            access
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun loadOwnerWorkspace(token: String) {
        try {
            workspace = api.getMe(token)
            screen = Screen.OwnerHome()
        } catch (cause: ApiException) {
            if (cause.notProvisioned) {
                screen = Screen.OwnerSetup
            } else {
                throw cause
            }
        }
    }

    private fun upsertEmployee(created: CreatedEmployeeDto) {
        val next = EmployeeDto(
            id = created.id,
            branchId = created.branchId,
            displayName = created.displayName,
            username = created.username,
            active = created.active,
        )
        employees = if (employees.any { it.id == next.id }) {
            employees.map { if (it.id == next.id) next else it }
        } else {
            listOf(next) + employees
        }
    }

    private fun requireOwnerToken(): String? {
        val token = tokens.ownerAccessToken()
        if (token == null) {
            error = "La sesión se cerró. Vuelve a entrar."
            go(Screen.OwnerLogin)
        }
        return token
    }

    private fun resetGuest() {
        workspace = null
        employeeSession = null
        employees = emptyList()
        ownerEmail = null
        revealed = null
        screen = Screen.RoleGate
    }

    private fun launchWork(busyLabel: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            error = null
            info = null
            try {
                block()
            } catch (cause: kotlinx.coroutines.CancellationException) {
                throw cause
            } catch (_: AuthCancelledException) {
                error = null
            } catch (cause: Exception) {
                error = cause.message ?: "No se pudo completar."
            } finally {
                busy = false
            }
        }
    }
}

fun formatEmployeeCode(raw: String): String {
    val compact = raw.uppercase().filter { it.isLetterOrDigit() }.take(10)
    return if (compact.length <= 5) compact else compact.substring(0, 5) + "-" + compact.substring(5)
}
