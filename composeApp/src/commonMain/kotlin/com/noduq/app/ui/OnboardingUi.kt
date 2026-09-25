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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.noduq.app.AppViewModel
import com.noduq.app.motionEnabled
import com.noduq.app.needsAttention
import com.noduq.app.planActive
import com.noduq.app.resources.Res
import com.noduq.app.resources.logo_nq_cian_noche
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private const val ONBOARD_STEPS = 8
private const val DefaultShopPreview = "MI NEGOCIO"
private const val NoticePayer = "RONALDINHO ORTEGA RUIZ"

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
            if (step < 7) BackIconButton(onClick = { vm.onboardBack() })
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
                3 -> NoticesStep(vm)
                4 -> ShopStep(vm)
                5 -> NameStep(vm)
                6 -> PlanStep(vm)
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

private val NoticeFill = Color(0xFF0F171A)
private val NoticeLine = Color.White.copy(alpha = 0.10f)
private const val FlowCycleMs = 3000

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    StepBody(
        title = "Tus empleados confirman el pago al instante. Sin llamarte.",
        lede = "NODUQ detecta las transferencias de tu QR en tiempo real y les avisa en el mostrador para que no tengas que estar confirmando cada cobro por WhatsApp.",
        art = { WelcomeFlowArt() },
        footer = {
            PrimaryButton("Comenzar configuración", hero = true, onClick = onStart)
        },
    )
}

