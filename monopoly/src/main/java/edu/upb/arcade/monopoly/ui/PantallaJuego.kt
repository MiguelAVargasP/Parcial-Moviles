package edu.upb.arcade.monopoly.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import edu.upb.arcade.monopoly.modelo.Casilla
import edu.upb.arcade.monopoly.modelo.CasillaPropiedad
import edu.upb.arcade.monopoly.modelo.CasillaSalida
import edu.upb.arcade.monopoly.modelo.CasillaSuerte
import edu.upb.arcade.monopoly.modelo.EstadoJuego
import edu.upb.arcade.monopoly.modelo.Fase
import edu.upb.arcade.monopoly.modelo.Jugador
import edu.upb.arcade.monopoly.modelo.Tablero
import kotlin.math.roundToInt

@Composable
fun PantallaJuego(
    estado: EstadoJuego,
    tablero: Tablero,
    rondasTotales: Int,
    onLanzarDado: () -> Unit,
    onSoltarFicha: (Int?) -> Unit,
    onDecidirCompra: (Boolean) -> Unit,
    cabecera: @Composable () -> Unit,
    finPartida: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cabecera()
        TableroTactil(estado, tablero, rondasTotales, onLanzarDado, onSoltarFicha)

        Text(
            text = estado.aviso ?: " ",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        PanelJugadores(estado, tablero)
        Bitacora(estado.bitacora, Modifier.weight(1f))
    }

    when (val fase = estado.fase) {
        is Fase.DecidirCompra -> DialogoCompra(
            jugador = estado.jugadorActual,
            propiedad = tablero.casillas[fase.casilla] as CasillaPropiedad,
            onDecidir = onDecidirCompra
        )
        is Fase.Terminado -> finPartida()
        else -> Unit
    }
}

// ---------------------------------------------------------------------------
// Tablero con las fichas que se arrastran con el dedo
// ---------------------------------------------------------------------------

@Composable
private fun TableroTactil(
    estado: EstadoJuego,
    tablero: Tablero,
    rondasTotales: Int,
    onLanzarDado: () -> Unit,
    onSoltarFicha: (Int?) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(ColorTablero, RoundedCornerShape(8.dp))
    ) {
        val celdaDp: Dp = maxWidth / Recorrido.LADO
        val celdaPx = with(LocalDensity.current) { celdaDp.toPx() }
        val destino = (estado.fase as? Fase.MoverFicha)?.destino

        // Parpadeo de la casilla destino para indicar a dónde arrastrar
        val pulso by rememberInfiniteTransition(label = "pulso").animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "alfa"
        )
        val haptic = LocalHapticFeedback.current

        // Casillas del borde
        tablero.casillas.forEachIndexed { i, casilla ->
            val (fila, col) = Recorrido.celda(i)
            CeldaCasilla(
                indice = i,
                casilla = casilla,
                colorDueno = estado.propietarios[i]?.let { ColoresJugador[it] },
                resaltada = i == destino,
                alfaResalte = pulso,
                onTocar = if (i == destino) {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSoltarFicha(destino)
                    }
                } else null,
                modifier = Modifier
                    .offset(x = celdaDp * col, y = celdaDp * fila)
                    .size(celdaDp)
                    .padding(1.5.dp)
            )
        }

        // Centro: ronda, turno y dado
        PanelCentral(
            estado = estado,
            rondasTotales = rondasTotales,
            onLanzarDado = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onLanzarDado()
            },
            modifier = Modifier
                .offset(x = celdaDp, y = celdaDp)
                .size(celdaDp * (Recorrido.LADO - 2))
        )

        // Fichas
        val tamFichaDp = celdaDp * 0.4f
        val tamFichaPx = celdaPx * 0.4f
        estado.jugadores.forEachIndexed { j, jugador ->
            val (fila, col) = Recorrido.celda(jugador.posicion)
            // Cada jugador ocupa una esquina distinta de la casilla para no taparse
            val dx = if (j % 2 == 0) -0.22f else 0.22f
            val dy = if (j < 2) -0.18f else 0.26f
            val centro = Offset(
                x = (col + 0.5f + dx) * celdaPx,
                y = (fila + 0.5f + dy) * celdaPx
            )
            FichaJugador(
                inicial = jugador.nombre.first().uppercaseChar(),
                color = ColoresJugador[j],
                centro = centro,
                tamDp = tamFichaDp,
                tamPx = tamFichaPx,
                arrastrable = j == estado.turno && estado.fase is Fase.MoverFicha,
                destino = destino,
                celdaPx = celdaPx,
                onSoltar = { casilla ->
                    if (casilla == destino) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onSoltarFicha(casilla)
                }
            )
        }
    }
}

/**
 * Ficha de un jugador. Si es [arrastrable], el jugador la mueve con el dedo;
 * al soltarla se calcula sobre qué casilla quedó y se avisa con [onSoltar].
 * Si no era la casilla correcta, la ficha vuelve a su sitio.
 */
