package edu.upb.arcade.ubicacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Lee el GPS y lo convierte en "Ciudad, País" con Geocoder.
 * Guarda la última ciudad conocida para usarla mientras llega la nueva lectura.
 */
class Ubicador(context: Context) {

    private val ctx = context.applicationContext
    private val lm = ctx.getSystemService(LocationManager::class.java)
    private val prefs = ctx.getSharedPreferences("arcade_ubicacion", Context.MODE_PRIVATE)

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun ultimaCiudad(): String? = prefs.getString("ultima_ciudad", null)

    /** Pide una lectura nueva del GPS. [alTerminar] se llama en un hilo de fondo. */
    @SuppressLint("MissingPermission")
    fun obtenerCiudad(alTerminar: (String?) -> Unit) {
        if (!tienePermiso() || lm == null) {
            alTerminar(null)
            return
        }
        val proveedor = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                alTerminar(null)
                return
            }
        }

        val entregar: (Location?) -> Unit = { lectura ->
            hilo.execute {
                val ubicacion = lectura ?: ultimaConocida()
                val ciudad = ubicacion?.let { geocodificar(it) }
                if (ciudad != null) prefs.edit { putString("ultima_ciudad", ciudad) }
                alTerminar(ciudad)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            lm.getCurrentLocation(proveedor, null, hilo) { entregar(it) }
        } else {
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(proveedor, object : LocationListener {
                override fun onLocationChanged(location: Location) = entregar(location)
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                @Deprecated("Solo para versiones viejas de Android")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }, Looper.getMainLooper())
        }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(): Location? =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { p -> runCatching { lm?.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }

    private fun geocodificar(ubicacion: Location): String {
        val coordenadas = String.format(Locale.US, "%.3f, %.3f", ubicacion.latitude, ubicacion.longitude)
        if (!Geocoder.isPresent()) return coordenadas
        return try {
            @Suppress("DEPRECATION")
            val dir = Geocoder(ctx, Locale.forLanguageTag("es-CO"))
                .getFromLocation(ubicacion.latitude, ubicacion.longitude, 1)
                ?.firstOrNull()
            val ciudad = dir?.locality ?: dir?.subAdminArea ?: dir?.adminArea
            val pais = dir?.countryName
            when {
                ciudad != null && pais != null -> "$ciudad, $pais"
                ciudad != null -> ciudad
                else -> coordenadas
            }
        } catch (e: Exception) {
            coordenadas // sin internet el Geocoder falla: guardamos las coordenadas
        }
    }

    private companion object {
        val hilo = Executors.newSingleThreadExecutor()
    }
}
