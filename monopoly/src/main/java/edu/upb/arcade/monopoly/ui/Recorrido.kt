package edu.upb.arcade.monopoly.ui

/**
 * Ubica las 20 casillas en el borde de una cuadrícula de 6x6
 * (6 arriba + 5 a la derecha + 5 abajo + 4 a la izquierda = 20),
 * avanzando en el sentido de las agujas del reloj desde la esquina superior izquierda.
 */
object Recorrido {
    const val LADO = 6
    const val TOTAL = 4 * LADO - 4 // 20

    /** Devuelve (fila, columna) de la casilla [indice]. */
    fun celda(indice: Int): Pair<Int, Int> = when (indice) {
        in 0..5 -> 0 to indice              // fila de arriba, izq -> der
        in 6..10 -> (indice - 5) to 5       // columna derecha, arriba -> abajo
        in 11..15 -> 5 to (15 - indice)     // fila de abajo, der -> izq
        else -> (20 - indice) to 0          // columna izquierda, abajo -> arriba
    }

    /** Qué casilla hay en (fila, columna); null si es el centro o está fuera. */
    fun indiceEn(fila: Int, columna: Int): Int? =
        (0 until TOTAL).firstOrNull { celda(it) == (fila to columna) }

    /** Qué casilla hay bajo el punto (x, y) en píxeles, dado el tamaño de cada celda. */
    fun indiceEnPunto(x: Float, y: Float, tamCelda: Float): Int? {
        if (x < 0f || y < 0f) return null
        val columna = (x / tamCelda).toInt()
        val fila = (y / tamCelda).toInt()
        if (columna >= LADO || fila >= LADO) return null
        return indiceEn(fila, columna)
    }
}