@Composable
private fun FichaJugador(
    inicial: Char,
    color: Color,
    centro: Offset,
    tamDp: Dp,
    tamPx: Float,
    arrastrable: Boolean,
    destino: Int?,
    celdaPx: Float,
    onSoltar: (Int?) -> Unit
) {
    var arrastre by remember { mutableStateOf(Offset.Zero) }
    var arrastrando by remember { mutableStateOf(false) }
    val centroActual by rememberUpdatedState(centro)
    val celdaActual by rememberUpdatedState(celdaPx)
    val onSoltarActual by rememberUpdatedState(onSoltar)

    val escala by animateFloatAsState(
        targetValue = if (arrastrando) 1.4f else 1f,
        label = "escalaFicha"
    )
    // Una ficha que se puede mover "respira" para llamar la atención
    val latido = if (arrastrable && !arrastrando) {
        rememberInfiniteTransition(label = "latido").animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
            label = "latidoFicha"
        ).value
    } else 1f

    // Al arrastrar, elevamos sutilmente la ficha por encima del dedo para no tapar la casilla destino
    val elevacionDedo = if (arrastrando) -tamPx * 0.75f else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .zIndex(if (arrastrable) 2f else 1f)
            .offset {
                IntOffset(
                    (centro.x + arrastre.x - tamPx / 2).roundToInt(),
                    (centro.y + arrastre.y + elevacionDedo - tamPx / 2).roundToInt()
                )
            }
            .size(tamDp)
            .graphicsLayer {
                scaleX = escala * latido
                scaleY = escala * latido
            }
            .shadow(if (arrastrable) 10.dp else 2.dp, CircleShape)
            .background(color, CircleShape)
            .border(if (arrastrable) 3.dp else 1.dp, Color.White, CircleShape)
            .pointerInput(arrastrable) {
                if (!arrastrable) return@pointerInput
                detectDragGestures(
                    onDragStart = { arrastrando = true },
                    onDragEnd = {
                        val punto = centroActual + arrastre
                        arrastrando = false
                        arrastre = Offset.Zero
                        var elegida = Recorrido.indiceEnPunto(punto.x, punto.y, celdaActual)
                        // Tolerancia magnética hacia la casilla destino para un arrastre cómodo
                        if (destino != null && elegida != destino) {
                            val (fDest, cDest) = Recorrido.celda(destino)
                            val centroDest = Offset((cDest + 0.5f) * celdaActual, (fDest + 0.5f) * celdaActual)
                            if ((punto - centroDest).getDistance() <= celdaActual * 1.25f) {
                                elegida = destino
                            }
                        }
                        onSoltarActual(elegida)
                    },
                    onDragCancel = {
                        arrastrando = false
                        arrastre = Offset.Zero
                    }
                ) { cambio, delta ->
                    cambio.consume()
                    arrastre += delta
                }
            }
    ) {
        Text(
            text = inicial.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CeldaCasilla(
    indice: Int,
    casilla: Casilla,
    colorDueno: Color?,
    resaltada: Boolean,
    alfaResalte: Float,
    onTocar: (() -> Unit)? = null,
    modifier: Modifier
) {
    val fondo = when (casilla) {
        is CasillaSalida -> ColorSalida
        is CasillaSuerte -> ColorSuerte
        is CasillaPropiedad -> colorDueno?.copy(alpha = 0.35f)?.let { mezclar(ColorPropiedad, it) }
            ?: ColorPropiedad
    }
    Box(
        modifier = modifier
            .background(fondo, RoundedCornerShape(4.dp))
            .then(
                if (resaltada) Modifier.border(
                    3.dp, Color(0xFFFFD600).copy(alpha = alfaResalte), RoundedCornerShape(4.dp)
                ) else Modifier
            )
            .then(
                if (onTocar != null) Modifier.clickable(onClick = onTocar) else Modifier
            )
    ) {
        Text(
            "$indice",
            fontSize = 8.sp,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp)
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (casilla) {
                is CasillaSalida -> Text("SALIDA", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                is CasillaSuerte -> {
                    Text("?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                    Text("Suerte", fontSize = 8.sp)
                }
                is CasillaPropiedad -> {
                    Text("P${casilla.indice}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$${casilla.precio}", fontSize = 8.sp)
                }
            }
        }
        if (colorDueno != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(7.dp)
                    .background(colorDueno, CircleShape)
            )
        }
    }
}

private fun mezclar(base: Color, encima: Color): Color {
    val a = encima.alpha
    return Color(
        red = base.red * (1 - a) + encima.red * a,
        green = base.green * (1 - a) + encima.green * a,
        blue = base.blue * (1 - a) + encima.blue * a
    )
}

@Composable
private fun PanelCentral(
    estado: EstadoJuego,
    rondasTotales: Int,
    onLanzarDado: () -> Unit,
    modifier: Modifier
) {
    val puedeTirar = estado.fase == Fase.TirarDado
    val jugador = estado.jugadorActual

    // Giro del dado cada vez que se lanza
    val giro = remember { Animatable(0f) }
    LaunchedEffect(estado.ronda, estado.turno, estado.dado) {
        if (estado.fase is Fase.MoverFicha) {
            giro.snapTo(0f)
            giro.animateTo(360f, tween(450))
        }
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            "Ronda ${estado.ronda} de $rondasTotales",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(14.dp)
                    .background(ColoresJugador[estado.turno], CircleShape)
                    .border(1.dp, Color.White, CircleShape)
            )
            Spacer(Modifier.size(6.dp))
            Text(
                "Turno de ${jugador.nombre}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Dado(
            valor = estado.dado,
            habilitado = puedeTirar,
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer { rotationZ = giro.value }
                .clickable(enabled = puedeTirar, onClick = onLanzarDado)
        )

        Text(
            text = when (val f = estado.fase) {
                Fase.TirarDado -> "Toca el dado para lanzarlo"
                is Fase.MoverFicha -> "Arrastra tu ficha a la casilla ${f.destino}"
                is Fase.DecidirCompra -> "¿Comprar la propiedad?"
                is Fase.Terminado -> "Partida terminada"
            },
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** Dado dibujado con puntos (sin depender de fuentes con símbolos). */
@Composable
private fun Dado(valor: Int?, habilitado: Boolean, modifier: Modifier) {
    val alfa = if (habilitado) 1f else 0.85f
    Canvas(
        modifier
            .shadow(if (habilitado) 10.dp else 3.dp, RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = alfa), RoundedCornerShape(14.dp))
            .border(
                if (habilitado) 3.dp else 1.dp,
                if (habilitado) Color(0xFFFFD600) else Color.LightGray,
                RoundedCornerShape(14.dp)
            )
    ) {
        val r = size.minDimension * 0.09f
        val p = size.minDimension
        fun punto(fx: Float, fy: Float) =
            drawCircle(Color(0xFF212121), r, Offset(p * fx, p * fy))

        val bajo = 0.27f
        val medio = 0.5f
        val alto = 0.73f
        when (valor ?: 0) {
            1 -> punto(medio, medio)
            2 -> { punto(bajo, bajo); punto(alto, alto) }
            3 -> { punto(bajo, bajo); punto(medio, medio); punto(alto, alto) }
            4 -> { punto(bajo, bajo); punto(alto, bajo); punto(bajo, alto); punto(alto, alto) }
            5 -> {
                punto(bajo, bajo); punto(alto, bajo); punto(medio, medio)
                punto(bajo, alto); punto(alto, alto)
            }
            6 -> {
                punto(bajo, bajo); punto(alto, bajo); punto(bajo, medio)
                punto(alto, medio); punto(bajo, alto); punto(alto, alto)
            }
            else -> Unit // aún no se ha lanzado: dado en blanco
        }
    }
}

// ---------------------------------------------------------------------------
// Resumen de jugadores (lo que antes imprimía Jugador.resumen())
// ---------------------------------------------------------------------------

@Composable
private fun PanelJugadores(estado: EstadoJuego, tablero: Tablero) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        estado.jugadores.forEachIndexed { i, jugador ->
            val activo = i == estado.turno
            val props = estado.propiedadesDe(i)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (activo) Modifier.border(2.dp, ColoresJugador[i], CardDefaults.shape)
                        else Modifier
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(ColoresJugador[i], CircleShape)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            jugador.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        "$${jugador.saldo}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (jugador.saldo < 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Text("Casilla ${jugador.posicion}", fontSize = 10.sp)
                    Text(
                        if (props.isEmpty()) "Sin propiedades"
                        else props.joinToString(" ") { "P$it" },
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun Bitacora(mensajes: List<String>, modifier: Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            reverseLayout = true, // lo más reciente queda abajo y siempre visible
            modifier = Modifier.fillMaxHeight().padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            items(mensajes.asReversed()) { m ->
                Text(m, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Diálogos
// ---------------------------------------------------------------------------

@Composable
private fun DialogoCompra(
    jugador: Jugador,
    propiedad: CasillaPropiedad,
    onDecidir: (Boolean) -> Unit
) {
    val alcanza = jugador.saldo >= propiedad.precio
    AlertDialog(
        onDismissRequest = { /* hay que elegir una opción */ },
        title = { Text(propiedad.nombre) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Precio: $${propiedad.precio}")
                Text("Alquiler que cobrarás: $${propiedad.alquiler}")
                Text("Tu saldo: $${jugador.saldo}")
                if (!alcanza) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "No tienes saldo suficiente para comprarla.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDecidir(true) }, enabled = alcanza) { Text("Comprar") }
        },
        dismissButton = {
            TextButton(onClick = { onDecidir(false) }) { Text("No comprar") }
        }
    )
}
