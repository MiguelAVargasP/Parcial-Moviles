package edu.upb.arcade.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.ui.ArcadeColores
import edu.upb.arcade.core.ui.LogoJuego
import edu.upb.arcade.datos.RegistroScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaMenu(vm: HubViewModel) {
    val context = LocalContext.current
    val usuario = vm.usuario ?: return
    var instalados by remember { mutableStateOf(emptySet<String>()) }

    fun revisarInstalados() {
        instalados = JuegoArcade.entries
            .filter { context.packageManager.getLaunchIntentForPackage(it.paquete) != null }
            .map { it.id }.toSet()
    }

    // Al volver de un juego: recargar scores y revisar qué juegos están instalados
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.recargar()
        revisarInstalados()
    }

    // Permiso de ubicación (para registrar la ciudad junto al score)
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.actualizarUbicacion() }
    LaunchedEffect(Unit) {
        pedirPermiso.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    fun abrir(juego: JuegoArcade) {
        val intent = context.packageManager.getLaunchIntentForPackage(juego.paquete)
        if (intent == null) {
            Toast.makeText(context, "${juego.titulo} no está instalado. Ejecútalo desde Android Studio.", Toast.LENGTH_LONG).show()
        } else {
            context.startActivity(intent)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(ArcadeColores.degradadoFondo)
            .safeDrawingPadding()
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Encabezado
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("ARCADE UPB", color = ArcadeColores.Hub, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 3.sp)
                    Text("Elige un juego", color = ArcadeColores.TextoSuave, fontSize = 13.sp)
                }
                OutlinedButton(onClick = vm::irAPerfil) { Text("Perfil") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = vm::cerrarSesion) { Text("Salir") }
            }

            // Cruz: cuadrícula de 3x3 donde solo se usan el centro y los 4 lados (las esquinas quedan
            // vacías). Filas y columnas reparten el espacio por igual (weight) y el contenedor es
            // cuadrado, así cada casilla queda cuadrada en cualquier pantalla sin medir el ancho.
            val separacion = 6.dp
            Column(Modifier.fillMaxWidth().aspectRatio(1f)) {
                // fila de arriba
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    TarjetaJuego(
                        JuegoArcade.TETRIS, vm.mejorDe(JuegoArcade.TETRIS), JuegoArcade.TETRIS.id in instalados,
                        Modifier.weight(1f).fillMaxHeight().padding(separacion)
                    ) { abrir(JuegoArcade.TETRIS) }
                    Spacer(Modifier.weight(1f))
                }
                // fila del medio
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    TarjetaJuego(
                        JuegoArcade.MONOPOLY, vm.mejorDe(JuegoArcade.MONOPOLY), JuegoArcade.MONOPOLY.id in instalados,
                        Modifier.weight(1f).fillMaxHeight().padding(separacion)
                    ) { abrir(JuegoArcade.MONOPOLY) }
                    CentroJugador(
                        nickname = usuario.nickname,
                        scoreTotal = usuario.score,
                        ciudad = vm.ciudadActual,
                        foto = vm.foto,
                        onPerfil = vm::irAPerfil,
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(separacion)
                    )
                    TarjetaJuego(
                        JuegoArcade.TRIQUI, vm.mejorDe(JuegoArcade.TRIQUI), JuegoArcade.TRIQUI.id in instalados,
                        Modifier.weight(1f).fillMaxHeight().padding(separacion)
                    ) { abrir(JuegoArcade.TRIQUI) }
                }
                // fila de abajo
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    TarjetaJuego(
                        JuegoArcade.CULEBRITA, vm.mejorDe(JuegoArcade.CULEBRITA), JuegoArcade.CULEBRITA.id in instalados,
                        Modifier.weight(1f).fillMaxHeight().padding(separacion)
                    ) { abrir(JuegoArcade.CULEBRITA) }
                    Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Últimas partidas", color = ArcadeColores.Texto, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (vm.historial.isEmpty()) {
                Text(
                    "Todavía no has jugado. ¡Toca un juego para empezar!",
                    color = ArcadeColores.TextoSuave, fontSize = 13.sp
                )
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(vm.historial, key = { it.id }) { FilaHistorial(it) }
            }
        }
    }

    if (vm.ofrecerHuella) DialogoActivarHuella(vm)
}

@Composable
private fun TarjetaJuego(
    juego: JuegoArcade,
    mejor: RegistroScore?,
    instalado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val color = ArcadeColores.de(juego)
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.92f else 1f, label = "escala")

    Column(
        modifier = modifier
            .scale(escala)
            .alpha(if (instalado) 1f else 0.55f)
            .background(
                Brush.verticalGradient(listOf(color.copy(alpha = 0.30f), ArcadeColores.Superficie)),
                RoundedCornerShape(22.dp)
            )
            .border(BorderStroke(2.dp, color.copy(alpha = 0.8f)), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interaccion, indication = null, onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LogoJuego(juego, Modifier.weight(1f).aspectRatio(1f))
        Text(juego.titulo, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
        // Score y ciudad debajo del logo
        if (mejor != null) {
            Text(" ${mejor.puntos}", color = ArcadeColores.Dorado, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                " ${mejor.ciudad}", color = ArcadeColores.TextoSuave, fontSize = 9.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                if (instalado) "Sin partidas" else "No instalado",
                color = ArcadeColores.TextoSuave, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CentroJugador(
    nickname: String, scoreTotal: Int, ciudad: String?,
    foto: ImageBitmap?, onPerfil: () -> Unit, modifier: Modifier
) {
    val brillo by rememberInfiniteTransition(label = "brillo").animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "alfa"
    )
    Column(
        modifier = modifier.clickable(onClick = onPerfil), // tocar al jugador abre su perfil
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(56.dp)
                .border(3.dp, ArcadeColores.Hub.copy(alpha = brillo), CircleShape)
                .padding(4.dp)
                .background(
                    Brush.linearGradient(listOf(ArcadeColores.Hub, ArcadeColores.Triqui)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (foto != null) {
                Image(
                    bitmap = foto,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(nickname.first().uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            nickname, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp, color = ArcadeColores.Texto
        )
        Text(" $scoreTotal", color = ArcadeColores.Dorado, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            " ${ciudad ?: "Ubicando…"}", color = ArcadeColores.TextoSuave, fontSize = 9.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FilaHistorial(r: RegistroScore) {
    val juego = JuegoArcade.porId(r.juego) ?: return
    val formato = remember { SimpleDateFormat("dd MMM · HH:mm", Locale.forLanguageTag("es-CO")) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(ArcadeColores.Superficie, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoJuego(juego, Modifier.size(28.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(juego.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                " ${r.ciudad} · ${formato.format(Date(r.fecha))}",
                color = ArcadeColores.TextoSuave, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Text("${r.puntos}", color = ArcadeColores.Dorado, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}