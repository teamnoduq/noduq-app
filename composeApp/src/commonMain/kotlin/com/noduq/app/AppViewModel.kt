package com.noduq.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class OwnerTab { Pagos, Empleados, Cuenta }

sealed interface Screen {
    data object Boot : Screen
    data object RoleGate : Screen
    data object OwnerLogin : Screen
    data object OwnerRegister : Screen
    data object OwnerSetup : Screen
    data object OwnerPlan : Screen
    data object OwnerPermissions : Screen
    data object OwnerForgotPassword : Screen
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

    var notices by mutableStateOf<List<PaymentNoticeDto>>(emptyList())
        private set
    var noticesLoading by mutableStateOf(false)
        private set
    var noticesError by mutableStateOf<String?>(null)
        private set
    var payQuery by mutableStateOf("")
        private set
    var paySince by mutableStateOf<String?>(null)
        private set
    var payUntil by mutableStateOf<String?>(null)
        private set
    var payRange by mutableStateOf("todos")
        private set
    private var paySearchJob: Job? = null

    /** The aviso that just landed, so the screen can shout about it once. */
    var freshNotice by mutableStateOf<PaymentNoticeDto?>(null)
        private set

    var notificationsAllowed by mutableStateOf(PermissionState.Denied)
        private set
    var smsAllowed by mutableStateOf(PermissionState.Denied)
        private set

    var gmail by mutableStateOf<GmailStatusDto?>(null)
        private set

    val apiBaseUrl: String get() = AppGraph.config.apiBaseUrl

    val readsBankSms: Boolean get() = smsAllowed == PermissionState.Granted

    fun needsSmsSetup(): Boolean = smsAllowed.needsAttention()

    fun needsNotificationSetup(): Boolean = notificationsAllowed.needsAttention()

    fun needsPermissionSetup(askSms: Boolean): Boolean =
        needsNotificationSetup() || (askSms && needsSmsSetup())

    init {
        viewModelScope.launch {
            PaymentSignals.arrivals.collect { loadPayments(quiet = true) }
        }
    }

    fun start() {
        readPermissions()
        viewModelScope.launch { restore() }
    }

    fun readPermissions() {
        notificationsAllowed = AppGraph.permissions.notifications()
        smsAllowed = AppGraph.permissions.sms()
    }

    fun askNotifications() {
        viewModelScope.launch {
            notificationsAllowed = AppGraph.permissions.requestNotifications()
            syncDevice()
        }
    }

    fun askSms() {
        viewModelScope.launch {
            smsAllowed = AppGraph.permissions.requestSms()
            syncDevice()
            // A receipt may have been parked while the app could not read texts yet.
            if (smsAllowed == PermissionState.Granted) {
                runCatching { AppGraph.bankSms.flushPending() }
            }
        }
    }

    fun openSystemSettings() {
        AppGraph.permissions.openSettings()
    }

    /** The shopkeeper may have granted a permission in system settings while we were away. */
    fun onForeground() {
        val smsWas = smsAllowed
        readPermissions()
        if (readsBankSms || !needsNotificationSetup()) {
            syncDevice()
        }
        if (smsAllowed == PermissionState.Granted && smsWas != PermissionState.Granted) {
            viewModelScope.launch { runCatching { AppGraph.bankSms.flushPending() } }
        }
        if (screen is Screen.OwnerPermissions || screen is Screen.OwnerPlan) {
            workspace?.let { screen = ownerDestination() }
            if (screen is Screen.OwnerHome) onSessionReady()
        }
        loadGmail()
    }

    fun loadGmail() {
        val token = tokens.ownerAccessToken() ?: return
        viewModelScope.launch {
            runCatching {
                gmail = asOwner { api.gmailStatus(it) }
            }
        }
    }

    fun connectGmail() {
        launchWork {
            val connect = asOwner { api.gmailConnect(it) }
            AppGraph.links.open(connect.authorizationUrl)
        }
    }

    fun disconnectGmail() {
        launchWork {
            asOwner { token ->
                api.gmailDisconnect(token)
            }
            gmail = gmail?.copy(connected = false, address = null)
            info = "Gmail se desconectó."
        }
    }

    fun loadPayments(quiet: Boolean = false) {
        val owner = tokens.ownerAccessToken()
        val employee = tokens.employeeToken()
        if (owner == null && employee == null) return
        viewModelScope.launch {
            if (!quiet) noticesLoading = true
            noticesError = null
            try {
                val feed = if (owner != null) {
                    asOwner {
                        api.listPayments(
                            it,
                            q = payQuery.ifBlank { null },
                            since = paySince,
                            until = payUntil,
                        )
                    }
                } else {
                    api.listEmployeePayments(employee!!)
                }
                val newest = feed.notices.firstOrNull()
                if (newest != null && newest.id != notices.firstOrNull()?.id && notices.isNotEmpty()) {
                    freshNotice = newest
                }
                notices = feed.notices
            } catch (cause: Exception) {
                noticesError = cause.message ?: "No se pudieron cargar los avisos."
            } finally {
                noticesLoading = false
            }
        }
    }

    fun dismissFreshNotice() {
        freshNotice = null
    }

    fun setPaySearch(value: String) {
        payQuery = value
        paySearchJob?.cancel()
        paySearchJob = viewModelScope.launch {
            delay(380)
            loadPayments(quiet = true)
        }
    }

    fun applyPayRange(range: String, since: String?, until: String?) {
        payRange = range
        paySince = since
        payUntil = until
        loadPayments()
    }

