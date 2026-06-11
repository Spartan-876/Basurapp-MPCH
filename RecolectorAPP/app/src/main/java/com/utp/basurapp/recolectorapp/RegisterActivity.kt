package com.utp.basurapp.recolectorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.RegisterRequest
import com.utp.basurapp.recolectorapp.service.GeocodingService
import com.utp.basurapp.recolectorapp.util.SessionManager
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var locationSource: String? = null
    private var direccionSeleccionada: String = ""

    private val mapSelectorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedLat = result.data?.getDoubleExtra("latitud", 0.0)
            selectedLon = result.data?.getDoubleExtra("longitud", 0.0)
            locationSource = "map"
            val cardMap = findViewById<MaterialCardView>(R.id.cardLocationMap)
            val cardGps = findViewById<MaterialCardView>(R.id.cardLocationGps)
            cardMap.strokeColor = ContextCompat.getColor(this, R.color.primary)
            cardGps.strokeColor = ContextCompat.getColor(this, R.color.outlineVariant)
            if (selectedLat != null && selectedLon != null) {
                GeocodingService.obtenerDireccion(selectedLat!!, selectedLon!!) { dir ->
                    direccionSeleccionada = dir ?: ""
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        RetrofitClient.init(sessionManager)

        val etNombre = findViewById<TextInputEditText>(R.id.etRegisterNombre)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val cardGps = findViewById<MaterialCardView>(R.id.cardLocationGps)
        val cardMap = findViewById<MaterialCardView>(R.id.cardLocationMap)

        cardGps.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    200
                )
                return@setOnClickListener
            }

            obtenerUbicacionGps { lat, lon ->
                selectedLat = lat
                selectedLon = lon
                locationSource = "gps"
                cardGps.strokeColor = ContextCompat.getColor(this, R.color.primary)
                cardMap.strokeColor = ContextCompat.getColor(this, R.color.outlineVariant)
                Toast.makeText(this, R.string.ubicacion_capturada, Toast.LENGTH_SHORT).show()
                GeocodingService.obtenerDireccion(lat, lon) { dir ->
                    direccionSeleccionada = dir ?: ""
                }
            }
        }

        cardMap.setOnClickListener {
            val intent = Intent(this, SeleccionarUbicacionActivity::class.java)
            if (selectedLat != null && selectedLon != null) {
                intent.putExtra("latitud", selectedLat)
                intent.putExtra("longitud", selectedLon)
            }
            mapSelectorLauncher.launch(intent)
        }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                mostrarError("Completa todos los campos")
                return@setOnClickListener
            }

            if (password.length < 6) {
                mostrarError("La contraseña debe tener al menos 6 caracteres")
                return@setOnClickListener
            }

            if (selectedLat == null || selectedLon == null) {
                mostrarError("Selecciona una ubicación usando GPS o el mapa")
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "Obteniendo dirección..."

            if (selectedLat != null && selectedLon != null) {
                GeocodingService.obtenerDireccion(selectedLat!!, selectedLon!!) { dir ->
                    direccionSeleccionada = dir ?: ""
                    runOnUiThread {
                        btnRegister.text = "Registrando..."
                        enviarRegistro(nombre, email, password, btnRegister)
                    }
                }
            } else {
                enviarRegistro(nombre, email, password, btnRegister)
            }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun enviarRegistro(nombre: String, email: String, password: String, btnRegister: MaterialButton) {
        val request = RegisterRequest(
            email = email,
            password = password,
            nombre = nombre,
            fcmToken = sessionManager.getFcmToken(),
            latitud = selectedLat,
            longitud = selectedLon,
            direccion = direccionSeleccionada.ifEmpty { null }
        )

        RetrofitClient.getApiService().register(request)
            .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.AuthResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                    response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.AuthResponse>
                ) {
                    btnRegister.isEnabled = true
                    btnRegister.text = getString(R.string.btn_register)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.token != null) {
                            sessionManager.guardarSesion(
                                body.token,
                                body.email ?: email,
                                body.nombre ?: nombre
                            )
                            sessionManager.guardarCoordenadas(selectedLat!!, selectedLon!!)
                            if (!body.direccion.isNullOrEmpty()) {
                                sessionManager.guardarDireccion(body.direccion)
                            } else if (direccionSeleccionada.isNotEmpty()) {
                                sessionManager.guardarDireccion(direccionSeleccionada)
                            }
                            sessionManager.setUbicacionRegistrada(true)
                            Toast.makeText(
                                this@RegisterActivity,
                                "Cuenta creada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        } else {
                            mostrarError("Error del servidor")
                        }
                    } else {
                        val errorMsg = parsearError(response.errorBody()?.string())
                        mostrarError(errorMsg)
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                    t: Throwable
                ) {
                    btnRegister.isEnabled = true
                    btnRegister.text = getString(R.string.btn_register)
                    mostrarError("Error de conexión: ${t.message}")
                }
            })
    }

    private fun mostrarError(mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun parsearError(errorBody: String?): String {
        if (errorBody == null) return "Error desconocido"
        return try {
            val json = JSONObject(errorBody)
            json.optString("error", "Error desconocido")
        } catch (e: Exception) {
            "Error desconocido"
        }
    }

    private fun obtenerUbicacionGps(onResult: (Double, Double) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { currentLocation ->
                        if (currentLocation != null) {
                            onResult(currentLocation.latitude, currentLocation.longitude)
                        } else {
                            Toast.makeText(this, R.string.gps_no_disponible, Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "${getString(R.string.gps_error)}: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.gps_no_disponible, Toast.LENGTH_LONG).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val cardGps = findViewById<MaterialCardView>(R.id.cardLocationGps)
            cardGps.performClick()
        }
    }
}
