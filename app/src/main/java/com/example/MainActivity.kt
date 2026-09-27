package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen(
                    viewModel = viewModel,
                    onWindowFlagChange = { enableSecure ->
                        updateWindowSecureFlag(enableSecure)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh MDM status when returning from device settings or admin prompt
        viewModel.refreshSecurityState()
    }

    private fun updateWindowSecureFlag(enableSecure: Boolean) {
        runOnUiThread {
            if (enableSecure) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}
