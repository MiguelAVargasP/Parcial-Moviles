package edu.upb.arcade

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.upb.arcade.core.ui.ArcadeTheme
import edu.upb.arcade.ui.HubViewModel
import edu.upb.arcade.ui.Pantalla
import edu.upb.arcade.ui.PantallaLogin
import edu.upb.arcade.ui.PantallaMenu
import edu.upb.arcade.ui.PantallaPerfil
import edu.upb.arcade.ui.PantallaRegistro

/** FragmentActivity (y no ComponentActivity) porque el lector de huella la necesita. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcadeTheme {
                HubApp()
            }
        }
    }
}

@Composable
fun HubApp(vm: HubViewModel = viewModel()) {
    when (vm.pantalla) {
        Pantalla.LOGIN -> PantallaLogin(vm)
        Pantalla.REGISTRO -> PantallaRegistro(vm)
        Pantalla.MENU -> PantallaMenu(vm)
        Pantalla.PERFIL -> PantallaPerfil(vm)
    }
}
