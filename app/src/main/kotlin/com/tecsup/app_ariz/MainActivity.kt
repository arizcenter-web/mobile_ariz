package com.tecsup.app_ariz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.navigation.ArizApp
import com.tecsup.app_ariz.navigation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // El splash se mantiene mientras se valida la sesión guardada (GET /auth/me).
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.sessionState.value is SessionState.Restoring
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArizTheme {
                ArizApp(viewModel = viewModel)
            }
        }
    }
}
