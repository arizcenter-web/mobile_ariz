package com.tecsup.app_ariz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.session.SessionState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.navigation.ArizApp
import com.tecsup.app_ariz.navigation.ArizSplash
import com.tecsup.app_ariz.navigation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // El splash del sistema (fondo blanco con el logo) dura un instante; la bienvenida animada la dibuja ArizSplash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val coldStart = savedInstanceState == null
        setContent {
            ArizTheme {
                var showSplash by rememberSaveable { mutableStateOf(coldStart) }
                val session by viewModel.sessionState.collectAsStateWithLifecycle()
                Box(Modifier.fillMaxSize()) {
                    // La app se arma debajo mientras la bienvenida está arriba, así al terminar ya está lista.
                    ArizApp(viewModel = viewModel)
                    AnimatedVisibility(visible = showSplash, exit = fadeOut(tween(450))) {
                        ArizSplash(ready = session !is SessionState.Restoring, onFinished = { showSplash = false })
                    }
                }
            }
        }
    }
}
