package com.utp.basurapp.recolectorapp

import android.Manifest
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
import com.utp.basurapp.recolectorapp.api.ApiService
import com.utp.basurapp.recolectorapp.data.ApiResponse
import com.utp.basurapp.recolectorapp.data.UsuarioRequest

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Error obteniendo el token de Firebase")
                return@addOnCompleteListener
            }
            val token = task.result
            println("Mi Token de Firebase es: $token")
        }

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        btnRegistrar.setOnClickListener {
            // Verificamos si tenemos permiso
            if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        println("Ubicación capturada: Lat $lat, Lon $lon")

                        // Aquí llamaremos a la función para enviar al backend
                        registrarEnBackend(lat, lon)
                    } else {
                        Toast.makeText(this, "No se pudo obtener ubicación. Activa el GPS", Toast.LENGTH_SHORT).show()
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

        // Añadimos notificaciones solo si el teléfono es Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listaPermisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Convertimos la lista a un Array y pedimos todo de golpe
        ActivityCompat.requestPermissions(
            this,
            listaPermisos.toTypedArray(),
            100
        )
    }

    private fun registrarEnBackend(lat: Double, lon: Double) {
        // 1. Obtenemos los datos de la interfaz
        val nombre = findViewById<EditText>(R.id.etNombre).text.toString()
        val telefono = findViewById<EditText>(R.id.etTelefonoFamiliar).text.toString()
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // 2. Necesitamos el Token de Firebase de nuevo
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

            // 3. Creamos el objeto con los datos (El DTO que hicimos antes)
            val request = UsuarioRequest(
                nombre = nombre,
                fcmToken = token,
                telefonoFamiliar = telefono,
                latitud = lat,
                longitud = lon
            )

            // 4. Configuramos Retrofit (Usa TU IP de ipconfig aquí)
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl("http://192.168.18.56:8080/") // <-- CAMBIA LA XX POR TU IP
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            // 5. Hacemos la llamada
            apiService.registrarUsuario(request).enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        // Accedemos al campo 'message' del objeto que viene del servidor
                        val mensajeServidor = response.body()?.message ?: "Usuario registrado"

                        tvStatus.text = "Estado: $mensajeServidor"
                        Toast.makeText(this@MainActivity, "Éxito: $mensajeServidor", Toast.LENGTH_LONG).show()
                    } else {
                        tvStatus.text = "Error del servidor: ${response.code()}"
                    }
                }

                override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                    tvStatus.text = "Fallo de red: ${t.message}"
                    Log.e("API_ERROR", "Error de conexión", t)
                }
            })
        }
    }

}