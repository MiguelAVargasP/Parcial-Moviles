package edu.upb.arcade.monopoly

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.rememberArcadeClient
import edu.upb.arcade.core.ui.ArcadeColores
import edu.upb.arcade.core.ui.ArcadeTheme
import edu.upb.arcade.core.ui.BarraJugador
import edu.upb.arcade.core.ui.DialogoFinPartida
import edu.upb.arcade.monopoly.modelo.Fase
import edu.upb.arcade.monopoly.ui.JuegoViewModel
import edu.upb.arcade.monopoly.ui.PantallaInicio
import edu.upb.arcade.monopoly.ui.PantallaJuego

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcadeTheme(acento = ArcadeColores.Monopoly) {
                App()
            }
        }
    }
}

@Composable
fun App(vm: JuegoViewModel = viewModel()) {
    val cliente = rememberArcadeClient(JuegoArcade.MONOPOLY)
    val activity = LocalContext.current as? Activity

    Box(Modifier.fillMaxSize().background(ArcadeColores.Fondo)) {
        val estado = vm.estado
        if (estado == null) {
            PantallaInicio(
                nombreJugador1 = cliente.nickname,
                cabecera = { BarraJugador(cliente) },
                onEmpezar = vm::empezar
            )
        } else {
            // Al terminar la partida se envía el score una sola vez
            val terminado = estado.fase as? Fase.Terminado
            LaunchedEffect(terminado != null) {
                if (terminado != null) {
                    vm.enviarScore { puntos -> if (cliente.nickname == null) null else cliente.guardarScore(puntos) }
                }
            }
            PantallaJuego(
                estado = estado,
                tablero = vm.juego.tablero,
                rondasTotales = vm.juego.rondasTotales,
                onLanzarDado = vm::lanzarDado,
                onSoltarFicha = vm::soltarFicha,
                onDecidirCompra = vm::decidirCompra,
                cabecera = { BarraJugador(cliente) },
                finPartida = {
                    val ganador = estado.jugadores[terminado?.ganador ?: 0]
                    DialogoFinPartida(
                        titulo = "¡Ganador: ${ganador.nombre}!",
                        puntos = vm.puntosJugador1(),
                        guardado = vm.scoreGuardado,
                        detalle = "Ranking: " + estado.jugadores
                            .sortedByDescending { it.saldo }
                            .joinToString("  ·  ") { "${it.nombre} $${it.saldo}" },
                        onOtraVez = vm::volverAlInicio,
                        onSalir = { activity?.finish() }
                    )
                }
            )
        }
    }
}
