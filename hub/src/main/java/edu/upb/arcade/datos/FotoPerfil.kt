package edu.upb.arcade.datos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

/**
 * Foto de perfil de cada usuario. Se guarda como archivo en el almacenamiento interno de la
 * app (filesDir/perfiles/<usuarioId>.jpg), recortada cuadrada y reducida, así pesa muy poco.
 * No hace falta tocar la base de datos: si el archivo existe, el usuario tiene foto.
 */
object FotoPerfil {
    private const val LADO_MAXIMO = 512
    private const val CALIDAD_JPEG = 85

    fun archivo(context: Context, usuarioId: Long): File =
        File(File(context.applicationContext.filesDir, "perfiles"), "$usuarioId.jpg")

    /** Lee la imagen elegida, la recorta cuadrada, la reduce y la guarda. Devuelve true si salió bien. */
    fun guardar(context: Context, usuarioId: Long, uri: Uri): Boolean {
        return try {
            val original = decodificar(context.applicationContext, uri) ?: return false
            val cuadrada = recortarCuadrada(original)
            val destino = archivo(context, usuarioId)
            destino.parentFile?.mkdirs()
            val temporal = File(destino.parentFile, "$usuarioId.tmp")
            temporal.outputStream().use { cuadrada.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, it) }
            if (!temporal.renameTo(destino)) {
                temporal.copyTo(destino, overwrite = true)
                temporal.delete()
            }
            true
        } catch (e: Exception) {
            false
        } catch (e: OutOfMemoryError) {
            false
        }
    }

    fun cargar(context: Context, usuarioId: Long): ImageBitmap? {
        val archivo = archivo(context, usuarioId)
        if (!archivo.exists()) return null
        return BitmapFactory.decodeFile(archivo.path)?.asImageBitmap()
    }

    fun borrar(context: Context, usuarioId: Long) {
        archivo(context, usuarioId).delete()
    }

    /** Decodifica la imagen ya reducida (sin cargar la foto completa de la cámara en memoria). */
    private fun decodificar(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder además respeta la rotación de la foto (EXIF)
            val fuente = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(fuente) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val menor = minOf(info.size.width, info.size.height)
                decoder.setTargetSampleSize((menor / LADO_MAXIMO).coerceAtLeast(1))
            }
        } else {
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
            val menor = minOf(medidas.outWidth, medidas.outHeight)
            if (menor <= 0) return null
            val opciones = BitmapFactory.Options().apply { inSampleSize = (menor / LADO_MAXIMO).coerceAtLeast(1) }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        }
    }

    /** Recorta el centro de la imagen en un cuadrado de máximo 512 px (fondo blanco por si tenía transparencia). */
    private fun recortarCuadrada(origen: Bitmap): Bitmap {
        val lado = minOf(origen.width, origen.height)
        val x = (origen.width - lado) / 2
        val y = (origen.height - lado) / 2
        val salidaLado = minOf(lado, LADO_MAXIMO)
        val salida = Bitmap.createBitmap(salidaLado, salidaLado, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(salida)
        lienzo.drawColor(Color.WHITE)
        lienzo.drawBitmap(
            origen,
            Rect(x, y, x + lado, y + lado),
            Rect(0, 0, salidaLado, salidaLado),
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        return salida
    }
}
