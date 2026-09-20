package com.noduq.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
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
import com.noduq.app.Screen
import com.noduq.app.motionEnabled
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
    var confirmation by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadGmail() }
    LaunchedEffect(vm.workspace) {
        displayName = vm.workspace?.profile?.displayName.orEmpty()
        orgName = vm.workspace?.organization?.name.orEmpty()
    }

    val dirty = displayName.trim() != vm.workspace?.profile?.displayName.orEmpty() ||
        orgName.trim() != vm.workspace?.organization?.name.orEmpty()

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

        AccountCard("Sincronización", "Gmail verifica el aviso del banco, más tarde.") {
            when {
                vm.gmail == null -> Text("Cargando…", color = NoduqColors.muted, fontSize = 14.sp)
                vm.gmail?.connected == true -> GmailConnectedRow(
                    address = vm.gmail?.address ?: "Gmail",
                    busy = vm.busy,
                    onDisconnect = { vm.disconnectGmail() },
                )
                vm.gmail?.configured == false -> Text(
                    "Gmail aún no está listo en el servidor.",
                    color = NoduqColors.muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                else -> PrimaryButton("Conectar Gmail", loading = vm.busy, onClick = { vm.connectGmail() })
            }
        }

        Spacer(Modifier.height(8.dp))
        AccountCard("Sesión y cuenta") {
            GhostButton("Cerrar sesión", onClick = { vm.ownerSignOut() })
            Text(
                "Borrar cuenta",
                color = NoduqColors.danger,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            GhostButton("Borrar cuenta", onClick = { deleteOpen = true }, danger = true)
        }
    }

    if (deleteOpen) {
        AlertDialog(
            onDismissRequest = { deleteOpen = false },
            properties = DialogProperties(dismissOnClickOutside = false),
            containerColor = NoduqColors.raised,
            titleContentColor = NoduqColors.ink,
            textContentColor = NoduqColors.ink,
            title = { Text("Borrar la cuenta") },
            text = {
                val org = vm.workspace?.organization?.name.orEmpty()
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Se borra $org, los empleados y el acceso. Para confirmar, escribe el nombre exacto del negocio. Esto no se puede deshacer.",
                        color = NoduqColors.muted,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                    NoduqField(
                        confirmation,
                        { confirmation = it },
                        "Nombre del negocio",
                        imeAction = ImeAction.Done,
                        enabled = !vm.busy,
                    )
                }
            },
            confirmButton = {
                GhostButton(
                    if (vm.busy) "Borrando…" else "Borrar para siempre",
                    onClick = { vm.deleteAccount(confirmation) },
                    danger = true,
                    enabled = !vm.busy,
                )
            },
            dismissButton = {
                QuietButton("Cancelar") {
                    deleteOpen = false
                    confirmation = ""
                }
            },
        )
    }
}

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
private fun GmailConnectedRow(
    address: String,
    busy: Boolean,
    onDisconnect: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NoduqColors.inset),
            contentAlignment = Alignment.Center,
        ) {
            Icon(NoduqIcons.Mail, contentDescription = null, tint = NoduqColors.cyan, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(address, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NoduqColors.ok),
                )
                Text("Conectado", color = NoduqColors.ok, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        QuietButton("Desconectar", enabled = !busy, onClick = onDisconnect)
    }
}
