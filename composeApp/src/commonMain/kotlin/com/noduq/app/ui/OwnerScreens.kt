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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppGraph
import com.noduq.app.AppViewModel
import com.noduq.app.CreatedEmployeeDto
import com.noduq.app.EmployeeDto
import com.noduq.app.OwnerTab
import com.noduq.app.Screen
import com.noduq.app.theme.NoduqColors

@Composable
fun OwnerShell(vm: AppViewModel, tab: OwnerTab) {
    Column(
        Modifier
            .fillMaxSize()
            .background(NoduqColors.night)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
            BrandMark(compact = true)
            vm.workspace?.organization?.name?.let {
                Text(it, color = NoduqColors.muted, fontSize = 13.sp)
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
private fun OwnerBottomBar(tab: OwnerTab, onTab: (OwnerTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, NoduqColors.line)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TabItem("Pagos", tab == OwnerTab.Pagos) { onTab(OwnerTab.Pagos) }
        TabItem("Empleados", tab == OwnerTab.Empleados) { onTab(OwnerTab.Empleados) }
        TabItem("Cuenta", tab == OwnerTab.Cuenta) { onTab(OwnerTab.Cuenta) }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) NoduqColors.cyan else NoduqColors.muted
    Box(
        Modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(width = 18.dp, height = 3.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (selected) NoduqColors.cyan else Color.Transparent),
            )
            Spacer(Modifier.height(6.dp))
            Text(label, color = color, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
fun PaymentsScreen(vm: AppViewModel) {
    val org = vm.workspace?.organization?.name
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Pagos", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text(
            "Aquí llega el aviso cuando confirmen el QR. NODUQ lee el mensaje tal cual llega, de los remitentes de Bancolombia.",
            color = NoduqColors.muted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Spacer(Modifier.height(12.dp))
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
                    Text("Todavía no hay avisos", color = NoduqColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
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

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Empleados", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text(
            "Cada uno entra con usuario y un código. El código solo se muestra una vez.",
            color = NoduqColors.muted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        PrimaryButton("Nuevo empleado", onClick = { createOpen = true })
        vm.error?.let { Banner(it) }
        vm.info?.let { Banner(it, "ok") }
        when {
            vm.employeesLoading -> Text("Cargando empleados…", color = NoduqColors.muted)
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
            onSave = { name, user, fields ->
                vm.saveEmployee(employee.id, name, user, fields)
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(employee.displayName, color = NoduqColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(employee.username, color = NoduqColors.muted, fontSize = 14.sp)
            }
            Text(
                if (employee.active) "Activo" else "Inactivo",
                color = if (employee.active) NoduqColors.cyan else NoduqColors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            QuietButton("Editar", enabled = !busy, onClick = onEdit)
            QuietButton("Nuevo código", enabled = !busy, onClick = onRegen)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GhostButton(
                text = if (employee.active) "Desactivar" else "Activar",
                onClick = onToggle,
                enabled = !busy,
                modifier = Modifier.weight(1f),
            )
            GhostButton(
                text = "Borrar",
                onClick = onDelete,
                enabled = !busy,
                danger = true,
                modifier = Modifier.weight(1f),
            )
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
    onSave: (String, String, (String?, String?, String?) -> Unit) -> Unit,
) {
    var name by rememberSaveable(employee.id) { mutableStateOf(employee.displayName) }
    var user by rememberSaveable(employee.id) { mutableStateOf(employee.username) }
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
                    onSave(name, user) { n, u, o ->
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
    AlertDialog(
        onDismissRequest = onClose,
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
                CodeBlock(created.code)
            }
        },
        confirmButton = { PrimaryButton("Copiar", onClick = onCopy) },
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
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = NoduqColors.raised,
        titleContentColor = NoduqColors.ink,
        textContentColor = NoduqColors.ink,
        title = { Text(title) },
        text = { Text(body, color = NoduqColors.muted, fontSize = 15.sp, lineHeight = 22.sp) },
        confirmButton = {
            if (danger) {
                GhostButton(if (busy) "Borrando…" else confirm, onClick = onConfirm, enabled = !busy, danger = true)
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

    LaunchedEffect(vm.workspace) {
        displayName = vm.workspace?.profile?.displayName.orEmpty()
        orgName = vm.workspace?.organization?.name.orEmpty()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Cuenta", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text("Tu nombre, la organización y el cierre de la cuenta.", color = NoduqColors.muted, fontSize = 16.sp)
        vm.error?.let { Banner(it) }
        vm.info?.let { Banner(it, "ok") }

        Text("Tu perfil", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = NoduqColors.ink)
        Text("Así te ven en este panel. El correo no se cambia aquí.", color = NoduqColors.muted)
        NoduqField(vm.ownerEmail.orEmpty(), {}, "Correo", enabled = false)
        NoduqField(displayName, { displayName = it }, "Tu nombre", enabled = !vm.busy)
        PrimaryButton("Guardar nombre", loading = vm.busy, onClick = { vm.saveProfile(displayName) })

        HorizontalDivider(color = NoduqColors.line)
        Text("Organización", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = NoduqColors.ink)
        Text("Este nombre hay que escribirlo para borrar la cuenta.", color = NoduqColors.muted)
        NoduqField(orgName, { orgName = it }, "Nombre", enabled = !vm.busy)
        PrimaryButton("Guardar", loading = vm.busy, onClick = { vm.saveOrganization(orgName) })

        HorizontalDivider(color = NoduqColors.line)
        Text("Sesión", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = NoduqColors.ink)
        Text("Sales de este teléfono. Las sesiones de empleados no se cierran.", color = NoduqColors.muted)
        GhostButton("Cerrar sesión", onClick = { vm.ownerSignOut() })

        HorizontalDivider(color = NoduqColors.line)
        Text("Borrar cuenta", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = NoduqColors.danger)
        Text(
            "Se borra la organización, los empleados y el acceso. Para confirmar, escribe el nombre exacto de la organización.",
            color = NoduqColors.muted,
        )
        GhostButton("Borrar cuenta", onClick = { deleteOpen = true }, danger = true)
    }

    if (deleteOpen) {
        AlertDialog(
            onDismissRequest = { deleteOpen = false },
            containerColor = NoduqColors.raised,
            titleContentColor = NoduqColors.ink,
            textContentColor = NoduqColors.ink,
            title = { Text("Borrar la cuenta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Escribe ${vm.workspace?.organization?.name.orEmpty()} para continuar. Esto no se puede deshacer.",
                        color = NoduqColors.muted,
                    )
                    NoduqField(
                        confirmation,
                        { confirmation = it },
                        "Organización",
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
