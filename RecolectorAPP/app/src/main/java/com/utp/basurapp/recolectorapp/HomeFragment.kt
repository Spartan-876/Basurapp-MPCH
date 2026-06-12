package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.CamionResponse
import com.utp.basurapp.recolectorapp.data.PerfilResponse
import com.utp.basurapp.recolectorapp.util.SessionManager
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.IconFactory
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
    private var camionLat: Double = 0.0
    private var camionLon: Double = 0.0
    private var camionActivo: Boolean = false

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

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.headerHome)) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, statusBarHeight + 12, v.paddingRight, 12)
            insets
        }

        view.findViewById<FloatingActionButton>(R.id.btnMiUbicacion).setOnClickListener {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 16.0))
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
                    .zoom(16.0)
                    .build()
            )

        val mapFragment = MapFragment.newInstance(options)
        childFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync { mapLibreMap ->
            map = mapLibreMap
            mapLibreMap.setStyle(Style.Builder().fromUrl("https://tiles.openfreemap.org/styles/liberty")) { style ->
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
                    var cLat = miLat + 0.003
                    var cLon = miLon + 0.003
                    var activo = false

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.coordenadas != null && body.activo) {
                            cLat = body.coordenadas.latitud
                            cLon = body.coordenadas.longitud
                            activo = true
                        }
                    }

                    camionLat = cLat
                    camionLon = cLon
                    camionActivo = activo
                    actualizarCardDistancia()
                    actualizarMarcadores(currentMap, camionLat, camionLon)
                }

                override fun onFailure(call: retrofit2.Call<CamionResponse>, t: Throwable) {
                    camionLat = miLat + 0.003
                    camionLon = miLon + 0.003
                    camionActivo = false
                    actualizarCardDistancia()
                    actualizarMarcadores(currentMap, camionLat, camionLon)
                }
            })
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

    private fun actualizarCardDistancia() {
        val card = view?.findViewById<MaterialCardView>(R.id.cardCamionDistancia) ?: return
        val tv = view?.findViewById<TextView>(R.id.tvDistanciaCamion) ?: return

        if (!camionActivo) {
            tv.text = getString(R.string.truck_no_data)
            card.visibility = View.VISIBLE
            return
        }

        val distancia = calcularDistanciaMetros(miLat, miLon, camionLat, camionLon)
        val distStr = if (distancia < 1000) "${distancia.toInt()} m" else "${"%.1f".format(distancia / 1000)} km"
        tv.text = distStr
        card.visibility = View.VISIBLE
    }

    private fun drawableToBitmap(resId: Int): Bitmap {
        val context = requireContext()
        val drawable = AppCompatResources.getDrawable(context, resId)
            ?: throw IllegalStateException("Drawable not found: $resId")
        val density = context.resources.displayMetrics.density
        val size = (48 * density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

    private fun actualizarMarcadores(mapLibreMap: MapLibreMap, cLat: Double, cLon: Double) {
        val context = requireContext()

        if (userMarker == null) {
            val homeIcon = IconFactory.getInstance(context).fromBitmap(drawableToBitmap(R.drawable.ic_home_marker))
            userMarker = mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(miLat, miLon))
                    .title("Mi Ubicacion")
                    .snippet("Tu punto de alerta")
                    .icon(homeIcon)
            )
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(miLat, miLon), 16.0))
        }

        if (truckMarker == null) {
            val truckIcon = IconFactory.getInstance(context).fromBitmap(drawableToBitmap(R.drawable.ic_truck_marker))
            truckMarker = mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(cLat, cLon))
                    .title("Camion Recolector")
                    .snippet("Ubicacion actual")
                    .icon(truckIcon)
            )
        } else {
            truckMarker?.position = LatLng(cLat, cLon)
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
