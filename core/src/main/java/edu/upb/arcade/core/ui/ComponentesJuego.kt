package edu.upb.arcade.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upb.arcade.core.ArcadeClient

/** Barra superior común: logo, nombre del juego, jugador conectado y su mejor score. */
@Composable
fun BarraJugador(cliente: ArcadeClient, modifier: Modifier = Modifier, extra: String? = null) {
    val acento = ArcadeColores.de(cliente.juego)
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ArcadeColores.FondoAlto, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoJuego(cliente.juego, Modifier.size(36.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(cliente.juego.titulo, color = acento, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            val nick = cliente.nickname
            Text(
                text = when {
                    !cliente.conectado -> "Sin conexión con Arcade UPB"
                    nick == null -> "Sin sesión en Arcade UPB"
                    else -> "👤 $nick"
                },
                color = ArcadeColores.TextoSuave,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (cliente.nickname != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text("Mejor", color = ArcadeColores.TextoSuave, fontSize = 11.sp)
                Text("${cliente.mejorScore}", color = ArcadeColores.Dorado, fontWeight = FontWeight.Bold)
                if (extra != null) Text(extra, color = ArcadeColores.TextoSuave, fontSize = 11.sp)
            }
        } else {
            TextButton(onClick = { cliente.abrirHub(context) }) { Text("Iniciar sesión") }
        }
    }
}

/**
 * Diálogo de fin de partida. [guardado] = null si no había sesión (el score no se guardó).
 */
@Composable
fun DialogoFinPartida(
    titulo: String,
    puntos: Int,
    guardado: Boolean?,
    onOtraVez: () -> Unit,
    onSalir: () -> Unit,
    detalle: String? = null
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Score: $puntos", style = MaterialTheme.typography.headlineSmall, color = ArcadeColores.Dorado)
                if (detalle != null) Text(detalle)
                Text(
                    when (guardado) {
                        true -> "✅ Guardado en Arcade UPB junto con tu ubicación."
                        false -> "⚠️ No se pudo guardar: abre Arcade UPB e inicia sesión."
                        null -> "⚠️ Sin sesión: este score no se guardó."
                    },
                    color = ArcadeColores.TextoSuave,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = { Button(onClick = onOtraVez) { Text("Jugar otra vez") } },
        dismissButton = { TextButton(onClick = onSalir) { Text("Salir al menú") } }
    )
}
