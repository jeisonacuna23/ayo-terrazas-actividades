package com.alfayomega.terrazas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
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

    private fun generarDiasMesActual(cal: Calendar): MutableList<DiaCalendario> {
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
            lista.add(DiaCalendario(i.toString(), calendarioLocal.get(Calendar.DAY_OF_WEEK)))
            calendarioLocal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return lista
    }
}

// Actualizamos la clase para saber si el día ya tiene un cronograma guardado
data class DiaCalendario(
    val numero: String?, 
    val diaSemana: Int, 
    var tieneCronograma: Boolean = false
)

class CalendarioAdapter(private val dias: MutableList<DiaCalendario>) : RecyclerView.Adapter<CalendarioAdapter.DiaViewHolder>() {
    class DiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDia: TextView = itemView.findViewById(R.id.tvDia)
        val cardView: MaterialCardView = itemView as MaterialCardView
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

            // Comprobar si el día ya tiene un cronograma para pintarlo de verde oscuro
            if (dia.tieneCronograma) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.verde_oscuro))
                holder.tvDia.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.blanco_fondo))
            } else {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.verde_claro))
                holder.tvDia.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.negro_contraste))
            }

            holder.itemView.setOnClickListener {
                when (dia.diaSemana) {
                    Calendar.TUESDAY, Calendar.SUNDAY -> {
                        mostrarModalActividad(holder.itemView.context, position, esJueves = false)
                    }
                    Calendar.THURSDAY -> {
                        mostrarModalActividad(holder.itemView.context, position, esJueves = true)
                    }
                    else -> {
                        Toast.makeText(holder.itemView.context, "Este día aún no tiene actividades configuradas", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
        }
    }

    private fun mostrarModalActividad(context: Context, position: Int, esJueves: Boolean) {
        val builder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_actividad, null)

        val layoutPredicador = view.findViewById<View>(R.id.layoutPredicador)
        val etNombreActividad = view.findViewById<TextInputEditText>(R.id.etNombreActividad)

        if (esJueves) {
            layoutPredicador.visibility = View.GONE
        } else {
            layoutPredicador.visibility = View.VISIBLE
        }

        builder.setView(view)
            .setPositiveButton("Guardar") { dialog, _ ->
                val nombre = etNombreActividad.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    // Marcamos el día como "con cronograma" y actualizamos el cuadro
                    dias[position].tieneCronograma = true
                    notifyItemChanged(position)
                    Toast.makeText(context, "Cronograma establecido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Por favor, ingresa el nombre de la actividad", Toast.LENGTH_SHORT).show()
                }
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
