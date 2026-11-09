package edu.upb.arcade.monopoly.modelo

import kotlin.random.Random

/**
 * Misma idea que en consola: cada tipo de casilla decide qué pasa cuando
 * un jugador cae en ella (polimorfismo con alCaer). En lugar de leer de
 * la consola, recibe el estado del juego y devuelve el estado nuevo.
 */
sealed interface Casilla {
    val nombre: String
    fun alCaer(estado: EstadoJuego, random: Random): EstadoJuego
}

class CasillaPropiedad(
    val indice: Int,
    override val nombre: String,
    val precio: Int,
    val alquiler: Int
) : Casilla {

    override fun alCaer(estado: EstadoJuego, random: Random): EstadoJuego {
        val jugador = estado.jugadorActual
        val dueno = estado.propietarios[indice]
        return when {
            // Libre: se le pregunta al jugador con un diálogo (antes era readLine)
            dueno == null -> estado
                .conMensaje("${jugador.nombre} cae en '$nombre' (precio: $$precio).")
                .copy(fase = Fase.DecidirCompra(indice))

            dueno == estado.turno -> estado
                .conMensaje("${jugador.nombre} cae en su propia propiedad '$nombre'.")

            else -> {
                val nombreDueno = estado.jugadores[dueno].nombre
                estado
                    .actualizarJugador(estado.turno) { it.copy(saldo = it.saldo - alquiler) }
                    .actualizarJugador(dueno) { it.copy(saldo = it.saldo + alquiler) }
                    .conMensaje("${jugador.nombre} cae en '$nombre' (de $nombreDueno). Paga $$alquiler de alquiler.")
            }
        }
    }
}

class CasillaSuerte(override val nombre: String = "Suerte") : Casilla {
    override fun alCaer(estado: EstadoJuego, random: Random): EstadoJuego {
        val jugador = estado.jugadorActual
        val monto = random.nextInt(50, 201) // 50 a 200, ambos incluidos
        return if (random.nextBoolean()) {
            estado.actualizarJugador(estado.turno) { it.copy(saldo = it.saldo + monto) }
                .conMensaje("${jugador.nombre} cae en '$nombre' y gana $$monto.")
        } else {
            estado.actualizarJugador(estado.turno) { it.copy(saldo = it.saldo - monto) }
                .conMensaje("${jugador.nombre} cae en '$nombre' y pierde $$monto.")
        }
    }
}

class CasillaSalida(override val nombre: String = "Salida") : Casilla {
    override fun alCaer(estado: EstadoJuego, random: Random): EstadoJuego =
        estado.conMensaje("${estado.jugadorActual.nombre} cae en '$nombre'.")
}
