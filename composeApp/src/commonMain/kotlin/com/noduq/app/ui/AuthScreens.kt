package com.noduq.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.Screen
import com.noduq.app.motionEnabled
import com.noduq.app.resources.Res
import com.noduq.app.resources.logo_nq_cian_noche
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun BootScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(NoduqColors.night)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.logo_nq_cian_noche),
                contentDescription = "NODUQ",
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "NODUQ",
                color = NoduqColors.cyan,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun RoleGateScreen(vm: AppViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Box(Modifier.padding(start = 22.dp, top = 12.dp, end = 22.dp)) {
            BrandMark(compact = true)
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "¿Quién entra?",
                color = NoduqColors.ink,
                fontWeight = FontWeight.Medium,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.6).sp,
            )
            Spacer(Modifier.height(20.dp))
            var picked by remember { mutableStateOf<RoleKind?>(null) }
            LaunchedEffect(picked) {
                val next = when (picked) {
                    RoleKind.Admin -> Screen.OwnerLogin
                    RoleKind.Employee -> Screen.EmployeeLogin
                    null -> return@LaunchedEffect
                }
                if (motionEnabled()) {
                    withFrameNanos { }
                    delay(NoduqMotion.selectLeadMs.toLong())
                }
                vm.go(next)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RoleCard(
                    kind = RoleKind.Admin,
                    title = "Administrador",
                    body = "Inicia con Correo o Google para gestionar tu negocio.",
                    selected = picked == RoleKind.Admin,
                    onClick = { picked = RoleKind.Admin },
                )
                RoleCard(
                    kind = RoleKind.Employee,
                    title = "Empleado",
                    body = "Entra rápidamente con tu usuario y PIN.",
                    selected = picked == RoleKind.Employee,
                    onClick = { picked = RoleKind.Employee },
                )
            }
            vm.error?.let {
                Spacer(Modifier.height(16.dp))
                Banner(it)
            }
        }
    }
}

@Composable
fun OwnerLoginScreen(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            Modifier.padding(start = 10.dp, top = 4.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIconButton(onClick = { vm.go(Screen.RoleGate) })
            BrandMark(compact = true)
        }
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Entrar",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        color = NoduqColors.ink,
                    )
                    Text(
                        "Gestiona tu negocio y accesos desde un solo lugar.",
                        color = NoduqColors.muted,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                    )
                    NoduqField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo",
                        keyboardType = KeyboardType.Email,
                        enabled = !vm.busy,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        NoduqField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            password = true,
                            imeAction = ImeAction.Done,
                            enabled = !vm.busy,
                            onIme = { vm.ownerSignIn(email, password) },
                        )
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            QuietTextLink(
                                text = "Olvidé mi contraseña",
                                onClick = { vm.go(Screen.OwnerForgotPassword) },
                                enabled = !vm.busy,
                            )
                        }
                    }
                    vm.error?.let { Banner(it) }
                    PrimaryButton(
                        text = if (vm.busy) "Entrando…" else "Entrar",
                        loading = vm.busy,
                        onClick = { vm.ownerSignIn(email, password) },
                    )
                }
                OrDivider()
                GoogleButton(
                    onClick = { vm.ownerGoogle() },
                    enabled = !vm.busy,
                )
                Spacer(Modifier.height(28.dp))
                AuthLinkRow(
                    prompt = "¿Aún no tienes cuenta?",
                    action = "Crear cuenta",
                    onClick = { vm.go(Screen.OwnerRegister) },
                    enabled = !vm.busy,
                )
            }
        }
    }
}

