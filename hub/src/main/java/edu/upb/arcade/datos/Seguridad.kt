package edu.upb.arcade.datos

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Las contraseñas nunca se guardan en texto plano: se guarda un hash PBKDF2 con sal. */
object Seguridad {
    private const val ITERACIONES = 120_000
    private const val BITS = 256

    fun nuevaSal(): String {
        val sal = ByteArray(16)
        SecureRandom().nextBytes(sal)
        return Base64.getEncoder().encodeToString(sal)
    }

    fun hash(contrasena: String, sal: String): String {
        val spec = PBEKeySpec(contrasena.toCharArray(), Base64.getDecoder().decode(sal), ITERACIONES, BITS)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun verificar(contrasena: String, sal: String, hashGuardado: String): Boolean =
        java.security.MessageDigest.isEqual(hash(contrasena, sal).toByteArray(), hashGuardado.toByteArray())
}

/** Reglas del formulario de registro. Devuelve el mensaje de error o null si todo está bien. */
object Validaciones {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val FECHA = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun email(valor: String): String? =
        if (EMAIL.matches(valor.trim())) null else "Escribe un email válido"

    fun contrasena(valor: String): String? =
        if (valor.length >= 6) null else "La contraseña debe tener al menos 6 caracteres"

    fun nickname(valor: String): String? = when {
        valor.trim().length < 3 -> "El nickname debe tener al menos 3 caracteres"
        valor.trim().length > 15 -> "El nickname puede tener máximo 15 caracteres"
        else -> null
    }

    /** [fecha] en formato yyyy-MM-dd; [hoy] también. */
    fun fechaNacimiento(fecha: String?, hoy: String): String? = when {
        fecha.isNullOrBlank() -> "Selecciona tu fecha de nacimiento"
        !FECHA.matches(fecha) -> "Fecha inválida"
        fecha > hoy -> "La fecha de nacimiento no puede ser futura"
        else -> null
    }
}
