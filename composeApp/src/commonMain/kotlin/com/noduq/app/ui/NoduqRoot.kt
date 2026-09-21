package com.noduq.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noduq.app.AppViewModel
import com.noduq.app.BackNavigation
import com.noduq.app.Screen
import com.noduq.app.motionEnabled
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion

@Composable
fun NoduqRoot(vm: AppViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .background(NoduqColors.night),
    ) {
        AnimatedContent(
            targetState = vm.screen,
            modifier = Modifier.fillMaxSize(),
            contentKey = { it.frameKey() },
            transitionSpec = {
                if (!motionEnabled()) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    screenTransition(initialState, targetState)
                }
            },
            label = "screen",
        ) { screen ->
            when (screen) {
                Screen.Boot -> BootScreen()
                Screen.RoleGate -> RoleGateScreen(vm)
                Screen.OwnerLogin -> OwnerLoginScreen(vm)
                Screen.OwnerRegister -> OwnerRegisterScreen(vm)
                Screen.OwnerSetup -> OwnerSetupScreen(vm)
                Screen.OwnerPlan -> OwnerPlanScreen(vm)
                Screen.OwnerPermissions -> OwnerPermissionsScreen(vm)
                Screen.OwnerForgotPassword -> OwnerForgotPasswordScreen(vm)
                is Screen.OwnerConfirmSent -> OwnerConfirmSentScreen(vm, screen.email)
                is Screen.OwnerResetSent -> OwnerResetSentScreen(vm, screen.email)
                is Screen.OwnerOnboard -> OwnerOnboardScreen(vm, screen.step)
                is Screen.OwnerHome -> OwnerShell(vm, screen.tab)
                Screen.EmployeeLogin -> EmployeeLoginScreen(vm)
                Screen.EmployeeWait -> WaitingRoomScreen(vm)
            }
        }
        val canBack = vm.screen is Screen.OwnerLogin ||
            vm.screen is Screen.OwnerRegister ||
            vm.screen is Screen.OwnerConfirmSent ||
            vm.screen is Screen.OwnerForgotPassword ||
            vm.screen is Screen.OwnerResetSent ||
            vm.screen is Screen.EmployeeLogin ||
            (vm.screen is Screen.OwnerOnboard && (vm.screen as Screen.OwnerOnboard).step < 6)
        BackNavigation(enabled = canBack) { vm.back() }
    }
}

private fun screenTransition(
    from: Screen,
    to: Screen,
) = if (from is Screen.Boot || to is Screen.Boot) {
    fadeIn(tween(NoduqMotion.fadeMs, easing = NoduqMotion.easeOut)) togetherWith
        fadeOut(tween(NoduqMotion.fadeMs, easing = NoduqMotion.easeOut))
} else {
    val forward = to.depth() >= from.depth()
    val enterX = { width: Int -> if (forward) width / 8 else -width / 8 }
    val exitX = { width: Int -> if (forward) -width / 14 else width / 14 }
    (
        slideInHorizontally(tween(NoduqMotion.screenMs, easing = NoduqMotion.easeOut), enterX) +
            fadeIn(tween(NoduqMotion.fadeMs, easing = NoduqMotion.easeOut))
        ) togetherWith (
        slideOutHorizontally(tween(NoduqMotion.fadeMs, easing = NoduqMotion.easeOut), exitX) +
            fadeOut(tween(NoduqMotion.fadeMs, easing = NoduqMotion.easeOut))
        )
}

private fun Screen.depth(): Int = when (this) {
    Screen.Boot -> 0
    Screen.RoleGate -> 1
    Screen.OwnerLogin, Screen.EmployeeLogin -> 2
    Screen.OwnerRegister, Screen.OwnerForgotPassword -> 3
    is Screen.OwnerConfirmSent, is Screen.OwnerResetSent -> 4
    Screen.OwnerSetup -> 4
    is Screen.OwnerOnboard -> 4 + step
    Screen.OwnerPlan -> 12
    Screen.OwnerPermissions -> 6
    is Screen.OwnerHome, Screen.EmployeeWait -> 7
}

private fun Screen.frameKey(): String = when (this) {
    is Screen.OwnerHome -> "OwnerHome"
    is Screen.OwnerOnboard -> "OwnerOnboard"
    else -> this::class.simpleName ?: "Screen"
}
