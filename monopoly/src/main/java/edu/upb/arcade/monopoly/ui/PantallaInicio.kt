package edu.upb.arcade.monopoly.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import edu.upb.arcade.core.ui.ArcadeColores
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.upb.arcade.monopoly.modelo.Juego
import edu.upb.arcade.monopoly.modelo.Jugador

@Composable
fun PantallaInicio(
    nombreJugador1: String?,
    cabecera: @Composable () -> Unit,
    onEmpezar: (List<String>) -> Unit
) {
    var cantidad by rememberSaveable { mutableIntStateOf(2) }
    val nombres = remember { mutableStateListOf("", "", "", "") }

    LaunchedEffect(nombreJugador1) {
        if (nombreJugador1 != null && nombres[0].isBlank()) nombres[0] = nombreJugador1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        cabecera()
        Text(
            "Compra y Venta de\nBienes Raíces",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Cada jugador empieza con $${Jugador.SALDO_INICIAL}. Se juegan 10 rondas " +
                    "y gana quien termine con más dinero. El saldo final del jugador 1 " +
                    "(rojo) es el score que se guarda en Arcade UPB.",
            textAlign = TextAlign.Center,
            color = ArcadeColores.Texto,
            style = MaterialTheme.typography.bodyMedium
        )

        Text("¿Cuántos jugadores?", color = ArcadeColores.Texto, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (n in Juego.MIN_JUGADORES..Juego.MAX_JUGADORES) {
                FilterChip(
                    selected = cantidad == n,
                    onClick = { cantidad = n },
                    label = { Text("$n") }
                )
            }
        }

        for (i in 0 until cantidad) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(ColoresJugador[i], CircleShape)
                )
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = nombres[i],
                    onValueChange = { nombres[i] = it.take(12) },
                    label = { Text("Nombre del jugador ${i + 1}") },
                    placeholder = { Text("Jugador ${i + 1}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = { onEmpezar(nombres.take(cantidad)) },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Empezar partida")
        }
    }
}