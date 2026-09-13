package com.noduq.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noduq.app.theme.NoduqTheme
import com.noduq.app.ui.NoduqRoot

@Composable
fun App() {
    val vm = viewModel {
        AppViewModel(AppGraph.tokens, AppGraph.api, AppGraph.supabase, AppGraph.googleAuth)
    }
    LaunchedEffect(Unit) { vm.start() }
    NoduqTheme {
        NoduqRoot(vm)
    }
}
