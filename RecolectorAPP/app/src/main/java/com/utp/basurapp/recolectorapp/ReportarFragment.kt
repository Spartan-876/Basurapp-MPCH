package com.utp.basurapp.recolectorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ReportarFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reportar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnCapture).setOnClickListener {
            Toast.makeText(requireContext(), "Cámara próximamente", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnSwitchCamera).setOnClickListener {
            Toast.makeText(requireContext(), "Cambio de cámara próximamente", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btnSendReport).setOnClickListener {
            Toast.makeText(requireContext(), "Reporte enviado", Toast.LENGTH_SHORT).show()
        }
    }
}
