package edu.upb.arcade.servicio

import android.app.Service
import android.content.Intent
import android.os.IBinder
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.IArcadeService
import edu.upb.arcade.datos.Repositorio
import edu.upb.arcade.datos.Sesion
import edu.upb.arcade.datos.Usuario
import edu.upb.arcade.ubicacion.Ubicador

/**
 * Servicio vinculado (bound service) que usan los 4 juegos para guardar sus scores.
 * Está protegido con un permiso de firma: solo las apps de este proyecto pueden usarlo.
 */
class ArcadeService : Service() {

    private val repo by lazy { Repositorio.obtener(this) }
    private val sesion by lazy { Sesion(this) }
    private val ubicador by lazy { Ubicador(this) }

    private val binder = object : IArcadeService.Stub() {

        override fun getNickname(): String? = usuarioActual()?.nickname

        override fun getMejorScore(juego: String?): Int {
            val usuario = usuarioActual() ?: return 0
            val id = juego?.let { JuegoArcade.porId(it) } ?: return 0
            return repo.mejorScore(usuario.id, id.id)
        }

        override fun guardarScore(juego: String?, puntos: Int): Boolean {
            val usuario = usuarioActual() ?: return false
            val id = juego?.let { JuegoArcade.porId(it) } ?: return false

            // 1) Se guarda ya, con la última ciudad conocida
            val scoreId = repo.insertarScore(
                usuario.id, id.id, puntos.coerceAtLeast(0),
                ubicador.ultimaCiudad() ?: CIUDAD_PENDIENTE
            )
            // 2) Se pide una lectura nueva del GPS y se actualiza la ciudad de ese score
            ubicador.obtenerCiudad { ciudad ->
                val ciudadFinal = ciudad ?: ubicador.ultimaCiudad() ?: SIN_UBICACION
                repo.actualizarCiudad(scoreId, ciudadFinal)
            }
            return true
        }
    }

    private fun usuarioActual(): Usuario? = sesion.usuarioId()?.let { repo.usuarioPorId(it) }

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        const val CIUDAD_PENDIENTE = "Ubicando…"
        const val SIN_UBICACION = "Sin ubicación"
    }
}
