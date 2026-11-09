package edu.upb.arcade.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Lector de huella del teléfono (todo es local: la huella nunca sale del dispositivo). */
object Huella {

    enum class Estado {
        DISPONIBLE,
        SIN_HUELLA_REGISTRADA, // El sensor existe pero falta configurar huella en Ajustes de Android
        NO_SOPORTADO
    }

    fun estado(context: Context): Estado =
        when (BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Estado.DISPONIBLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Estado.SIN_HUELLA_REGISTRADA
            else -> Estado.NO_SOPORTADO
        }

    fun disponible(context: Context): Boolean =
        estado(context) == Estado.DISPONIBLE

    fun pedir(
        activity: FragmentActivity,
        titulo: String,
        subtitulo: String,
        onExito: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onExito()

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val cancelado = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    if (!cancelado) onError(errString.toString())
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setNegativeButtonText("Usar contraseña")
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }
}