@Composable
private fun WelcomeFlowArt() {
    val reduce = !motionEnabled()
    val motion = rememberInfiniteTransition(label = "welcome-flow")
    val cycle by motion.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(FlowCycleMs, easing = LinearEasing), RepeatMode.Restart),
        label = "cycle",
    )
    val dash by motion.animateFloat(
        0f,
        26f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "dash",
    )
    val nodePulse by motion.animateFloat(
        1f,
        1.07f,
        infiniteRepeatable(tween(1800, easing = NoduqMotion.easeOut), RepeatMode.Reverse),
        label = "node",
    )
    var sceneCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var ownerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var nqCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var empCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var posCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .onGloballyPositioned { sceneCoords = it },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                fun rectOf(target: LayoutCoordinates?): Rect? {
                    val scene = sceneCoords
                    if (scene == null || target == null || !scene.isAttached || !target.isAttached) return null
                    val origin = scene.localPositionOf(target, Offset.Zero)
                    return Rect(origin.x, origin.y, origin.x + target.size.width, origin.y + target.size.height)
                }
                val owner = rectOf(ownerCoords) ?: return@Canvas
                val nqBox = rectOf(nqCoords) ?: return@Canvas
                val emp = rectOf(empCoords) ?: return@Canvas
                val pos = rectOf(posCoords) ?: return@Canvas
                if (owner.width < 4f || nqBox.width < 4f) return@Canvas
                val nodeR = nqBox.width / 2f + 8.dp.toPx()
                val o = Offset(owner.right, owner.center.y)
                val n = Offset(nqBox.center.x, nqBox.center.y)
                val e = Offset(emp.left, emp.center.y)
                val p = Offset(pos.left, pos.center.y)
                val trunkStart = o
                val trunkEnd = Offset(n.x - nodeR, n.y)
                val fork = Offset(n.x + nodeR, n.y)
                fun sweep(to: Offset): Pair<Offset, Offset> {
                    val dx = (to.x - fork.x).coerceAtLeast(1f)
                    return Offset(fork.x + dx * 0.62f, fork.y) to Offset(fork.x + dx * 0.82f, to.y)
                }
                val (upC1, upC2) = sweep(e)
                val (downC1, downC2) = sweep(p)
                val trunk = Path().apply {
                    moveTo(trunkStart.x, trunkStart.y)
                    lineTo(trunkEnd.x, trunkEnd.y)
                }
                val up = Path().apply {
                    moveTo(fork.x, fork.y)
                    cubicTo(upC1.x, upC1.y, upC2.x, upC2.y, e.x, e.y)
                }
                val down = Path().apply {
                    moveTo(fork.x, fork.y)
                    cubicTo(downC1.x, downC1.y, downC2.x, downC2.y, p.x, p.y)
                }
                val stroke = Stroke(
                    width = 1.9.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(5.dp.toPx(), 8.dp.toPx()),
                        if (reduce) 0f else dash,
                    ),
                )
                val wire = NoduqColors.cyan.copy(alpha = 0.78f)
                drawPath(trunk, wire, style = stroke)
                drawPath(up, wire, style = stroke)
                drawPath(down, wire, style = stroke)
                if (!reduce) {
                    val t = cycle
                    val r = 5.2.dp.toPx()
                    fun dot(
                        from: Offset,
                        to: Offset,
                        start: Float,
                        end: Float,
                        cubic: Pair<Offset, Offset>? = null,
                        fadeOut: Boolean = true,
                    ) {
                        if (t < start || t > end) return
                        val local = ((t - start) / (end - start)).coerceIn(0f, 1f)
                        val fade = when {
                            local < 0.08f -> local / 0.08f
                            fadeOut && local > 0.9f -> (1f - local) / 0.1f
                            else -> 1f
                        }
                        val at = if (cubic == null) {
                            Offset(
                                from.x + (to.x - from.x) * local,
                                from.y + (to.y - from.y) * local,
                            )
                        } else {
                            cubicPoint(from, cubic.first, cubic.second, to, local)
                        }
                        drawCircle(NoduqColors.cyan, r, at, alpha = fade)
                    }
                    // Una bolita entra al nodo; al llegar se parte en dos.
                    dot(trunkStart, n, 0.00f, 0.42f, fadeOut = false)
                    dot(fork, e, 0.40f, 0.96f, upC1 to upC2)
                    dot(fork, p, 0.40f, 0.96f, downC1 to downC2)
                }
            }
            NoticeCard(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .onGloballyPositioned { ownerCoords = it },
                icon = Phosphor.DeviceMobile,
                title = "Notificación Banco",
                lifted = true,
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(NoduqColors.cyan.copy(alpha = 0.10f))
                        .border(1.dp, NoduqColors.cyan.copy(alpha = 0.20f), RoundedCornerShape(99.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text("Bancolombia", color = NoduqColors.cyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "$ 5.000",
                    color = NoduqColors.ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.4).sp,
                )
            }
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .graphicsLayer {
                        val s = if (reduce) 1f else nodePulse
                        scaleX = s
                        scaleY = s
                    }
                    .clip(CircleShape)
                    .background(NoduqColors.night)
                    .border(1.dp, NoduqColors.cyan.copy(alpha = 0.7f), CircleShape)
                    .onGloballyPositioned { nqCoords = it },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_nq_cian_noche),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                Modifier.align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.End,
            ) {
                NoticeCard(
                    modifier = Modifier.onGloballyPositioned { empCoords = it },
                    icon = Phosphor.DeviceMobile,
                    title = "Teléfono Empleado",
                ) {
                    StatusBanner("Pago verificado $ 5.000")
                }
                NoticeCard(
                    modifier = Modifier.onGloballyPositioned { posCoords = it },
                    icon = Phosphor.Monitor,
                    title = "Web",
                ) {
                    StatusBanner("$ 5.000 confirmado")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DUEÑO", color = NoduqColors.muted, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.4.sp)
            Text("EMPLEADO / LOCAL", color = NoduqColors.muted, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.4.sp)
        }
    }
}

private fun cubicPoint(p0: Offset, p1: Offset, p2: Offset, p3: Offset, t: Float): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    return Offset(
        uu * u * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + tt * t * p3.x,
        uu * u * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + tt * t * p3.y,
    )
}

@Composable
private fun NoticeCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    lifted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .width(132.dp)
            .then(
                if (lifted) {
                    Modifier.shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x40021113),
                        spotColor = Color(0x40021113),
                    )
                } else {
                    Modifier
                },
            )
            .clip(RoundedCornerShape(16.dp))
            .background(NoticeFill)
            .border(1.dp, NoticeLine, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = NoduqColors.muted, modifier = Modifier.size(14.dp))
            Text(
                title,
                color = NoduqColors.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 14.sp,
                maxLines = 2,
            )
        }
        content()
    }
}

