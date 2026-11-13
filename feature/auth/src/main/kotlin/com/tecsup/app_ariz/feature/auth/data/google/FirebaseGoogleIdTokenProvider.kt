package com.tecsup.app_ariz.feature.auth.data.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.repository.GoogleIdTokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Flujo: selector de cuentas (Credential Manager) -> credencial de Google -> Firebase Auth ->
 * ID token de Firebase, que es lo que acepta `POST /auth/google`.
 *
 * Requiere `app/google-services.json` (el plugin genera `default_web_client_id`). Sin él,
 * [isConfigured] es `false` y la UI no muestra el botón.
 */
class FirebaseGoogleIdTokenProvider @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : GoogleIdTokenProvider {

    // El recurso lo genera el plugin google-services en :app; una librería lo busca por nombre.
    private val webClientId: String? by lazy {
        val id = appContext.resources.getIdentifier("default_web_client_id", "string", appContext.packageName)
        if (id == 0) null else appContext.getString(id)
    }

    override val isConfigured: Boolean
        get() = webClientId != null && FirebaseApp.getApps(appContext).isNotEmpty()

    override suspend fun requestFirebaseIdToken(activityContext: Context): AppResult<String> {
        val clientId = webClientId
        if (clientId == null || !isConfigured) {
            return AppResult.Failure(AppError.Unknown("El inicio con Google no está configurado"))
        }
        return try {
            AppResult.Success(fetchFirebaseIdToken(activityContext, clientId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            AppResult.Failure(AppError.Cancelled)
        } catch (e: NoCredentialException) {
            AppResult.Failure(AppError.Unknown("No hay una cuenta de Google disponible en este dispositivo"))
        } catch (e: GetCredentialException) {
            AppResult.Failure(AppError.Unknown("No se pudo iniciar sesión con Google"))
        } catch (e: GoogleIdTokenParsingException) {
            AppResult.Failure(AppError.Unknown("Google devolvió una credencial inválida"))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Network("No se pudo completar el inicio con Google"))
        }
    }

    private suspend fun fetchFirebaseIdToken(activityContext: Context, clientId: String): String {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(activityContext)
            .getCredential(activityContext, request)
            .credential

        require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Tipo de credencial inesperado"
        }
        val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

        val auth = FirebaseAuth.getInstance()
        val firebaseUser = auth.signInWithCredential(GoogleAuthProvider.getCredential(googleToken, null))
            .await()
            .user
        val firebaseToken = checkNotNull(firebaseUser?.getIdToken(false)?.await()?.token) {
            "Firebase no devolvió un token"
        }
        // La sesión real es la cookie del backend; no se deja una sesión de Firebase duplicada.
        auth.signOut()
        return firebaseToken
    }
}
