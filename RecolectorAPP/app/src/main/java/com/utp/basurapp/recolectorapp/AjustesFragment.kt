package com.utp.basurapp.recolectorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.ActualizarUbicacionRequest
import com.utp.basurapp.recolectorapp.service.GeocodingService
import com.utp.basurapp.recolectorapp.util.SessionManager

class AjustesFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestMapLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("latitud", 0.0) ?: 0.0
            val lon = result.data?.getDoubleExtra("longitud", 0.0) ?: 0.0
            if (lat != 0.0 && lon != 0.0) {
                actualizarUbicacion(lat, lon)
            }
        }
    }

    private val requestGpsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            obtenerUbicacionGPS()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicacion necesario", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.titleAjustes)) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, statusBarHeight + 16, v.paddingRight, 20)
            insets
        }

        val nombre = sessionManager.getNombre()
        val email = sessionManager.getEmail()

        view.findViewById<TextView>(R.id.tvSettingsName).text = nombre ?: "Usuario Ciudadano"
        view.findViewById<TextView>(R.id.tvSettingsEmail).text = email ?: "usuario@ciudad.gob"

        val direccion = sessionManager.getDireccion()
        view.findViewById<TextView>(R.id.tvSettingsAddress).text =
            if (direccion.isNotEmpty()) direccion else "Sin direccion registrada"

        val tvTemaActual = view.findViewById<TextView>(R.id.tvTemaActual)
        tvTemaActual.text = obtenerNombreTema(sessionManager.getTema())

        view.findViewById<View>(R.id.cardTheme).setOnClickListener {
            mostrarSelectorTema()
        }

        view.findViewById<ImageButton>(R.id.btnEditAddress).setOnClickListener {
            mostrarDialogoActualizarUbicacion()
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun mostrarDialogoActualizarUbicacion() {
        val opciones = arrayOf("Usar mi ubicacion actual (GPS)", "Seleccionar en el mapa")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Actualizar ubicacion del hogar")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisoGPS()
                    1 -> abrirSelectorMapa()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarPermisoGPS() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacionGPS()
            }
            else -> {
                requestGpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @Suppress("MissingPermission")
    private fun obtenerUbicacionGPS() {
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    actualizarUbicacion(location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener la ubicacion", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener ubicacion", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirSelectorMapa() {
        val lat = sessionManager.getLatitud()
        val lon = sessionManager.getLongitud()
        val intent = Intent(requireContext(), SeleccionarUbicacionActivity::class.java)
        intent.putExtra("latitud", lat)
        intent.putExtra("longitud", lon)
        requestMapLauncher.launch(intent)
    }

    private fun actualizarUbicacion(lat: Double, lon: Double) {
        view?.findViewById<TextView>(R.id.tvSettingsAddress)?.text = "Obteniendo direccion..."

        GeocodingService.obtenerDireccion(lat, lon) { direccion ->
            activity?.runOnUiThread {
                if (direccion != null) {
                    llamadaActualizarUbicacion(lat, lon, direccion)
                } else {
                    val fallback = String.format("%.6f, %.6f", lat, lon)
                    llamadaActualizarUbicacion(lat, lon, fallback)
                }
            }
        }
    }

    private fun llamadaActualizarUbicacion(lat: Double, lon: Double, direccion: String) {
        val request = ActualizarUbicacionRequest(lat, lon, direccion)

        RetrofitClient.getApiService().actualizarUbicacion(request)
            .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.ApiResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.ApiResponse>,
                    response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        sessionManager.guardarCoordenadas(lat, lon)
                        sessionManager.guardarDireccion(direccion)
                        view?.findViewById<TextView>(R.id.tvSettingsAddress)?.text = direccion
                        Toast.makeText(requireContext(), "Ubicacion actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        val error = response.errorBody()?.string() ?: "Error al actualizar"
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        val direccionActual = sessionManager.getDireccion()
                        view?.findViewById<TextView>(R.id.tvSettingsAddress)?.text =
                            if (direccionActual.isNotEmpty()) direccionActual else "Sin direccion registrada"
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.ApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de conexion", Toast.LENGTH_SHORT).show()
                    val direccionActual = sessionManager.getDireccion()
                    view?.findViewById<TextView>(R.id.tvSettingsAddress)?.text =
                        if (direccionActual.isNotEmpty()) direccionActual else "Sin direccion registrada"
                }
            })
    }

    private fun mostrarSelectorTema() {
        val opciones = arrayOf("Claro", "Oscuro", "Seguir sistema")
        val valores = arrayOf("light", "dark", "system")
        val actual = sessionManager.getTema()
        val indiceActual = valores.indexOf(actual).coerceAtLeast(2)

        AlertDialog.Builder(requireContext())
            .setTitle("Apariencia")
            .setSingleChoiceItems(opciones, indiceActual) { dialog, which ->
                val tema = valores[which]
                sessionManager.guardarTema(tema)
                view?.findViewById<TextView>(R.id.tvTemaActual)?.text = opciones[which]

                when (tema) {
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun obtenerNombreTema(tema: String): String {
        return when (tema) {
            "light" -> "Claro"
            "dark" -> "Oscuro"
            else -> "Seguir sistema"
        }
    }
}
