package edu.upb.arcade.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import edu.upb.arcade.core.ArcadeContract.JuegoArcade

/** Paleta "Cyber Arcade Neón": fondo oscuro profundo con identidad cromática vibrante por juego. */
object ArcadeColores {
    val Fondo = Color(0xFF090E1A)
    val FondoAlto = Color(0xFF10192D)
    val Superficie = Color(0xFF182442)
    val Texto = Color(0xFFF1F5F9)
    val TextoSuave = Color(0xFF94A3B8)
    val Dorado = Color(0xFFFBBF24)

    val Monopoly = Color(0xFF10B981)  // Verde esmeralda neón (dinero / bienes raíces)
    val Tetris = Color(0xFFA855F7)    // Púrpura neón synthwave
    val Triqui = Color(0xFFF43F5E)    // Rojo carmesí neón
    val TriquiO = Color(0xFF06B6D4)   // Cian neón (antagonista de Triqui)
    val Culebrita = Color(0xFF22C55E) // Verde lima arcade retro
    val Hub = Color(0xFF38BDF8)       // Cian eléctrico arcade

    fun de(juego: JuegoArcade): Color = when (juego) {
        JuegoArcade.MONOPOLY -> Monopoly
        JuegoArcade.TETRIS -> Tetris
        JuegoArcade.TRIQUI -> Triqui
        JuegoArcade.CULEBRITA -> Culebrita
    }

    val degradadoFondo = Brush.verticalGradient(listOf(FondoAlto, Fondo))
}

@Composable
fun ArcadeTheme(acento: Color = ArcadeColores.Hub, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = acento,
            onPrimary = Color(0xFF040814),
            secondary = ArcadeColores.Dorado,
            background = ArcadeColores.Fondo,
            onBackground = ArcadeColores.Texto,
            surface = ArcadeColores.Superficie,
            onSurface = ArcadeColores.Texto,
            surfaceVariant = ArcadeColores.FondoAlto,
            onSurfaceVariant = ArcadeColores.TextoSuave,
            error = Color(0xFFFF4D4D)
        ),
        content = content
    )
}
