package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.maplibre.android.annotations.Marker
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
    private var userMarker: Marker? = null
    private val truckMarkers = mutableMapOf<String, Marker>()
    private val truckDataMap = mutableMapOf<String, CamionResponse>()

    private val truckMarkerResIds = intArrayOf(
        R.drawable.ic_truck_marker_green,
        R.drawable.ic_truck_marker_orange
    )

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 8000L
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchCamiones()
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
                mapLibreMap.setInfoWindowAdapter(TruckInfoWindowAdapter())
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
                    agregarPinUsuario(mapLibreMap)
                    fetchCamiones()
                }

                override fun onFailure(call: retrofit2.Call<PerfilResponse>, t: Throwable) {
                    agregarPinUsuario(mapLibreMap)
                    fetchCamiones()
                }
            })
    }

    private fun agregarPinUsuario(mapLibreMap: MapLibreMap) {
        if (userMarker == null) {
            val context = requireContext()
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
    }

    private fun fetchCamiones() {
        val currentMap = map ?: return

        RetrofitClient.getApiService().getCamiones()
            .enqueue(object : retrofit2.Callback<List<CamionResponse>> {
                override fun onResponse(
                    call: retrofit2.Call<List<CamionResponse>>,
                    response: retrofit2.Response<List<CamionResponse>>
                ) {
                    val camiones = response.body()
                    if (camiones != null && camiones.isNotEmpty()) {
                        truckDataMap.clear()
                        for (camion in camiones) {
                            if (camion.idCamion != null) {
                                truckDataMap[camion.idCamion] = camion
                            }
                        }
                        actualizarMarcadoresCamiones(currentMap, camiones)
                        actualizarCardDistancia(camiones)
                    } else {
                        actualizarCardDistancia(emptyList())
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<CamionResponse>>, t: Throwable) {
                    actualizarCardDistancia(emptyList())
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

    private fun actualizarCardDistancia(camiones: List<CamionResponse>) {
        val card = view?.findViewById<MaterialCardView>(R.id.cardCamionDistancia) ?: return
        val tv = view?.findViewById<TextView>(R.id.tvDistanciaCamion) ?: return

        val activos = camiones.filter { it.activo && it.coordenadas != null }

        if (activos.isEmpty()) {
            tv.text = getString(R.string.truck_no_data)
            card.visibility = View.VISIBLE
            return
        }

        var menorDistancia = Double.MAX_VALUE
        var camionMasCercano: CamionResponse? = null

        for (camion in activos) {
            val dist = calcularDistanciaMetros(miLat, miLon, camion.coordenadas!!.latitud, camion.coordenadas.longitud)
            if (dist < menorDistancia) {
                menorDistancia = dist
                camionMasCercano = camion
            }
        }

        if (camionMasCercano != null) {
            val distStr = if (menorDistancia < 1000) "${menorDistancia.toInt()} m" else "${"%.1f".format(menorDistancia / 1000)} km"
            tv.text = "${camionMasCercano.placa ?: camionMasCercano.idCamion} — $distStr"
        } else {
            tv.text = getString(R.string.truck_no_data)
        }

        val tvActivos = view?.findViewById<TextView>(R.id.tvTrucksActivos)
        if (tvActivos != null) {
            val activosCount = activos.size
            if (activosCount > 1) {
                tvActivos.text = getString(R.string.trucks_active_count, activosCount)
                tvActivos.visibility = View.VISIBLE
            } else {
                tvActivos.visibility = View.GONE
            }
        }

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

    private fun getTruckMarkerRes(index: Int): Int {
        return truckMarkerResIds[index % truckMarkerResIds.size]
    }

    private fun actualizarMarcadoresCamiones(mapLibreMap: MapLibreMap, camiones: List<CamionResponse>) {
        val context = requireContext()

        val activeIds = camiones.mapNotNull { it.idCamion }.toSet()

        val markersToRemove = truckMarkers.keys.filter { it !in activeIds }
        for (id in markersToRemove) {
            truckMarkers[id]?.let { mapLibreMap.removeMarker(it) }
            truckMarkers.remove(id)
        }

        for ((index, camion) in camiones.withIndex()) {
            val id = camion.idCamion ?: continue
            val coords = camion.coordenadas ?: continue

            val markerRes = getTruckMarkerRes(index)
            val truckIcon = IconFactory.getInstance(context).fromBitmap(drawableToBitmap(markerRes))

            val existingMarker = truckMarkers[id]
            if (existingMarker == null) {
                val marker = mapLibreMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(coords.latitud, coords.longitud))
                        .title(camion.placa ?: id)
                        .snippet(if (camion.activo) "Activo" else "Inactivo")
                        .icon(truckIcon)
                )
                truckMarkers[id] = marker
            } else {
                existingMarker.position = LatLng(coords.latitud, coords.longitud)
                existingMarker.title = camion.placa ?: id
                existingMarker.snippet = if (camion.activo) "Activo" else "Inactivo"
            }
        }
    }

    inner class TruckInfoWindowAdapter : MapLibreMap.InfoWindowAdapter {

        override fun getInfoWindow(marker: Marker): View {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_truck_info_window, null)

            val tvPlaca = view.findViewById<TextView>(R.id.tvInfoPlaca)
            val tvEstado = view.findViewById<TextView>(R.id.tvInfoEstado)
            val tvDistancia = view.findViewById<TextView>(R.id.tvInfoDistancia)

            val truckId = truckMarkers.entries.find { it.value == marker }?.key
            val camion = truckId?.let { truckDataMap[it] }

            if (camion != null) {
                tvPlaca.text = camion.placa ?: camion.idCamion

                if (camion.activo) {
                    tvEstado.text = "• ${getString(R.string.truck_active)}"
                    tvEstado.setTextColor(Color.parseColor("#2E7D32"))
                } else {
                    tvEstado.text = "• ${getString(R.string.truck_inactive)}"
                    tvEstado.setTextColor(Color.parseColor("#FF6D00"))
                }

                if (camion.coordenadas != null) {
                    val dist = calcularDistanciaMetros(miLat, miLon, camion.coordenadas.latitud, camion.coordenadas.longitud)
                    tvDistancia.text = if (dist < 1000) "${dist.toInt()} m" else "${"%.1f".format(dist / 1000)} km"
                } else {
                    tvDistancia.text = "N/D"
                }
            } else {
                tvPlaca.text = marker.title
                tvEstado.text = "• ${marker.snippet}"
                tvEstado.setTextColor(Color.parseColor("#888888"))
                tvDistancia.text = "N/D"
            }

            return view
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
        truckMarkers.clear()
        truckDataMap.clear()
    }
}
