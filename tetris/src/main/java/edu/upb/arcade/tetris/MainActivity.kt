package edu.upb.arcade.tetris

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.rememberArcadeClient
import edu.upb.arcade.core.ui.ArcadeColores
import edu.upb.arcade.core.ui.ArcadeTheme
import edu.upb.arcade.core.ui.BarraJugador
import edu.upb.arcade.core.ui.DialogoFinPartida
import edu.upb.arcade.tetris.logica.EstadoTetris
import edu.upb.arcade.tetris.logica.Pieza
import edu.upb.arcade.tetris.logica.Tetris
import edu.upb.arcade.tetris.logica.TipoPieza
import kotlinx.coroutines.delay
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcadeTheme(acento = ArcadeColores.Tetris) { PantallaTetris() }
        }
    }
}

fun colorDe(tipo: TipoPieza): Color = when (tipo) {
    TipoPieza.I -> Color(0xFF22D3EE)
    TipoPieza.O -> Color(0xFFFACC15)
    TipoPieza.T -> Color(0xFF818CF8)
    TipoPieza.S -> Color(0xFF34D399)
    TipoPieza.Z -> Color(0xFFF87171)
    TipoPieza.J -> Color(0xFF60A5FA)
    TipoPieza.L -> Color(0xFFFB923C)
}

private enum class Eje { HORIZONTAL, VERTICAL }

@Composable
fun PantallaTetris() {
    val cliente = rememberArcadeClient(JuegoArcade.TETRIS)
    val activity = LocalContext.current as? Activity
    val motor = remember { Tetris() }

    var estado by remember { mutableStateOf(motor.nuevaPartida()) }
    var partida by remember { mutableIntStateOf(0) }
    var pausado by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf<Boolean?>(null) }

    // Gravedad: baja la pieza cada cierto tiempo según el nivel
    LaunchedEffect(partida, pausado, estado.terminado) {
        while (!pausado && !estado.terminado) {
            delay(motor.velocidadMs(estado))
            estado = motor.caer(estado)
        }
    }
    // Al terminar, guardar el score una sola vez
    LaunchedEffect(partida, estado.terminado) {
        if (estado.terminado) {
            guardado = if (cliente.nickname == null) null else cliente.guardarScore(estado.puntos)
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { pausado = true }

    val jugar = !pausado && !estado.terminado
    fun accion(f: (EstadoTetris) -> EstadoTetris) {
        if (jugar) estado = f(estado)
    }

    val haptic = LocalHapticFeedback.current

    Column(
        Modifier
            .fillMaxSize()
            .background(ArcadeColores.degradadoFondo)
            .safeDrawingPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BarraJugador(cliente)

        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                TableroTetris(
                    estado = estado,
                    pausado = pausado,
                    onDespausar = { pausado = false },
                    modifier = Modifier.aspectRatio(estado.ancho / estado.alto.toFloat()),
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        accion(motor::girar)
                    },
                    onMover = { dx ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        accion { motor.mover(it, dx) }
                    },
                    onBajar = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        accion(motor::empujarAbajo)
                    }
                )
            }
            PanelLateral(estado, pausado, onPausa = { if (!estado.terminado) pausado = !pausado })
        }

        Text(
            "Arrastra la ficha · tap para girar (H/V)",
            color = ArcadeColores.TextoSuave, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Controles arcade ergonómicos: Movimiento agrupado a la izquierda, Acción y Caída rápida a la derecha
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // D-Pad / Movimiento
            BotonControl("◀", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                accion { motor.mover(it, -1) }
            }
            BotonControl("▼", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                accion(motor::empujarAbajo)
            }
            BotonControl("▶", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                accion { motor.mover(it, 1) }
            }

            Spacer(Modifier.width(4.dp))

            // Rotar
            BotonControl("↻", Modifier.weight(1f)) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                accion(motor::girar)
            }

            // Caída rápida destacada (separada y con acento dorado para evitar toques accidentales)
            BotonControl(
                texto = "⤓",
                modifier = Modifier.weight(1.15f),
                color = ArcadeColores.Dorado,
                colorTexto = Color(0xFF0F172A)
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                accion(motor::caidaRapida)
            }
        }
    }

    if (estado.terminado) {
        DialogoFinPartida(
            titulo = "Game over",
            puntos = estado.puntos,
            guardado = guardado,
            detalle = "Nivel ${estado.nivel} · ${estado.lineas} líneas",
            onOtraVez = {
                estado = motor.nuevaPartida()
                guardado = null
                pausado = false
                partida++
            },
            onSalir = { activity?.finish() }
        )
    }
}

