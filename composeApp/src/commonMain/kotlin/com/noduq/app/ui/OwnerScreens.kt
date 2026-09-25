package com.noduq.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppGraph
import com.noduq.app.AppViewModel
import com.noduq.app.CreatedEmployeeDto
import com.noduq.app.EmployeeDto
import com.noduq.app.OwnerTab
import com.noduq.app.PermissionState
import com.noduq.app.Screen
import com.noduq.app.longDateLabel
import com.noduq.app.motionEnabled
import com.noduq.app.needsAttention
import com.noduq.app.planActive
import com.noduq.app.planCancelling
import com.noduq.app.planRenewing
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion

@Composable
fun OwnerShell(vm: AppViewModel, tab: OwnerTab) {
    Column(
        Modifier
            .fillMaxSize()
            .background(NoduqColors.night)
            .statusBarsPadding(),
    ) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandMark(compact = true)
                Spacer(Modifier.weight(1f))
                vm.workspace?.organization?.name?.let { ShopBadge(it) }
            }
        }
        Box(Modifier.weight(1f)) {
            when (tab) {
                OwnerTab.Pagos -> PaymentsScreen(vm)
                OwnerTab.Empleados -> EmployeesScreen(vm)
                OwnerTab.Cuenta -> AccountScreen(vm)
            }
        }
        OwnerBottomBar(tab) { vm.go(Screen.OwnerHome(it)) }
    }
}

