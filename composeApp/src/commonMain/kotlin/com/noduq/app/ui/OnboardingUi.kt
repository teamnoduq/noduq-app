package com.noduq.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.motionEnabled
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion
import kotlinx.coroutines.delay

private const val ONBOARD_STEPS = 7
private val SmsBody =
    "Bancolombia: DROGUERIA RICKY, recibiste un pago de JOSE FRANCISCO PEROZA PABUENA por $5,000.00 en tu cuenta *8186 el 19/09/2026 a las 19:38."
private val MailBody =
    "Hola recibiste una transferencia de JOSE FRANCISCO PEROZA PABUENA por $5,000.00 el 19/09/2026 a las 19:38."

@Composable
fun OwnerOnboardScreen(vm: AppViewModel, step: Int) {
    val progress by animateFloatAsState(
        targetValue = (step + 1f) / ONBOARD_STEPS,
        animationSpec = tween(420, easing = NoduqMotion.easeOut),
        label = "onboard-progress",
    )
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = NoduqColors.cyan,
            trackColor = NoduqColors.line,
            strokeCap = StrokeCap.Butt,
            drawStopIndicator = {},
        )
        Row(
            Modifier.padding(start = 10.dp, top = 4.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (step < 6) BackIconButton(onClick = { vm.onboardBack() })
            BrandMark(compact = true)
        }
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                if (!motionEnabled()) {
                    fadeIn() togetherWith fadeOut()
                } else {
                    val forward = targetState >= initialState
                    val enterX = { w: Int -> if (forward) w else -w }
                    val exitX = { w: Int -> if (forward) -w / 3 else w / 3 }
                    (
                        slideInHorizontally(tween(NoduqMotion.screenMs, easing = NoduqMotion.easeOut), enterX) +
                            fadeIn(tween(NoduqMotion.fadeMs))
                        ) togetherWith (
                        slideOutHorizontally(tween(NoduqMotion.fadeMs, easing = NoduqMotion.easeOut), exitX) +
                            fadeOut(tween(NoduqMotion.fadeMs))
                        )
                }
            },
            label = "onboard-step",
        ) { page ->
            when (page) {
                0 -> WelcomeStep { vm.goOnboard(1) }
                1 -> SmsStep(vm)
                2 -> GmailStep(vm)
                3 -> ShopStep(vm)
                4 -> NameStep(vm)
                5 -> PlanStep(vm)
                else -> SuccessStep(vm)
            }
        }
    }
}

@Composable
private fun StepBody(
    title: String,
    lede: String,
    art: @Composable () -> Unit,
    extra: @Composable (() -> Unit)? = null,
    footer: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
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
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                art()
                Spacer(Modifier.height(20.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp)
                Spacer(Modifier.height(10.dp))
                Text(lede, color = NoduqColors.muted, fontSize = 16.sp, lineHeight = 24.sp)
                extra?.invoke()
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 20.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            footer()
        }
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pay-pulse")
    val glow by pulse.animateFloat(0.35f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "glow")
    StepBody(
        title = "Tus pagos confirmados y notificados al instante a tus empleados.",
        lede = "NODUQ detecta las transferencias bancarias y le avisa a tu equipo sin que tengas que enviar capturas.",
        art = {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhoneFrame(label = "Dueño", modifier = Modifier.weight(1f)) {
                    BankChip(amount = "$5,000", glow = glow)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp),
                ) {
                    Box(
                        Modifier
                            .width(28.dp)
                            .height(2.dp)
                            .background(NoduqColors.cyan.copy(alpha = 0.25f + glow * 0.6f)),
                    )
                    Text("→", color = NoduqColors.cyan.copy(alpha = glow), fontSize = 22.sp)
                }
                PhoneFrame(label = "Empleado", modifier = Modifier.weight(1f)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NoduqColors.inset)
                            .border(1.dp, NoduqColors.ok.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Phosphor.Bell, null, tint = NoduqColors.cyan, modifier = Modifier.size(14.dp))
                        Text(
                            "Pago validado: $5,000 — Droguería Ricky",
                            color = Color.White,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        },
        footer = {
            PrimaryButton("Comenzar configuración", hero = true, onClick = onStart)
        },
    )
}

@Composable
private fun PhoneFrame(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(NoduqColors.inset)
                .border(1.5.dp, NoduqColors.line, RoundedCornerShape(22.dp))
                .padding(10.dp),
        ) {
            Column {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(NoduqColors.line),
                )
                content()
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = NoduqColors.muted, fontSize = 12.sp)
    }
}

@Composable
private fun BankChip(amount: String, glow: Float) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NoduqColors.card)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.25f + glow * 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Bancolombia", color = NoduqColors.cyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text("Pago recibido", color = NoduqColors.muted, fontSize = 10.sp)
        Text(amount, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    }
}

