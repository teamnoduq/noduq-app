package com.noduq.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.OwnerTab
import com.noduq.app.AppViewModel
import com.noduq.app.PaymentNoticeDto
import com.noduq.app.PermissionState
import com.noduq.app.Screen
import com.noduq.app.clockLabel
import com.noduq.app.dayStartIsoFromUtcMillis
import com.noduq.app.filterDateLabel
import com.noduq.app.localDayEndExclusiveIso
import com.noduq.app.localDayStartIso
import com.noduq.app.localWeekStartIso
import com.noduq.app.motionEnabled
import com.noduq.app.momentIso
import com.noduq.app.nextDayStartIsoFromUtcMillis
import com.noduq.app.planActive
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion
import com.noduq.app.titledDay
import com.noduq.app.whoPaid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(vm: AppViewModel) {
    LaunchedEffect(Unit) {
        vm.loadPayments()
        vm.loadGmail()
    }

    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    var draftSince by rememberSaveable { mutableStateOf(vm.paySince) }
    var draftUntil by rememberSaveable { mutableStateOf(vm.payUntil) }
    val picker = rememberDatePickerState()

    val viewTotal = vm.notices.mapNotNull { it.amount }.sum()
    val heading = when (vm.payRange) {
        "ayer" -> "Pagos de ayer"
        "semana" -> "Pagos de la semana"
        "hoy" -> "Pagos de hoy"
        else -> "Pagos"
    }

    val grouped = vm.notices
        .groupBy { titledDay(it.momentIso()).ifBlank { "Reciente" } }
        .toList()

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    heading,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.7).sp,
                    modifier = Modifier.weight(1f),
                )
                CountingCop(amount = viewTotal)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = vm.payQuery,
                onValueChange = vm::setPaySearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                placeholder = { Text("Buscar por nombre…", color = NoduqColors.muted) },
                leadingIcon = {
                    Icon(NoduqIcons.Search, contentDescription = null, tint = NoduqColors.muted, modifier = Modifier.size(20.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.searchPayments() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NoduqColors.ink,
                    unfocusedTextColor = NoduqColors.ink,
                    focusedBorderColor = NoduqColors.cyan,
                    unfocusedBorderColor = NoduqColors.line,
                    focusedContainerColor = NoduqColors.inset,
                    unfocusedContainerColor = NoduqColors.inset,
                    cursorColor = NoduqColors.cyan,
                ),
            )
            Spacer(Modifier.height(16.dp))
            SlidingFilterChips(
                options = listOf(
                    "hoy" to "Hoy",
                    "ayer" to "Ayer",
                    "semana" to "Semana",
                    "todos" to "Todos",
                ),
                selected = vm.payRange,
                onSelect = { id ->
                    when (id) {
                        "hoy" -> vm.applyPayRange("hoy", localDayStartIso(0), localDayEndExclusiveIso(0))
                        "ayer" -> vm.applyPayRange("ayer", localDayStartIso(1), localDayEndExclusiveIso(1))
                        "semana" -> vm.applyPayRange("semana", localWeekStartIso(), localDayEndExclusiveIso(0))
                        else -> vm.applyPayRange("todos", null, null)
                    }
                },
            )
            val showNotificationBanner = vm.needsNotificationSetup() && !vm.notificationPromptDismissed
            if (showNotificationBanner) {
                Spacer(Modifier.height(16.dp))
                NotificationListenBanner(
                    onActivate = {
                        if (vm.notificationsAllowed == PermissionState.Blocked) vm.openSystemSettings()
                        else vm.askNotifications()
                    },
                    onDismiss = vm::dismissNotificationPrompt,
                )
            }
            vm.noticesError?.let { message ->
                Spacer(Modifier.height(12.dp))
                Banner(message)
                QuietButton("Reintentar") { vm.loadPayments() }
            }
            Spacer(Modifier.height(if (showNotificationBanner) 16.dp else 8.dp))
        }
        val slidePx = with(LocalDensity.current) { 8.dp.roundToPx() }
        val feedKey = when {
            vm.noticesLoading -> "loading-${vm.payRange}"
            vm.notices.isEmpty() -> "empty-${vm.payRange}"
            else -> "list-${vm.payRange}"
        }
        AnimatedContent(
            targetState = feedKey,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                if (!motionEnabled()) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (
                        fadeIn(tween(180, delayMillis = 150, easing = NoduqMotion.easeOut)) +
                            slideInVertically(tween(180, delayMillis = 150, easing = NoduqMotion.easeOut)) { -slidePx }
                        ) togetherWith (
                        fadeOut(tween(150, easing = NoduqMotion.easeOut)) +
                            slideOutVertically(tween(150, easing = NoduqMotion.easeOut)) { slidePx }
                        )
                }
            },
            label = "pay-feed",
        ) { key ->
            when (key.substringBefore("-")) {
                "loading" -> PaymentSkeletonList(Modifier.fillMaxSize())
                "empty" -> Box(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val gmail = vm.gmail
                    EmptyPayments(
                        todayish = vm.payRange == "todos" || vm.payRange == "hoy",
                        planActive = vm.workspace?.planActive() == true,
                        smsReady = !vm.needsSmsSetup(),
                        offerMail = gmail != null &&
                            gmail.configured &&
                            !gmail.connected &&
                            !vm.mailPromptDismissed,
                        onActivatePlan = {
                            vm.go(Screen.OwnerHome(OwnerTab.Cuenta))
                            vm.buyPlan()
                        },
                        onGrantSms = {
                            if (vm.smsAllowed == PermissionState.Blocked) vm.openSystemSettings()
                            else vm.askSms()
                        },
                        onConnectMail = vm::connectGmail,
                        onSkipMail = vm::dismissMailPrompt,
                    )
                }
                else -> LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    grouped.forEach { (day, notices) ->
                        item(key = "day-$day") {
                            Text(
                                day,
                                color = NoduqColors.muted,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        items(notices, key = { it.id }) { notice ->
                            NoticeRow(notice)
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = NoduqColors.raised,
            contentColor = NoduqColors.ink,
        ) {
            Column(
                Modifier.padding(horizontal = 22.dp, vertical = 8.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Filtros", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Text("Desde", color = NoduqColors.muted, fontSize = 13.sp)
                DatePickField(filterDateLabel(draftSince), chosen = draftSince != null) { picking = "from" }
                Text("Hasta", color = NoduqColors.muted, fontSize = 13.sp)
                DatePickField(filterDateLabel(draftUntil), chosen = draftUntil != null) { picking = "until" }
                PrimaryButton(
                    text = "Aplicar filtros",
                    onClick = {
                        vm.applyPayRange("custom", draftSince, draftUntil)
                        sheetOpen = false
                    },
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    QuietButton("Quitar fechas") {
                        draftSince = null
                        draftUntil = null
                        vm.applyPayRange("todos", null, null)
                        sheetOpen = false
                    }
                }
            }
        }
    }

    picking?.let { field ->
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = picker.selectedDateMillis
                        if (millis != null) {
                            if (field == "from") draftSince = dayStartIsoFromUtcMillis(millis)
                            else draftUntil = nextDayStartIsoFromUtcMillis(millis)
                        }
                        picking = null
                    },
                ) { Text("Listo", color = NoduqColors.cyan) }
            },
            dismissButton = {
                TextButton(onClick = { picking = null }) { Text("Cancelar", color = NoduqColors.muted) }
            },
        ) {
            DatePicker(state = picker)
        }
    }
}

