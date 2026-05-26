package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.RegisterRequest
import com.utp.basurapp.recolectorapp.util.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)

        val etNombre = findViewById<TextInputEditText>(R.id.etRegisterNombre)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etRegisterConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvError = findViewById<TextView>(R.id.tvRegisterError)

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvError.text = "Completa todos los campos"
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                tvError.text = "Las contrasenas no coinciden"
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                tvError.text = "La contrasena debe tener al menos 6 caracteres"
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
                            tvError.text = "El email ya esta registrado"
                            tvError.visibility = TextView.VISIBLE
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                        t: Throwable
                    ) {
                        btnRegister.isEnabled = true
                        tvError.text = "Error de conexion: ${t.message}"
                        tvError.visibility = TextView.VISIBLE
                    }
                })
        }
    }
}
