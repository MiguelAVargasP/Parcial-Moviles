package edu.upb.arcade.triqui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.rememberArcadeClient
import edu.upb.arcade.core.ui.ArcadeColores
import edu.upb.arcade.core.ui.ArcadeTheme
import edu.upb.arcade.core.ui.BarraJugador
import edu.upb.arcade.core.ui.DialogoFinPartida
import edu.upb.arcade.triqui.logica.Ficha
import edu.upb.arcade.triqui.logica.Resultado
import edu.upb.arcade.triqui.logica.Triqui
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcadeTheme(acento = ArcadeColores.Triqui) { PantallaTriqui() }
        }
    }
}

private val ColorX = ArcadeColores.Triqui
private val ColorO = ArcadeColores.TriquiO

/**
 * Tú (X) contra la CPU (O). Cada victoria suma 100 a tu racha y cada empate 30.
 * Si pierdes, la racha termina y se guarda como score. También puedes "plantarte".
 */
@Composable
fun PantallaTriqui() {
    val cliente = rememberArcadeClient(JuegoArcade.TRIQUI)
    val activity = LocalContext.current as? Activity

    var tablero by remember { mutableStateOf(List<Ficha?>(9) { null }) }
    var turnoCpu by remember { mutableStateOf(false) }
    var racha by remember { mutableIntStateOf(0) }
    var victorias by remember { mutableIntStateOf(0) }
    var empiezaCpu by remember { mutableStateOf(false) }
    var finRacha by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf<Boolean?>(null) }
    val resultado = Triqui.resultado(tablero)
    val haptic = LocalHapticFeedback.current

    fun nuevaRonda() {
        empiezaCpu = !empiezaCpu // se alterna quién empieza
        tablero = List(9) { null }
        turnoCpu = empiezaCpu
    }

    fun terminarRacha() {
        guardado = if (cliente.nickname == null) null else cliente.guardarScore(racha)
        finRacha = true
    }

    // Jugada de la CPU con una pequeña pausa
    LaunchedEffect(turnoCpu, tablero) {
        if (turnoCpu && resultado == null) {
            delay(450)
            Triqui.jugadaCpu(tablero)?.let { i ->
                tablero = tablero.toMutableList().also { it[i] = Ficha.O }
            }
            turnoCpu = false
        }
    }

    // Cuando termina la ronda: sumar a la racha o cerrarla
    LaunchedEffect(resultado) {
        val r = resultado ?: return@LaunchedEffect
        racha += Triqui.puntosRonda(r)
        if (r is Resultado.Gana && r.ficha == Ficha.X) {
            victorias++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (r is Resultado.Gana && r.ficha == Ficha.O) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(900)
            terminarRacha()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(ArcadeColores.degradadoFondo)
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BarraJugador(cliente)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Marcador("Racha", "$racha", ArcadeColores.Dorado)
            Marcador("Victorias", "$victorias", ColorX)
        }

        Text(
            text = when (resultado) {
                is Resultado.Gana -> if (resultado.ficha == Ficha.X) "¡Ganaste la ronda! +100" else "La CPU ganó"
                Resultado.Empate -> "Empate +30"
                null -> if (turnoCpu) "🤖 CPU pensando…" else "Tu turno (X)"
            },
            fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            color = when (resultado) {
                is Resultado.Gana -> if (resultado.ficha == Ficha.X) ColorX else ColorO
                else -> ArcadeColores.Texto
            }
        )

        TableroTriqui(
            tablero = tablero,
            lineaGanadora = (resultado as? Resultado.Gana)?.linea,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            onTocar = { i ->
                // Si la ronda terminó en victoria o empate, tocar el tablero avanza a la siguiente ronda
                if (resultado != null && !(resultado is Resultado.Gana && resultado.ficha == Ficha.O)) {
                    nuevaRonda()
                } else if (!turnoCpu && resultado == null && tablero[i] == null) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    tablero = tablero.toMutableList().also { it[i] = Ficha.X }
                    turnoCpu = true
                }
            }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { terminarRacha() },
                enabled = racha > 0 && !finRacha,
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Text("Plantarme") }
            Button(
                onClick = { nuevaRonda() },
                enabled = resultado != null && !(resultado is Resultado.Gana && resultado.ficha == Ficha.O),
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Text("Siguiente ronda") }
        }
    }

    if (finRacha) {
        DialogoFinPartida(
            titulo = "Fin de la racha",
            puntos = racha,
            guardado = guardado,
            detalle = "$victorias victorias seguidas contra la CPU",
            onOtraVez = {
                racha = 0; victorias = 0; finRacha = false; guardado = null
                empiezaCpu = true // nuevaRonda() lo alterna: empieza el jugador
                nuevaRonda()
            },
            onSalir = { activity?.finish() }
        )
    }
}

