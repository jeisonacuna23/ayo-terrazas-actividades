package com.alfayomega.terrazas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvMes = findViewById<TextView>(R.id.tvMes)
        val calendario = Calendar.getInstance()

        val formatoMes = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val mesEnCurso = formatoMes.format(calendario.time)
        tvMes.text = mesEnCurso.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCalendario)
        recyclerView.layoutManager = GridLayoutManager(this, 7)

        val daysList = generarDiasMesActual(calendario)
        recyclerView.adapter = CalendarioAdapter(daysList)
    }

    private fun generarDiasMesActual(cal: Calendar): List<DiaCalendario> {
        val calendarioLocal = cal.clone() as Calendar
        calendarioLocal.set(Calendar.DAY_OF_MONTH, 1)

        val primerDiaSemana = calendarioLocal.get(Calendar.DAY_OF_WEEK)
        val huecos = if (primerDiaSemana == Calendar.SUNDAY) 6 else primerDiaSemana - 2

        val totalDias = calendarioLocal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lista = mutableListOf<DiaCalendario>()

        for (i in 0 until huecos) {
            lista.add(DiaCalendario(null, 0))
        }
        for (i in 1..totalDias) {
            // Guardamos el número del día y a qué día de la semana corresponde
            lista.add(DiaCalendario(i.toString(), calendarioLocal.get(Calendar.DAY_OF_WEEK)))
            calendarioLocal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return lista
    }
}

// Clase para guardar los datos de cada celda
data class DiaCalendario(val numero: String?, val diaSemana: Int)

class CalendarioAdapter(private val dias: List<DiaCalendario>) : RecyclerView.Adapter<CalendarioAdapter.DiaViewHolder>() {
    class DiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDia: TextView = itemView.findViewById(R.id.tvDia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dia_calendario, parent, false)
        return DiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val dia = dias[position]
        if (dia.numero != null) {
            holder.tvDia.text = dia.numero
            holder.itemView.visibility = View.VISIBLE

            // Aquí atrapamos el clic en el día
            holder.itemView.setOnClickListener {
                when (dia.diaSemana) {
                    Calendar.TUESDAY, Calendar.SUNDAY -> {
                        mostrarModalActividad(holder.itemView.context, dia.diaSemana, esJueves = false)
                    }
                    Calendar.THURSDAY -> {
                        mostrarModalActividad(holder.itemView.context, dia.diaSemana, esJueves = true)
                    }
                    else -> {
                        Toast.makeText(holder.itemView.context, "Este día aún no tiene actividades", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
        }
    }

    private fun mostrarModalActividad(context: Context, diaSemana: Int, esJueves: Boolean) {
        val builder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_actividad, null)

        val layoutPredicador = view.findViewById<View>(R.id.layoutPredicador)

        // Si es Jueves, ocultamos el campo de Predicador
        if (esJueves) {
            layoutPredicador.visibility = View.GONE
        } else {
            layoutPredicador.visibility = View.VISIBLE
        }

        builder.setView(view)
            .setPositiveButton("Guardar") { dialog, _ ->
                // Aquí irá la lógica para guardar los datos después
                Toast.makeText(context, "Guardado (próximamente)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    override fun getItemCount(): Int = dias.size
}
