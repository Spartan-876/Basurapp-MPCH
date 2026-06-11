package com.utp.basurapp.recolectorapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.slider.Slider
import com.utp.basurapp.recolectorapp.util.SessionManager

class AlertasConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas_config)

        val sessionManager = SessionManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerConfig)) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, statusBarHeight + 12, v.paddingRight, 12)
            insets
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val direccion = sessionManager.getDireccion()
        findViewById<TextView>(R.id.tvAlertDireccion).text =
            if (direccion.isNotEmpty()) direccion else "Sin dirección registrada"

        val sliderRadius = findViewById<Slider>(R.id.sliderRadius)
        val tvRadiusValue = findViewById<TextView>(R.id.tvRadiusValue)
        val switchVibration = findViewById<SwitchMaterial>(R.id.switchAlertVibration)
        val switchSound = findViewById<SwitchMaterial>(R.id.switchSound)

        val radioGuardado = sessionManager.getRadioAlertas()
        sliderRadius.value = radioGuardado.toFloat().coerceIn(sliderRadius.valueFrom, sliderRadius.valueTo)
        tvRadiusValue.text = "${radioGuardado} m"

        switchVibration.isChecked = sessionManager.isVibracionActivada()
        switchSound.isChecked = sessionManager.isSonidoActivado()

        sliderRadius.addOnChangeListener { _, value, _ ->
            tvRadiusValue.text = "${value.toInt()} m"
        }

        val dayIds = listOf(
            R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu,
            R.id.dayFri, R.id.daySat, R.id.daySun
        )

        val selectedDays = sessionManager.getDiasActivos().toMutableSet()

        fun actualizarVistasDias() {
            dayIds.forEachIndexed { index, id ->
                val view = findViewById<View>(id)
                if (selectedDays.contains(index)) {
                    view.setBackgroundResource(R.drawable.bg_info_message)
                } else {
                    view.setBackgroundResource(0)
                }
            }
        }

        actualizarVistasDias()

        dayIds.forEachIndexed { index, id ->
            findViewById<View>(id).setOnClickListener {
                if (selectedDays.contains(index)) {
                    selectedDays.remove(index)
                } else {
                    selectedDays.add(index)
                }
                actualizarVistasDias()
            }
        }

        findViewById<MaterialButton>(R.id.btnSaveAlertConfig).setOnClickListener {
            val radius = sliderRadius.value.toInt()
            val vibrationOn = switchVibration.isChecked
            val soundOn = switchSound.isChecked

            sessionManager.guardarRadioAlertas(radius)
            sessionManager.setVibracionActivada(vibrationOn)
            sessionManager.setSonidoActivado(soundOn)
            sessionManager.guardarDiasActivos(selectedDays)

            Toast.makeText(
                this,
                "Configuración guardada: $radius m, ${selectedDays.size} días",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
