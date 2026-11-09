package edu.upb.arcade.culebrita

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import edu.upb.arcade.culebrita.logica.Direccion
import edu.upb.arcade.culebrita.logica.direccionPorInclinacion

/**
 * Lee la inclinación del teléfono para mover la culebrita.
 *
 * Usa el sensor TYPE_GAME_ROTATION_VECTOR, que Android calcula con el GIROSCOPIO
 * (más el acelerómetro): responde rápido y sin temblores. Si el teléfono no tiene
 * giroscopio, usa solo el acelerómetro.
 */
class ControlGiroscopio(context: Context) : SensorEventListener {

    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorRotacion: Sensor? = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val sensorAcel: Sensor? = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val tieneGiroscopio: Boolean = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null && sensorRotacion != null

    val nombreSensor: String = when {
        tieneGiroscopio -> "Giroscopio"
        sensorAcel != null -> "Acelerómetro (sin giroscopio)"
        else -> "Sin sensores"
    }

    /** Inclinación actual relativa a la neutral (para dibujar el indicador). */
    var inclinacion by mutableStateOf(Offset.Zero)
        private set

    var onDireccion: (Direccion) -> Unit = {}

    private var neutral: Offset? = null
    private val matriz = FloatArray(9)

    fun iniciar() {
        neutral = null // se calibra con la primera lectura
        val sensor = if (tieneGiroscopio) sensorRotacion else sensorAcel
        sensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun detener() = sm.unregisterListener(this)

    /** Toma la posición actual del teléfono como "recta". */
    fun calibrar() {
        neutral = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Gravedad en los ejes del teléfono (m/s²)
        val gravedad = if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(matriz, event.values)
            // La 3.ª fila de la matriz es el "arriba" del mundo visto desde el teléfono
            Offset(matriz[6] * SensorManager.GRAVITY_EARTH, matriz[7] * SensorManager.GRAVITY_EARTH)
        } else {
            Offset(event.values[0], event.values[1])
        }
        val base = neutral ?: gravedad.also { neutral = it }
        val delta = gravedad - base
        // Filtro pasa-bajas para eliminar micro-temblores manteniendo respuesta instantánea
        val suave = Offset(
            inclinacion.x * 0.65f + delta.x * 0.35f,
            inclinacion.y * 0.65f + delta.y * 0.35f
        )
        inclinacion = suave
        direccionPorInclinacion(suave.x, suave.y)?.let { onDireccion(it) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
