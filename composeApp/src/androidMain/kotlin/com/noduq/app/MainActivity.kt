package com.noduq.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        googleAuth()?.attach(this)
        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        googleAuth()?.onIntent(intent)
    }

    override fun onDestroy() {
        googleAuth()?.detach(this)
        super.onDestroy()
    }

    private fun googleAuth(): AndroidGoogleAuth? = AppGraph.googleAuth as? AndroidGoogleAuth
}
