package com.utp.basurapp.recolectorapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.slider.Slider

class AlertasConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas_config)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val sliderRadius = findViewById<Slider>(R.id.sliderRadius)
        val tvRadiusValue = findViewById<TextView>(R.id.tvRadiusValue)

        sliderRadius.addOnChangeListener { _, value, _ ->
            tvRadiusValue.text = "${value.toInt()} m"
        }

        val dayIds = listOf(
            R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu,
            R.id.dayFri, R.id.daySat, R.id.daySun
        )

        val selectedDays = mutableSetOf(0, 1, 2, 3, 4)

        dayIds.forEachIndexed { index, id ->
            findViewById<View>(id).setOnClickListener {
                val view = findViewById<View>(id)
                if (selectedDays.contains(index)) {
                    selectedDays.remove(index)
                    view.setBackgroundResource(0)
                } else {
                    selectedDays.add(index)
                    view.setBackgroundResource(R.drawable.bg_info_message)
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnSaveAlertConfig).setOnClickListener {
            val radius = sliderRadius.value.toInt()
            val alertsOn = findViewById<SwitchMaterial>(R.id.switchAlerts).isChecked
            val vibrationOn = findViewById<SwitchMaterial>(R.id.switchAlertVibration).isChecked

            Toast.makeText(
                this,
                "Configuración guardada: $radius m, ${selectedDays.size} días",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
