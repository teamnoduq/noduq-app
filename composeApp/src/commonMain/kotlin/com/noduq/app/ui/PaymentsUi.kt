package com.noduq.app.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.PaymentNoticeDto
import com.noduq.app.PermissionState
import com.noduq.app.clockLabel
import com.noduq.app.momentIso
import com.noduq.app.theme.NoduqColors
import com.noduq.app.titledDay
import com.noduq.app.whoPaid

@Composable
fun PaymentsScreen(vm: AppViewModel) {
    val org = vm.workspace?.organization?.name
    LaunchedEffect(Unit) { vm.loadPayments() }

    val grouped = vm.notices
        .groupBy { titledDay(it.momentIso()).ifBlank { "Reciente" } }
        .toList()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("Pagos", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Aquí llega el aviso cuando confirmen el QR. NODUQ lee el mensaje tal cual llega, de los remitentes de Bancolombia.",
                color = NoduqColors.muted,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
        }
        if (vm.needsPermissionSetup(askSms = true)) {
            item {
                PermissionCard(
                    askSms = true,
                    notifications = vm.notificationsAllowed,
                    sms = vm.smsAllowed,
                    onAskNotifications = vm::askNotifications,
                    onAskSms = vm::askSms,
                    onOpenSettings = vm::openSystemSettings,
                )
            }
        }
        vm.noticesError?.let { message ->
            item {
                Banner(message)
                QuietButton("Reintentar") { vm.loadPayments() }
            }
        }
        when {
            vm.noticesLoading && vm.notices.isEmpty() -> item {
                Text("Cargando avisos…", color = NoduqColors.muted)
            }
            vm.notices.isEmpty() -> item {
                EmptyPayments(org)
            }
            else -> grouped.forEach { (day, notices) ->
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
        }
        item { Spacer(Modifier.height(16.dp)) }
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
private fun EmptyPayments(org: String?) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveDot()
                Spacer(Modifier.width(8.dp))
                Text(
                    "Todavía no hay avisos",
                    color = NoduqColors.ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Cuando paguen el QR, el aviso aparece aquí${if (org != null) " en $org" else ""}. NODUQ lo muestra; no hace falta el comprobante que manda el cliente.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
        }
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
        Text(
            notice.amountLabel ?: "—",
            color = NoduqColors.cyan,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
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
            notice.amountLabel ?: "Pago confirmado",
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
