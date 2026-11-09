package edu.upb.arcade.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upb.arcade.core.ui.ArcadeColores
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Perfil del usuario: foto, nickname, fecha de nacimiento y contraseña.
 * El email se muestra pero no se puede cambiar (es la identidad de la cuenta).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(vm: HubViewModel) {
    val usuario = vm.usuario ?: return
    BackHandler(onBack = vm::volverAlMenu)

    var nickname by rememberSaveable(usuario.id) { mutableStateOf(usuario.nickname) }
    var fecha by rememberSaveable(usuario.id) { mutableStateOf<String?>(usuario.fechaNacimiento) }
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var mostrarCalendario by remember { mutableStateOf(false) }

    // Selector de fotos del sistema: no necesita ningún permiso
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.cambiarFoto(uri)
    }
    fun elegirFoto() {
        selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val scroll = rememberScrollState()
    // Cada mensaje nuevo (error o éxito) se muestra arriba: volvemos arriba para que se vea
    LaunchedEffect(vm.eventos) { if (vm.eventos > 0) scroll.animateScrollTo(0) }

    Box(
        Modifier
            .fillMaxSize()
            .background(ArcadeColores.degradadoFondo)
            .safeDrawingPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = vm::volverAlMenu) { Text("← Volver") }
            }
            Text("Mi perfil", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ArcadeColores.Hub)

            MensajeEstado(error = vm.error, aviso = vm.aviso)

            // ------------------------------------------------------------ foto
            Box(
                Modifier
                    .size(128.dp)
                    .border(3.dp, ArcadeColores.Hub, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ArcadeColores.Hub, ArcadeColores.Triqui)))
                    .clickable { elegirFoto() },
                contentAlignment = Alignment.Center
            ) {
                val foto = vm.foto
                if (foto != null) {
                    Image(
                        bitmap = foto,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        usuario.nickname.first().uppercase(),
                        fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color.White
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { elegirFoto() }) {
                    Text(if (vm.foto == null) "Agregar foto" else "Cambiar foto")
                }
                if (vm.foto != null) {
                    TextButton(onClick = vm::quitarFoto) { Text("Quitar") }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ------------------------------------------------------------ datos
            Seccion("Mis datos")
            OutlinedTextField(
                value = usuario.email, onValueChange = {}, enabled = false, singleLine = true,
                label = { Text("Email (no se puede cambiar)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it.take(15) }, singleLine = true,
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth()
            )
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
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            mostrarCalendario = true
                        }
                )
            }
            Button(
                onClick = { vm.guardarDatos(nickname, fecha) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Guardar cambios", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))

            // ------------------------------------------------------------ contraseña
            Seccion("Cambiar contraseña")
            OutlinedTextField(
                value = actual, onValueChange = { actual = it }, singleLine = true,
                label = { Text("Contraseña actual") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nueva, onValueChange = { nueva = it }, singleLine = true,
                label = { Text("Contraseña nueva") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmar, onValueChange = { confirmar = it }, singleLine = true,
                label = { Text("Confirmar contraseña nueva") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = {
                    if (vm.cambiarContrasena(actual, nueva, confirmar)) {
                        actual = ""; nueva = ""; confirmar = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (mostrarCalendario) {
        val milisActuales = fecha?.let {
            runCatching { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
        }
        val estadoFecha = rememberDatePickerState(initialSelectedDateMillis = milisActuales)
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
private fun Seccion(titulo: String) {
    Text(
        titulo, color = ArcadeColores.Texto, fontWeight = FontWeight.Bold, fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MensajeEstado(error: String?, aviso: String?) {
    if (error != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) { Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
    } else if (aviso != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(ArcadeColores.Hub.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) { Text(aviso, color = ArcadeColores.Texto, fontSize = 14.sp) }
    }
}
