package edu.upb.arcade.monopoly.modelo

/** Mismo tablero que en consola: 20 casillas, Suerte cada 5, el resto propiedades. */
class Tablero(val tamano: Int = 20) {
    val casillas: List<Casilla> = buildList {
        add(CasillaSalida())
        for (i in 1 until tamano) {
            if (i % 5 == 0) {
                add(CasillaSuerte())
            } else {
                val precio = 100 + (i * 15)
                add(CasillaPropiedad(i, "Propiedad $i", precio, precio / 10))
            }
        }
    }
}
