package com.utp.basurapp.recolectorapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapFragment
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.Style

class SeleccionarUbicacionActivity : AppCompatActivity() {

    private var selectedLat: Double = -6.7716
    private var selectedLon: Double = -79.8409

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, "", WellKnownTileServer.MapLibre)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        selectedLat = intent.getDoubleExtra("latitud", -6.7716)
        selectedLon = intent.getDoubleExtra("longitud", -79.8409)

        val tvCoordenadas = findViewById<TextView>(R.id.tvCoordenadas)
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarUbicacion)

        tvCoordenadas.text = String.format("Lat: %.6f, Lon: %.6f", selectedLat, selectedLon)

        val options = MapLibreMapOptions.createFromAttributes(this, null)
            .camera(
                CameraPosition.Builder()
                    .target(LatLng(selectedLat, selectedLon))
                    .zoom(14.0)
                    .build()
            )

        val mapFragment = MapFragment.newInstance(options)
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle(Style.Builder().fromUrl("https://tiles.openfreemap.org/styles/liberty"))

            mapLibreMap.addOnCameraIdleListener {
                val center = mapLibreMap.cameraPosition.target
                if (center != null) {
                    selectedLat = center.latitude
                    selectedLon = center.longitude
                    tvCoordenadas.text = String.format("Lat: %.6f, Lon: %.6f", selectedLat, selectedLon)
                }
            }
        }

        btnConfirmar.setOnClickListener {
            val resultIntent = android.content.Intent()
            resultIntent.putExtra("latitud", selectedLat)
            resultIntent.putExtra("longitud", selectedLon)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
