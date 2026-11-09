package edu.upb.arcade.datos

import android.content.Context
import androidx.core.content.edit

/**
 * Sesión guardada en el teléfono. Mientras exista, los juegos guardan
 * los scores a nombre de este usuario (aunque el hub esté cerrado).
 */
class Sesion(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("arcade_sesion", Context.MODE_PRIVATE)

    fun usuarioId(): Long? = prefs.getLong(CLAVE_USUARIO, -1L).takeIf { it != -1L }

    fun iniciar(usuarioId: Long) = prefs.edit { putLong(CLAVE_USUARIO, usuarioId) }

    fun cerrar() = prefs.edit { remove(CLAVE_USUARIO) }

    /** Usuario que activó el ingreso con huella en este teléfono (solo uno a la vez). */
    fun usuarioHuella(): Long? = prefs.getLong(CLAVE_HUELLA, -1L).takeIf { it != -1L }

    fun activarHuella(usuarioId: Long) = prefs.edit { putLong(CLAVE_HUELLA, usuarioId) }

    fun desactivarHuella() = prefs.edit { remove(CLAVE_HUELLA) }

    private companion object {
        const val CLAVE_USUARIO = "usuario_id"
        const val CLAVE_HUELLA = "huella_usuario_id"
    }
}
