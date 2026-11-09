package edu.upb.arcade.culebrita

import edu.upb.arcade.culebrita.logica.Celda
import edu.upb.arcade.culebrita.logica.Culebrita
import edu.upb.arcade.culebrita.logica.Direccion
import edu.upb.arcade.culebrita.logica.EstadoCulebrita
import edu.upb.arcade.culebrita.logica.direccionPorInclinacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class CulebritaTest {
    private val motor = Culebrita(Random(5))

    @Test
    fun comerHaceCrecerYSumaPuntos() {
        val e = EstadoCulebrita(cuerpo = listOf(Celda(5, 5), Celda(4, 5), Celda(3, 5)), comida = Celda(6, 5))
        val r = motor.avanzar(e)
        assertEquals(4, r.cuerpo.size)
        assertEquals(10, r.puntos)
        assertEquals(Celda(6, 5), r.cabeza)
    }

    @Test
    fun noPuedeDarMediaVuelta() {
        val e = motor.nuevaPartida() // va a la derecha
        val r = motor.avanzar(motor.girar(e, Direccion.IZQUIERDA))
        assertEquals(Direccion.DERECHA, r.direccion)
    }

    @Test
    fun chocarConLaParedTerminaLaPartida() {
        var e = motor.nuevaPartida()
        e = motor.girar(e, Direccion.ARRIBA)
        repeat(40) { e = motor.avanzar(e) }
        assertFalse(e.viva)
    }

    @Test
    fun inclinacionADireccion() {
        assertEquals(Direccion.DERECHA, direccionPorInclinacion(-3f, 0.5f))
        assertEquals(Direccion.IZQUIERDA, direccionPorInclinacion(3f, 0.5f))
        assertEquals(Direccion.ARRIBA, direccionPorInclinacion(0.2f, -3f))
        assertEquals(Direccion.ABAJO, direccionPorInclinacion(0.2f, 3f))
        assertNull(direccionPorInclinacion(0.5f, -0.5f))
    }
}
