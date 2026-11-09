package edu.upb.arcade.culebrita

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.rememberArcadeClient
import edu.upb.arcade.core.ui.ArcadeColores
import edu.upb.arcade.core.ui.ArcadeTheme
import edu.upb.arcade.core.ui.BarraJugador
import edu.upb.arcade.core.ui.DialogoFinPartida
import edu.upb.arcade.culebrita.logica.Culebrita
import edu.upb.arcade.culebrita.logica.Direccion
import edu.upb.arcade.culebrita.logica.EstadoCulebrita
import kotlinx.coroutines.delay
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcadeTheme(acento = ArcadeColores.Culebrita) { PantallaCulebrita() }
        }
    }
}

@Composable
fun PantallaCulebrita() {
    val context = LocalContext.current
    val activity = context as? Activity
    val cliente = rememberArcadeClient(JuegoArcade.CULEBRITA)
    val motor = remember { Culebrita() }
    val control = remember { ControlGiroscopio(context) }

    var estado by remember { mutableStateOf(motor.nuevaPartida()) }
    var partida by remember { mutableIntStateOf(0) }
    var enJuego by remember { mutableStateOf(false) } // empieza en pausa: "Toca Empezar"
    var guardado by remember { mutableStateOf<Boolean?>(null) }
    val haptic = LocalHapticFeedback.current

    // El sensor cambia la dirección
    control.onDireccion = { d -> if (enJuego) estado = motor.girar(estado, d) }

    // Feedback háptico al comer o morir
    var puntosAnteriores by remember { mutableIntStateOf(0) }
    LaunchedEffect(estado.puntos) {
        if (estado.puntos > puntosAnteriores) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            puntosAnteriores = estado.puntos
        } else if (estado.puntos == 0) {
            puntosAnteriores = 0
        }
    }

    // Sensor encendido solo mientras la app está en pantalla
    LifecycleResumeEffect(Unit) {
        control.iniciar()
        onPauseOrDispose {
            control.detener()
            enJuego = false
        }
    }

    // Bucle del juego
    LaunchedEffect(partida, enJuego) {
        while (enJuego && estado.viva) {
            delay(motor.velocidadMs(estado))
            estado = motor.avanzar(estado)
        }
    }
    LaunchedEffect(partida, estado.viva) {
        if (!estado.viva) {
            enJuego = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            guardado = if (cliente.nickname == null) null else cliente.guardarScore(estado.puntos)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(ArcadeColores.degradadoFondo)
            .safeDrawingPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BarraJugador(cliente)

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Puntos: ${estado.puntos}", color = ArcadeColores.Dorado, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Control: ${control.nombreSensor}", color = ArcadeColores.TextoSuave, fontSize = 12.sp)
                Text("Dirección: ${flecha(estado.pedida)} · Swipe disponible", color = ArcadeColores.TextoSuave, fontSize = 12.sp)
            }
            IndicadorInclinacion(control.inclinacion)
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            TableroCulebrita(
                estado = estado,
                onGiro = { d -> if (enJuego) estado = motor.girar(estado, d) },
                modifier = Modifier.aspectRatio(estado.ancho / estado.alto.toFloat())
            )
            if (!enJuego && estado.viva) {
                Column(
                    Modifier
                        .background(ArcadeColores.Fondo.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📱 Inclina el teléfono", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Sostenlo como vas a jugar y toca Empezar:\nesa posición queda como \"recta\".\n(También puedes deslizar el dedo en el tablero)",
                        color = ArcadeColores.TextoSuave, fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        control.calibrar()
                        enJuego = true
                    }) { Text(if (estado.puntos == 0 && estado.cuerpo.size == 3) "Empezar" else "Continuar") }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { control.calibrar() }, modifier = Modifier.weight(1f)) { Text("Recalibrar") }
            OutlinedButton(
                onClick = { enJuego = false },
                enabled = enJuego,
                modifier = Modifier.weight(1f)
            ) { Text("Pausa") }
        }
    }

    if (!estado.viva) {
        DialogoFinPartida(
            titulo = "¡Chocaste!",
            puntos = estado.puntos,
            guardado = guardado,
            detalle = "Largo final: ${estado.cuerpo.size}",
            onOtraVez = {
                estado = motor.nuevaPartida()
                guardado = null
                partida++
            },
            onSalir = { activity?.finish() }
        )
    }
}

