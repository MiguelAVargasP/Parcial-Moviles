package edu.upb.arcade.monopoly.modelo

import kotlin.random.Random

/**
 * Las reglas del Juego.kt de consola, divididas en pasos que dispara la
 * interfaz táctil: lanzar el dado -> arrastrar la ficha -> (comprar o no).
 * Cada función recibe un estado y devuelve el siguiente; no guarda nada.
 */
class Juego(
    val tablero: Tablero = Tablero(),
    private val random: Random = Random.Default,
    val rondasTotales: Int = 10
) {

    fun nuevaPartida(nombres: List<String>): EstadoJuego {
        require(nombres.size in MIN_JUGADORES..MAX_JUGADORES) {
            "Se necesitan entre $MIN_JUGADORES y $MAX_JUGADORES jugadores"
        }
        val jugadores = nombres.mapIndexed { i, n ->
            Jugador(nombre = n.trim().ifEmpty { "Jugador ${i + 1}" })
        }
        return EstadoJuego(jugadores = jugadores)
            .conMensaje("Empieza la partida. Turno de ${jugadores[0].nombre}.")
    }

    fun lanzarDado(estado: EstadoJuego): EstadoJuego {
        if (estado.fase != Fase.TirarDado) return estado
        val dado = random.nextInt(1, 7)
        val destino = (estado.jugadorActual.posicion + dado) % tablero.tamano
        return estado
            .copy(dado = dado, fase = Fase.MoverFicha(destino), aviso = null)
            .conMensaje("${estado.jugadorActual.nombre} obtiene $dado. Arrastra tu ficha a la casilla $destino.")
    }

    /**
     * Se llama cuando el jugador suelta su ficha sobre [casillaSoltada]
     * (null si la soltó fuera del recorrido). Solo avanza si es la casilla correcta.
     */
    fun moverFicha(estado: EstadoJuego, casillaSoltada: Int?): EstadoJuego {
        val fase = estado.fase as? Fase.MoverFicha ?: return estado
        if (casillaSoltada != fase.destino) {
            return estado.copy(aviso = "Esa no es: debes llevar la ficha a la casilla ${fase.destino}.")
        }

        val dado = estado.dado ?: 0
        val posAnterior = estado.jugadorActual.posicion
        var nuevo = estado
            .copy(aviso = null)
            .actualizarJugador(estado.turno) { it.copy(posicion = fase.destino) }

        if (posAnterior + dado >= tablero.tamano) {
            nuevo = nuevo
                .actualizarJugador(estado.turno) { it.copy(saldo = it.saldo + BONO_SALIDA) }
                .conMensaje("${estado.jugadorActual.nombre} pasa por la Salida y recibe $$BONO_SALIDA.")
        }

        nuevo = tablero.casillas[fase.destino].alCaer(nuevo, random)
        return if (nuevo.fase is Fase.DecidirCompra) nuevo else terminarTurno(nuevo)
    }

    fun decidirCompra(estado: EstadoJuego, comprar: Boolean): EstadoJuego {
        val fase = estado.fase as? Fase.DecidirCompra ?: return estado
        val propiedad = tablero.casillas[fase.casilla] as CasillaPropiedad
        val jugador = estado.jugadorActual

        val nuevo = when {
            comprar && jugador.saldo >= propiedad.precio -> estado
                .actualizarJugador(estado.turno) { it.copy(saldo = it.saldo - propiedad.precio) }
                .copy(propietarios = estado.propietarios + (fase.casilla to estado.turno))
                .conMensaje("${jugador.nombre} compró '${propiedad.nombre}'.")

            comprar -> estado
                .conMensaje("${jugador.nombre} no tiene saldo suficiente para '${propiedad.nombre}'.")

            else -> estado
                .conMensaje("${jugador.nombre} no compró '${propiedad.nombre}'.")
        }
        return terminarTurno(nuevo)
    }

    private fun terminarTurno(estado: EstadoJuego): EstadoJuego {
        val siguiente = estado.turno + 1
        if (siguiente < estado.jugadores.size) {
            return estado
                .copy(turno = siguiente, fase = Fase.TirarDado)
                .conMensaje("Turno de ${estado.jugadores[siguiente].nombre}.")
        }

        // Se completó la ronda
        val conResumen = estado.conMensaje("— Fin de la ronda ${estado.ronda} —")
        if (estado.ronda >= rondasTotales) {
            val ganador = estado.jugadores.indices.maxBy { estado.jugadores[it].saldo }
            val g = estado.jugadores[ganador]
            return conResumen
                .copy(fase = Fase.Terminado(ganador))
                .conMensaje("¡Ganador: ${g.nombre} con $${g.saldo}!")
        }
        return conResumen
            .copy(turno = 0, ronda = estado.ronda + 1, fase = Fase.TirarDado)
            .conMensaje("Ronda ${estado.ronda + 1}. Turno de ${estado.jugadores[0].nombre}.")
    }

    companion object {
        const val MIN_JUGADORES = 2
        const val MAX_JUGADORES = 4
        const val BONO_SALIDA = 200
    }
}
