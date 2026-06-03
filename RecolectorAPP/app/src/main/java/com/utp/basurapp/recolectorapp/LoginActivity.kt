package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.FcmTokenRequest
import com.utp.basurapp.recolectorapp.data.LoginRequest
import com.utp.basurapp.recolectorapp.util.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                sessionManager.guardarFcmToken(task.result)
            }
        }

        if (sessionManager.isLoggedIn()) {
            irALaPantallaCorrespondiente()
            return
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etLoginEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvError = findViewById<TextView>(R.id.tvLoginError)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.text = "Completa todos los campos"
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = TextView.GONE
            btnLogin.isEnabled = false

            RetrofitClient.getApiService().login(LoginRequest(email, password, sessionManager.getFcmToken()))
                .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.AuthResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                        response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.AuthResponse>
                    ) {
                        btnLogin.isEnabled = true
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.token != null) {
                                sessionManager.guardarSesion(
                                    body.token,
                                    body.email ?: email,
                                    body.nombre ?: ""
                                )
                                sessionManager.setUbicacionRegistrada(true)
                                sessionManager.getFcmToken()?.let { token ->
                                    RetrofitClient.getApiService().actualizarFcmToken(FcmTokenRequest(token))
                                        .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.ApiResponse> {
                                            override fun onResponse(
                                                call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.ApiResponse>,
                                                response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.ApiResponse>
                                            ) {}
                                            override fun onFailure(
                                                call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.ApiResponse>,
                                                t: Throwable
                                            ) {}
                                        })
                                }
                                irALaPantallaCorrespondiente()
                            } else {
                                tvError.text = "Error del servidor"
                                tvError.visibility = TextView.VISIBLE
                            }
                        } else {
                            tvError.text = "Credenciales inválidas"
                            tvError.visibility = TextView.VISIBLE
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.AuthResponse>,
                        t: Throwable
                    ) {
                        btnLogin.isEnabled = true
                        tvError.text = "Error de conexión: ${t.message}"
                        tvError.visibility = TextView.VISIBLE
                    }
                })
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            tvError.text = "Funcionalidad próximamente"
            tvError.visibility = TextView.VISIBLE
        }
    }

    private fun irALaPantallaCorrespondiente() {
        val destino = if (sessionManager.isUbicacionRegistrada()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, RegisterActivity::class.java)
        }
        startActivity(destino)
        finish()
    }
}