@Composable
private fun SmsStep(vm: AppViewModel) {
    var privacy by remember { mutableStateOf(false) }
    val scan = rememberInfiniteTransition(label = "scan")
    val sweep by scan.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "scan-x")
    StepBody(
        title = "Rastreo de pagos por SMS",
        lede = "Leemos los mensajes de texto de tu banco en segundo plano para validar los ingresos automáticamente.",
        art = {
            Box {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1C1C1E))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34C759)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Phosphor.ChatText, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Bancolombia", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("ahora", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                        }
                    }
                    Text(
                        highlightText(SmsBody, listOf("DROGUERIA RICKY", "$5,000.00", "19:38")),
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
                Box(
                    Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    (sweep - 0.12f).coerceIn(0f, 1f) to Color.Transparent,
                                    sweep.coerceIn(0f, 1f) to NoduqColors.cyan.copy(alpha = 0.28f),
                                    (sweep + 0.12f).coerceIn(0f, 1f) to Color.Transparent,
                                ),
                            ),
                        ),
                )
            }
        },
        footer = {
            vm.error?.let { Banner(it) }
            PrimaryButton(
                "Conectar y dar permiso",
                loading = vm.busy,
                hero = true,
                onClick = { vm.onboardGrantSms() },
            )
            QuietButton("Saber más sobre la privacidad de tus datos") { privacy = true }
            QuietButton("Ahora no") { vm.goOnboard(2) }
        },
    )
    if (privacy) {
        AlertDialog(
            onDismissRequest = { privacy = false },
            containerColor = NoduqColors.raised,
            title = { Text("Privacidad", color = Color.White) },
            text = {
                Text(
                    "NODUQ solo mira el SMS del 85540 de Bancolombia. El resto de la bandeja no se lee, no se guarda y no se comparte.",
                    color = NoduqColors.muted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
            },
            confirmButton = { QuietButton("Entendido") { privacy = false } },
        )
    }
}

@Composable
private fun GmailStep(vm: AppViewModel) {
    val pulse = rememberInfiniteTransition(label = "gmail")
    val blink by pulse.animateFloat(0.35f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "sync")
    val connected = vm.gmail?.connected == true
    LaunchedEffect(Unit) { vm.loadGmail() }
    LaunchedEffect(connected) {
        if (connected) {
            delay(450)
            vm.goOnboard(3)
        }
    }
    StepBody(
        title = "Vincula tu correo bancario",
        lede = "Muchos avisos llegan por email. Conecta la cuenta de Gmail donde tu banco envía los comprobantes.",
        art = {
            Box {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NoduqColors.card)
                        .border(1.dp, NoduqColors.line, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("notificaciones@bancolombia.com.co", color = NoduqColors.muted, fontSize = 11.sp)
                    Text(
                        "Transferencia recibida - DROGUERÍA RICKY",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                    )
                    Text(
                        highlightText(MailBody, listOf("JOSE FRANCISCO PEROZA PABUENA", "$5,000.00", "19:38")),
                        color = NoduqColors.ink,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                    if (connected) {
                        Text(vm.gmail?.address ?: "Gmail conectado", color = NoduqColors.ok, fontSize = 12.sp)
                    }
                }
                Row(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(NoduqColors.ok.copy(alpha = 0.14f * blink + 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Phosphor.Check, null, tint = NoduqColors.ok.copy(alpha = blink), modifier = Modifier.size(12.dp))
                    Text("Detectado automáticamente", color = NoduqColors.ok, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        footer = {
            vm.error?.let { Banner(it) }
            if (!connected) {
                GoogleButton(
                    onClick = { vm.connectGmail() },
                    enabled = !vm.busy,
                    loading = vm.busy,
                    label = "Vincular cuenta de Gmail",
                )
                QuietButton("Ahora no") { vm.goOnboard(3) }
            }
        },
    )
}

@Composable
private fun ShopStep(vm: AppViewModel) {
    StepBody(
        title = "¿Cómo se llama tu negocio?",
        lede = "Escribe el nombre tal como aparece registrado en tus cuentas bancarias o comprobantes.",
        art = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NoduqColors.card)
                        .border(1.dp, NoduqColors.cyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Así sale en el aviso", color = NoduqColors.muted, fontSize = 12.sp)
                        Text(
                            vm.onboardShop.ifBlank { "DROGUERÍA RICKY" },
                            color = if (vm.onboardShop.isBlank()) NoduqColors.muted else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                        )
                    }
                }
                Text(
                    "Ejemplo: si tu cuenta Nequi/Bancolombia dice “DROGUERÍA RICKY”, escríbelo idéntico.",
                    color = NoduqColors.cyan.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        },
        extra = {
            Spacer(Modifier.height(18.dp))
            NoduqField(vm.onboardShop, { vm.onboardShop = it }, "Nombre del negocio", enabled = !vm.busy)
            vm.error?.let {
                Spacer(Modifier.height(10.dp))
                Banner(it)
            }
        },
        footer = {
            PrimaryButton("Continuar", hero = true, enabled = !vm.busy, onClick = { vm.onboardSaveShop() })
        },
    )
}

@Composable
private fun NameStep(vm: AppViewModel) {
    val hello = vm.onboardName.trim().ifBlank { "…" }
    StepBody(
        title = "¿Cómo te llamaremos?",
        lede = "Tu nombre para identificarte en la administración del local.",
        art = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                    )
                    Icon(Phosphor.UserCircle, null, tint = NoduqColors.cyan, modifier = Modifier.size(72.dp))
                }
                Text("¡Hola, $hello!", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, textAlign = TextAlign.Center)
            }
        },
        extra = {
            Spacer(Modifier.height(18.dp))
            NoduqField(vm.onboardName, { vm.onboardName = it }, "Tu nombre", enabled = !vm.busy)
            vm.error?.let {
                Spacer(Modifier.height(10.dp))
                Banner(it)
            }
        },
        footer = {
            PrimaryButton(
                if (vm.busy) "Guardando…" else "Siguiente",
                loading = vm.busy,
                hero = true,
                onClick = { vm.onboardSaveName() },
            )
        },
    )
}

