package edu.upb.arcade.ui

import androidx.compose.foundation.layout.width
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import edu.upb.arcade.core.ArcadeContract.JuegoArcade
import edu.upb.arcade.core.ui.ArcadeColores
import edu.upb.arcade.core.ui.LogoJuego
import java.time.Instant
import java.time.ZoneOffset

fun Context.buscarActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.buscarActivity()
    else -> null
}

/** Encabezado con los 4 logos en cruz pequeña + título. */
@Composable
private fun Encabezado(subtitulo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(110.dp)) {
            LogoJuego(JuegoArcade.TETRIS, Modifier.size(36.dp).align(Alignment.TopCenter))
            LogoJuego(JuegoArcade.MONOPOLY, Modifier.size(36.dp).align(Alignment.CenterStart))
            LogoJuego(JuegoArcade.TRIQUI, Modifier.size(36.dp).align(Alignment.CenterEnd))
            LogoJuego(JuegoArcade.CULEBRITA, Modifier.size(36.dp).align(Alignment.BottomCenter))
        }
        Spacer(Modifier.height(8.dp))
        Text("ARCADE UPB", fontSize = 32.sp, fontWeight = FontWeight.Black, color = ArcadeColores.Hub, letterSpacing = 4.sp)
        Text(subtitulo, color = ArcadeColores.TextoSuave, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Fondo(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(ArcadeColores.degradadoFondo)
            .safeDrawingPadding()
    ) { content() }
}

@Composable
fun PantallaLogin(vm: HubViewModel) {
    val context = LocalContext.current
    val estadoHuella = remember { Huella.estado(context) }
    val usuarioHuella = remember(vm.pantalla) { vm.usuarioHuella }
    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }

    fun pedirHuella() {
        when (estadoHuella) {
            Huella.Estado.NO_SOPORTADO -> {
                vm.mostrarError("Este dispositivo no cuenta con sensor de huella dactilar.")
            }
            Huella.Estado.SIN_HUELLA_REGISTRADA -> {
                vm.mostrarError("El lector está presente pero no hay huellas registradas en Ajustes de Android (en emulador: Extended controls ⋯ › Fingerprint).")
            }
            Huella.Estado.DISPONIBLE -> {
                val activity = context.buscarActivity() ?: return
                val nick = usuarioHuella?.nickname
                if (nick == null) {
                    vm.mostrarError("No hay cuentas creadas en el dispositivo. Regístrate primero para vincular tu huella.")
                    return
                }
                Huella.pedir(
                    activity,
                    titulo = "Entrar a Arcade UPB",
                    subtitulo = "Pon tu dedo en el lector para entrar como $nick",
                    onExito = { vm.entrarConHuella() },
                    onError = { vm.mostrarError(it) }
                )
            }
        }
    }

    // Si la huella está lista y hay un usuario ya vinculado, solicitarla de una vez
    LaunchedEffect(Unit) {
        if (estadoHuella == Huella.Estado.DISPONIBLE && usuarioHuella != null) {
            pedirHuella()
        }
    }

    Fondo {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Encabezado("Inicia sesión para guardar tus scores")
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contrasena, onValueChange = { contrasena = it },
                label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            MensajeError(vm.error)

            Button(
                onClick = { vm.iniciarSesion(email, contrasena, estadoHuella == Huella.Estado.DISPONIBLE) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Entrar con contraseña", fontWeight = FontWeight.Bold) }

            // Opción de ingreso biométrico SIEMPRE VISIBLE
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(ArcadeColores.TextoSuave.copy(alpha = 0.35f)))
                Text("  o con huella  ", color = ArcadeColores.TextoSuave, fontSize = 12.sp)
                Box(Modifier.weight(1f).height(1.dp).background(ArcadeColores.TextoSuave.copy(alpha = 0.35f)))
            }

            OutlinedButton(
                onClick = { pedirHuella() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                val etiqueta = when {
                    usuarioHuella != null -> "Entrar con huella (${usuarioHuella.nickname})"
                    else -> "Ingresar con huella dactilar"
                }
                Text(etiqueta, fontWeight = FontWeight.Bold, color = ArcadeColores.Texto)
            }

            TextButton(onClick = vm::irARegistro) { Text("¿No tienes cuenta? Regístrate") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(vm: HubViewModel) {
    val context = LocalContext.current
    val estadoHuella = remember { Huella.estado(context) }
    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf<String?>(null) }
    var vincularHuella by rememberSaveable { mutableStateOf(estadoHuella != Huella.Estado.NO_SOPORTADO) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    Fondo {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Encabezado("Crea tu cuenta")
            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmar, onValueChange = { confirmar = it }, label = { Text("Confirmar contraseña") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it.take(15) }, label = { Text("Nickname") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            // Campo de fecha: al tocarlo se abre el calendario
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fecha ?: "", onValueChange = {}, readOnly = true,
                    label = { Text("Fecha de nacimiento") },
                    placeholder = { Text("Toca para elegir") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { mostrarCalendario = true }
                )
            }

            // Opción para activar huella directamente desde el registro
            if (estadoHuella != Huella.Estado.NO_SOPORTADO) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { vincularHuella = !vincularHuella }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = vincularHuella,
                        onCheckedChange = { vincularHuella = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Activar ingreso con huella para esta cuenta",
                        color = ArcadeColores.Texto,
                        fontSize = 13.sp
                    )
                }
            }

            MensajeError(vm.error)
            Button(
                onClick = {
                    vm.registrar(
                        email = email,
                        contrasena = contrasena,
                        confirmar = confirmar,
                        nickname = nickname,
                        fecha = fecha,
                        huellaDisponible = estadoHuella == Huella.Estado.DISPONIBLE,
                        vincularHuella = vincularHuella
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Crear cuenta", fontWeight = FontWeight.Bold) }
            TextButton(onClick = vm::irALogin) { Text("Ya tengo cuenta") }
        }
    }

    if (mostrarCalendario) {
        val estadoFecha = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { ms ->
                        fecha = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = estadoFecha)
        }
    }
}

@Composable
private fun MensajeError(error: String?) {
    if (error != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
    }
}

/** Diálogo que ofrece activar la huella después de entrar con contraseña. */
@Composable
fun DialogoActivarHuella(vm: HubViewModel) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = vm::descartarHuella,
        title = { Text("¿Entrar con huella?") },
        text = { Text("La próxima vez podrás entrar a Arcade UPB solo con tu huella. Se guarda únicamente en este teléfono.") },
        confirmButton = {
            Button(onClick = {
                val activity = context.buscarActivity()
                if (activity == null) {
                    vm.descartarHuella()
                } else {
                    Huella.pedir(
                        activity,
                        titulo = "Activar huella",
                        subtitulo = "Confirma con tu huella",
                        onExito = vm::activarHuella,
                        onError = { vm.descartarHuella() }
                    )
                }
            }) { Text("Activar") }
        },
        dismissButton = { TextButton(onClick = vm::descartarHuella) { Text("Ahora no") } }
    )
}