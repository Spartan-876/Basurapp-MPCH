package com.utp.basurapp.recolectorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.ApiResponse
import com.utp.basurapp.recolectorapp.data.UsuarioRequest
import com.utp.basurapp.recolectorapp.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        val nombre = sessionManager.getNombre() ?: ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Error obteniendo el token de Firebase")
                return@addOnCompleteListener
            }
            val token = task.result
            println("Mi Token de Firebase es: $token")
        }

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        if (nombre.isNotEmpty()) etNombre.setText(nombre)

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        btnRegistrar.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        registrarEnBackend(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(this, "No se pudo obtener ubicacion. Activa el GPS", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                pedirPermisosApp()
            }
        }

        pedirPermisosApp()
    }

    private fun pedirPermisosApp() {
        val listaPermisos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listaPermisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        ActivityCompat.requestPermissions(
            this,
            listaPermisos.toTypedArray(),
            100
        )
    }

    private fun registrarEnBackend(lat: Double, lon: Double) {
        val nombre = findViewById<EditText>(R.id.etNombre).text.toString()
        val telefono = findViewById<EditText>(R.id.etTelefonoFamiliar).text.toString()
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val request = UsuarioRequest(
                nombre = nombre,
                email = sessionManager.getEmail(),
                fcmToken = token,
                telefonoFamiliar = telefono,
                latitud = lat,
                longitud = lon
            )

            RetrofitClient.getApiService().registrarUsuario(request).enqueue(
                object : retrofit2.Callback<ApiResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<ApiResponse>,
                        response: retrofit2.Response<ApiResponse>
                    ) {
                        if (response.isSuccessful) {
                            val mensajeServidor = response.body()?.message ?: "Usuario registrado"

                            tvStatus.text = "Estado: $mensajeServidor"
                            sessionManager.setUbicacionRegistrada(true)
                            sessionManager.guardarCoordenadas(lat, lon)
                            Toast.makeText(this@MainActivity, "Exito: $mensajeServidor", Toast.LENGTH_LONG).show()

                            startActivity(Intent(this@MainActivity, MapaActivity::class.java))
                            finish()
                        } else {
                            tvStatus.text = "Error del servidor: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                        tvStatus.text = "Fallo de red: ${t.message}"
                        Log.e("API_ERROR", "Error de conexion", t)
                    }
                }
            )
        }
    }
}
