package edu.upb.arcade.monopoly.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Color de la ficha de cada jugador (máximo 4): tonos vibrantes y contrastantes. */
val ColoresJugador = listOf(
    Color(0xFFEF4444), // Rojo vibrante
    Color(0xFF38BDF8), // Cian eléctrico
    Color(0xFF10B981), // Verde esmeralda
    Color(0xFFFBBF24)  // Oro ámbar
)

val ColorSalida = Color(0xFFA7F3D0)    // Verde menta suave
val ColorSuerte = Color(0xFFC7D2FE)    // Índigo lavanda suave
val ColorPropiedad = Color(0xFFF1F5F9) // Slate claro
val ColorTablero = Color(0xFF0F172A)   // Pizarra oscuro profundo cyber

@Composable
fun BienesRaicesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1565C0),
            secondary = Color(0xFF3949AB),
            background = Color(0xFFEEF4FB),
            surface = Color(0xFFFFFFFF)
        ),
        content = content
    )
}
