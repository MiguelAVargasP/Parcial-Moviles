package edu.upb.arcade

import edu.upb.arcade.datos.Seguridad
import edu.upb.arcade.datos.Validaciones
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {
    @Test
    fun hashVerificaSoloLaContrasenaCorrecta() {
        val sal = Seguridad.nuevaSal()
        val h = Seguridad.hash("secreta123", sal)
        assertNotEquals("secreta123", h)
        assertTrue(Seguridad.verificar("secreta123", sal, h))
        assertFalse(Seguridad.verificar("otra", sal, h))
    }

    @Test
    fun validaciones() {
        assertNull(Validaciones.email("miguel@upb.edu.co"))
        assertNotNull(Validaciones.email("miguel@"))
        assertNotNull(Validaciones.contrasena("123"))
        assertNotNull(Validaciones.nickname("ab"))
        assertNull(Validaciones.fechaNacimiento("2004-05-10", "2026-10-25"))
        assertNotNull(Validaciones.fechaNacimiento("2030-01-01", "2026-10-25"))
        assertNotNull(Validaciones.fechaNacimiento(null, "2026-10-25"))
    }
}
