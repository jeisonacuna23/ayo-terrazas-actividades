package com.alfayomega.terrazas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCalendario)
        recyclerView.layoutManager = GridLayoutManager(this, 7)

        val daysList = generarDiasMesActual()
        recyclerView.adapter = CalendarioAdapter(daysList)
    }

    private fun generarDiasMesActual(): List<String?> {
        val cal = Calendar.getInstance()
        // Ir al primer día del mes actual
        cal.set(Calendar.DAY_OF_MONTH, 1)

        // Calcular los huecos antes del día 1 (para que la semana inicie en Lunes)
        // En Calendar: Domingo=1, Lunes=2... Sábado=7
        val primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK)
        val huecos = if (primerDiaSemana == Calendar.SUNDAY) 6 else primerDiaSemana - 2

        val totalDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lista = mutableListOf<String?>()

        // Agregar espacios vacíos
        for (i in 0 until huecos) {
            lista.add(null)
        }
        // Agregar los días reales del mes
        for (i in 1..totalDias) {
            lista.add(i.toString())
        }
        return lista
    }
}

class CalendarioAdapter(private val dias: List<String?>) : RecyclerView.Adapter<CalendarioAdapter.DiaViewHolder>() {
    class DiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDia: TextView = itemView.findViewById(R.id.tvDia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dia_calendario, parent, false)
        return DiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val dia = dias[position]
        if (dia != null) {
            holder.tvDia.text = dia
            holder.tvDia.visibility = View.VISIBLE
            // Aquí conectaremos el click para abrir tu modal más adelante.
        } else {
            holder.tvDia.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = dias.size
}