@Composable
private fun DatePickField(label: String, chosen: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            label,
            color = if (chosen) NoduqColors.cyan else Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun PermissionCard(
    askSms: Boolean,
    notifications: PermissionState,
    sms: PermissionState,
    onAskNotifications: () -> Unit,
    onAskSms: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            if (askSms) "Este teléfono tiene que oír al banco" else "Este teléfono tiene que sonar",
            color = NoduqColors.ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
        Text(
            if (askSms) {
                "Sin el permiso de mensajes, el comprobante de tu banco no entra. Las notificaciones hacen sonar el mostrador aunque la app esté cerrada."
            } else {
                "Las notificaciones avisan cuando el banco confirma el QR, aunque estés fuera de esta pantalla."
            },
            color = NoduqColors.muted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
        if (notifications.needsRow()) {
            PermissionRow(
                title = "Avisar cuando llegue un pago",
                detail = "Suena aunque la app esté cerrada.",
                state = notifications,
                onAsk = onAskNotifications,
                onOpenSettings = onOpenSettings,
            )
        }
        if (askSms && sms.needsRow()) {
            PermissionRow(
                title = "Leer los comprobantes de tu banco",
                detail = "Solo esos avisos bancarios. El resto de la bandeja no se toca.",
                state = sms,
                onAsk = onAskSms,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    detail: String,
    state: PermissionState,
    onAsk: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = NoduqColors.ink, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        Text(detail, color = NoduqColors.muted, fontSize = 14.sp, lineHeight = 20.sp)
        when (state) {
            PermissionState.Blocked -> GhostButton("Abrir ajustes", onClick = onOpenSettings)
            PermissionState.Denied -> PrimaryButton("Permitir", onClick = onAsk)
            PermissionState.Granted, PermissionState.NotNeeded -> Unit
        }
    }
}

@Composable
private fun PaymentSkeletonList(modifier: Modifier = Modifier) {
    Column(
        modifier.padding(horizontal = 22.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(4) { PaymentSkeletonCard() }
    }
}

@Composable
private fun PaymentSkeletonCard() {
    val pulse = rememberInfiniteTransition(label = "skel")
    val wash by pulse.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = NoduqMotion.easeOut),
            RepeatMode.Reverse,
        ),
        label = "skel-wash",
    )
    val alpha = if (motionEnabled()) wash else 0.10f
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.48f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NoduqColors.cyan.copy(alpha = alpha)),
            )
            Box(
                Modifier
                    .fillMaxWidth(0.28f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NoduqColors.ink.copy(alpha = alpha * 0.7f)),
            )
        }
        Box(
            Modifier
                .width(72.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(NoduqColors.cyan.copy(alpha = alpha)),
        )
    }
}

