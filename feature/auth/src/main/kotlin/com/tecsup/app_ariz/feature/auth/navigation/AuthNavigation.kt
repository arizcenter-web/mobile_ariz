package com.tecsup.app_ariz.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.tecsup.app_ariz.feature.auth.BuildConfig
import com.tecsup.app_ariz.feature.auth.ui.forgotpassword.ForgotPasswordScreen
import com.tecsup.app_ariz.feature.auth.ui.login.LoginScreen
import com.tecsup.app_ariz.feature.auth.ui.register.RegisterScreen
import com.tecsup.app_ariz.feature.auth.ui.registerprofessional.RegisterProfessionalScreen
import com.tecsup.app_ariz.feature.auth.ui.resetpassword.ResetPasswordScreen
import kotlinx.serialization.Serializable

/** Grafo completo de autenticación. Es lo único que otros módulos (`:app`) necesitan conocer. */
@Serializable
data object AuthRoute

@Serializable
internal data object LoginRoute

@Serializable
internal data object RegisterRoute

@Serializable
internal data object RegisterProfessionalRoute

@Serializable
internal data object ForgotPasswordRoute

/** `token` es el argumento del enlace `https://<web>/reset-password?token=...` del correo. */
@Serializable
internal data class ResetPasswordRoute(val token: String)

private val resetPasswordLink = "https://${BuildConfig.WEB_HOST}/reset-password"

/**
 * Registra login, registro (cliente y profesional), recuperación y restablecimiento de contraseña.
 *
 * @param onAuthenticated se invoca al iniciar sesión o registrarse con éxito; quien llama decide
 * a dónde ir (normalmente cerrar el grafo y volver a la pantalla anterior).
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    onAuthenticated: () -> Unit,
) {
    navigation<AuthRoute>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onAuthenticated = onAuthenticated,
                onRegisterClick = { navController.navigate(RegisterRoute) },
                onForgotPasswordClick = { navController.navigate(ForgotPasswordRoute) },
                // Salir del login = cerrar todo el grafo y volver a donde estaba el usuario.
                onBackClick = { navController.popBackStack<AuthRoute>(inclusive = true) },
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegistered = onAuthenticated,
                onLoginClick = { navController.popBackStack() },
                onProfessionalClick = { navController.navigate(RegisterProfessionalRoute) },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<RegisterProfessionalRoute> {
            RegisterProfessionalScreen(
                onRegistered = onAuthenticated,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
        }
        composable<ResetPasswordRoute>(
            deepLinks = listOf(navDeepLink<ResetPasswordRoute>(basePath = resetPasswordLink)),
        ) {
            ResetPasswordScreen(
                onDoneClick = {
                    // Con el enlace del correo el login queda debajo; si no, se navega hacia él.
                    if (!navController.popBackStack<LoginRoute>(inclusive = false)) {
                        navController.navigate(LoginRoute)
                    }
                },
            )
        }
    }
}