@Composable
private fun BotonControl(
    texto: String,
    modifier: Modifier,
    color: Color? = null,
    colorTexto: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val colores = if (color != null) {
        ButtonDefaults.filledTonalButtonColors(containerColor = color, contentColor = colorTexto)
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = colores,
        contentPadding = ButtonDefaults.TextButtonContentPadding
    ) { Text(texto, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
}

/**
 * Tablero con gestos refinados:
 *  - Tap limpio -> girar (alterna horizontal / vertical).
 *  - Arrastre con umbral mínimo (slop) -> solo bloquea el eje tras superar el umbral,
 *    evitando que ligeros temblores bloqueen la dirección no deseada.
 */
@Composable
private fun TableroTetris(
    estado: EstadoTetris,
    pausado: Boolean,
    onDespausar: () -> Unit,
    modifier: Modifier,
    onTap: () -> Unit,
    onMover: (Int) -> Unit,
    onBajar: () -> Unit
) {
    val tap by rememberUpdatedState(onTap)
    val mover by rememberUpdatedState(onMover)
    val bajar by rememberUpdatedState(onBajar)
    val density = LocalDensity.current
    val columnas = estado.ancho

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF050D21), RoundedCornerShape(10.dp))
                .border(2.dp, ArcadeColores.Tetris.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .pointerInput(columnas) {
                    val celda = size.width / columnas.toFloat()
                    val slop = with(density) { 14.dp.toPx() }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var acumulado = Offset.Zero
                        var totalMovido = 0f
                        var eje: Eje? = null
                        var arrastrado = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                // Se levantó el dedo sin arrastrar -> es un TAP para girar
                                if (!arrastrado && totalMovido < slop) {
                                    tap()
                                }
                                break
                            }
                            val delta = change.position - change.previousPosition
                            acumulado += delta
                            totalMovido += delta.getDistance()

                            if (totalMovido >= slop) {
                                arrastrado = true
                                if (eje == null) {
                                    eje = if (abs(acumulado.x) >= abs(acumulado.y)) Eje.HORIZONTAL else Eje.VERTICAL
                                }
                                when (eje) {
                                    Eje.HORIZONTAL -> {
                                        while (acumulado.x >= celda) {
                                            mover(1)
                                            acumulado = acumulado.copy(x = acumulado.x - celda)
                                        }
                                        while (acumulado.x <= -celda) {
                                            mover(-1)
                                            acumulado = acumulado.copy(x = acumulado.x + celda)
                                        }
                                    }
                                    Eje.VERTICAL -> {
                                        while (acumulado.y >= celda * 0.75f) {
                                            bajar()
                                            acumulado = acumulado.copy(y = acumulado.y - celda * 0.75f)
                                        }
                                        if (acumulado.y < 0f) acumulado = acumulado.copy(y = 0f)
                                    }
                                    null -> Unit
                                }
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            val celda = size.width / estado.ancho
            // cuadrícula
            for (c in 1 until estado.ancho) {
                drawLine(Color.White.copy(alpha = 0.04f), Offset(c * celda, 0f), Offset(c * celda, size.height))
            }
            for (f in 1 until estado.alto) {
                drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, f * celda), Offset(size.width, f * celda))
            }
            // fijas
            for (f in 0 until estado.alto) for (c in 0 until estado.ancho) {
                estado.fijas[f * estado.ancho + c]?.let { bloque(c, f, celda, colorDe(it)) }
            }
            if (!estado.terminado) {
                // sombra (dónde caerá)
                estado.sombra().celdas().forEach { (c, f) ->
                    if (f >= 0) drawRoundRect(
                        colorDe(estado.actual.tipo).copy(alpha = 0.35f),
                        Offset(c * celda + 2, f * celda + 2), Size(celda - 4, celda - 4),
                        CornerRadius(celda * 0.2f), style = Stroke(2f)
                    )
                }
                estado.actual.celdas().forEach { (c, f) -> if (f >= 0) bloque(c, f, celda, colorDe(estado.actual.tipo)) }
            }
        }

        // Overlay visual de pausa sobre el tablero
        if (pausado) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xE6050D21), RoundedCornerShape(10.dp))
                    .clickable(onClick = onDespausar),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏸", fontSize = 38.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("PAUSA", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ArcadeColores.Texto, letterSpacing = 2.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Toca para continuar", color = ArcadeColores.TextoSuave, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun DrawScope.bloque(col: Int, fila: Int, celda: Float, color: Color) {
    val m = celda * 0.06f
    drawRoundRect(color, Offset(col * celda + m, fila * celda + m), Size(celda - 2 * m, celda - 2 * m), CornerRadius(celda * 0.18f))
    drawRoundRect(
        Color.White.copy(alpha = 0.30f), Offset(col * celda + celda * 0.18f, fila * celda + celda * 0.15f),
        Size(celda * 0.45f, celda * 0.16f), CornerRadius(celda * 0.08f)
    )
}

@Composable
private fun PanelLateral(estado: EstadoTetris, pausado: Boolean, onPausa: () -> Unit) {
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Caja("Siguiente") {
            Canvas(Modifier.size(64.dp)) {
                val pieza = Pieza(estado.siguiente, 0, 0)
                val celda = size.width / 4
                val cs = pieza.celdas()
                val ancho = (cs.maxOf { it.first } + 1) * celda
                val alto = (cs.maxOf { it.second } - cs.minOf { it.second } + 1) * celda
                val ox = (size.width - ancho) / 2
                val oy = (size.height - alto) / 2 - cs.minOf { it.second } * celda
                cs.forEach { (c, f) ->
                    drawRoundRect(
                        colorDe(estado.siguiente), Offset(ox + c * celda + 1, oy + f * celda + 1),
                        Size(celda - 2, celda - 2), CornerRadius(celda * 0.2f)
                    )
                }
            }
        }
        Caja("Puntos") { Text("${estado.puntos}", color = ArcadeColores.Dorado, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        Caja("Nivel") { Text("${estado.nivel}", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        Caja("Líneas") { Text("${estado.lineas}", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        Spacer(Modifier.weight(1f))
        Button(onClick = onPausa, modifier = Modifier.width(84.dp), contentPadding = ButtonDefaults.TextButtonContentPadding) { Text(if (pausado) "Seguir" else "Pausa") }
    }
}

@Composable
private fun Caja(titulo: String, contenido: @Composable () -> Unit) {
    Column(
        Modifier
            .width(84.dp)
            .background(ArcadeColores.Superficie, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, color = ArcadeColores.TextoSuave, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Box(contentAlignment = Alignment.Center) { contenido() }
    }
}