@Composable
private fun StatusBanner(text: String) {
    val wash = NoduqColors.cyan
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(wash.copy(alpha = 0.10f))
            .border(1.dp, wash.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Phosphor.Check, null, tint = wash, modifier = Modifier.size(12.dp).padding(top = 1.dp))
        Text(
            text,
            color = NoduqColors.ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 14.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SmsStep(vm: AppViewModel) {
    var privacy by remember { mutableStateOf(false) }
    var skipConfirm by remember { mutableStateOf(false) }
    StepBody(
        title = "Detección automática por SMS",
        lede = "Leemos el comprobante de Bancolombia en segundo plano para validar los pagos de tu negocio al instante.",
        art = { BankNoticeCard(Phosphor.Chat, "SMS de confirmación recibido", "Bancolombia") },
        footer = {
            vm.error?.let { Banner(it) }
            PrimaryButton(
                "Conectar y dar permiso",
                loading = vm.busy,
                hero = true,
                onClick = { vm.onboardGrantSms() },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PrivacyLink { privacy = true }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Ahora no") { skipConfirm = true }
            }
        },
    )
    if (privacy) {
        NoduqDialog(onDismiss = { privacy = false }) {
            Text(
                "¿Por qué necesitamos este permiso?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "Para que NODUQ pueda avisar a tu equipo en tiempo real cada vez que un cliente te paga por QR, sin que tengas que revisar el teléfono ni mandar capturas manualmente.",
                color = NoduqColors.ink,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Text(
                "Solo leemos el comprobante de Bancolombia. El resto de la bandeja no se toca, no se guarda ni se comparte.",
                color = NoduqColors.muted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                QuietButton("Entendido") { privacy = false }
            }
        }
    }
    if (skipConfirm) {
        NoduqDialog(onDismiss = { skipConfirm = false }) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Phosphor.WarningCircle, null, tint = NoduqColors.cyan, modifier = Modifier.size(26.dp))
            }
            Text(
                "¿Seguro que quieres omitir?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "Sin este permiso, NODUQ no lee el comprobante de Bancolombia. El correo es otro camino.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PrimaryButton(
                "Entendido, activar permiso",
                loading = vm.busy,
                onClick = {
                    skipConfirm = false
                    vm.onboardGrantSms()
                },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Continuar de todos modos") {
                    skipConfirm = false
                    vm.goOnboard(2)
                }
            }
        }
    }
}

@Composable
private fun BankNoticeCard(
    icon: ImageVector,
    kicker: String,
    sender: String,
    shop: String = DefaultShopPreview,
    payer: String = NoticePayer,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoticeFill)
            .border(1.dp, NoticeLine, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, tint = NoduqColors.cyan, modifier = Modifier.size(18.dp))
            Text(
                kicker,
                color = NoduqColors.ink.copy(alpha = 0.72f),
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        Column {
            Text(sender, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("ahora", color = NoduqColors.muted, fontSize = 11.sp)
        }
        Text(
            bankNoticeAnnotated(shop, payer),
            color = NoduqColors.ink.copy(alpha = 0.88f),
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun NoduqDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NoticeFill)
                .border(1.dp, NoticeLine, RoundedCornerShape(16.dp))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
private fun PrivacyLink(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = NoduqColors.cyan),
    ) {
        Text(
            "Saber más sobre la privacidad de tus datos",
            color = NoduqColors.cyan,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DiscreteSkipLink(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    TextButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = NoduqColors.muted),
    ) {
        Text(
            text,
            color = if (pressed) Color.White else NoduqColors.muted,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun GmailStep(vm: AppViewModel) {
    var privacy by remember { mutableStateOf(false) }
    var skipConfirm by remember { mutableStateOf(false) }
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
        lede = "Muchos bancos envían comprobantes por email. Conecta la cuenta donde recibes los avisos de tus transferencias para validarlas al instante.",
        art = {
            BankNoticeCard(
                Phosphor.Envelope,
                "Correo bancario recibido",
                "Notificación de Banco",
            )
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
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PrivacyLink { privacy = true }
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DiscreteSkipLink("Ahora no") { skipConfirm = true }
                }
            }
        },
    )
    if (privacy) {
        NoduqDialog(onDismiss = { privacy = false }) {
            Text(
                "¿Por qué conectamos tu correo?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "Para detectar las notificaciones de pago que tu banco no envía por SMS y asegurar que ningún cobro por QR quede sin validar.",
                color = NoduqColors.ink,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Text(
                "Solo buscamos y leemos correos recibidos de remitentes oficiales de entidades financieras. No leemos, guardamos ni compartimos tus correos personales jamás.",
                color = NoduqColors.muted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                QuietButton("Entendido") { privacy = false }
            }
        }
    }
    if (skipConfirm) {
        NoduqDialog(onDismiss = { skipConfirm = false }) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Phosphor.WarningCircle, null, tint = NoduqColors.cyan, modifier = Modifier.size(26.dp))
            }
            Text(
                "¿Seguro que quieres omitir?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "Sin este permiso, las transferencias que lleguen únicamente a tu correo no se notificarán automáticamente a tu equipo en el mostrador.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PrimaryButton(
                "Entendido, conectar correo",
                loading = vm.busy,
                onClick = {
                    skipConfirm = false
                    vm.connectGmail()
                },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Continuar de todos modos") {
                    skipConfirm = false
                    vm.goOnboard(3)
                }
            }
        }
    }
}

