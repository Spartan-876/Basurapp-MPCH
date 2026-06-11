package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
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
import org.maplibre.android.maps.Style

class HomeFragment : Fragment() {

    private var miLat: Double = -6.8681
    private var miLon: Double = -79.8201
    private lateinit var sessionManager: SessionManager

    private var map: MapLibreMap? = null
    private var userMarker: org.maplibre.android.annotations.Marker? = null
    private var truckMarker: org.maplibre.android.annotations.Marker? = null

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

        setupMap(view)

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

    private fun setupMap(view: View) {
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
                    fetchUbicacionCamion()
                }

                override fun onFailure(call: retrofit2.Call<PerfilResponse>, t: Throwable) {
                    fetchUbicacionCamion()
                }
            })
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

                    actualizarMarcadores(currentMap, camionLat, camionLon)
                }

                override fun onFailure(call: retrofit2.Call<CamionResponse>, t: Throwable) {
                    val camionLat = miLat + 0.003
                    val camionLon = miLon + 0.003
                    actualizarMarcadores(currentMap, camionLat, camionLon)
                }
            })
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
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshHandler.removeCallbacks(refreshRunnable)
        map = null
        userMarker = null
        truckMarker = null
    }
}
