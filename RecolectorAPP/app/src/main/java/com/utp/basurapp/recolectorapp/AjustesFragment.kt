package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        view.findViewById<View>(R.id.switchVibration).setOnClickListener {
            Toast.makeText(requireContext(), "Preferencia guardada", Toast.LENGTH_SHORT).show()
        }
    }
}