private fun flecha(d: Direccion) = when (d) {
    Direccion.ARRIBA -> "↑"
    Direccion.ABAJO -> "↓"
    Direccion.IZQUIERDA -> "←"
    Direccion.DERECHA -> "→"
}

/** Burbuja que muestra hacia dónde está inclinado el teléfono. */
@Composable
private fun IndicadorInclinacion(inclinacion: Offset) {
    Canvas(
        Modifier
            .size(64.dp)
            .background(ArcadeColores.Superficie, CircleShape)
            .border(2.dp, ArcadeColores.Culebrita.copy(alpha = 0.6f), CircleShape)
    ) {
        val r = size.minDimension / 2
        drawCircle(Color.White.copy(alpha = 0.15f), r * 0.35f, center, style = Stroke(2f))
        // gx negativo = inclinado a la derecha (burbuja a la derecha);
        // gy negativo = inclinado hacia adelante (burbuja arriba, como la culebrita)
        val limite = 5f
        val dx = (-inclinacion.x / limite).coerceIn(-1f, 1f) * r * 0.65f
        val dy = (inclinacion.y / limite).coerceIn(-1f, 1f) * r * 0.65f
        drawCircle(ArcadeColores.Culebrita, r * 0.2f, center + Offset(dx, dy))
    }
}

@Composable
private fun TableroCulebrita(
    estado: EstadoCulebrita,
    onGiro: (Direccion) -> Unit,
    modifier: Modifier
) {
    // Animación de latido en la fruta/comida para facilitar su visión periférica
    val pulsoComida by rememberInfiniteTransition(label = "comida").animateFloat(
        initialValue = 0.32f,
        targetValue = 0.44f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "pulso"
    )

    Canvas(
        modifier
            .background(Color(0xFF050D21), RoundedCornerShape(10.dp))
            .border(2.dp, ArcadeColores.Culebrita.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                var deltaAcumulado = Offset.Zero
                detectDragGestures(
                    onDragStart = { deltaAcumulado = Offset.Zero },
                    onDragEnd = {
                        val slop = 16.dp.toPx()
                        if (abs(deltaAcumulado.x) >= slop || abs(deltaAcumulado.y) >= slop) {
                            val dir = if (abs(deltaAcumulado.x) >= abs(deltaAcumulado.y)) {
                                if (deltaAcumulado.x > 0) Direccion.DERECHA else Direccion.IZQUIERDA
                            } else {
                                if (deltaAcumulado.y > 0) Direccion.ABAJO else Direccion.ARRIBA
                            }
                            onGiro(dir)
                        }
                    }
                ) { cambio, delta ->
                    cambio.consume()
                    deltaAcumulado += delta
                }
            }
    ) {
        val c = size.width / estado.ancho
        // tablero de ajedrez muy suave
        for (x in 0 until estado.ancho) for (y in 0 until estado.alto) {
            if ((x + y) % 2 == 0) drawRect(Color.White.copy(alpha = 0.025f), Offset(x * c, y * c), Size(c, c))
        }
        // comida con halo brillante y latido animado
        val comida = estado.comida
        val centroComida = Offset((comida.x + 0.5f) * c, (comida.y + 0.5f) * c)
        drawCircle(Color(0xFFFF5A5F).copy(alpha = 0.25f), c * (pulsoComida + 0.12f), centroComida)
        drawCircle(Color(0xFFFF5A5F), c * pulsoComida, centroComida)

        // cuerpo (de la cola a la cabeza, para que la cabeza quede encima)
        val total = estado.cuerpo.size
        estado.cuerpo.asReversed().forEachIndexed { i, celda ->
            val t = i / total.toFloat()
            val color = ArcadeColores.Culebrita.copy(alpha = 0.45f + 0.55f * t)
            drawRoundRect(
                color, Offset(celda.x * c + c * 0.08f, celda.y * c + c * 0.08f),
                Size(c * 0.84f, c * 0.84f), CornerRadius(c * 0.3f)
            )
        }
        // ojos
        val cab = estado.cabeza
        val centro = Offset((cab.x + 0.5f) * c, (cab.y + 0.5f) * c)
        val d = estado.direccion
        val lado = Offset(-d.dy.toFloat(), d.dx.toFloat()) * (c * 0.18f)
        val frente = Offset(d.dx.toFloat(), d.dy.toFloat()) * (c * 0.15f)
        drawCircle(Color(0xFF06122B), c * 0.08f, centro + frente + lado)
        drawCircle(Color(0xFF06122B), c * 0.08f, centro + frente - lado)
    }
}
