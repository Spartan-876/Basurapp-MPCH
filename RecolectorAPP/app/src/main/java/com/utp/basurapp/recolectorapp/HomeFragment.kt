package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.util.SessionManager
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.maps.MapFragment
import org.maplibre.android.maps.Style
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.annotations.MarkerOptions

class HomeFragment : Fragment() {

    private var miLat: Double = -6.8681
    private var miLon: Double = -79.8201
    private var camionLat: Double = -6.8681
    private var camionLon: Double = -79.8201
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
                cargarUbicacionCamion(mapLibreMap)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnShareAlert).setOnClickListener {
            compartirAlerta()
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

    private fun cargarUbicacionCamion(mapLibreMap: org.maplibre.android.maps.MapLibreMap) {
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
                    actualizarMapa(mapLibreMap)
                }

                override fun onFailure(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.CamionResponse>,
                    t: Throwable
                ) {
                    actualizarMapa(mapLibreMap)
                }
            })
    }

    private fun actualizarMapa(mapLibreMap: org.maplibre.android.maps.MapLibreMap) {
        mapLibreMap.clear()

        var truckLat = camionLat
        var truckLon = camionLon

        if (Math.abs(truckLat - miLat) < 0.001 && Math.abs(truckLon - miLon) < 0.001) {
            truckLat = miLat + 0.003
            truckLon = miLon + 0.003
        }

        mapLibreMap.addMarker(
            MarkerOptions()
                .position(LatLng(miLat, miLon))
                .title("Mi Casa")
                .snippet("Punto de alerta")
        )

        mapLibreMap.addMarker(
            MarkerOptions()
                .position(LatLng(truckLat, truckLon))
                .title("Camión Recolector")
                .snippet("Ubicación actual")
        )

        mapLibreMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 14.0)
        )
    }

    private fun compartirAlerta() {
        val telefono = "51"
        val mensaje = "El camión recolector está cerca. Saca la basura de tu casa."
        val uri = "https://api.whatsapp.com/send?phone=$telefono&text=${android.net.Uri.encode(mensaje)}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri)))
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "WhatsApp no está instalado", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
