package edu.upb.arcade.triqui

import edu.upb.arcade.triqui.logica.Ficha
import edu.upb.arcade.triqui.logica.Ficha.O
import edu.upb.arcade.triqui.logica.Ficha.X
import edu.upb.arcade.triqui.logica.Resultado
import edu.upb.arcade.triqui.logica.Triqui
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TriquiTest {
    @Test
    fun detectaGanadorYEmpate() {
        val gana = listOf<Ficha?>(X, X, X, O, O, null, null, null, null)
        assertEquals(Resultado.Gana(X, listOf(0, 1, 2)), Triqui.resultado(gana))
        val empate = listOf<Ficha?>(X, O, X, X, O, O, O, X, X)
        assertEquals(Resultado.Empate, Triqui.resultado(empate))
        assertNull(Triqui.resultado(List(9) { null }))
    }

    @Test
    fun cpuGanaSiPuedeYBloqueaSiNo() {
        val puedeGanar = listOf<Ficha?>(O, O, null, X, X, null, X, null, null)
        assertEquals(2, Triqui.jugadaCpu(puedeGanar, Random(1), descuido = 0.0))
        val debeBloquear = listOf<Ficha?>(X, X, null, null, O, null, null, null, null)
        assertEquals(2, Triqui.jugadaCpu(debeBloquear, Random(1), descuido = 0.0))
    }

    @Test
    fun cpuSiempreJuegaEnCasillaLibre() {
        val r = Random(3)
        repeat(200) {
            val t = MutableList<Ficha?>(9) { null }
            repeat(r.nextInt(8)) { t[r.nextInt(9)] = if (r.nextBoolean()) X else O }
            val j = Triqui.jugadaCpu(t, r)
            if (j != null) assertTrue(t[j] == null)
        }
    }
}
