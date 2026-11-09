package edu.upb.arcade.monopoly.modelo

/** En qué momento del turno está el jugador actual. */
sealed interface Fase {
    /** Hay que tocar el dado. */
    data object TirarDado : Fase

    /** Ya salió el dado: hay que arrastrar la ficha hasta [destino]. */
    data class MoverFicha(val destino: Int) : Fase

    /** Cayó en una propiedad libre: hay que decidir si comprarla. */
    data class DecidirCompra(val casilla: Int) : Fase

    /** Se jugaron todas las rondas. */
    data class Terminado(val ganador: Int) : Fase
}

data class EstadoJuego(
    val jugadores: List<Jugador>,
    val turno: Int = 0,
    val ronda: Int = 1,
    val fase: Fase = Fase.TirarDado,
    val dado: Int? = null,
    /** casilla -> índice del jugador dueño */
    val propietarios: Map<Int, Int> = emptyMap(),
    /** Mensajes de lo que va pasando (lo que antes salía por println). */
    val bitacora: List<String> = emptyList(),
    /** Aviso corto para errores de interacción (p. ej. soltar la ficha en otra casilla). */
    val aviso: String? = null
) {
    val jugadorActual: Jugador get() = jugadores[turno]

    fun propiedadesDe(indiceJugador: Int): List<Int> =
        propietarios.filterValues { it == indiceJugador }.keys.sorted()

    fun conMensaje(mensaje: String): EstadoJuego =
        copy(bitacora = (bitacora + mensaje).takeLast(MAX_MENSAJES))

    fun actualizarJugador(indice: Int, cambio: (Jugador) -> Jugador): EstadoJuego =
        copy(jugadores = jugadores.mapIndexed { i, j -> if (i == indice) cambio(j) else j })

    companion object {
        const val MAX_MENSAJES = 60
    }
}