@Composable
private fun ShopBadge(name: String) {
    Row(
        Modifier
            .widthIn(max = 200.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(999.dp))
            .padding(start = 10.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Phosphor.Storefront,
            contentDescription = null,
            tint = NoduqColors.cyan,
            modifier = Modifier.size(16.dp),
        )
        Text(
            name,
            color = NoduqColors.ink.copy(alpha = 0.82f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OwnerBottomBar(tab: OwnerTab, onTab: (OwnerTab) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(NoduqColors.inset)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NoduqColors.line),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TabItem(
                "Pagos",
                Phosphor.ListBullets,
                Phosphor.ListBulletsFill,
                tab == OwnerTab.Pagos,
            ) { onTab(OwnerTab.Pagos) }
            TabItem(
                "Empleados",
                Phosphor.UsersThree,
                Phosphor.UsersThreeFill,
                tab == OwnerTab.Empleados,
            ) { onTab(OwnerTab.Empleados) }
            TabItem(
                "Cuenta",
                Phosphor.UserCircle,
                Phosphor.UserCircleFill,
                tab == OwnerTab.Cuenta,
            ) { onTab(OwnerTab.Cuenta) }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    regular: ImageVector,
    fill: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) NoduqColors.cyan else NoduqColors.muted.copy(alpha = 0.55f)
    Column(
        Modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (selected) fill else regular,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Text(
            label,
            color = color,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun EmployeesScreen(vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.loadEmployees() }
    var createOpen by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRegen by rememberSaveable { mutableStateOf<String?>(null) }

    val editingEmployee = vm.employees.find { it.id == editing }
    val deleteEmployee = vm.employees.find { it.id == pendingDelete }
    val regenEmployee = vm.employees.find { it.id == pendingRegen }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Empleados", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, letterSpacing = (-0.8).sp)
            Text(
                "Cada uno entra con usuario y un código. El código solo se muestra una vez.",
                color = NoduqColors.muted,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
            PrimaryButton("Nuevo empleado", onClick = { createOpen = true })
            vm.error?.let { Banner(it) }
            when {
                vm.employeesLoading -> EmployeeSkeletonList()
                vm.employees.isEmpty() -> Text(
                    "Aún no hay empleados. Crea uno para dar usuario y código.",
                    color = NoduqColors.muted,
                )
                else -> vm.employees.forEach { employee ->
                    EmployeeCard(
                        employee = employee,
                        busy = vm.busy,
                        onEdit = { editing = employee.id },
                        onRegen = { pendingRegen = employee.id },
                        onToggle = { vm.toggleEmployee(employee) },
                        onDelete = { pendingDelete = employee.id },
                    )
                }
            }
        }
        FeedbackToast(
            text = vm.info,
            onDismiss = vm::clearInfo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        )
    }

    if (createOpen) {
        CreateEmployeeDialog(
            busy = vm.busy,
            onClose = { createOpen = false },
            onCreate = { name, user, fields ->
                vm.createEmployee(name, user) { n, u, other ->
                    fields(n, u, other)
                    if (n == null && u == null && other == null) {
                        // no-op
                    }
                }
            },
        )
    }

    // Close create dialog when a code appears.
    LaunchedEffect(vm.revealed) {
        if (vm.revealed != null) createOpen = false
    }

    editingEmployee?.let { employee ->
        EditEmployeeDialog(
            employee = employee,
            busy = vm.busy,
            onClose = { editing = null },
            onSave = { name, user, lookback, fields ->
                vm.saveEmployee(employee.id, name, user, lookback, fields)
            },
        )
        LaunchedEffect(vm.info) {
            if (vm.info == "Datos guardados.") editing = null
        }
    }

    vm.revealed?.let { created ->
        CodeRevealDialog(
            created = created,
            onCopy = { AppGraph.clipboard.copy(created.code) },
            onClose = { vm.dismissReveal() },
        )
    }

    regenEmployee?.let { employee ->
        ConfirmDialog(
            title = "¿Regenerar el código?",
            body = "El código actual deja de servir y la sesión abierta se cierra. Tiene que entrar otra vez.",
            confirm = "Regenerar",
            busy = vm.busy,
            onConfirm = { vm.regenerate(employee); pendingRegen = null },
            onClose = { pendingRegen = null },
        )
    }

    deleteEmployee?.let { employee ->
        ConfirmDialog(
            title = "¿Borrar a este empleado?",
            body = "Se elimina ${employee.displayName} (${employee.username}). No podrá entrar.",
            confirm = "Borrar",
            danger = true,
            busy = vm.busy,
            onConfirm = { vm.deleteEmployee(employee); pendingDelete = null },
            onClose = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmployeeCard(
    employee: EmployeeDto,
    busy: Boolean,
    onEdit: () -> Unit,
    onRegen: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(employee.displayName, color = NoduqColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(employee.username, color = NoduqColors.muted, fontSize = 14.sp)
            }
            val inactiveInk = Color(0xFFA8B4B6)
            val inactiveWash = Color(0xFF3A4749)
            Row(
                Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (employee.active) NoduqColors.ok.copy(alpha = 0.14f) else inactiveWash)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (employee.active) NoduqColors.ok else inactiveInk),
                )
                Text(
                    if (employee.active) "Activo" else "Inactivo",
                    color = if (employee.active) NoduqColors.ok else inactiveInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            TextAction("Editar", NoduqIcons.Pencil, enabled = !busy, onClick = onEdit)
            TextAction("Nuevo código", NoduqIcons.Key, enabled = !busy, onClick = onRegen)
            TextAction(
                if (employee.active) "Desactivar" else "Activar",
                if (employee.active) NoduqIcons.Pause else NoduqIcons.Play,
                enabled = !busy,
                onClick = onToggle,
            )
            TextAction(
                "Borrar",
                NoduqIcons.Trash,
                enabled = !busy,
                danger = true,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun TextAction(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    danger: Boolean = false,
) {
    val color = if (danger) NoduqColors.danger else NoduqColors.cyan
    TextButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun EmployeeSkeletonList() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) { EmployeeSkeletonCard() }
    }
}

@Composable
private fun EmployeeSkeletonCard() {
    val pulse = rememberInfiniteTransition(label = "emp-skel")
    val wash by pulse.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = NoduqMotion.easeOut),
            RepeatMode.Reverse,
        ),
        label = "emp-skel-wash",
    )
    val alpha = if (motionEnabled()) wash else 0.10f
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.52f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NoduqColors.cyan.copy(alpha = alpha)),
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.34f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NoduqColors.ink.copy(alpha = alpha * 0.7f)),
                )
            }
            Box(
                Modifier
                    .width(72.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(NoduqColors.ink.copy(alpha = alpha * 0.55f)),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                Box(
                    Modifier
                        .width(64.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NoduqColors.cyan.copy(alpha = alpha)),
                )
            }
        }
    }
}

