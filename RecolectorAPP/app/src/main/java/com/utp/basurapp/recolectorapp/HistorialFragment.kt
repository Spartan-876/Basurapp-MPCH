package com.utp.basurapp.recolectorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.utp.basurapp.recolectorapp.util.NotificationHistoryManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HistorialFragment : Fragment() {

    private lateinit var scrollHistorial: androidx.core.widget.NestedScrollView
    private lateinit var linealHistorial: LinearLayout
    private lateinit var tvEmptyHistory: TextView
    private var currentFilter: String = "all"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollHistorial = view.findViewById(R.id.scrollHistorial)
        linealHistorial = view.findViewById(R.id.linealHistorial)
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.headerHistorial)) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, statusBarHeight + 16, v.paddingRight, 8)
            insets
        }

        val filterAll = view.findViewById<TextView>(R.id.filterAll)
        val filterImportant = view.findViewById<TextView>(R.id.filterImportant)
        val filterResolved = view.findViewById<TextView>(R.id.filterResolved)

        val filters = listOf(filterAll to "all", filterImportant to "importante", filterResolved to "resuelto")

        filters.forEach { (tv, tipo) ->
            tv.setOnClickListener {
                filters.forEach { (f, _) ->
                    f.setBackgroundResource(0)
                    f.setTextColor(MaterialColors.getColor(f, com.google.android.material.R.attr.colorOnSurfaceVariant))
                }
                tv.setBackgroundResource(R.drawable.bg_info_message)
                tv.setTextColor(MaterialColors.getColor(tv, com.google.android.material.R.attr.colorOnSurface))
                currentFilter = tipo
                cargarHistorial()
            }
        }

        filterAll.performClick()
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    private fun cargarHistorial() {
        val manager = NotificationHistoryManager(requireContext())
        val registros = when (currentFilter) {
            "all" -> manager.obtenerHistorial()
            else -> manager.obtenerHistorialPorTipo(currentFilter)
        }

        linealHistorial.removeAllViews()

        if (registros.isEmpty()) {
            tvEmptyHistory.visibility = View.VISIBLE
            scrollHistorial.visibility = View.GONE
            return
        }

        tvEmptyHistory.visibility = View.GONE
        scrollHistorial.visibility = View.VISIBLE

        var lastDateLabel = ""

        for (record in registros) {
            val dateLabel = obtenerFechaLabel(record.timestamp)
            if (dateLabel != lastDateLabel) {
                val header = TextView(requireContext()).apply {
                    text = dateLabel
                    textSize = 14f
                    setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant))
                    setPadding(0, if (lastDateLabel.isEmpty()) 0 else 24, 0, 8)
                }
                linealHistorial.addView(header)
                lastDateLabel = dateLabel
            }
            linealHistorial.addView(crearCard(record))
        }
    }

    private fun crearCard(record: NotificationHistoryManager.NotificationRecord): View {
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        val dp8 = (8 * resources.displayMetrics.density).toInt()
        val dp12 = (12 * resources.displayMetrics.density).toInt()

        val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp8
            }
            radius = (12 * resources.displayMetrics.density)
            cardElevation = (1 * resources.displayMetrics.density)
            setCardBackgroundColor(com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface))
        }

        val cardContent = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp16, dp16, dp16, dp16)
        }

        val iconRes = when (record.tipo) {
            "camion" -> R.drawable.ic_local_shipping
            "importante" -> R.drawable.ic_schedule
            "resuelto" -> R.drawable.ic_check_circle
            else -> R.drawable.ic_info
        }

        val icon = android.widget.ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dp12 * 3 + dp12, dp12 * 3 + dp12)
            setImageResource(iconRes)
            contentDescription = record.tipo
        }

        val textSection = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp12
            }
        }

        val topRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val typeLabel = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            textSize = 12f
            setTextColor(when (record.tipo) {
                "camion" -> 0xFF2E7D32.toInt()
                "importante" -> 0xFFE65100.toInt()
                "resuelto" -> 0xFF2E7D32.toInt()
                else -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant)
            })
            text = when (record.tipo) {
                "camion" -> getString(R.string.alert_info)
                "importante" -> getString(R.string.alert_important)
                "resuelto" -> getString(R.string.alert_resolved)
                else -> getString(R.string.alert_warning)
            }
        }

        val timeLabel = TextView(requireContext()).apply {
            textSize = 12f
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant))
            text = formatearTiempoRelativo(record.timestamp)
        }

        topRow.addView(typeLabel)
        topRow.addView(timeLabel)

        val title = TextView(requireContext()).apply {
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface))
            setPadding(0, dp12 / 6, 0, 0)
            text = record.titulo
        }

        val desc = TextView(requireContext()).apply {
            textSize = 13f
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant))
            setPadding(0, dp12 / 6, 0, 0)
            maxLines = 2
            text = record.descripcion
        }

        textSection.addView(topRow)
        textSection.addView(title)
        textSection.addView(desc)

        cardContent.addView(icon)
        cardContent.addView(textSection)

        card.addView(cardContent)
        return card
    }

    private fun obtenerFechaLabel(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hoy = Calendar.getInstance()
        val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            cal.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR) -> "Hoy"
            cal.get(Calendar.YEAR) == ayer.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == ayer.get(Calendar.DAY_OF_YEAR) -> "Ayer"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun formatearTiempoRelativo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hoy, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
            days < 7 -> "Hace $days días"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
