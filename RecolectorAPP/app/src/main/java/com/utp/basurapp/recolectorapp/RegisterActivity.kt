package com.utp.basurapp.recolectorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.RegisterRequest
import com.utp.basurapp.recolectorapp.util.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var locationSelectedViaGps: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        val etNombre = findViewById<TextInputEditText>(R.id.etRegisterNombre)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvError = findViewById<TextView>(R.id.tvRegisterError)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val cardGps = findViewById<MaterialCardView>(R.id.cardLocationGps)
        val cardMap = findViewById<MaterialCardView>(R.id.cardLocationMap)

        cardGps.setOnClickListener {
            cardGps.strokeColor = ContextCompat.getColor(this, R.color.primary)
            cardMap.strokeColor = ContextCompat.getColor(this, R.color.outlineVariant)
            locationSelectedViaGps = true
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    200
                )
            }
        }

        cardMap.setOnClickListener {
            cardMap.strokeColor = ContextCompat.getColor(this, R.color.primary)
            cardGps.strokeColor = ContextCompat.getColor(this, R.color.outlineVariant)
            locationSelectedViaGps = false
            tvError.text = "Selector de mapa próximamente"
            tvError.visibility = TextView.VISIBLE
        }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvError.text = "Completa todos los campos"
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                tvError.text = "La contraseña debe tener al menos 6 caracteres"
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (!locationSelectedViaGps) {
                tvError.text = "Selecciona una ubicación usando GPS"
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = TextView.GONE
            btnRegister.isEnabled = false

            RetrofitClient.getApiService().register(RegisterRequest(email, password, nombre))
                .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.AuthResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                        response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.AuthResponse>
                    ) {
                        btnRegister.isEnabled = true
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.token != null) {
                                sessionManager.guardarSesion(
                                    body.token,
                                    body.email ?: email,
                                    body.nombre ?: nombre
                                )
                                sessionManager.setUbicacionRegistrada(true)
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Cuenta creada exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                finish()
                            } else {
                                tvError.text = "Error del servidor"
                                tvError.visibility = TextView.VISIBLE
                            }
                        } else {
                            tvError.text = "El email ya está registrado"
                            tvError.visibility = TextView.VISIBLE
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                        t: Throwable
                    ) {
                        btnRegister.isEnabled = true
                        tvError.text = "Error de conexión: ${t.message}"
                        tvError.visibility = TextView.VISIBLE
                    }
                })
        }

        tvGoToLogin.setOnClickListener {
            finish()
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
            Toast.makeText(this, "Ubicación disponible", Toast.LENGTH_SHORT).show()
        }
    }
}
