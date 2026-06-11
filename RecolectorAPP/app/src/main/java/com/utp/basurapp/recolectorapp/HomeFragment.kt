package com.utp.basurapp.recolectorapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.CamionResponse
import com.utp.basurapp.recolectorapp.data.PerfilResponse
import com.utp.basurapp.recolectorapp.util.SessionManager
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapFragment
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Circle
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions

class HomeFragment : Fragment() {

    private var miLat: Double = -6.8681
    private var miLon: Double = -79.8201
    private lateinit var sessionManager: SessionManager

    private var map: MapLibreMap? = null
    private var userMarker: org.maplibre.android.annotations.Marker? = null
    private var truckMarker: org.maplibre.android.annotations.Marker? = null
    private var circleManager: CircleManager? = null
    private var userCircle: Circle? = null
    private var notificacionesEnviadas: Int = 0
    private var camionEnZona: Boolean = false
    private val MAX_NOTIFICACIONES_LOCALES = 2

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 8000L
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchUbicacionCamion()
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MapLibre.getInstance(requireContext(), "", WellKnownTileServer.MapLibre)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        RetrofitClient.init(sessionManager)

        miLat = sessionManager.getLatitud()
        miLon = sessionManager.getLongitud()

        val direccionGuardada = sessionManager.getDireccion()
        if (direccionGuardada.isNotEmpty()) {
            view.findViewById<TextView>(R.id.tvZona).text = direccionGuardada
        }

        setupMap()

        view.findViewById<FloatingActionButton>(R.id.btnMiUbicacion).setOnClickListener {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0))
        }

        view.findViewById<FloatingActionButton>(R.id.btnCompartir).setOnClickListener {
            startActivity(Intent(requireContext(), CompartirAlertaActivity::class.java))
        }

        view.findViewById<ImageButton>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(requireContext(), AlertasConfigActivity::class.java))
        }
    }

    private fun setupMap() {
        val options = MapLibreMapOptions.createFromAttributes(requireContext(), null)
            .camera(
                CameraPosition.Builder()
                    .target(LatLng(miLat, miLon))
                    .zoom(14.0)
                    .build()
            )

        val mapFragment = MapFragment.newInstance(options)
        childFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync { mapLibreMap ->
            map = mapLibreMap
            mapLibreMap.setStyle(Style.Builder().fromUrl("https://tiles.openfreemap.org/styles/liberty")) { style ->
                circleManager = CircleManager(mapFragment.requireView() as MapView, mapLibreMap, style)
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
                        if (body?.direccion != null && body.direccion.isNotEmpty()) {
                            sessionManager.guardarDireccion(body.direccion)
                            view?.findViewById<TextView>(R.id.tvZona)?.text = body.direccion
                        }
                    }
                    dibujarRadioUsuario(mapLibreMap)
                    fetchUbicacionCamion()
                }

                override fun onFailure(call: retrofit2.Call<PerfilResponse>, t: Throwable) {
                    dibujarRadioUsuario(mapLibreMap)
                    fetchUbicacionCamion()
                }
            })
    }

    private fun dibujarRadioUsuario(mapLibreMap: MapLibreMap) {
        val radioMetros = sessionManager.getRadioAlertas().toDouble()
        val radioGrados = radioMetros / 111000.0

        if (userCircle != null) {
            circleManager?.delete(userCircle)
        }

        val circleOptions = CircleOptions()
            .withLatLng(LatLng(miLat, miLon))
            .withCircleRadius(radioGrados.toFloat())
            .withCircleColor("#2E7D32")
            .withCircleOpacity(0.15f)
            .withCircleStrokeColor("#0d631b")
            .withCircleStrokeWidth(2f)
            .withCircleStrokeOpacity(0.5f)

        userCircle = circleManager?.create(circleOptions)
    }

    private fun fetchUbicacionCamion() {
        val currentMap = map ?: return

        RetrofitClient.getApiService().getCamionUbicacion()
            .enqueue(object : retrofit2.Callback<CamionResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CamionResponse>,
                    response: retrofit2.Response<CamionResponse>
                ) {
                    var camionLat = miLat + 0.003
                    var camionLon = miLon + 0.003

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.coordenadas != null && body.activo) {
                            camionLat = body.coordenadas.latitud
                            camionLon = body.coordenadas.longitud
                        }
                    }

                    verificarProximidad(camionLat, camionLon)
                    actualizarMarcadores(currentMap, camionLat, camionLon)
                }

                override fun onFailure(call: retrofit2.Call<CamionResponse>, t: Throwable) {
                    val camionLat = miLat + 0.003
                    val camionLon = miLon + 0.003
                    verificarProximidad(camionLat, camionLon)
                    actualizarMarcadores(currentMap, camionLat, camionLon)
                }
            })
    }

    private fun verificarProximidad(camionLat: Double, camionLon: Double) {
        if (!sessionManager.isAlertasActivadas()) return

        val hoy = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1
        if (!sessionManager.isDiaActivo(hoy)) return

        val distancia = calcularDistanciaMetros(miLat, miLon, camionLat, camionLon)
        val radioMetros = sessionManager.getRadioAlertas().toDouble()
        val distanciaEnZona = distancia <= radioMetros

        if (!distanciaEnZona) {
            if (camionEnZona) {
                camionEnZona = false
                notificacionesEnviadas = 0
            }
            return
        }

        if (!camionEnZona) {
            camionEnZona = true
            notificacionesEnviadas = 0
        }

        if (notificacionesEnviadas < MAX_NOTIFICACIONES_LOCALES) {
            val distStr = if (distancia < 1000) "${distancia.toInt()} m" else "${"%.1f".format(distancia / 1000)} km"
            mostrarNotificacionCamion(distStr)
            notificacionesEnviadas++
        }
    }

    private fun calcularDistanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radioTierra * c
    }

    private fun mostrarNotificacionCamion(distancia: String) {
        val channelId = "basura_alerta"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Camion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando el camion esta cerca"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                if (sessionManager.isVibracionActivada()) {
                    enableVibration(true)
                } else {
                    enableVibration(false)
                }
            }
            val nm = requireContext().getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_notificacion_camion)
            .setContentTitle("Camion Recolector cerca")
            .setContentText("A $distancia de tu ubicacion")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("El camion recolector esta a $distancia. Prepara las bolsas de basura."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (sessionManager.isVibracionActivada()) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        }

        if (sessionManager.isSonidoActivado()) {
            builder.setSound(sonido)
        }

        builder.setDefaults(NotificationCompat.DEFAULT_ALL)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(requireContext()).notify(1001, builder.build())
        }
    }

    private fun actualizarMarcadores(mapLibreMap: MapLibreMap, camionLat: Double, camionLon: Double) {
        if (userMarker == null) {
            userMarker = mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(miLat, miLon))
                    .title("Mi Ubicacion")
                    .snippet("Tu punto de alerta")
            )
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0))
        }

        if (truckMarker == null) {
            truckMarker = mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(camionLat, camionLon))
                    .title("Camion Recolector")
                    .snippet("Ubicacion actual")
            )
        } else {
            truckMarker?.position = LatLng(camionLat, camionLon)
        }
    }

    override fun onResume() {
        super.onResume()
        dibujarRadioUsuario(map ?: return)
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshHandler.removeCallbacks(refreshRunnable)
        circleManager?.onDestroy()
        map = null
        userMarker = null
        truckMarker = null
        userCircle = null
        circleManager = null
    }
}