@Composable
private fun CreateEmployeeDialog(
    busy: Boolean,
    onClose: () -> Unit,
    onCreate: (String, String, (String?, String?, String?) -> Unit) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var user by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var userError by rememberSaveable { mutableStateOf<String?>(null) }
    var other by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = NoduqColors.raised,
        titleContentColor = NoduqColors.ink,
        textContentColor = NoduqColors.ink,
        title = { Text("Nuevo empleado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NoduqField(name, { name = it }, "Nombre", error = nameError, enabled = !busy)
                NoduqField(
                    user,
                    { user = it },
                    "Usuario",
                    optional = true,
                    hint = "Letras, números o _ · 3 a 32. Si lo dejas vacío, lo generamos.",
                    error = userError,
                    enabled = !busy,
                )
                other?.let { Banner(it) }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (busy) "Creando…" else "Crear y ver código",
                loading = busy,
                onClick = {
                    nameError = null
                    userError = null
                    other = null
                    onCreate(name, user) { n, u, o ->
                        nameError = n
                        userError = u
                        other = o
                    }
                },
            )
        },
        dismissButton = { QuietButton("Cancelar", onClick = onClose) },
    )
}

@Composable
private fun EditEmployeeDialog(
    employee: EmployeeDto,
    busy: Boolean,
    onClose: () -> Unit,
    onSave: (String, String, Int, (String?, String?, String?) -> Unit) -> Unit,
) {
    var name by rememberSaveable(employee.id) { mutableStateOf(employee.displayName) }
    var user by rememberSaveable(employee.id) { mutableStateOf(employee.username) }
    var lookback by rememberSaveable(employee.id) { mutableStateOf(employee.lookbackDays) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var userError by rememberSaveable { mutableStateOf<String?>(null) }
    var other by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = NoduqColors.raised,
        titleContentColor = NoduqColors.ink,
        textContentColor = NoduqColors.ink,
        title = { Text("Editar empleado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NoduqField(name, { name = it }, "Nombre", error = nameError, enabled = !busy)
                NoduqField(user, { user = it }, "Usuario", error = userError, enabled = !busy)
                Text("Avisos que ve", color = NoduqColors.muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Hoy", 3 to "3 días", 7 to "7 días").forEach { (days, label) ->
                        ChipButton(label, selected = lookback == days, enabled = !busy, onClick = { lookback = days })
                    }
                }
                other?.let { Banner(it) }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (busy) "Guardando…" else "Guardar",
                loading = busy,
                onClick = {
                    nameError = null
                    userError = null
                    other = null
                    onSave(name, user, lookback) { n, u, o ->
                        nameError = n
                        userError = u
                        other = o
                    }
                },
            )
        },
        dismissButton = { QuietButton("Cancelar", onClick = onClose) },
    )
}

@Composable
private fun CodeRevealDialog(
    created: CreatedEmployeeDto,
    onCopy: () -> Unit,
    onClose: () -> Unit,
) {
    var copied by remember { mutableStateOf(false) }
    val copyNow = {
        onCopy()
        copied = true
    }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            onClose()
        }
    }
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
        containerColor = NoduqColors.raised,
        titleContentColor = NoduqColors.ink,
        textContentColor = NoduqColors.ink,
        title = { Text("Guarda este código ahora") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${created.displayName} entra con el usuario ${created.username}. El código no vuelve a mostrarse.",
                    color = NoduqColors.muted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
                CodeBlock(created.code, onCopy = copyNow, copied = copied)
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (copied) "¡Copiado!" else "Copiar",
                icon = if (copied) NoduqIcons.Check else NoduqIcons.Copy,
                iconTint = NoduqColors.night,
                onClick = copyNow,
            )
        },
        dismissButton = { QuietButton("Ya lo anoté", onClick = onClose) },
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    confirm: String,
    busy: Boolean,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    danger: Boolean = false,
) {
    var armed by remember { mutableStateOf(!danger) }
    LaunchedEffect(danger) {
        if (danger) {
            armed = false
            delay(800)
            armed = true
        }
    }
    AlertDialog(
        onDismissRequest = { if (!danger) onClose() },
        properties = DialogProperties(
            dismissOnClickOutside = !danger,
            dismissOnBackPress = true,
        ),
        containerColor = NoduqColors.raised,
        titleContentColor = NoduqColors.ink,
        textContentColor = NoduqColors.ink,
        title = { Text(title) },
        text = { Text(body, color = NoduqColors.muted, fontSize = 15.sp, lineHeight = 22.sp) },
        confirmButton = {
            if (danger) {
                GhostButton(
                    if (busy) "Borrando…" else confirm,
                    onClick = onConfirm,
                    enabled = !busy && armed,
                    danger = true,
                )
            } else {
                PrimaryButton(if (busy) "…" else confirm, onClick = onConfirm, loading = busy)
            }
        },
        dismissButton = { QuietButton("Cancelar", onClick = onClose) },
    )
}

