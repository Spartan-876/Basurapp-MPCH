package com.utp.basurapp.recolectorapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapFragment
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.Style
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.CamionResponse
import com.utp.basurapp.recolectorapp.data.PerfilResponse
import com.utp.basurapp.recolectorapp.util.SessionManager

class MapaActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var map: MapLibreMap? = null
    private var userMarker: org.maplibre.android.annotations.Marker? = null
    private var truckMarker: org.maplibre.android.annotations.Marker? = null

    private var miLat: Double = -6.7716
    private var miLon: Double = -79.8409

    private val CHANNEL_ID = "basura_alerta"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, "", WellKnownTileServer.MapLibre)
        setContentView(R.layout.activity_mapa)

        sessionManager = SessionManager(this)
        miLat = sessionManager.getLatitud()
        miLon = sessionManager.getLongitud()

        createNotificationChannel()
        requestNotificationPermission()

        setupMap()

        findViewById<FloatingActionButton>(R.id.btnMiUbicacion).setOnClickListener {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0))
        }

        findViewById<FloatingActionButton>(R.id.btnCompartir).setOnClickListener {
            startActivity(Intent(this, CompartirAlertaActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnNotificaciones).setOnClickListener {
            Toast.makeText(this, R.string.proximoamente, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMap() {
        val options = MapLibreMapOptions.createFromAttributes(this, null)
            .camera(
                CameraPosition.Builder()
                    .target(LatLng(miLat, miLon))
                    .zoom(14.0)
                    .build()
            )

        val mapFragment = MapFragment.newInstance(options)
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync { mapLibreMap ->
            map = mapLibreMap
            mapLibreMap.setStyle(Style.Builder().fromUrl("https://tiles.openfreemap.org/styles/liberty")) {
                fetchPerfil(mapLibreMap)
            }
        }
    }

    private fun fetchPerfil(mapLibreMap: MapLibreMap) {
        RetrofitClient.getApiService().obtenerPerfil()
            .enqueue(object : retrofit2.Callback<PerfilResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PerfilResponse>,
                    response: retrofit2.Response<PerfilResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.latitud != null && body.longitud != null) {
                            miLat = body.latitud
                            miLon = body.longitud
                            sessionManager.guardarCoordenadas(miLat, miLon)
                        }
                    }
                    cargarUbicacionCamion(mapLibreMap)
                }

                override fun onFailure(call: retrofit2.Call<PerfilResponse>, t: Throwable) {
                    cargarUbicacionCamion(mapLibreMap)
                }
            })
    }

    private fun cargarUbicacionCamion(mapLibreMap: MapLibreMap) {
        RetrofitClient.getApiService().getCamionUbicacion()
            .enqueue(object : retrofit2.Callback<CamionResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CamionResponse>,
                    response: retrofit2.Response<CamionResponse>
                ) {
                    var camionLat = miLat + 0.008
                    var camionLon = miLon + 0.005

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.coordenadas != null && body.activo) {
                            camionLat = body.coordenadas.latitud
                            camionLon = body.coordenadas.longitud
                        }
                    }

                    colocarMarcadores(mapLibreMap, camionLat, camionLon)
                }

                override fun onFailure(call: retrofit2.Call<CamionResponse>, t: Throwable) {
                    colocarMarcadores(mapLibreMap, miLat + 0.008, miLon + 0.005)
                }
            })
    }

    private fun colocarMarcadores(mapLibreMap: MapLibreMap, camionLat: Double, camionLon: Double) {
        userMarker = mapLibreMap.addMarker(
            MarkerOptions()
                .position(LatLng(miLat, miLon))
                .title("Mi Ubicacion")
                .snippet("Tu punto de alerta")
        )

        truckMarker = mapLibreMap.addMarker(
            MarkerOptions()
                .position(LatLng(camionLat, camionLon))
                .title("Camion Recolector")
                .snippet("En ruta")
        )

        mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.canal_alertas),
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    300
                )
            }
        }
    }
}
