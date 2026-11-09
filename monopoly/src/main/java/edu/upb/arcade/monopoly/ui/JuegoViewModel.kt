package edu.upb.arcade.monopoly.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import edu.upb.arcade.monopoly.modelo.EstadoJuego
import edu.upb.arcade.monopoly.modelo.Juego

/** Guarda el estado de la partida (sobrevive a rotaciones) y conecta la UI con las reglas. */
class JuegoViewModel : ViewModel() {

    val juego = Juego()

    /** null = todavía estamos en la pantalla de inicio. */
    var estado by mutableStateOf<EstadoJuego?>(null)
        private set

    /** Resultado de enviar el score al hub: null = sin sesión. */
    var scoreGuardado by mutableStateOf<Boolean?>(null)
        private set
    private var scoreEnviado = false

    fun empezar(nombres: List<String>) {
        estado = juego.nuevaPartida(nombres)
        scoreEnviado = false
        scoreGuardado = null
    }

    /** Score de Arcade UPB: saldo final del jugador 1. */
    fun puntosJugador1(): Int = estado?.jugadores?.firstOrNull()?.saldo?.coerceAtLeast(0) ?: 0

    /** Se llama una sola vez por partida, al terminar. */
    fun enviarScore(enviar: (Int) -> Boolean?) {
        if (scoreEnviado) return
        scoreEnviado = true
        scoreGuardado = enviar(puntosJugador1())
    }

    fun lanzarDado() = actualizar { juego.lanzarDado(it) }

    fun soltarFicha(casilla: Int?) = actualizar { juego.moverFicha(it, casilla) }

    fun decidirCompra(comprar: Boolean) = actualizar { juego.decidirCompra(it, comprar) }

    fun volverAlInicio() {
        estado = null
        scoreEnviado = false
        scoreGuardado = null
    }

    private fun actualizar(paso: (EstadoJuego) -> EstadoJuego) {
        estado = estado?.let(paso)
    }
}