@Composable
private fun NotificationListenBanner(onActivate: () -> Unit, onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F171A))
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Activa las notificaciones para escuchar cuando llegue un pago en el mostrador.",
            color = NoduqColors.ink,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
        )
        TextButton(
            onClick = onActivate,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                "Activar",
                color = NoduqColors.cyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
            Icon(
                Phosphor.X,
                contentDescription = "Ahora no",
                tint = NoduqColors.muted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun EmptyPayments(
    todayish: Boolean,
    planActive: Boolean,
    smsReady: Boolean,
    offerMail: Boolean,
    onActivatePlan: () -> Unit,
    onGrantSms: () -> Unit,
    onConnectMail: () -> Unit,
    onSkipMail: () -> Unit,
) {
    when {
        planActive && !smsReady -> PaymentsEmptyCluster(
            icon = Phosphor.WarningCircle,
            iconTint = NoduqColors.cyan,
            title = "Permiso de lectura requerido",
            body = "Tu plan está activo, pero NODUQ necesita permiso para leer los avisos de tu banco y registrar los cobros.",
            action = "Conceder permiso",
            onAction = onGrantSms,
        )
        planActive && offerMail -> PaymentsEmptyCluster(
            icon = Phosphor.Envelope,
            iconTint = NoduqColors.cyan,
            title = "Para confirmar los del SMS",
            body = "Conecta el correo y NODUQ confirma por ahí los pagos que ya llegaron como aviso de tu banco.",
            action = "Conectar correo",
            onAction = onConnectMail,
            skip = "Omitir por ahora",
            onSkip = onSkipMail,
        )
        !planActive -> PaymentsEmptyCluster(
            icon = Phosphor.Receipt,
            iconTint = NoduqColors.muted,
            title = "Validación automática inactiva",
            body = if (!smsReady) {
                "Activa tu plan y conecta tus permisos para validar los pagos por QR al instante."
            } else {
                "Activa tu plan para validar los pagos por QR al instante."
            },
            action = "Activar plan · $24.900/mes",
            onAction = onActivatePlan,
        )
        else -> QuietEmptyPayments(todayish = todayish)
    }
}

@Composable
private fun PaymentsEmptyCluster(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    skip: String? = null,
    onSkip: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F171A))
                .border(1.dp, NoduqColors.line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
        }
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            body,
            color = NoduqColors.muted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        PrimaryButton(action, onClick = onAction)
        if (skip != null && onSkip != null) {
            TextButton(
                onClick = onSkip,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    skip,
                    color = NoduqColors.muted,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun QuietEmptyPayments(todayish: Boolean) {
    val pulse = rememberInfiniteTransition(label = "empty")
    val wash by pulse.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = NoduqMotion.easeOut),
            RepeatMode.Reverse,
        ),
        label = "empty-wash",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(NoduqColors.cyan.copy(alpha = if (motionEnabled()) wash else 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Phosphor.Receipt,
                contentDescription = null,
                tint = NoduqColors.cyan.copy(alpha = 0.72f),
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            if (todayish) "Aún no hay pagos hoy" else "Sin movimientos registrados",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            "Los pagos confirmados aparecerán aquí automáticamente.",
            color = NoduqColors.muted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NoticeRow(notice: PaymentNoticeDto) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                notice.whoPaid(),
                color = NoduqColors.ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            val whenLabel = listOfNotNull(
                titledDay(notice.momentIso()).takeIf { it.isNotBlank() && it != "Hoy" },
                clockLabel(notice.momentIso()).ifBlank { null },
            ).joinToString(" · ")
            if (whenLabel.isNotBlank()) {
                Text(whenLabel, color = NoduqColors.muted, fontSize = 13.sp)
            }
            if (!notice.readable) {
                Text(
                    "El banco avisó, pero no se pudieron leer el nombre ni el monto.",
                    color = NoduqColors.muted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
            if (notice.confirmedByEmail) {
                Text("Verificado con correo", color = NoduqColors.ok, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                NoduqIcons.Check,
                contentDescription = null,
                tint = NoduqColors.ok,
                modifier = Modifier.size(16.dp),
            )
            Text(
                notice.amountLabel ?: "—",
                color = NoduqColors.cyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
fun LivePaymentCard(notice: PaymentNoticeDto) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveDot()
            Spacer(Modifier.width(10.dp))
            Text(
                "El pago ya llegó",
                color = NoduqColors.cyan,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            notice.amountLabel ?: "—",
            color = NoduqColors.ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = (-1).sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            notice.whoPaid(),
            color = NoduqColors.ink,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
        )
        val whenLabel = listOfNotNull(
            titledDay(notice.momentIso()).takeIf { it.isNotBlank() },
            clockLabel(notice.momentIso()).ifBlank { null },
        ).joinToString(" · ").lowercase()
        if (whenLabel.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(whenLabel, color = NoduqColors.muted, fontSize = 15.sp)
        }
        if (!notice.readable) {
            Spacer(Modifier.height(12.dp))
            Text(
                "El banco avisó. Ábrelo en Pagos si hace falta ver el detalle.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
        }
    }
}

private fun PermissionState.needsRow(): Boolean =
    this != PermissionState.Granted && this != PermissionState.NotNeeded
