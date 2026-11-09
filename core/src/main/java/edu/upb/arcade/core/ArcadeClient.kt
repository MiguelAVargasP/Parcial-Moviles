package edu.upb.arcade.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Lo usa cada juego para conectarse al servicio de la app central (bound service + AIDL).
 * Expone estado de Compose, así la UI se actualiza sola al conectarse.
 */
class ArcadeClient(context: Context, val juego: ArcadeContract.JuegoArcade) {

    private val appContext = context.applicationContext
    private var servicio: IArcadeService? = null
    private var vinculado = false

    /** El hub está instalado y nos conectamos a su servicio. */
    var conectado by mutableStateOf(false)
        private set

    /** Nickname de la sesión del hub (null = nadie ha iniciado sesión). */
    var nickname by mutableStateOf<String?>(null)
        private set

    var mejorScore by mutableIntStateOf(0)
        private set

    private val conexion = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            servicio = IArcadeService.Stub.asInterface(binder)
            conectado = true
            refrescar()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            servicio = null
            conectado = false
        }
    }

    fun conectar() {
        if (vinculado) return
        val intent = Intent(ArcadeContract.ACCION_SERVICIO).setPackage(ArcadeContract.PAQUETE_HUB)
        var flags = Context.BIND_AUTO_CREATE
        // Le "presta" al hub el permiso de primer plano: así puede leer el GPS
        // mientras el juego está en pantalla.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) flags = flags or Context.BIND_INCLUDE_CAPABILITIES
        vinculado = try {
            appContext.bindService(intent, conexion, flags)
        } catch (e: SecurityException) {
            false
        }
    }

    fun desconectar() {
        if (vinculado) {
            runCatching { appContext.unbindService(conexion) }
            vinculado = false
        }
        servicio = null
        conectado = false
    }

    fun refrescar() {
        val s = servicio ?: return
        try {
            nickname = s.nickname
            mejorScore = s.getMejorScore(juego.id)
        } catch (e: RemoteException) {
            conectado = false
        }
    }

    /** Envía el score al hub. Devuelve true si quedó guardado. */
    fun guardarScore(puntos: Int): Boolean {
        val s = servicio ?: return false
        return try {
            val ok = s.guardarScore(juego.id, puntos.coerceAtLeast(0))
            refrescar()
            ok
        } catch (e: RemoteException) {
            false
        }
    }

    /** Abre la app central (para iniciar sesión, por ejemplo). */
    fun abrirHub(context: Context): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(ArcadeContract.PAQUETE_HUB)
            ?: return false
        context.startActivity(intent)
        return true
    }
}

/** Crea el cliente, lo conecta mientras la pantalla existe y refresca al volver al juego. */
@Composable
fun rememberArcadeClient(juego: ArcadeContract.JuegoArcade): ArcadeClient {
    val context = LocalContext.current
    val cliente = remember { ArcadeClient(context, juego) }
    DisposableEffect(cliente) {
        cliente.conectar()
        onDispose { cliente.desconectar() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { cliente.refrescar() }
    return cliente
}