@Composable
fun AccountScreen(vm: AppViewModel) {
    var displayName by rememberSaveable { mutableStateOf(vm.workspace?.profile?.displayName.orEmpty()) }
    var orgName by rememberSaveable { mutableStateOf(vm.workspace?.organization?.name.orEmpty()) }
    var deleteOpen by rememberSaveable { mutableStateOf(false) }
    var deleteConfirm by rememberSaveable { mutableStateOf("") }
    var cancelPlanOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.readPermissions()
        vm.loadGmail()
    }
    LaunchedEffect(vm.workspace) {
        displayName = vm.workspace?.profile?.displayName.orEmpty()
        orgName = vm.workspace?.organization?.name.orEmpty()
    }

    val dirty = displayName.trim() != vm.workspace?.profile?.displayName.orEmpty() ||
        orgName.trim() != vm.workspace?.organization?.name.orEmpty()
    val planOn = vm.workspace?.planActive() == true
    val planRenewing = vm.workspace?.planRenewing() == true
    val planCancelling = vm.workspace?.planCancelling() == true
    val periodEnd = longDateLabel(vm.workspace?.plan?.periodEndsAt).ifBlank { "el final del periodo" }
    val businessName = vm.workspace?.organization?.name.orEmpty()
    val deleteNameMatches = deleteConfirm == businessName && businessName.isNotEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Cuenta",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            letterSpacing = (-0.8).sp,
        )
        vm.error?.let { Banner(it) }
        vm.info?.let { Banner(it, "ok") }

        AccountCard("Perfil y negocio", "Cómo te ven en este local.") {
            NoduqField(vm.ownerEmail.orEmpty(), {}, "Correo", enabled = false)
            NoduqField(displayName, { displayName = it }, "Tu nombre", enabled = !vm.busy)
            NoduqField(orgName, { orgName = it }, "Negocio", enabled = !vm.busy)
            if (dirty) {
                PrimaryButton(
                    "Guardar cambios",
                    loading = vm.busy,
                    onClick = { vm.saveAccount(displayName, orgName) },
                )
            }
        }

        if (planCancelling) {
            AccountCard(
                "Suscripción cancelada",
                "Tu suscripción finaliza el $periodEnd. Hasta esa fecha la validación automática seguirá funcionando.",
            ) {
                PrimaryButton(
                    if (vm.busy) "Reactivando…" else "Reactivar plan",
                    loading = vm.busy,
                    onClick = { vm.reactivatePlan() },
                )
            }
        } else {
            AccountCard(
                if (planRenewing) "Plan" else "Plan Pro NODUQ",
                if (planRenewing) "Activo. NODUQ valida y avisa los pagos." else null,
            ) {
                if (planRenewing) {
                    Text(
                        "$24.900 / mes · cancela cuando quieras.",
                        color = NoduqColors.muted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                    TextButton(
                        onClick = { cancelPlanOpen = true },
                        enabled = !vm.busy,
                        modifier = Modifier.heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Cancelar suscripción",
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        )
                    }
                } else {
                    PlanBenefit("Validaciones automáticas e ilimitadas por SMS y Correo")
                    PlanBenefit("Notificaciones instantáneas para tu equipo en el mostrador")
                    PrimaryButton(
                        if (vm.busy) "Activando…" else "Activar plan · $24.900/mes",
                        loading = vm.busy,
                        onClick = { vm.buyPlan() },
                    )
                }
            }
        }

        AccountCard(
            "Permisos y Conexiones",
            "Administra qué permisos de este celular o cuentas de correo usas para automatizar tus pagos.",
        ) {
            PermissionStatusCard(
                icon = Phosphor.DeviceMobile,
                title = "SMS",
                detail = "Para validación automática",
                granted = !vm.smsAllowed.needsAttention(),
                grantLabel = vm.smsAllowed.accountGrantLabel(),
                revokeLabel = if (!vm.smsAllowed.needsAttention()) "Quitar" else null,
                busy = vm.busy,
                onGrant = {
                    if (vm.smsAllowed == PermissionState.Blocked) vm.openSystemSettings()
                    else vm.askSms()
                },
                onRevoke = { vm.openSystemSettings() },
            )
            PermissionStatusCard(
                icon = Phosphor.Envelope,
                title = "Correo",
                detail = when {
                    vm.gmail == null -> "Cargando…"
                    vm.gmail?.connected == true -> vm.gmail?.address ?: "Cuenta vinculada"
                    vm.gmail?.configured == false -> "Gmail aún no está listo en el servidor."
                    else -> "Sin cuenta vinculada"
                },
                granted = vm.gmail?.connected == true,
                grantLabel = when {
                    vm.gmail == null -> null
                    vm.gmail?.connected == true -> null
                    vm.gmail?.configured == false -> null
                    else -> "Conectar"
                },
                revokeLabel = if (vm.gmail?.connected == true) "Desconectar" else null,
                busy = vm.busy,
                onGrant = { vm.connectGmail() },
                onRevoke = { vm.disconnectGmail() },
            )
            PermissionStatusCard(
                icon = Phosphor.Bell,
                title = "Notificaciones",
                detail = "Alertas para el mostrador",
                granted = !vm.notificationsAllowed.needsAttention(),
                grantLabel = vm.notificationsAllowed.accountGrantLabel(),
                revokeLabel = if (
                    vm.notificationsAllowed == PermissionState.Granted
                ) "Quitar" else null,
                busy = vm.busy,
                onGrant = {
                    if (vm.notificationsAllowed == PermissionState.Blocked) vm.openSystemSettings()
                    else vm.askNotifications()
                },
                onRevoke = { vm.openSystemSettings() },
            )
        }

        Spacer(Modifier.height(8.dp))
        AccountCard("Sesión y cuenta") {
            GhostButton("Cerrar sesión", onClick = { vm.ownerSignOut() })
            AccountDangerButton(
                "Eliminar cuenta",
                enabled = !vm.busy,
                onClick = {
                    deleteConfirm = ""
                    deleteOpen = true
                },
            )
        }
    }

    if (deleteOpen) {
        NoduqSheet(
            onDismiss = {
                if (!vm.busy) {
                    deleteOpen = false
                    deleteConfirm = ""
                }
            },
        ) {
            Text(
                "¿Eliminar cuenta de NODUQ?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            Text(
                "Esta acción es permanente e irreversible. Perderás la configuración de tu local, tu historial y la conexión con tus empleados.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            if (planOn) {
                Text(
                    "Atención: Borrar la cuenta no cancela tu cobro recurrente. Cancela tu suscripción en Google Play Store para evitar cargos.",
                    color = Color(0xFFFDE68A),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33422006))
                        .border(1.dp, Color(0x66B45309), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                )
                GhostButton(
                    "Gestionar suscripción en Play Store",
                    color = NoduqColors.cyan,
                    enabled = !vm.busy,
                    onClick = { AppGraph.links.open(PLAY_SUBSCRIPTIONS) },
                )
            }
            Text(
                "Para confirmar, escribe el nombre de tu negocio ($businessName):",
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            OutlinedTextField(
                value = deleteConfirm,
                onValueChange = { deleteConfirm = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.busy,
                placeholder = {
                    Text("Nombre del negocio", color = NoduqColors.muted, fontSize = 15.sp)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = NoduqColors.muted,
                    focusedBorderColor = Color.White.copy(alpha = 0.22f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                    focusedContainerColor = Color(0xFF0F171A),
                    unfocusedContainerColor = Color(0xFF0F171A),
                    disabledContainerColor = Color(0xFF0F171A),
                    cursorColor = NoduqColors.cyan,
                ),
            )
            AccountDangerButton(
                if (vm.busy) "Eliminando…" else "Eliminar mi cuenta definitivamente",
                enabled = deleteNameMatches && !vm.busy,
                onClick = { vm.deleteAccount() },
            )
            TextButton(
                onClick = {
                    deleteOpen = false
                    deleteConfirm = ""
                },
                enabled = !vm.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(
                    "Cancelar",
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
            }
        }
    }

    if (cancelPlanOpen) {
        NoduqSheet(onDismiss = { if (!vm.busy) cancelPlanOpen = false }) {
            Text(
                "¿Deseas cancelar tu suscripción?",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            Text(
                "Tus empleados dejarán de recibir la confirmación de pagos en el mostrador al finalizar el periodo actual.",
                color = NoduqColors.muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PrimaryButton(
                "Mantener mi plan",
                onClick = { cancelPlanOpen = false },
                enabled = !vm.busy,
            )
            TextButton(
                onClick = {
                    vm.cancelPlan()
                    cancelPlanOpen = false
                },
                enabled = !vm.busy,
            ) {
                Text(
                    "Sí, cancelar plan",
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun PlanBenefit(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            "✓",
            color = NoduqColors.cyan,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text,
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun AccountDangerButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val red = Color(0xFFEF4444)
    val label = Color(0xFFF87171)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = red.copy(alpha = 0.05f),
            contentColor = label,
            disabledContainerColor = red.copy(alpha = 0.05f),
            disabledContentColor = label,
        ),
        border = BorderStroke(1.dp, red.copy(alpha = 0.20f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text, color = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun NoduqSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F171A))
                .border(1.dp, NoduqColors.line, RoundedCornerShape(16.dp))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

private const val PLAY_SUBSCRIPTIONS =
    "https://play.google.com/store/account/subscriptions?package=com.noduq.app"

@Composable
private fun AccountCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NoduqColors.card)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        subtitle?.let { Text(it, color = NoduqColors.muted, fontSize = 13.sp, lineHeight = 18.sp) }
        content()
    }
}

@Composable
private fun PermissionStatusCard(
    icon: ImageVector,
    title: String,
    detail: String,
    granted: Boolean,
    grantLabel: String?,
    revokeLabel: String?,
    busy: Boolean,
    onGrant: () -> Unit,
    onRevoke: () -> Unit,
) {
    val well = Color(0xFF0F171A)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(well)
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(NoduqColors.cyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = NoduqColors.cyan, modifier = Modifier.size(24.dp))
        }
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Text(
                detail,
                color = NoduqColors.muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (granted) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(NoduqColors.cyan.copy(alpha = 0.12f))
                        .border(1.dp, NoduqColors.cyan.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "✓ Activo",
                        color = NoduqColors.cyan,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                    )
                }
            }
            if (revokeLabel != null) {
                TextButton(
                    onClick = onRevoke,
                    enabled = !busy,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = NoduqColors.muted,
                    ),
                ) {
                    Text(
                        revokeLabel,
                        color = NoduqColors.muted,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    )
                }
            }
            if (!granted && grantLabel != null) {
                TextButton(
                    onClick = onGrant,
                    enabled = !busy,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 40.dp),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = NoduqColors.cyan,
                    ),
                ) {
                    Text(
                        grantLabel,
                        color = NoduqColors.cyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

private fun PermissionState.accountGrantLabel(): String? = when (this) {
    PermissionState.Denied -> "Permitir"
    PermissionState.Blocked -> "Ajustes"
    PermissionState.Granted, PermissionState.NotNeeded -> null
}
