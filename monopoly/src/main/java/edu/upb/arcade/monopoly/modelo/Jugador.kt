package edu.upb.arcade.monopoly.modelo

/**
 * Igual que en la versión de consola: nombre, saldo y posición.
 * Es inmutable (data class) para que Compose detecte los cambios;
 * las propiedades de cada jugador se guardan en EstadoJuego.propietarios.
 */
data class Jugador(
    val nombre: String,
    val saldo: Int = SALDO_INICIAL,
    val posicion: Int = 0
) {
    companion object {
        const val SALDO_INICIAL = 1500
    }
}
