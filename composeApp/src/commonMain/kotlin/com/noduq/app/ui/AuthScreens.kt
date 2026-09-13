package com.noduq.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.Screen
import com.noduq.app.resources.Res
import com.noduq.app.resources.logo_nq_cian_noche
import com.noduq.app.theme.NoduqColors
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
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        ScreenColumn {
            Spacer(Modifier.height(12.dp))
            BrandMark()
            Spacer(Modifier.height(28.dp))
            Text(
                "Entrar",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            )
            Text(
                "Correo y contraseña, o usuario y código.",
                color = NoduqColors.muted,
                fontSize = 17.sp,
                lineHeight = 26.sp,
            )
            Spacer(Modifier.height(8.dp))
            SurfaceCard(highlighted = true, onClick = { vm.go(Screen.EmployeeLogin) }) {
                Kicker("Empleado")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Usuario y código",
                    color = NoduqColors.night,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.6).sp,
                )
                Text(
                    "Entras y esperas el aviso del pago.",
                    color = NoduqColors.night.copy(alpha = 0.78f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
            }
            SurfaceCard(onClick = { vm.go(Screen.OwnerLogin) }) {
                Kicker("Cuenta")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Correo y contraseña",
                    color = NoduqColors.ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                )
                Text(
                    "Pagos, empleados y cuenta.",
                    color = NoduqColors.muted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
            }
            GhostButton("Crear cuenta", onClick = { vm.go(Screen.OwnerRegister) })
            vm.error?.let { Banner(it) }
        }
    }
}

@Composable
fun OwnerLoginScreen(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AuthScaffold(
        title = "Entrar",
        lede = "Correo y contraseña. Si aún no tienes cuenta, créala aquí.",
        onBack = { vm.go(Screen.RoleGate) },
    ) {
        NoduqField(
            value = email,
            onValueChange = { email = it },
            label = "Correo",
            keyboardType = KeyboardType.Email,
            enabled = !vm.busy,
        )
        NoduqField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            password = true,
            imeAction = ImeAction.Done,
            enabled = !vm.busy,
            onIme = { vm.ownerSignIn(email, password) },
        )
        vm.error?.let { Banner(it) }
        PrimaryButton(
            text = if (vm.busy) "Entrando…" else "Entrar",
            loading = vm.busy,
            onClick = { vm.ownerSignIn(email, password) },
        )
        GhostButton("Crear cuenta", onClick = { vm.go(Screen.OwnerRegister) })
    }
}

@Composable
fun OwnerRegisterScreen(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    AuthScaffold(
        title = "Crear cuenta",
        lede = "Correo y contraseña. Después el nombre del comercio.",
        onBack = { vm.go(Screen.OwnerLogin) },
    ) {
        NoduqField(email, { email = it }, "Correo", keyboardType = KeyboardType.Email, enabled = !vm.busy)
        NoduqField(
            password,
            { password = it },
            "Contraseña",
            password = true,
            hint = "Mínimo 6 caracteres.",
            enabled = !vm.busy,
        )
        NoduqField(
            confirm,
            { confirm = it },
            "Repite la contraseña",
            password = true,
            imeAction = ImeAction.Done,
            enabled = !vm.busy,
            onIme = { vm.ownerSignUp(email, password, confirm) },
        )
        vm.error?.let { Banner(it) }
        vm.info?.let { Banner(it, tone = "ok") }
        PrimaryButton(
            text = if (vm.busy) "Creando…" else "Crear cuenta",
            loading = vm.busy,
            onClick = { vm.ownerSignUp(email, password, confirm) },
        )
        QuietButton("Ya tengo cuenta", onClick = { vm.go(Screen.OwnerLogin) })
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
            text = if (vm.busy) "Guardando…" else "Abrir el panel",
            loading = vm.busy,
            onClick = { vm.bootstrap(org, name) },
        )
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    lede: String,
    onBack: () -> Unit,
    kicker: String? = null,
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
            BrandMark()
            QuietButton("Volver", onClick = onBack)
            if (kicker != null) Kicker(kicker)
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
            Text(lede, color = NoduqColors.muted, fontSize = 16.sp, lineHeight = 24.sp)
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
        }
    }
}
