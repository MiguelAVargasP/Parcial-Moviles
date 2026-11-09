package edu.upb.arcade.tetris.logica

import kotlin.random.Random

/** Las 7 piezas, dibujadas en su posición horizontal (como salen). */
enum class TipoPieza(val tamano: Int, val celdas: List<Pair<Int, Int>>) {
    I(4, listOf(0 to 1, 1 to 1, 2 to 1, 3 to 1)),
    O(2, listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1)),
    T(3, listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1)),
    S(3, listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1)),
    Z(3, listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1)),
    J(3, listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1)),
    L(3, listOf(2 to 0, 0 to 1, 1 to 1, 2 to 1));
}

/**
 * Pieza en el tablero. Solo tiene dos posiciones: horizontal (como sale)
 * o vertical (girada 90°). Cada tap alterna entre las dos.
 */
data class Pieza(val tipo: TipoPieza, val x: Int, val y: Int, val vertical: Boolean = false) {
    fun celdas(): List<Pair<Int, Int>> = tipo.celdas.map { (cx, cy) ->
        val (rx, ry) = if (vertical) (tipo.tamano - 1 - cy) to cx else cx to cy
        (x + rx) to (y + ry)
    }
}

data class EstadoTetris(
    val ancho: Int = 10,
    val alto: Int = 20,
    /** Celdas fijas: índice = fila * ancho + columna. null = vacía. */
    val fijas: List<TipoPieza?> = List(ancho * alto) { null },
    val actual: Pieza,
    val siguiente: TipoPieza,
    val puntos: Int = 0,
    val lineas: Int = 0,
    val terminado: Boolean = false
) {
    val nivel: Int get() = 1 + lineas / 10

    fun ocupada(col: Int, fila: Int): Boolean =
        col !in 0 until ancho || fila >= alto || (fila >= 0 && fijas[fila * ancho + col] != null)

    fun cabe(p: Pieza): Boolean = p.celdas().none { (c, f) -> ocupada(c, f) }

    /** Dónde caería la pieza actual (para dibujar la "sombra"). */
    fun sombra(): Pieza {
        var p = actual
        while (cabe(p.copy(y = p.y + 1))) p = p.copy(y = p.y + 1)
        return p
    }
}

class Tetris(private val random: Random = Random.Default) {

    fun nuevaPartida(): EstadoTetris {
        val primero = sortear()
        return EstadoTetris(actual = aparecer(primero, 10), siguiente = sortear())
    }

    fun mover(e: EstadoTetris, dx: Int): EstadoTetris {
        if (e.terminado) return e
        val p = e.actual.copy(x = e.actual.x + dx)
        return if (e.cabe(p)) e.copy(actual = p) else e
    }

    /** Alterna horizontal/vertical. Si choca, prueba corriéndola un poco a los lados. */
    fun girar(e: EstadoTetris): EstadoTetris {
        if (e.terminado || e.actual.tipo == TipoPieza.O) return e
        val girada = e.actual.copy(vertical = !e.actual.vertical)
        for (dx in listOf(0, -1, 1, -2, 2)) {
            val p = girada.copy(x = girada.x + dx)
            if (e.cabe(p)) return e.copy(actual = p)
        }
        return e
    }

    /** Bajar una fila por gravedad (sin puntos extra). */
    fun caer(e: EstadoTetris): EstadoTetris = bajar(e, puntosPorFila = 0)

    /** Bajar una fila porque el jugador la empujó (1 punto por fila). */
    fun empujarAbajo(e: EstadoTetris): EstadoTetris = bajar(e, puntosPorFila = 1)

    /** Caída instantánea (2 puntos por fila). */
    fun caidaRapida(e: EstadoTetris): EstadoTetris {
        if (e.terminado) return e
        val destino = e.sombra()
        val filas = destino.y - e.actual.y
        return fijar(e.copy(actual = destino, puntos = e.puntos + filas * 2))
    }

    fun velocidadMs(e: EstadoTetris): Long = maxOf(90L, 800L - (e.nivel - 1) * 70L)

    private fun bajar(e: EstadoTetris, puntosPorFila: Int): EstadoTetris {
        if (e.terminado) return e
        val p = e.actual.copy(y = e.actual.y + 1)
        return if (e.cabe(p)) e.copy(actual = p, puntos = e.puntos + puntosPorFila) else fijar(e)
    }

    private fun fijar(e: EstadoTetris): EstadoTetris {
        val fijas = e.fijas.toMutableList()
        for ((c, f) in e.actual.celdas()) {
            if (f < 0) return e.copy(terminado = true) // se salió por arriba
            fijas[f * e.ancho + c] = e.actual.tipo
        }
        // Quitar filas completas
        val filas = (0 until e.alto).map { f -> fijas.subList(f * e.ancho, (f + 1) * e.ancho).toList() }
        val incompletas = filas.filter { fila -> fila.any { it == null } }
        val borradas = e.alto - incompletas.size
        val nuevas = List(borradas) { List<TipoPieza?>(e.ancho) { null } } + incompletas

        val lineas = e.lineas + borradas
        val bono = PUNTOS_LINEAS[borradas] * e.nivel
        val siguientePieza = aparecer(e.siguiente, e.ancho)
        val resultado = e.copy(
            fijas = nuevas.flatten(),
            actual = siguientePieza,
            siguiente = sortear(),
            puntos = e.puntos + bono,
            lineas = lineas
        )
        return if (resultado.cabe(siguientePieza)) resultado else resultado.copy(terminado = true)
    }

    private fun aparecer(tipo: TipoPieza, ancho: Int) = Pieza(tipo, x = (ancho - tipo.tamano) / 2, y = 0)

    private fun sortear(): TipoPieza = TipoPieza.entries[random.nextInt(TipoPieza.entries.size)]

    companion object {
        /** Puntos por 0, 1, 2, 3 o 4 líneas a la vez (se multiplican por el nivel). */
        val PUNTOS_LINEAS = listOf(0, 100, 300, 500, 800)
    }
}
