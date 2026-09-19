package com.noduq.app.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.PaymentNoticeDto
import com.noduq.app.PermissionState
import com.noduq.app.clockLabel
import com.noduq.app.copLabel
import com.noduq.app.dayLabel
import com.noduq.app.dayStartIsoFromUtcMillis
import com.noduq.app.filterDateLabel
import com.noduq.app.localDayEndExclusiveIso
import com.noduq.app.localDayStartIso
import com.noduq.app.localWeekStartIso
import com.noduq.app.momentIso
import com.noduq.app.nextDayStartIsoFromUtcMillis
import com.noduq.app.theme.NoduqColors
import com.noduq.app.titledDay
import com.noduq.app.whoPaid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.loadPayments() }

    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    var draftSince by rememberSaveable { mutableStateOf(vm.paySince) }
    var draftUntil by rememberSaveable { mutableStateOf(vm.payUntil) }
    val picker = rememberDatePickerState()

    val todayTotal = vm.notices
        .filter { dayLabel(it.momentIso()) == "hoy" }
        .mapNotNull { it.amount }
        .sum()
    val viewTotal = vm.notices.mapNotNull { it.amount }.sum()
    val amountShown = if (vm.payRange == "todos" || vm.payRange == "hoy") todayTotal else viewTotal
    val summaryPrefix = if (vm.payRange == "todos" || vm.payRange == "hoy") "Hoy" else "En esta vista"

    val grouped = vm.notices
        .groupBy { titledDay(it.momentIso()).ifBlank { "Reciente" } }
        .toList()

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Pagos",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    letterSpacing = (-0.8).sp,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LiveDot(NoduqColors.ok)
                    Text(
                        "En vivo",
                        color = NoduqColors.ok,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    )
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "$summaryPrefix:",
                    color = NoduqColors.cyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Text(
                    copLabel(amountShown),
                    color = NoduqColors.cyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = vm.payQuery,
                    onValueChange = vm::setPaySearch,
                    modifier = Modifier
                        .weight(1f)
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
                IconButton(
                    onClick = {
                        draftSince = vm.paySince
                        draftUntil = vm.payUntil
                        sheetOpen = true
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NoduqColors.inset)
                        .border(1.dp, NoduqColors.line, RoundedCornerShape(14.dp)),
                ) {
                    Icon(NoduqIcons.Sliders, contentDescription = "Filtros", tint = NoduqColors.cyan)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipButton("Hoy", vm.payRange == "hoy") {
                    vm.applyPayRange("hoy", localDayStartIso(0), localDayEndExclusiveIso(0))
                }
                ChipButton("Ayer", vm.payRange == "ayer") {
                    vm.applyPayRange("ayer", localDayStartIso(1), localDayEndExclusiveIso(1))
                }
                ChipButton("Semana", vm.payRange == "semana") {
                    vm.applyPayRange("semana", localWeekStartIso(), localDayEndExclusiveIso(0))
                }
                ChipButton("Todos", vm.payRange == "todos") {
                    vm.applyPayRange("todos", null, null)
                }
            }
            if (vm.needsPermissionSetup(askSms = true)) {
                Spacer(Modifier.height(12.dp))
                PermissionCard(
                    askSms = true,
                    notifications = vm.notificationsAllowed,
                    sms = vm.smsAllowed,
                    onAskNotifications = vm::askNotifications,
                    onAskSms = vm::askSms,
                    onOpenSettings = vm::openSystemSettings,
                )
            }
            vm.noticesError?.let { message ->
                Spacer(Modifier.height(12.dp))
                Banner(message)
                QuietButton("Reintentar") { vm.loadPayments() }
            }
            Spacer(Modifier.height(8.dp))
        }
        when {
            vm.noticesLoading && vm.notices.isEmpty() -> Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cargando avisos…", color = NoduqColors.muted)
            }
            vm.notices.isEmpty() -> Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyPayments(todayish = vm.payRange == "todos" || vm.payRange == "hoy")
            }
            else -> LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                "Sin el permiso, el SMS del 85540 no entra a NODUQ. Las notificaciones son para que suene en el mostrador aunque la app esté cerrada."
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
                title = "Leer los mensajes de Bancolombia",
                detail = "Solo el 85540. El resto de la bandeja no se toca.",
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
private fun EmptyPayments(todayish: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            NoduqIcons.BellOff,
            contentDescription = null,
            tint = NoduqColors.cyan.copy(alpha = 0.45f),
            modifier = Modifier.size(48.dp),
        )
        Text(
            if (todayish) "Aún no hay pagos hoy" else "Sin movimientos registrados",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            "Los pagos confirmados por QR o Bancolombia aparecerán aquí automáticamente.",
            color = NoduqColors.muted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
