package com.utp.basurapp.recolectorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class HistorialFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filterAll = view.findViewById<TextView>(R.id.filterAll)
        val filterImportant = view.findViewById<TextView>(R.id.filterImportant)
        val filterResolved = view.findViewById<TextView>(R.id.filterResolved)

        val filters = listOf(filterAll, filterImportant, filterResolved)

        filters.forEach { tv ->
            tv.setOnClickListener {
                filters.forEach { f ->
                    f.setBackgroundResource(0)
                    f.setTextColor(requireContext().getColorStateList(R.color.onSurfaceVariant))
                }
                tv.setBackgroundResource(R.drawable.bg_info_message)
                tv.setTextColor(requireContext().getColorStateList(R.color.onSurface))
            }
        }

        filterAll.performClick()
    }
}