@Composable
fun OwnerRegisterScreen(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            Modifier.padding(start = 10.dp, top = 4.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIconButton(onClick = { vm.go(Screen.OwnerLogin) })
            BrandMark(compact = true)
        }
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Crear cuenta",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        color = NoduqColors.ink,
                    )
                    Text(
                        "Regístrate en NODUQ y empieza a gestionar tu negocio de forma inteligente.",
                        color = NoduqColors.muted,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                    )
                    NoduqField(email, { email = it }, "Correo", keyboardType = KeyboardType.Email, enabled = !vm.busy)
                    NoduqField(
                        password,
                        { password = it },
                        "Contraseña",
                        password = true,
                        enabled = !vm.busy,
                    )
                    PasswordChecklist(password = password)
                    NoduqField(
                        confirm,
                        { confirm = it },
                        "Repite la contraseña",
                        password = true,
                        imeAction = ImeAction.Done,
                        enabled = !vm.busy,
                        onIme = { vm.ownerSignUp(email, password, confirm) },
                    )
                    if (confirm.isNotEmpty()) {
                        PasswordMatchHint(password = password, confirm = confirm)
                    }
                    vm.error?.let { Banner(it) }
                    vm.info?.let { Banner(it, tone = "ok") }
                    PrimaryButton(
                        text = if (vm.busy) "Creando…" else "Crear cuenta",
                        loading = vm.busy,
                        onClick = { vm.ownerSignUp(email, password, confirm) },
                    )
                }
                OrDivider()
                GoogleButton(
                    onClick = { vm.ownerGoogle() },
                    enabled = !vm.busy,
                )
                Spacer(Modifier.height(28.dp))
                AuthLinkRow(
                    prompt = "¿Ya tienes cuenta?",
                    action = "Entrar",
                    onClick = { vm.go(Screen.OwnerLogin) },
                    enabled = !vm.busy,
                )
            }
        }
    }
}

@Composable
fun OwnerSetupScreen(vm: AppViewModel) {
    var org by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    AuthScaffold(
        title = "Tu comercio",
        lede = "Así aparece en NODUQ. Los avisos salen de los remitentes de Bancolombia; no hay que pegar números de cuenta.",
        onBack = { vm.ownerSignOut() },
    ) {
        NoduqField(
            org,
            { org = it },
            "Organización",
            enabled = !vm.busy,
        )
        NoduqField(
            name,
            { name = it },
            "Tu nombre",
            optional = true,
            imeAction = ImeAction.Done,
            enabled = !vm.busy,
            onIme = { vm.bootstrap(org, name) },
        )
        vm.error?.let { Banner(it) }
        PrimaryButton(
            text = if (vm.busy) "Guardando…" else "Continuar",
            loading = vm.busy,
            onClick = { vm.bootstrap(org, name) },
        )
    }
}

