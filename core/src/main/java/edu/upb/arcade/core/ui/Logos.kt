package edu.upb.arcade.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import edu.upb.arcade.core.ArcadeContract.JuegoArcade

/** Logo de cada juego dibujado con Canvas (no depende de imágenes). */
@Composable
fun LogoJuego(juego: JuegoArcade, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        when (juego) {
            JuegoArcade.MONOPOLY -> logoMonopoly()
            JuegoArcade.TETRIS -> logoTetris()
            JuegoArcade.TRIQUI -> logoTriqui()
            JuegoArcade.CULEBRITA -> logoCulebrita()
        }
    }
}

private fun DrawScope.logoMonopoly() {
    val w = size.minDimension
    val o = Offset((size.width - w) / 2, (size.height - w) / 2)
    // casa
    val techo = Path().apply {
        moveTo(o.x + w * 0.50f, o.y + w * 0.12f)
        lineTo(o.x + w * 0.90f, o.y + w * 0.48f)
        lineTo(o.x + w * 0.10f, o.y + w * 0.48f)
        close()
    }
    drawPath(techo, ArcadeColores.Monopoly)
    drawRoundRect(
        Color.White, Offset(o.x + w * 0.20f, o.y + w * 0.46f), Size(w * 0.60f, w * 0.40f),
        CornerRadius(w * 0.04f)
    )
    drawRoundRect(
        ArcadeColores.Monopoly, Offset(o.x + w * 0.42f, o.y + w * 0.60f), Size(w * 0.16f, w * 0.26f),
        CornerRadius(w * 0.03f)
    )
    // moneda
    drawCircle(ArcadeColores.Dorado, w * 0.13f, Offset(o.x + w * 0.80f, o.y + w * 0.78f))
    drawCircle(Color(0xFFE0A800), w * 0.08f, Offset(o.x + w * 0.80f, o.y + w * 0.78f), style = Stroke(w * 0.025f))
}

private fun DrawScope.logoTetris() {
    val w = size.minDimension
    val c = w / 4.6f
    val ox = (size.width - c * 4) / 2
    val oy = (size.height - c * 3) / 2
    fun bloque(col: Int, fila: Int, color: Color) {
        drawRoundRect(
            color, Offset(ox + col * c + c * 0.06f, oy + fila * c + c * 0.06f),
            Size(c * 0.88f, c * 0.88f), CornerRadius(c * 0.18f)
        )
        drawRoundRect(
            Color.White.copy(alpha = 0.35f), Offset(ox + col * c + c * 0.18f, oy + fila * c + c * 0.16f),
            Size(c * 0.5f, c * 0.18f), CornerRadius(c * 0.09f)
        )
    }
    // pieza T arriba, pieza L abajo
    bloque(0, 0, Color(0xFF818CF8)); bloque(1, 0, Color(0xFF818CF8)); bloque(2, 0, Color(0xFF818CF8))
    bloque(1, 1, Color(0xFF818CF8))
    bloque(3, 0, ArcadeColores.Tetris); bloque(3, 1, ArcadeColores.Tetris); bloque(3, 2, ArcadeColores.Tetris)
    bloque(2, 2, ArcadeColores.Tetris)
    bloque(0, 2, Color(0xFF22D3EE)); bloque(1, 2, Color(0xFF22D3EE))
}

private fun DrawScope.logoTriqui() {
    val w = size.minDimension
    val o = Offset((size.width - w) / 2, (size.height - w) / 2)
    val grosor = w * 0.05f
    val lineas = Color.White.copy(alpha = 0.85f)
    for (i in 1..2) {
        val p = w * (0.12f + 0.76f * i / 3f)
        drawLine(lineas, Offset(o.x + p, o.y + w * 0.12f), Offset(o.x + p, o.y + w * 0.88f), grosor, StrokeCap.Round)
        drawLine(lineas, Offset(o.x + w * 0.12f, o.y + p), Offset(o.x + w * 0.88f, o.y + p), grosor, StrokeCap.Round)
    }
    val celda = w * 0.76f / 3f
    fun centro(col: Int, fila: Int) =
        Offset(o.x + w * 0.12f + celda * (col + 0.5f), o.y + w * 0.12f + celda * (fila + 0.5f))
    fun x(col: Int, fila: Int) {
        val c = centro(col, fila); val r = celda * 0.28f
        drawLine(ArcadeColores.Triqui, c - Offset(r, r), c + Offset(r, r), grosor * 1.3f, StrokeCap.Round)
        drawLine(ArcadeColores.Triqui, c + Offset(-r, r), c + Offset(r, -r), grosor * 1.3f, StrokeCap.Round)
    }
    fun oo(col: Int, fila: Int) =
        drawCircle(ArcadeColores.TriquiO, celda * 0.3f, centro(col, fila), style = Stroke(grosor * 1.2f))
    x(0, 0); oo(1, 0); x(1, 1); oo(2, 1); x(2, 2)
}

private fun DrawScope.logoCulebrita() {
    val w = size.minDimension
    val o = Offset((size.width - w) / 2, (size.height - w) / 2)
    val cuerpo = Path().apply {
        moveTo(o.x + w * 0.15f, o.y + w * 0.78f)
        cubicTo(o.x + w * 0.35f, o.y + w * 0.95f, o.x + w * 0.55f, o.y + w * 0.55f, o.x + w * 0.40f, o.y + w * 0.45f)
        cubicTo(o.x + w * 0.25f, o.y + w * 0.35f, o.x + w * 0.45f, o.y + w * 0.12f, o.x + w * 0.66f, o.y + w * 0.24f)
    }
    drawPath(cuerpo, ArcadeColores.Culebrita, style = Stroke(w * 0.13f, cap = StrokeCap.Round))
    val cabeza = Offset(o.x + w * 0.68f, o.y + w * 0.24f)
    drawCircle(ArcadeColores.Culebrita, w * 0.11f, cabeza)
    drawCircle(Color(0xFF06122B), w * 0.025f, cabeza + Offset(w * 0.03f, -w * 0.03f))
    // manzana
    drawCircle(Color(0xFFFF5A5F), w * 0.10f, Offset(o.x + w * 0.80f, o.y + w * 0.70f))
    drawLine(
        Color(0xFF22D3EE), Offset(o.x + w * 0.80f, o.y + w * 0.60f), Offset(o.x + w * 0.86f, o.y + w * 0.54f),
        w * 0.03f, StrokeCap.Round
    )
}