@Composable
private fun NoticesStep(vm: AppViewModel) {
    var skipConfirm by remember { mutableStateOf(false) }
    StepBody(
        title = "Tus empleados y el panel también lo ven",
        lede = "Cuando llega el comprobante, sale una notificación en el panel web y en los teléfonos del equipo. Este permiso es para que también te llegue a ti, en este celular.",
        art = { TillRingCard() },
        footer = {
            vm.error?.let { Banner(it) }
            PrimaryButton(
                "Permitir notificaciones",
                loading = vm.busy,
                hero = true,
                onClick = { vm.onboardGrantNotifications() },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Ahora no") { skipConfirm = true }
            }
        },
    )
    if (skipConfirm) {
        NoduqDialog(onDismiss = { skipConfirm = false }) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Phosphor.WarningCircle, null, tint = NoduqColors.cyan, modifier = Modifier.size(26.dp))
            }
            Text(
                "¿Seguro que quieres omitir?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "No te llegarían notificaciones a este teléfono. Tus empleados sí pueden recibirlas en los suyos.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PrimaryButton(
                "Entendido, activar notificaciones",
                loading = vm.busy,
                onClick = {
                    skipConfirm = false
                    vm.onboardGrantNotifications()
                },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Continuar de todos modos") {
                    skipConfirm = false
                    vm.goOnboard(4)
                }
            }
        }
    }
}

@Composable
private fun TillRingCard() {
    val motion = rememberInfiniteTransition(label = "till-bell")
    val scale by motion.animateFloat(
        1f,
        1.08f,
        infiniteRepeatable(tween(1400, easing = NoduqMotion.easeOut), RepeatMode.Reverse),
        label = "bell",
    )
    val pulse = if (motionEnabled()) scale else 1f
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoticeFill)
            .border(1.dp, NoticeLine, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Phosphor.Bell, null, tint = NoduqColors.cyan, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("NODUQ", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("ahora", color = NoduqColors.muted, fontSize = 11.sp)
            }
        }
        Text("El pago llegó.", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)
        Text("$48.000 · Ronaldinho Ortega", color = NoduqColors.ink.copy(alpha = 0.88f), fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun ShopStep(vm: AppViewModel) {
    var skipConfirm by remember { mutableStateOf(false) }
    val previewShop = vm.onboardShop.trim().ifBlank { DefaultShopPreview }
    StepBody(
        title = "¿Cómo aparece tu negocio en los avisos bancarios?",
        lede = "Escribe el nombre tal como aparece en los mensajes de confirmación de tu banco para identificar tus cobros sin errores.",
        art = {
            BankNoticeCard(
                Phosphor.Storefront,
                "Vista previa de tus notificaciones",
                "Bancolombia",
                shop = previewShop,
            )
        },
        extra = {
            Spacer(Modifier.height(18.dp))
            ShopNameField(
                value = vm.onboardShop,
                onValueChange = { vm.typeOnboardShop(it) },
                enabled = true,
                isError = vm.error != null,
                onDone = { vm.onboardSaveShop() },
            )
            Text(
                "Ejemplo: Si tu banco te notifica como 'DROGUERÍA RICKY', escríbelo idéntico.",
                color = NoduqColors.muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            vm.error?.let {
                Spacer(Modifier.height(10.dp))
                Banner(it)
            }
        },
        footer = {
            PrimaryButton("Continuar", hero = true, enabled = !vm.busy, onClick = { vm.onboardSaveShop() })
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Ahora no") { skipConfirm = true }
            }
        },
    )
    if (skipConfirm) {
        NoduqDialog(onDismiss = { skipConfirm = false }) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Phosphor.WarningCircle, null, tint = NoduqColors.cyan, modifier = Modifier.size(26.dp))
            }
            Text(
                "¿Seguro que quieres omitir?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "Tu cuenta quedará como “Mi negocio”. Es un solo local por cuenta; puedes cambiar el nombre después.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PrimaryButton(
                "Escribir el nombre",
                onClick = { skipConfirm = false },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Continuar de todos modos") {
                    skipConfirm = false
                    vm.onboardSkipShop()
                }
            }
        }
    }
}