@Composable
fun OwnerForgotPasswordScreen(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    val sent = vm.info != null
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            Modifier.padding(start = 10.dp, top = 4.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIconButton(onClick = { vm.go(Screen.OwnerLogin) })
            BrandMark(compact = true)
        }
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Restablecer contraseña",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        color = NoduqColors.ink,
                    )
                    if (sent) {
                        ResetSentCard()
                        PrimaryButton(
                            text = "Volver a entrar",
                            onClick = { vm.go(Screen.OwnerLogin) },
                        )
                    } else {
                        Text(
                            "Ingresa tu correo y te enviaremos un enlace para crear una nueva contraseña.",
                            color = NoduqColors.muted,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                        )
                        NoduqField(
                            email,
                            { email = it },
                            "Correo",
                            placeholder = "Correo",
                            floatLabel = false,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                            enabled = !vm.busy,
                            onIme = { vm.requestPasswordReset(email) },
                        )
                        vm.error?.let { Banner(it) }
                        PrimaryButton(
                            text = if (vm.busy) "Enviando…" else "Enviar enlace",
                            loading = vm.busy,
                            onClick = { vm.requestPasswordReset(email) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetSentCard() {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.ok.copy(alpha = 0.45f), shape)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                imageVector = Phosphor.Check,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(NoduqColors.ok),
            )
            Text(
                "¡Correo enviado!",
                color = NoduqColors.ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
        Text(
            "Revisa tu bandeja de entrada y sigue las instrucciones.",
            color = NoduqColors.muted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
    }
}

@Composable
fun OwnerPlanScreen(vm: AppViewModel) {
    OnboardingShell(
        onLeave = { vm.ownerSignOut() },
        footer = {
            vm.error?.let { Banner(it) }
            PrimaryButton(
                text = if (vm.busy) "Activando…" else "Suscribirme por $38.900/mes",
                loading = vm.busy,
                hero = true,
                onClick = { vm.buyPlan() },
            )
            QuietButton("Ahora no") { vm.finishOnboarding() }
            Text(
                "Cancela cuando quieras. El cobro es seguro.",
                color = NoduqColors.muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        },
    ) {
        Text(
            "Activa tu suscripción",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = NoduqColors.ink,
        )
        Text(
            "Recibe avisos de pago en el mostrador, en el momento.",
            color = NoduqColors.muted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "$38.900",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(
                    color = NoduqColors.cyan,
                    fontSize = 56.sp,
                    lineHeight = 56.sp,
                    letterSpacing = (-1.8).sp,
                    fontFeatureSettings = "tnum",
                ),
            )
            Text(
                "/mes",
                color = NoduqColors.muted,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        PlanBenefitRow(
            title = "Avisos al instante",
            body = "Suena en el mostrador apenas entra el pago.",
        )
        PlanBenefitRow(
            title = "Doble verificación",
            body = "El comprobante de Bancolombia y el correo confirman el mismo pago.",
        )
        PlanBenefitRow(
            title = "Multi-empleado",
            body = "Todo el equipo ve el aviso en el momento.",
        )
    }
}

@Composable
fun OwnerPermissionsScreen(vm: AppViewModel) {
    val ready = !vm.needsPermissionSetup(askSms = true)
    OnboardingShell(
        onLeave = { vm.ownerSignOut() },
        footer = {
            vm.error?.let { Banner(it) }
            PrimaryButton(
                text = "Continuar al panel",
                enabled = ready,
                hero = true,
                onClick = { vm.finishPermissions() },
            )
        },
    ) {
        Text(
            "Configuración de alertas",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = NoduqColors.ink,
        )
        Text(
            "Concede estos dos permisos para que el mostrador reciba los avisos automáticamente.",
            color = NoduqColors.muted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        PermissionGrantCard(
            icon = Phosphor.Bell,
            title = "Notificaciones de pantalla y sonido",
            body = "Permite que el teléfono suene en el mostrador aunque la pantalla esté bloqueada o la app cerrada.",
            state = vm.notificationsAllowed,
            onAsk = { vm.askNotifications() },
            onOpenSettings = { vm.openSystemSettings() },
        )
        PermissionGrantCard(
            icon = Phosphor.ChatText,
            title = "Detección de pagos por SMS",
            body = "Permite detectar automáticamente los comprobantes de Bancolombia. El resto de la bandeja no se toca.",
            state = vm.smsAllowed,
            onAsk = { vm.askSms() },
            onOpenSettings = { vm.openSystemSettings() },
        )
        PrivacyBadge(
            "Privacidad protegida: NODUQ solo procesa los comprobantes de transferencia. Tus chats y mensajes personales jamás se leen, guardan ni se comparten.",
        )
    }
}

@Composable
private fun OnboardingShell(
    onLeave: () -> Unit,
    footer: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier.padding(start = 10.dp, top = 4.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIconButton(onClick = onLeave)
            BrandMark(compact = true)
        }
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content,
                )
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = footer,
                )
            }
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    lede: String,
    onBack: () -> Unit,
    kicker: String? = null,
    backLabel: String = "Volver",
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        ScreenColumn {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (backLabel == "Volver") {
                    BackIconButton(onClick = onBack)
                    BrandMark(compact = true)
                } else {
                    BrandMark()
                }
            }
            if (backLabel != "Volver") {
                QuietButton(backLabel, onClick = onBack)
            }
            if (kicker != null) Kicker(kicker)
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge, color = NoduqColors.ink)
            Text(lede, color = NoduqColors.muted, fontSize = 16.sp, lineHeight = 24.sp)
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
        }
    }
}
