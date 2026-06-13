package com.utp.basurapp.recolectorapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.google.gson.Gson
import com.utp.basurapp.recolectorapp.data.ApiResponse
import com.utp.basurapp.recolectorapp.data.ErrorResponse
import com.utp.basurapp.recolectorapp.data.ReporteResponse
import com.utp.basurapp.recolectorapp.service.GeocodingService
import com.utp.basurapp.recolectorapp.util.ImageCompressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReportarFragment : Fragment() {

    private var fotoFile: File? = null
    private var fotoUri: Uri? = null
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var direccion: String = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val reportes = mutableListOf<ReporteResponse>()
    private lateinit var adapter: ReporteAdapter

    private lateinit var ivPreview: ImageView
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var btnCapture: ImageButton
    private lateinit var btnRetake: ImageButton
    private lateinit var tvReportAddress: TextView
    private lateinit var etReportDetails: TextInputEditText
    private lateinit var btnSendReport: MaterialButton
    private lateinit var tvSinReportes: TextView

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            abrirCamara()
        } else {
            Toast.makeText(requireContext(), R.string.report_camera_permission, Toast.LENGTH_SHORT).show()
        }
    }

    private val requestTakePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && fotoFile != null) {
            mostrarPreview()
            obtenerUbicacion()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reportar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        ivPreview = view.findViewById(R.id.ivPreview)
        layoutPlaceholder = view.findViewById(R.id.layoutPlaceholder)
        btnCapture = view.findViewById(R.id.btnCapture)
        btnRetake = view.findViewById(R.id.btnRetake)
        tvReportAddress = view.findViewById(R.id.tvReportAddress)
        etReportDetails = view.findViewById(R.id.etReportDetails)
        btnSendReport = view.findViewById(R.id.btnSendReport)
        tvSinReportes = view.findViewById(R.id.tvSinReportes)

        val rvReportes = view.findViewById<RecyclerView>(R.id.rvReportes)
        rvReportes.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReporteAdapter(reportes)
        rvReportes.adapter = adapter

        btnCapture.setOnClickListener {
            verificarPermisoCamara()
        }

        btnRetake.setOnClickListener {
            verificarPermisoCamara()
        }

        btnSendReport.setOnClickListener {
            enviarReporte()
        }

        RetrofitClient.init(com.utp.basurapp.recolectorapp.util.SessionManager(requireContext()))
        cargarReportes()
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirCamara() {
        val photoFile = crearArchivoFoto()
        if (photoFile == null) {
            Toast.makeText(requireContext(), "Error al crear archivo", Toast.LENGTH_SHORT).show()
            return
        }
        fotoFile = photoFile
        fotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        requestTakePicture.launch(fotoUri!!)
    }

    private fun crearArchivoFoto(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile("REPORTE_${timestamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mostrarPreview() {
        fotoFile?.let { file ->
            val compressed = ImageCompressor.comprimir(requireContext(), file)
            val bitmap = BitmapFactory.decodeFile(compressed.absolutePath)
            ivPreview.setImageBitmap(bitmap)
            ivPreview.visibility = View.VISIBLE
            layoutPlaceholder.visibility = View.GONE
            btnRetake.visibility = View.VISIBLE
        }
    }

    @Suppress("MissingPermission")
    private fun obtenerUbicacion() {
        tvReportAddress.text = getString(R.string.report_location_loading)
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    latitud = location.latitude
                    longitud = location.longitude
                    GeocodingService.obtenerDireccion(latitud, longitud) { dir ->
                        activity?.runOnUiThread {
                            direccion = dir ?: String.format("%.6f, %.6f", latitud, longitud)
                            tvReportAddress.text = direccion
                        }
                    }
                } else {
                    tvReportAddress.text = getString(R.string.gps_no_disponible)
                }
            }
            .addOnFailureListener {
                tvReportAddress.text = getString(R.string.gps_error)
            }
    }

    private fun enviarReporte() {
        val file = fotoFile
        if (file == null) {
            Toast.makeText(requireContext(), R.string.report_photo_hint, Toast.LENGTH_SHORT).show()
            return
        }

        if (latitud == 0.0 && longitud == 0.0) {
            Toast.makeText(requireContext(), R.string.gps_no_disponible, Toast.LENGTH_SHORT).show()
            return
        }

        btnSendReport.isEnabled = false
        btnSendReport.text = getString(R.string.report_sending)

        val compressed = ImageCompressor.comprimir(requireContext(), file)
        val requestFile = compressed.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", compressed.name, requestFile)

        val descripcion = (etReportDetails.text?.toString() ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val lat = latitud.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val lon = longitud.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dir = direccion.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.getApiService().enviarReporte(filePart, descripcion, lat, lon, dir)
            .enqueue(object : retrofit2.Callback<ReporteResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ReporteResponse>,
                    response: retrofit2.Response<ReporteResponse>
                ) {
                    activity?.runOnUiThread {
                        btnSendReport.isEnabled = true
                        btnSendReport.text = getString(R.string.btn_send_report)

                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), R.string.report_sent, Toast.LENGTH_SHORT).show()
                            limpiarFormulario()
                            cargarReportes()
                        } else {
                            val errorMsg = try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                errorResponse.error
                            } catch (e: Exception) {
                                getString(R.string.report_error)
                            }
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<ReporteResponse>, t: Throwable) {
                    activity?.runOnUiThread {
                        btnSendReport.isEnabled = true
                        btnSendReport.text = getString(R.string.btn_send_report)
                        Toast.makeText(requireContext(), R.string.report_error, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun limpiarFormulario() {
        fotoFile = null
        fotoUri = null
        latitud = 0.0
        longitud = 0.0
        direccion = ""
        ivPreview.setImageBitmap(null)
        ivPreview.visibility = View.GONE
        layoutPlaceholder.visibility = View.VISIBLE
        btnRetake.visibility = View.GONE
        tvReportAddress.text = getString(R.string.report_location_loading)
        etReportDetails.text?.clear()
    }

    private fun cargarReportes() {
        RetrofitClient.getApiService().listarReportes()
            .enqueue(object : retrofit2.Callback<List<ReporteResponse>> {
                override fun onResponse(
                    call: retrofit2.Call<List<ReporteResponse>>,
                    response: retrofit2.Response<List<ReporteResponse>>
                ) {
                    if (response.isSuccessful) {
                        reportes.clear()
                        response.body()?.let { reportes.addAll(it) }
                        adapter.notifyDataSetChanged()
                        tvSinReportes.visibility = if (reportes.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<ReporteResponse>>, t: Throwable) {
                    // Silenciar error
                }
            })
    }

    private class ReporteAdapter(
        private val items: List<ReporteResponse>
    ) : RecyclerView.Adapter<ReporteAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivFoto: ImageView = view.findViewById(R.id.ivReporteFoto)
            val tvDireccion: TextView = view.findViewById(R.id.tvReporteDireccion)
            val tvDescripcion: TextView = view.findViewById(R.id.tvReporteDescripcion)
            val tvFecha: TextView = view.findViewById(R.id.tvReporteFecha)
            val tvEstado: TextView = view.findViewById(R.id.tvReporteEstado)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reporte, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context

            holder.tvDireccion.text = item.direccion ?: "Sin direccion"
            holder.tvDescripcion.text = item.descripcion?.ifEmpty { "Sin descripcion" } ?: "Sin descripcion"

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(item.fecha)
                holder.tvFecha.text = if (date != null) outputFormat.format(date) else item.fecha
            } catch (e: Exception) {
                holder.tvFecha.text = item.fecha
            }

            val (colorRes, textRes) = when (item.estado) {
                "PENDIENTE" -> Pair(android.graphics.Color.parseColor("#FF6D00"), R.string.estado_pendiente)
                "EN_PROCESO" -> Pair(android.graphics.Color.parseColor("#1565C0"), R.string.estado_en_proceso)
                "RESUELTO" -> Pair(android.graphics.Color.parseColor("#2E7D32"), R.string.estado_resuelto)
                else -> Pair(android.graphics.Color.GRAY, R.string.estado_pendiente)
            }
            holder.tvEstado.setTextColor(colorRes)
            holder.tvEstado.text = context.getString(textRes)

            val baseUrl = com.utp.basurapp.recolectorapp.api.RetrofitClient.getBaseUrl()
            val imageUrl = "${baseUrl}FotosReportes/${item.nombreFoto}"

            holder.ivFoto.setImageResource(R.drawable.ic_add_photo)
            Thread {
                try {
                    val url = java.net.URL(imageUrl)
                    val connection = url.openConnection()
                    connection.doInput = true
                    connection.connect()
                    val input = connection.getInputStream()
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    input.close()
                    holder.ivFoto.post {
                        holder.ivFoto.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    holder.ivFoto.post {
                        holder.ivFoto.setImageResource(R.drawable.ic_add_photo)
                    }
                }
            }.start()
        }

        override fun getItemCount() = items.size
    }
}