@Composable
private fun ShopNameField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    isError: Boolean,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        enabled = enabled,
        isError = isError,
        label = { Text("Nombre del negocio") },
        placeholder = { Text("Ej. Droguería Ricky", color = NoduqColors.muted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
            color = Color.White,
            fontSize = 16.sp,
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = NoduqColors.muted,
            focusedBorderColor = NoduqColors.cyan.copy(alpha = 0.30f),
            unfocusedBorderColor = NoticeLine,
            errorBorderColor = NoduqColors.danger,
            focusedContainerColor = NoticeFill,
            unfocusedContainerColor = NoticeFill,
            disabledContainerColor = NoticeFill,
            cursorColor = NoduqColors.cyan,
            focusedLabelColor = NoduqColors.cyan,
            unfocusedLabelColor = NoduqColors.muted,
            focusedPlaceholderColor = NoduqColors.muted,
            unfocusedPlaceholderColor = NoduqColors.muted,
        ),
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
            NoduqField(
                vm.onboardName,
                { vm.typeOnboardName(it) },
                "Tu nombre",
                imeAction = ImeAction.Done,
                onIme = { vm.onboardSaveName() },
            )
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
    var skipConfirm by remember { mutableStateOf(false) }
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
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Ahora no") { skipConfirm = true }
            }
        },
    )
    if (skipConfirm) {
        NoduqDialog(onDismiss = { skipConfirm = false }) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Phosphor.WarningCircle, null, tint = NoduqColors.cyan, modifier = Modifier.size(26.dp))
            }
            Text(
                "¿Seguro que quieres omitir?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
            Text(
                "Sin plan, NODUQ no valida ni avisa los pagos. Puedes activarlo después en Cuenta.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PrimaryButton(
                "Entendido, activar plan",
                loading = vm.busy,
                onClick = {
                    skipConfirm = false
                    vm.buyPlan()
                },
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DiscreteSkipLink("Continuar de todos modos") {
                    skipConfirm = false
                    vm.goOnboard(7)
                }
            }
        }
    }
}

@Composable
private fun SuccessStep(vm: AppViewModel) {
    var checks by remember { mutableIntStateOf(0) }
    val planOn = vm.workspace?.planActive() == true
    val lines = listOf(
        "Negocio configurado" to true,
        "SMS vinculado" to vm.readsBankSms,
        "Gmail conectado" to (vm.gmail?.connected == true),
        "Notificaciones" to !vm.notificationsAllowed.needsAttention(),
        "Plan activo" to planOn,
    )
    LaunchedEffect(Unit) {
        lines.indices.forEach { i ->
            delay(380)
            checks = i + 1
        }
    }
    val revealed = checks >= lines.size
    val burst = revealed && lines.all { it.second }
    val pop by animateFloatAsState(if (burst) 1f else 0f, tween(500), label = "confetti")
    StepBody(
        title = if (planOn) "Todo listo" else "Cuenta lista",
        lede = if (planOn) {
            "Tu local ya puede recibir avisos de pago en el mostrador."
        } else {
            "Cuando actives el plan en Cuenta, los avisos llegan al mostrador."
        },
        art = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lines.forEachIndexed { i, (line, done) ->
                    val on = i < checks && done
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
            PrimaryButton("Ir a mi panel de pagos", hero = true, enabled = revealed, onClick = { vm.finishOnboarding() })
        },
    )
}

private fun bankNoticeAnnotated(shop: String, payer: String) = buildAnnotatedString {
    val name = shop.trim().ifBlank { DefaultShopPreview }
    fun mark(value: String) {
        withStyle(SpanStyle(color = NoduqColors.cyan, fontWeight = FontWeight.SemiBold)) {
            append(value)
        }
    }
    append("Bancolombia: ")
    mark(name)
    append(", recibiste un pago de ")
    mark(payer)
    append(" por ")
    mark("\$5,000.00")
    append(" el 19/09/2026 a las ")
    mark("19:38")
    append(".")
}
