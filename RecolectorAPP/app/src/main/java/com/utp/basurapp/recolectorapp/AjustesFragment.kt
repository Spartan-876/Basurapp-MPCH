package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.utp.basurapp.recolectorapp.util.SessionManager

class AjustesFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val nombre = sessionManager.getNombre()
        val email = sessionManager.getEmail()

        view.findViewById<TextView>(R.id.tvSettingsName).text = nombre ?: "Usuario Ciudadano"
        view.findViewById<TextView>(R.id.tvSettingsEmail).text = email ?: "usuario@ciudad.gob"

        val direccion = sessionManager.getDireccion()
        view.findViewById<TextView>(R.id.tvSettingsAddress).text =
            if (direccion.isNotEmpty()) direccion else "Sin dirección registrada"

        val tvTemaActual = view.findViewById<TextView>(R.id.tvTemaActual)
        tvTemaActual.text = obtenerNombreTema(sessionManager.getTema())

        view.findViewById<View>(R.id.cardTheme).setOnClickListener {
            mostrarSelectorTema()
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        view.findViewById<View>(R.id.switchVibration).setOnClickListener {
            Toast.makeText(requireContext(), "Preferencia guardada", Toast.LENGTH_SHORT).show()
        }
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
