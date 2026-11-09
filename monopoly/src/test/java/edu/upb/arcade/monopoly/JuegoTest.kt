package edu.upb.arcade.monopoly

import edu.upb.arcade.monopoly.modelo.Fase
import edu.upb.arcade.monopoly.modelo.Juego
import edu.upb.arcade.monopoly.ui.Recorrido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class JuegoTest {

    private val juego = Juego(random = Random(42))

    @Test
    fun nombresVaciosReciben_nombrePorDefecto() {
        val e = juego.nuevaPartida(listOf("Ana", "  "))
        assertEquals("Jugador 2", e.jugadores[1].nombre)
    }

    @Test
    fun soltarEnCasillaEquivocada_noMueveLaFicha() {
        var e = juego.lanzarDado(juego.nuevaPartida(listOf("Ana", "Beto")))
        val destino = (e.fase as Fase.MoverFicha).destino
        e = juego.moverFicha(e, (destino + 1) % 20)
        assertEquals(0, e.jugadores[0].posicion)
        assertTrue(e.fase is Fase.MoverFicha)
        assertTrue(e.aviso != null)
    }

    @Test
    fun soltarEnCasillaCorrecta_mueveLaFicha() {
        var e = juego.lanzarDado(juego.nuevaPartida(listOf("Ana", "Beto")))
        val destino = (e.fase as Fase.MoverFicha).destino
        e = juego.moverFicha(e, destino)
        assertEquals(destino, e.jugadores[0].posicion)
        assertNull(e.aviso)
    }

    @Test
    fun partidaCompleta_terminaDespuesDe10Rondas() {
        var e = juego.nuevaPartida(listOf("Ana", "Beto", "Caro"))
        var pasos = 0
        while (e.fase !is Fase.Terminado && pasos < 10_000) {
            e = when (val f = e.fase) {
                Fase.TirarDado -> juego.lanzarDado(e)
                is Fase.MoverFicha -> juego.moverFicha(e, f.destino)
                is Fase.DecidirCompra -> juego.decidirCompra(e, comprar = true)
                is Fase.Terminado -> e
            }
            pasos++
        }
        assertTrue(e.fase is Fase.Terminado)
        assertEquals(10, e.ronda)
        val ganador = (e.fase as Fase.Terminado).ganador
        assertEquals(e.jugadores.maxOf { it.saldo }, e.jugadores[ganador].saldo)
    }

    @Test
    fun recorridoCubreLas20CasillasDelBorde() {
        val celdas = (0 until 20).map { Recorrido.celda(it) }.toSet()
        assertEquals(20, celdas.size)
        celdas.forEach { (f, c) -> assertTrue(f == 0 || f == 5 || c == 0 || c == 5) }
        for (i in 0 until 20) {
            val (f, c) = Recorrido.celda(i)
            assertEquals(i, Recorrido.indiceEn(f, c))
        }
        assertNull(Recorrido.indiceEn(2, 2))
    }
}
