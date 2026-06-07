package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.CamionResponse
import com.utp.basurapp.recolectorapp.data.PerfilResponse
import com.utp.basurapp.recolectorapp.util.SessionManager
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapFragment
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class HomeFragment : Fragment() {

    private var miLat: Double = -6.8681
    private var miLon: Double = -79.8201
    private lateinit var sessionManager: SessionManager

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? MapFragment

        mapFragment?.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle(Style.Builder().fromUrl("https://tiles.openfreemap.org/styles/liberty")) {
                fetchPerfil(mapLibreMap)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnShareAlert).setOnClickListener {
            startActivity(Intent(requireContext(), CompartirAlertaActivity::class.java))
        }

        view.findViewById<View>(R.id.btnMyLocation).setOnClickListener {
            val mf = childFragmentManager.findFragmentById(R.id.mapFragment) as? MapFragment
            mf?.getMapAsync { mapLibreMap ->
                mapLibreMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0)
                )
            }
        }

        view.findViewById<View>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(requireContext(), AlertasConfigActivity::class.java))
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
                    var camionLat = miLat + 0.003
                    var camionLon = miLon + 0.003

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
                    colocarMarcadores(mapLibreMap, miLat + 0.003, miLon + 0.003)
                }
            })
    }

    private fun colocarMarcadores(mapLibreMap: MapLibreMap, camionLat: Double, camionLon: Double) {
        mapLibreMap.addMarker(
            MarkerOptions()
                .position(LatLng(miLat, miLon))
                .title("Mi Ubicacion")
                .snippet("Punto de alerta")
        )

        mapLibreMap.addMarker(
            MarkerOptions()
                .position(LatLng(camionLat, camionLon))
                .title("Camion Recolector")
                .snippet("Ubicacion actual")
        )

        mapLibreMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0)
        )
    }
}
