package com.noduq.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.EmployeeSessionDto
import com.noduq.app.Screen
import com.noduq.app.formatEmployeeCode
import com.noduq.app.theme.NoduqColors

@Composable
fun EmployeeLoginScreen(vm: AppViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
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
            BrandMark()
            QuietButton("Volver") { vm.go(Screen.RoleGate) }
            Text("Entrar", style = androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp))
            Text(
                "Usuario y código. El siguiente paso es esperar a que el QR se confirme.",
                color = NoduqColors.muted,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
            NoduqField(
                value = username,
                onValueChange = { username = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
                label = "Usuario",
                enabled = !vm.busy,
                keyboardType = KeyboardType.Text,
            )
            NoduqField(
                value = code,
                onValueChange = { code = formatEmployeeCode(it) },
                label = "Código",
                hint = "Formato XXXXX-XXXXX",
                mono = true,
                imeAction = ImeAction.Done,
                enabled = !vm.busy,
                onIme = { vm.employeeSignIn(username, code) },
            )
            vm.error?.let { Banner(it) }
            PrimaryButton(
                text = if (vm.busy) "Entrando…" else "Entrar",
                loading = vm.busy,
                hero = true,
                onClick = { vm.employeeSignIn(username, code) },
            )
        }
    }
}

@Composable
fun WaitingRoomScreen(vm: AppViewModel) {
    val session = vm.employeeSession
    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(NoduqColors.night)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(compact = true)
            Spacer(Modifier.weight(1f))
            QuietButton("Salir") { vm.employeeSignOut() }
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (vm.needsPermissionSetup(askSms = false)) {
                PermissionCard(
                    askSms = false,
                    notifications = vm.notificationsAllowed,
                    sms = vm.smsAllowed,
                    onAskNotifications = vm::askNotifications,
                    onAskSms = vm::askSms,
                    onOpenSettings = vm::openSystemSettings,
                )
                Spacer(Modifier.height(16.dp))
            }
            val live = vm.freshNotice ?: vm.notices.firstOrNull()
            if (live != null) {
                LivePaymentCard(live)
            } else {
                WaitingPulse(alpha = alpha, session = session)
            }
        }
        Text(
            "NODUQ avisa. El comprobante del cliente no cuenta.",
            color = NoduqColors.ink.copy(alpha = 0.4f),
            fontSize = 13.sp,
            modifier = Modifier.padding(22.dp),
        )
    }
}

@Composable
private fun WaitingPulse(alpha: Float, session: EmployeeSessionDto?) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .padding(horizontal = 24.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .alpha(alpha)
                        .clip(CircleShape)
                        .background(NoduqColors.cyan),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Esperando el aviso",
                    color = NoduqColors.cyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Cuando confirmen el QR",
                color = NoduqColors.ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.8).sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "el aviso aparece aquí. Tú no gestionas empleados: solo recibes la confirmación.",
                color = NoduqColors.muted,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
            Spacer(Modifier.height(22.dp))
            session?.let {
                Text(it.organization.name, color = NoduqColors.ink, fontWeight = FontWeight.Medium)
                Text(
                    "${it.employee.displayName} · ${it.branch.name}",
                    color = NoduqColors.muted,
                    fontSize = 14.sp,
                )
            }
        }
    }
}
