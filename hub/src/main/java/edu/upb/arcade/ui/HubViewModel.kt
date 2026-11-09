package edu.upb.arcade.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.datos.FotoPerfil
import edu.upb.arcade.datos.RegistroScore
import edu.upb.arcade.datos.Repositorio
import edu.upb.arcade.datos.Sesion
import edu.upb.arcade.datos.Usuario
import edu.upb.arcade.datos.Validaciones
import edu.upb.arcade.ubicacion.Ubicador
import java.time.LocalDate
import kotlin.concurrent.thread

enum class Pantalla { LOGIN, REGISTRO, MENU, PERFIL }

class HubViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repositorio.obtener(app)
    private val sesion = Sesion(app)
    private val ubicador = Ubicador(app)

    var pantalla by mutableStateOf(Pantalla.LOGIN)
        private set
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var mejores by mutableStateOf<Map<String, RegistroScore>>(emptyMap())
        private set
    var historial by mutableStateOf<List<RegistroScore>>(emptyList())
        private set
    var ciudadActual by mutableStateOf(ubicador.ultimaCiudad())
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Mensaje de éxito (por ejemplo "Datos actualizados"). */
    var aviso by mutableStateOf<String?>(null)
        private set

    /** Foto de perfil del usuario actual (null = sin foto, se muestra la inicial). */
    var foto by mutableStateOf<ImageBitmap?>(null)
        private set

    /** Sube cada vez que hay un mensaje nuevo, para que la pantalla de perfil vuelva arriba a mostrarlo. */
    var eventos by mutableIntStateOf(0)
        private set

    /** Se muestra después de entrar con contraseña, para ofrecer activar la huella. */
    var ofrecerHuella by mutableStateOf(false)
        private set

    /** Usuario que puede entrar con huella en este teléfono (para el botón del login). */
    val usuarioHuella: Usuario?
        get() = sesion.usuarioHuella()?.let { repo.usuarioPorId(it) }

    // ---------------------------------------------------------------- navegación

    fun irARegistro() { error = null; pantalla = Pantalla.REGISTRO }
    fun irALogin() { error = null; pantalla = Pantalla.LOGIN }

    // ---------------------------------------------------------------- login

    fun iniciarSesion(email: String, contrasena: String, huellaDisponible: Boolean) {
        val u = repo.login(email, contrasena)
        if (u == null) {
            error = "Email o contraseña incorrectos"
            return
        }
        entrar(u)
        ofrecerHuella = huellaDisponible && sesion.usuarioHuella() != u.id
    }

    fun registrar(
        email: String, contrasena: String, confirmar: String, nickname: String, fecha: String?,
        huellaDisponible: Boolean, vincularHuella: Boolean = false
    ) {
        val problema = Validaciones.email(email)
            ?: Validaciones.contrasena(contrasena)
            ?: (if (contrasena != confirmar) "Las contraseñas no coinciden" else null)
            ?: Validaciones.nickname(nickname)
            ?: Validaciones.fechaNacimiento(fecha, LocalDate.now().toString())
        if (problema != null) {
            error = problema
            return
        }
        when (val r = repo.registrar(email, contrasena, nickname, fecha!!)) {
            is Repositorio.ResultadoRegistro.Error -> error = r.mensaje
            is Repositorio.ResultadoRegistro.Ok -> {
                entrar(r.usuario)
                // Si marcó "Activar ingreso con huella", se le pide confirmar con su huella
                ofrecerHuella = huellaDisponible && vincularHuella
            }
        }
    }

    /** Llamado cuando el lector de huella confirmó la identidad. */
    fun entrarConHuella() {
        val u = usuarioHuella
        if (u == null) {
            error = "No hay ninguna cuenta con huella activada"
            return
        }
        entrar(u)
    }

    fun activarHuella() {
        usuario?.let { sesion.activarHuella(it.id) }
        ofrecerHuella = false
    }

    fun descartarHuella() { ofrecerHuella = false }

    fun mostrarError(mensaje: String) { error = mensaje }

    fun cerrarSesion() {
        sesion.cerrar()
        usuario = null
        foto = null
        aviso = null
        pantalla = Pantalla.LOGIN
    }

    private fun entrar(u: Usuario) {
        sesion.iniciar(u.id)
        usuario = u
        error = null
        aviso = null
        foto = FotoPerfil.cargar(getApplication<Application>(), u.id)
        pantalla = Pantalla.MENU
        recargar()
    }

    // ---------------------------------------------------------------- perfil

    fun irAPerfil() { error = null; aviso = null; pantalla = Pantalla.PERFIL }

    fun volverAlMenu() {
        error = null
        aviso = null
        pantalla = Pantalla.MENU
        recargar()
    }

    private fun notificar(error: String?, aviso: String?) {
        this.error = error
        this.aviso = aviso
        eventos++
    }

    fun guardarDatos(nickname: String, fecha: String?) {
        val u = usuario ?: return
        val problema = Validaciones.nickname(nickname)
            ?: Validaciones.fechaNacimiento(fecha, LocalDate.now().toString())
        if (problema != null) {
            notificar(problema, null)
            return
        }
        if (repo.actualizarDatos(u.id, nickname, fecha!!)) {
            usuario = repo.usuarioPorId(u.id)
            notificar(null, "Datos actualizados")
        } else {
            notificar("No se pudieron guardar los cambios", null)
        }
    }

    /** Devuelve true si la contraseña quedó cambiada (para que la pantalla limpie los campos). */
    fun cambiarContrasena(actual: String, nueva: String, confirmar: String): Boolean {
        val u = usuario ?: return false
        val problema = (if (actual.isEmpty()) "Escribe tu contraseña actual" else null)
            ?: Validaciones.contrasena(nueva)
            ?: (if (nueva != confirmar) "Las contraseñas nuevas no coinciden" else null)
        if (problema != null) {
            notificar(problema, null)
            return false
        }
        return if (repo.cambiarContrasena(u.id, actual, nueva)) {
            notificar(null, "Contraseña cambiada")
            true
        } else {
            notificar("La contraseña actual es incorrecta", null)
            false
        }
    }

    /** Procesa la imagen elegida (recorte y reducción) en un hilo aparte para no congelar la pantalla. */
    fun cambiarFoto(uri: Uri) {
        val u = usuario ?: return
        val app = getApplication<Application>()
        thread {
            val ok = FotoPerfil.guardar(app, u.id, uri)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (usuario?.id != u.id) return@post // cerraron sesión mientras se procesaba
                if (ok) {
                    foto = FotoPerfil.cargar(app, u.id)
                    notificar(null, "Foto actualizada")
                } else {
                    notificar("No se pudo usar esa imagen", null)
                }
            }
        }
    }

    fun quitarFoto() {
        val u = usuario ?: return
        FotoPerfil.borrar(getApplication<Application>(), u.id)
        foto = null
        notificar(null, "Foto eliminada")
    }

    // ---------------------------------------------------------------- menú

    /** Vuelve a leer scores (por ejemplo al regresar de un juego). */
    fun recargar() {
        val id = usuario?.id ?: return
        usuario = repo.usuarioPorId(id)
        mejores = repo.mejoresPorJuego(id)
        historial = repo.historial(id)
        ciudadActual = ubicador.ultimaCiudad()
    }

    /** Toma una lectura nueva del GPS (se llama al abrir el menú y al dar permiso). */
    fun actualizarUbicacion() {
        ubicador.obtenerCiudad { ciudad ->
            if (ciudad != null) {
                // volvemos al hilo principal para tocar el estado de Compose
                android.os.Handler(android.os.Looper.getMainLooper()).post { ciudadActual = ciudad }
            }
        }
    }

    fun mejorDe(juego: JuegoArcade): RegistroScore? = mejores[juego.id]
}