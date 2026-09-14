package com.noduq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noduq.app.AppViewModel
import com.noduq.app.BackNavigation
import com.noduq.app.Screen
import com.noduq.app.theme.NoduqColors

@Composable
fun NoduqRoot(vm: AppViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .background(NoduqColors.night),
    ) {
        when (val screen = vm.screen) {
            Screen.Boot -> BootScreen()
            Screen.RoleGate -> RoleGateScreen(vm)
            Screen.OwnerLogin -> OwnerLoginScreen(vm)
            Screen.OwnerRegister -> OwnerRegisterScreen(vm)
            Screen.OwnerSetup -> OwnerSetupScreen(vm)
            Screen.OwnerPlan -> OwnerPlanScreen(vm)
            Screen.OwnerPermissions -> OwnerPermissionsScreen(vm)
            Screen.OwnerForgotPassword -> OwnerForgotPasswordScreen(vm)
            is Screen.OwnerHome -> OwnerShell(vm, screen.tab)
            Screen.EmployeeLogin -> EmployeeLoginScreen(vm)
            Screen.EmployeeWait -> WaitingRoomScreen(vm)
        }
        val canBack = vm.screen is Screen.OwnerLogin ||
            vm.screen is Screen.OwnerRegister ||
            vm.screen is Screen.OwnerForgotPassword ||
            vm.screen is Screen.EmployeeLogin
        BackNavigation(enabled = canBack) { vm.back() }
    }
}
