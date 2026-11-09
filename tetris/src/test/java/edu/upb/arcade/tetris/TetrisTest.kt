package edu.upb.arcade.tetris

import edu.upb.arcade.tetris.logica.EstadoTetris
import edu.upb.arcade.tetris.logica.Pieza
import edu.upb.arcade.tetris.logica.Tetris
import edu.upb.arcade.tetris.logica.TipoPieza
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TetrisTest {
    private val motor = Tetris(Random(7))

    @Test
    fun tapAlternaSoloHorizontalYVertical() {
        val e = EstadoTetris(actual = Pieza(TipoPieza.I, 3, 0), siguiente = TipoPieza.O)
        val g1 = motor.girar(e)
        assertTrue(g1.actual.vertical)
        assertEquals(setOf(5), g1.actual.celdas().map { it.first }.toSet()) // columna vertical
        val g2 = motor.girar(g1)
        assertFalse(g2.actual.vertical)
        assertEquals(e.actual.celdas(), g2.actual.celdas()) // vuelve a horizontal
    }

    @Test
    fun noSeSaleDelTablero() {
        var e = motor.nuevaPartida()
        repeat(20) { e = motor.mover(e, -1) }
        assertEquals(0, e.actual.celdas().minOf { it.first })
        repeat(20) { e = motor.mover(e, 1) }
        assertEquals(e.ancho - 1, e.actual.celdas().maxOf { it.first })
    }

    @Test
    fun completarUnaFilaLaBorraYSumaPuntos() {
        // fila de abajo llena excepto las 4 columnas donde caerá la I horizontal (3..6)
        val ancho = 10
        val alto = 20
        val fijas = MutableList<TipoPieza?>(ancho * alto) { null }
        for (c in 0 until ancho) if (c !in 3..6) fijas[(alto - 1) * ancho + c] = TipoPieza.O
        val e = EstadoTetris(fijas = fijas, actual = Pieza(TipoPieza.I, 3, 0), siguiente = TipoPieza.T)
        val r = motor.caidaRapida(e)
        assertEquals(1, r.lineas)
        assertTrue(r.fijas.all { it == null })
        assertTrue(r.puntos >= 100)
    }

    @Test
    fun partidaTerminaCuandoSeLlena() {
        var e = motor.nuevaPartida()
        var pasos = 0
        while (!e.terminado && pasos < 1000) { e = motor.caidaRapida(e); pasos++ }
        assertTrue(e.terminado)
    }
}
