package edu.upb.arcade.triqui.logica

import kotlin.random.Random

enum class Ficha { X, O }

sealed interface Resultado {
    data class Gana(val ficha: Ficha, val linea: List<Int>) : Resultado
    data object Empate : Resultado
}

object Triqui {
    /** Las 8 formas de ganar (índices 0..8 del tablero). */
    val LINEAS = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // filas
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columnas
        listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonales
    )

    fun resultado(tablero: List<Ficha?>): Resultado? {
        for (l in LINEAS) {
            val f = tablero[l[0]]
            if (f != null && tablero[l[1]] == f && tablero[l[2]] == f) return Resultado.Gana(f, l)
        }
        return if (tablero.all { it != null }) Resultado.Empate else null
    }

    /**
     * Jugada de la CPU (juega con O): gana si puede, si no bloquea, si no centro/esquinas.
     * Con probabilidad [descuido] juega al azar, para que se le pueda ganar.
     */
    fun jugadaCpu(tablero: List<Ficha?>, random: Random = Random.Default, descuido: Double = 0.25): Int? {
        val libres = tablero.indices.filter { tablero[it] == null }
        if (libres.isEmpty()) return null
        if (random.nextDouble() < descuido) return libres.random(random)

        fun completa(ficha: Ficha): Int? = libres.firstOrNull { i ->
            resultado(tablero.toMutableList().also { it[i] = ficha }) is Resultado.Gana
        }
        return completa(Ficha.O)
            ?: completa(Ficha.X)
            ?: 4.takeIf { it in libres }
            ?: listOf(0, 2, 6, 8).filter { it in libres }.randomOrNull(random)
            ?: libres.random(random)
    }

    /** Puntos que suma cada ronda a la racha. */
    fun puntosRonda(r: Resultado): Int = when (r) {
        is Resultado.Gana -> if (r.ficha == Ficha.X) 100 else 0
        Resultado.Empate -> 30
    }
}