@Composable
private fun Marcador(titulo: String, valor: String, color: Color) {
    Column(
        Modifier
            .background(ArcadeColores.Superficie, RoundedCornerShape(14.dp))
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, color = ArcadeColores.TextoSuave, fontSize = 12.sp)
        Text(valor, color = color, fontSize = 26.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TableroTriqui(
    tablero: List<Ficha?>,
    lineaGanadora: List<Int>?,
    modifier: Modifier,
    onTocar: (Int) -> Unit
) {
    val progresoLinea = remember(lineaGanadora) { Animatable(0f) }
    LaunchedEffect(lineaGanadora) {
        if (lineaGanadora != null) {
            progresoLinea.snapTo(0f)
            progresoLinea.animateTo(1f, tween(320))
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxSize()
                .background(ArcadeColores.FondoAlto, RoundedCornerShape(24.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (fila in 0 until 3) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (col in 0 until 3) {
                        val i = fila * 3 + col
                        val ganadora = lineaGanadora?.contains(i) == true
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    if (ganadora) Color.White.copy(alpha = 0.12f) else ArcadeColores.Superficie,
                                    RoundedCornerShape(16.dp)
                                )
                                .then(
                                    if (ganadora) Modifier.border(2.dp, ArcadeColores.Dorado, RoundedCornerShape(16.dp))
                                    else Modifier
                                )
                                .clickable { onTocar(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            tablero[i]?.let { FichaAnimada(it, Modifier.fillMaxSize().padding(18.dp)) }
                        }
                    }
                }
            }
        }

        // Trazado de línea ganadora continua y animada que cruza las casillas ganadoras
        if (lineaGanadora != null && lineaGanadora.size >= 3) {
            Canvas(Modifier.fillMaxSize().padding(22.dp)) {
                val i0 = lineaGanadora[0]
                val i2 = lineaGanadora[2]
                val f0 = i0 / 3
                val c0 = i0 % 3
                val f2 = i2 / 3
                val c2 = i2 % 3
                val celdaW = size.width / 3f
                val celdaH = size.height / 3f
                val p0 = Offset((c0 + 0.5f) * celdaW, (f0 + 0.5f) * celdaH)
                val p2 = Offset((c2 + 0.5f) * celdaW, (f2 + 0.5f) * celdaH)
                val actual = p0 + (p2 - p0) * progresoLinea.value

                drawLine(
                    color = ArcadeColores.Dorado,
                    start = p0,
                    end = actual,
                    strokeWidth = 12f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/** X u O que se "dibuja" al aparecer. */
@Composable
private fun FichaAnimada(ficha: Ficha, modifier: Modifier) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progreso.animateTo(1f, tween(280)) }
    Canvas(modifier) {
        val p = progreso.value
        val grosor = size.minDimension * 0.14f
        if (ficha == Ficha.X) {
            val a = minOf(1f, p * 2)
            val b = ((p - 0.5f) * 2).coerceIn(0f, 1f)
            drawLine(ColorX, Offset.Zero, Offset(size.width * a, size.height * a), grosor, StrokeCap.Round)
            if (b > 0f) drawLine(
                ColorX, Offset(size.width, 0f),
                Offset(size.width * (1 - b), size.height * b), grosor, StrokeCap.Round
            )
        } else {
            drawArc(
                ColorO, -90f, 360f * p, useCenter = false,
                topLeft = Offset(grosor / 2, grosor / 2),
                size = androidx.compose.ui.geometry.Size(size.width - grosor, size.height - grosor),
                style = Stroke(grosor, cap = StrokeCap.Round)
            )
        }
    }
}
