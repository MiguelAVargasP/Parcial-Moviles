package edu.upb.arcade.culebrita.logica

import kotlin.math.abs
import kotlin.random.Random

enum class Direccion(val dx: Int, val dy: Int) {
    ARRIBA(0, -1), ABAJO(0, 1), IZQUIERDA(-1, 0), DERECHA(1, 0);

    fun opuesta(): Direccion = when (this) {
        ARRIBA -> ABAJO
        ABAJO -> ARRIBA
        IZQUIERDA -> DERECHA
        DERECHA -> IZQUIERDA
    }
}

data class Celda(val x: Int, val y: Int)

data class EstadoCulebrita(
    val ancho: Int = 16,
    val alto: Int = 24,
    /** La cabeza es el primer elemento. */
    val cuerpo: List<Celda>,
    val direccion: Direccion = Direccion.DERECHA,
    /** Dirección pedida por el sensor o swipe; se aplica en el siguiente paso. */
    val pedida: Direccion = Direccion.DERECHA,
    /** Siguiente giro en cola si el usuario gira dos veces rápido antes del tick. */
    val siguiente: Direccion? = null,
    val comida: Celda,
    val puntos: Int = 0,
    val viva: Boolean = true
) {
    val cabeza: Celda get() = cuerpo.first()
}

class Culebrita(private val random: Random = Random.Default) {

    fun nuevaPartida(ancho: Int = 16, alto: Int = 24): EstadoCulebrita {
        val y = alto / 2
        val cuerpo = listOf(Celda(4, y), Celda(3, y), Celda(2, y))
        return EstadoCulebrita(ancho, alto, cuerpo, comida = nuevaComida(ancho, alto, cuerpo))
    }

    /** No se puede dar media vuelta sobre sí misma; almacena giro en cola si ya hay uno pendiente. */
    fun girar(e: EstadoCulebrita, nueva: Direccion): EstadoCulebrita {
        if (!e.viva) return e
        val base = e.pedida
        if (nueva == base || nueva == base.opuesta()) return e
        return if (e.pedida == e.direccion) {
            e.copy(pedida = nueva)
        } else {
            e.copy(siguiente = nueva)
        }
    }

    fun avanzar(e: EstadoCulebrita): EstadoCulebrita {
        if (!e.viva) return e
        val dir = if (e.pedida == e.direccion.opuesta()) e.direccion else e.pedida
        val nueva = Celda(e.cabeza.x + dir.dx, e.cabeza.y + dir.dy)
        val come = nueva == e.comida

        val fueraDelTablero = nueva.x !in 0 until e.ancho || nueva.y !in 0 until e.alto
        // la cola se mueve en este paso, así que no cuenta (salvo que esté comiendo)
        val cuerpoQueEstorba = if (come) e.cuerpo else e.cuerpo.dropLast(1)
        if (fueraDelTablero || nueva in cuerpoQueEstorba) return e.copy(direccion = dir, viva = false)

        val cuerpo = listOf(nueva) + (if (come) e.cuerpo else e.cuerpo.dropLast(1))
        return e.copy(
            cuerpo = cuerpo,
            direccion = dir,
            pedida = e.siguiente ?: dir,
            siguiente = null,
            comida = if (come) nuevaComida(e.ancho, e.alto, cuerpo) else e.comida,
            puntos = if (come) e.puntos + PUNTOS_COMIDA else e.puntos
        )
    }

    /** Más rápida a medida que come. */
    fun velocidadMs(e: EstadoCulebrita): Long = maxOf(85L, 230L - (e.puntos / PUNTOS_COMIDA) * 6L)

    private fun nuevaComida(ancho: Int, alto: Int, cuerpo: List<Celda>): Celda {
        val libres = (0 until ancho).flatMap { x -> (0 until alto).map { y -> Celda(x, y) } } - cuerpo.toSet()
        return libres.random(random)
    }

    companion object {
        const val PUNTOS_COMIDA = 10
    }
}

/**
 * Convierte la inclinación del teléfono (en m/s², relativa a la posición "neutral")
 * en una dirección. [gx] y [gy] son la gravedad en los ejes X/Y del teléfono.
 *  - inclinar a la derecha  -> gx negativo -> DERECHA
 *  - inclinar hacia adelante (borde de arriba hacia abajo) -> gy negativo -> ARRIBA
 * Devuelve null si la inclinación es menor que [umbral].
 */
fun direccionPorInclinacion(gx: Float, gy: Float, umbral: Float = 1.8f): Direccion? {
    if (maxOf(abs(gx), abs(gy)) < umbral) return null
    return if (abs(gx) > abs(gy)) {
        if (gx < 0) Direccion.DERECHA else Direccion.IZQUIERDA
    } else {
        if (gy < 0) Direccion.ARRIBA else Direccion.ABAJO
    }
}