    fun searchPayments() {
        loadPayments()
    }

    private fun syncDevice() {
        viewModelScope.launch {
            runCatching { registerThisDevice(smsReader = readsBankSms) }
        }
    }

    fun go(next: Screen) {
        error = null
        info = null
        screen = next
    }

    fun back() {
        when (screen) {
            Screen.OwnerRegister -> go(Screen.OwnerLogin)
            Screen.OwnerForgotPassword -> go(Screen.OwnerLogin)
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
            password.none { it.isUpperCase() } -> error = "Incluye al menos una mayúscula."
            password.none { it.isDigit() } -> error = "Incluye al menos un número."
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
            screen = ownerDestination()
            if (screen is Screen.OwnerHome) onSessionReady()
        }
    }

    fun saveAccount(displayName: String, organizationName: String) {
        val name = displayName.trim()
        val org = organizationName.trim()
        if (name.isBlank()) {
            error = "El nombre es obligatorio."
            return
        }
        if (org.length < 2 || org.length > 80) {
            error = "El negocio debe tener entre 2 y 80 caracteres."
            return
        }
        val token = requireOwnerToken() ?: return
        val profileChanged = name != workspace?.profile?.displayName.orEmpty()
        val orgChanged = org != workspace?.organization?.name.orEmpty()
        if (!profileChanged && !orgChanged) return
        launchWork {
            if (profileChanged) {
                val profile = api.patchMe(token, PatchNameRequest(name))
                workspace = workspace?.copy(profile = profile)
            }
            if (orgChanged) {
                workspace = api.patchOrganization(token, PatchOrganizationRequest(org))
            }
            info = "Cambios guardados."
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

    fun saveEmployee(id: String, displayName: String, username: String, lookbackDays: Int, onFieldError: (name: String?, user: String?, other: String?) -> Unit) {
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
                val next = api.patchEmployee(token, id, PatchEmployeeRequest(name, user, lookbackDays = lookbackDays))
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
            onSessionReady()
        }
    }

    fun ownerSignOut() {
        viewModelScope.launch {
            runCatching { forgetThisDevice() }
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
            runCatching { forgetThisDevice() }
            api.deleteMe(token, confirmation)
            runCatching { supabase.signOut(token) }
            tokens.clear()
            resetGuest()
            info = "La cuenta se borró."
        }
    }

    fun employeeSignOut() {
        viewModelScope.launch {
            runCatching { forgetThisDevice() }
            tokens.employeeToken()?.let { runCatching { api.employeeLogout(it) } }
            tokens.clear()
            resetGuest()
        }
    }

    fun dismissReveal() {
        revealed = null
    }

    fun clearInfo() {
        info = null
    }

    private fun ownerDestination(): Screen {
        val shop = workspace
        return when {
            shop == null -> Screen.OwnerSetup
            !shop.planActive() -> Screen.OwnerPlan
            needsPermissionSetup(askSms = true) -> Screen.OwnerPermissions
            else -> Screen.OwnerHome()
        }
    }

    fun requestPasswordReset(email: String) {
        val mail = email.trim()
        if (mail.isBlank()) {
            error = "Escribe el correo."
            return
        }
        launchWork {
            supabase.recoverPassword(mail, "https://noduq.app/recuperar")
            info = "sent"
        }
    }

    fun buyPlan() {
        val orgId = workspace?.organization?.id
        if (orgId.isNullOrBlank()) {
            error = "Falta el comercio."
            return
        }
        launchWork("Activando…") {
            AppGraph.billing.logIn(orgId)
            val bought = AppGraph.billing.purchaseSmsMonthly()
            if (!bought) return@launchWork
            val token = requireOwnerToken() ?: return@launchWork
            val plan = api.activatePlan(token)
            workspace = workspace?.copy(plan = plan)
            screen = ownerDestination()
            if (screen is Screen.OwnerHome) onSessionReady()
        }
    }

    fun finishPermissions() {
        readPermissions()
        screen = ownerDestination()
        if (screen is Screen.OwnerHome) onSessionReady()
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
                onSessionReady()
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

    /** Runs an owner call, renewing the Supabase session once if it had just expired. */
    private suspend fun <T> asOwner(call: suspend (String) -> T): T {
        val token = tokens.ownerAccessToken()
            ?: throw ApiException(401, "AUTH", "La sesión se cerró. Vuelve a entrar.")
        return try {
            call(token)
        } catch (cause: ApiException) {
            if (cause.status != 401) throw cause
            val refreshed = refreshOwner() ?: throw cause
            call(refreshed)
        }
    }

    private suspend fun loadOwnerWorkspace(token: String) {
        try {
            workspace = api.getMe(token)
            screen = ownerDestination()
            if (screen is Screen.OwnerHome) onSessionReady()
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
            lookbackDays = created.lookbackDays,
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

    /**
     * A session just opened on this phone: tell the server where to ring it, pull the feed, and
     * push out any bank text that was parked while nobody was signed in.
     */
    private fun onSessionReady() {
        readPermissions()
        syncDevice()
        loadPayments()
        loadGmail()
        viewModelScope.launch { runCatching { AppGraph.bankSms.flushPending() } }
    }

    private fun resetGuest() {
        workspace = null
        employeeSession = null
        employees = emptyList()
        ownerEmail = null
        revealed = null
        notices = emptyList()
        freshNotice = null
        noticesError = null
        paySearchJob?.cancel()
        payQuery = ""
        paySince = null
        payUntil = null
        payRange = "todos"
        gmail = null
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
