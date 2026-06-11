package com.utp.basurapp.recolectorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.FamiliarRequest
import com.utp.basurapp.recolectorapp.data.FamiliarResponse
import com.utp.basurapp.recolectorapp.util.SessionManager

class CompartirAlertaActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val familiares = mutableListOf<FamiliarResponse>()
    private lateinit var adapter: FamiliarAdapter
    private lateinit var rvFamiliares: RecyclerView
    private lateinit var tvSinFamiliares: TextView
    private lateinit var etMensaje: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compartir_alerta)

        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, statusBarHeight, v.paddingRight, 0)
            insets
        }

        rvFamiliares = findViewById(R.id.rvFamiliares)
        tvSinFamiliares = findViewById(R.id.tvSinFamiliares)
        etMensaje = findViewById(R.id.etMensaje)

        rvFamiliares.layoutManager = LinearLayoutManager(this)
        adapter = FamiliarAdapter(familiares, { familiar ->
            mostrarDialogEditarFamiliar(familiar)
        }, { familiar ->
            confirmarEliminarFamiliar(familiar)
        })
        rvFamiliares.adapter = adapter

        findViewById<MaterialButton>(R.id.btnNuevoFamiliar).setOnClickListener {
            mostrarDialogNuevoFamiliar()
        }

        findViewById<MaterialButton>(R.id.btnWhatsApp).setOnClickListener {
            enviarWhatsApp()
        }

        findViewById<MaterialButton>(R.id.btnSMS).setOnClickListener {
            enviarSMS()
        }

        findViewById<MaterialButton>(R.id.btnEnviarAlerta).setOnClickListener {
            if (familiares.isEmpty()) {
                Toast.makeText(this, R.string.sin_familiares, Toast.LENGTH_SHORT).show()
            } else {
                enviarWhatsApp()
            }
        }

        cargarFamiliares()
    }

    private fun cargarFamiliares() {
        RetrofitClient.getApiService().listarFamiliares()
            .enqueue(object : retrofit2.Callback<List<FamiliarResponse>> {
                override fun onResponse(
                    call: retrofit2.Call<List<FamiliarResponse>>,
                    response: retrofit2.Response<List<FamiliarResponse>>
                ) {
                    if (response.isSuccessful) {
                        familiares.clear()
                        response.body()?.let { familiares.addAll(it) }
                        actualizarLista()
                    } else {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.error_cargar_familiares, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<List<FamiliarResponse>>,
                    t: Throwable
                ) {
                    Toast.makeText(this@CompartirAlertaActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarLista() {
        adapter.notifyDataSetChanged()
        tvSinFamiliares.visibility = if (familiares.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogNuevoFamiliar() {
        mostrarDialogFamiliar(null)
    }

    private fun mostrarDialogEditarFamiliar(familiar: FamiliarResponse) {
        mostrarDialogFamiliar(familiar)
    }

    private fun mostrarDialogFamiliar(familiar: FamiliarResponse?) {
        val isEdit = familiar != null
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_familiar, null)
        val etNombre = view.findViewById<TextInputEditText>(R.id.etDialogNombre)
        val etTelefono = view.findViewById<TextInputEditText>(R.id.etDialogTelefono)

        if (isEdit) {
            etNombre.setText(familiar.nombre)
            etTelefono.setText(familiar.telefono)
        }

        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(if (isEdit) "Editar Familiar" else getString(R.string.nuevo_familiar_title))
        builder.setView(view)
        builder.setPositiveButton(R.string.btn_guardar) { _, _ ->
            val nombre = etNombre.text?.toString()?.trim() ?: ""
            val telefono = etTelefono.text?.toString()?.trim() ?: ""

            if (nombre.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Nombre y teléfono son obligatorios", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (isEdit) {
                actualizarFamiliar(familiar.id, nombre, telefono)
            } else {
                agregarFamiliar(nombre, telefono)
            }
        }
        builder.setNegativeButton(R.string.btn_cancelar, null)
        builder.show()
    }

    private fun agregarFamiliar(nombre: String, telefono: String) {
        RetrofitClient.getApiService().agregarFamiliar(FamiliarRequest(nombre, telefono))
            .enqueue(object : retrofit2.Callback<FamiliarResponse> {
                override fun onResponse(
                    call: retrofit2.Call<FamiliarResponse>,
                    response: retrofit2.Response<FamiliarResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.familiar_guardado, Toast.LENGTH_SHORT).show()
                        cargarFamiliares()
                    } else {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.error_server, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<FamiliarResponse>, t: Throwable) {
                    Toast.makeText(this@CompartirAlertaActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarFamiliar(id: Long, nombre: String, telefono: String) {
        RetrofitClient.getApiService().actualizarFamiliar(id, FamiliarRequest(nombre, telefono))
            .enqueue(object : retrofit2.Callback<FamiliarResponse> {
                override fun onResponse(
                    call: retrofit2.Call<FamiliarResponse>,
                    response: retrofit2.Response<FamiliarResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.familiar_guardado, Toast.LENGTH_SHORT).show()
                        cargarFamiliares()
                    } else {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.error_server, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<FamiliarResponse>, t: Throwable) {
                    Toast.makeText(this@CompartirAlertaActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmarEliminarFamiliar(familiar: FamiliarResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.confirmar_eliminar, familiar.nombre))
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                eliminarFamiliar(familiar.id)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun eliminarFamiliar(id: Long) {
        RetrofitClient.getApiService().eliminarFamiliar(id)
            .enqueue(object : retrofit2.Callback<com.utp.basurapp.recolectorapp.data.ApiResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.ApiResponse>,
                    response: retrofit2.Response<com.utp.basurapp.recolectorapp.data.ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.familiar_eliminado, Toast.LENGTH_SHORT).show()
                        cargarFamiliares()
                    } else {
                        Toast.makeText(this@CompartirAlertaActivity, R.string.error_server, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.utp.basurapp.recolectorapp.data.ApiResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(this@CompartirAlertaActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun enviarWhatsApp() {
        val telefono = obtenerTelefonoSeleccionado()
        val mensaje = etMensaje.text?.toString() ?: getString(R.string.mensaje_whatsapp)
        val uri = "https://api.whatsapp.com/send?phone=$telefono&text=${Uri.encode(mensaje)}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarSMS() {
        val telefono = obtenerTelefonoSeleccionado()
        val mensaje = etMensaje.text?.toString() ?: getString(R.string.mensaje_whatsapp)
        val uri = "sms:$telefono?body=${Uri.encode(mensaje)}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.sms_not_available, Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerTelefonoSeleccionado(): String {
        return if (familiares.isNotEmpty()) {
            familiares[0].telefono
        } else {
            "51"
        }
    }

    private class FamiliarAdapter(
        private val items: List<FamiliarResponse>,
        private val onEdit: (FamiliarResponse) -> Unit,
        private val onDelete: (FamiliarResponse) -> Unit
    ) : RecyclerView.Adapter<FamiliarAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvItemNombre)
            val tvTelefono: TextView = view.findViewById(R.id.tvItemTelefono)
            val btnEliminar: MaterialButton = view.findViewById(R.id.btnItemEliminar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_familiar, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvNombre.text = item.nombre
            holder.tvTelefono.text = item.telefono
            holder.itemView.setOnClickListener { onEdit(item) }
            holder.btnEliminar.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = items.size
    }
}