@Composable
private fun PlanStep(vm: AppViewModel) {
    var shown by remember { mutableIntStateOf(0) }
    val benefits = listOf(
        "Confirmación de pagos ilimitada",
        "Alertas instantáneas a empleados",
        "Sincronización de SMS y Gmail",
    )
    LaunchedEffect(Unit) {
        benefits.indices.forEach { i ->
            delay(110)
            shown = i + 1
        }
    }
    StepBody(
        title = "Activa tu plan NODUQ",
        lede = "Comienza a automatizar la verificación de pagos de tu negocio hoy.",
        art = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(NoduqColors.card.copy(alpha = 0.92f))
                    .border(1.dp, NoduqColors.cyan.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(NoduqColors.cyan.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("PLAN NEGOCIO", color = NoduqColors.cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("$38.900", color = NoduqColors.cyan, fontWeight = FontWeight.SemiBold, fontSize = 40.sp)
                    Text("/mes", color = NoduqColors.muted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
                benefits.forEachIndexed { i, line ->
                    if (i < shown) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Phosphor.Check, null, tint = NoduqColors.ok, modifier = Modifier.size(16.dp))
                            Text(line, color = NoduqColors.ink, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        footer = {
            vm.error?.let { Banner(it) }
            PrimaryButton(
                if (vm.busy) "Activando…" else "Pagar y activar cuenta",
                loading = vm.busy,
                hero = true,
                onClick = { vm.buyPlan() },
            )
        },
    )
}

@Composable
private fun SuccessStep(vm: AppViewModel) {
    var checks by remember { mutableIntStateOf(0) }
    val lines = listOf(
        "Negocio configurado",
        "SMS vinculado",
        "Gmail conectado",
        "Plan activo",
    )
    LaunchedEffect(Unit) {
        lines.indices.forEach { i ->
            delay(380)
            checks = i + 1
        }
    }
    val burst = checks >= 4
    val pop by animateFloatAsState(if (burst) 1f else 0f, tween(500), label = "confetti")
    StepBody(
        title = "Todo listo",
        lede = "Tu local ya puede recibir avisos de pago en el mostrador.",
        art = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lines.forEachIndexed { i, line ->
                    val on = i < checks
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NoduqColors.card)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Phosphor.Check,
                            null,
                            tint = if (on) NoduqColors.ok else NoduqColors.muted.copy(alpha = 0.35f),
                            modifier = Modifier.size(20.dp).scale(if (on) 1f else 0.85f),
                        )
                        Text(line, color = if (on) Color.White else NoduqColors.muted, fontSize = 16.sp)
                    }
                }
                if (burst) {
                    Text(
                        "●  ●  ●",
                        color = NoduqColors.cyan.copy(alpha = 0.35f + pop * 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                    )
                }
            }
        },
        footer = {
            PrimaryButton("Ir a mi panel de pagos", hero = true, enabled = checks >= 4, onClick = { vm.finishOnboarding() })
        },
    )
}

private fun highlightText(text: String, keys: List<String>) = buildAnnotatedString {
    data class Hit(val start: Int, val end: Int)
    val hits = keys.mapNotNull { key ->
        val i = text.indexOf(key, ignoreCase = true)
        if (i < 0) null else Hit(i, i + key.length)
    }.sortedBy { it.start }
    var last = 0
    for (hit in hits) {
        if (hit.start < last) continue
        append(text.substring(last, hit.start))
        withStyle(SpanStyle(color = NoduqColors.cyan, fontWeight = FontWeight.SemiBold)) {
            append(text.substring(hit.start, hit.end))
        }
        last = hit.end
    }
    if (last < text.length) append(text.substring(last))
}
