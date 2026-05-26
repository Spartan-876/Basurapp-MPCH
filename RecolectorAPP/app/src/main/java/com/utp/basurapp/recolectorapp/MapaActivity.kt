package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.appbar.MaterialToolbar
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.util.SessionManager
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapFragment
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.annotations.MarkerOptions

class MapaActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var map: MapLibreMap? = null
    private var miLat: Double = -6.8681
    private var miLon: Double = -79.8201
    private var camionLat: Double = -6.8681
    private var camionLon: Double = -79.8201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, "", WellKnownTileServer.MapLibre)
        setContentView(R.layout.activity_mapa)

        sessionManager = SessionManager(this)
        miLat = sessionManager.getLatitud()
        miLon = sessionManager.getLongitud()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as MapFragment

        mapFragment.getMapAsync { mapLibreMap ->
            map = mapLibreMap
            mapLibreMap.setStyle(Style.Builder().fromUrl("https://tiles.openfreemap.org/styles/liberty")) {
                cargarUbicacionCamion()
            }
            // Zoom gestures habilitados por defecto en MapLibre
        }

        findViewById<MaterialButton>(R.id.btnWhatsApp).setOnClickListener {
            enviarWhatsApp()
        }

        findViewById<MaterialButton>(R.id.btnSMS).setOnClickListener {
            enviarSMS()
        }
    }

    private fun cargarUbicacionCamion() {
        RetrofitClient.getApiService().getCamionUbicacion()
            .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.CamionResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.CamionResponse>,
                    response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.CamionResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            camionLat = body.latitud
                            camionLon = body.longitud
                        }
                    }
                    actualizarMapa()
                }

                override fun onFailure(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.CamionResponse>,
                    t: Throwable
                ) {
                    actualizarMapa()
                }
            })
    }

    private fun actualizarMapa() {
        map?.let { mapa ->
            mapa.clear()

            var truckLat = camionLat
            var truckLon = camionLon

            if (Math.abs(truckLat - miLat) < 0.001 && Math.abs(truckLon - miLon) < 0.001) {
                truckLat = miLat + 0.003
                truckLon = miLon + 0.003
            }

            mapa.addMarker(
                MarkerOptions()
                    .position(LatLng(miLat, miLon))
                    .title("Mi Casa")
                    .snippet("Punto de alerta")
            )

            mapa.addMarker(
                MarkerOptions()
                    .position(LatLng(truckLat, truckLon))
                    .title("Camion Recolector")
                    .snippet("Ubicacion actual")
            )

            mapa.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(miLat, miLon), 14.0
                )
            )
        }
    }

    private fun enviarWhatsApp() {
        val telefono = "51" // se completaria con el telefono del familiar
        val mensaje = "El camion recolector esta cerca. Saca la basura de tu casa."
        val uri = "https://api.whatsapp.com/send?phone=$telefono&text=${Uri.encode(mensaje)}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp no esta instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarSMS() {
        val telefono = "51" // se completaria con el telefono del familiar
        val mensaje = "El camion recolector esta cerca. Saca la basura de tu casa."
        val uri = "sms:$telefono?body=${Uri.encode(mensaje)}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: Exception) {
            Toast.makeText(this, "No se puede enviar SMS", Toast.LENGTH_SHORT).show()
        }
    }
}
