package com.alfayomega.terrazas

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Actividad(
    val nombre: String,
    val director: String,
    val predicador: String,
    val ujier: String,
    val sonidista: String,
    val nota: String
)

data class DiaCalendario(
    val numero: String?,
    val diaSemana: Int,
    val fechaStr: String?
)

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private val actividadesGuardadas = mutableMapOf<String, Actividad>()
    private lateinit var adapterCalendario: CalendarioAdapter
    private lateinit var tvResumenCompleto: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("AyO_Prefs", Context.MODE_PRIVATE)
        cargarDesdePrefs()

        val tvMes = findViewById<TextView>(R.id.tvMes)
        tvResumenCompleto = findViewById(R.id.tvResumenCompleto)
        val calendario = Calendar.getInstance()

        val formatoMes = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val mesEnCurso = formatoMes.format(calendario.time)
        tvMes.text = mesEnCurso.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val recyclerViewCalendario = findViewById<RecyclerView>(R.id.recyclerViewCalendario)
        recyclerViewCalendario.layoutManager = GridLayoutManager(this, 7)
        val daysList = generarDiasMesActual(calendario)

        adapterCalendario = CalendarioAdapter(daysList, actividadesGuardadas) { fecha, actividad ->
            actividadesGuardadas[fecha] = actividad
            guardarEnPrefs()
            
            val pos = daysList.indexOfFirst { it.fechaStr == fecha }
            if (pos != -1) {
                adapterCalendario.notifyItemChanged(pos)
            }
            actualizarResumen()
        }
        recyclerViewCalendario.adapter = adapterCalendario

        actualizarResumen()
    }

    private fun generarDiasMesActual(cal: Calendar): MutableList<DiaCalendario> {
        val calendarioLocal = cal.clone() as Calendar
        calendarioLocal.set(Calendar.DAY_OF_MONTH, 1)

        val primerDiaSemana = calendarioLocal.get(Calendar.DAY_OF_WEEK)
        val huecos = if (primerDiaSemana == Calendar.SUNDAY) 6 else primerDiaSemana - 2

        val totalDias = calendarioLocal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lista = mutableListOf<DiaCalendario>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until huecos) {
            lista.add(DiaCalendario(null, 0, null))
        }
        for (i in 1..totalDias) {
            val fechaStr = sdf.format(calendarioLocal.time)
            lista.add(DiaCalendario(i.toString(), calendarioLocal.get(Calendar.DAY_OF_WEEK), fechaStr))
            calendarioLocal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return lista
    }

    private fun actualizarResumen() {
        val sb = SpannableStringBuilder()
        // Ordenar las actividades por fecha
        val listaOrdenada = actividadesGuardadas.entries.sortedBy { it.key }

        for ((fecha, act) in listaOrdenada) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
            // Usamos EEEE para el nombre completo del día (ej. "martes 15")
            val diaTexto = SimpleDateFormat("EEEE d", Locale("es", "ES")).format(date!!)
            val diaCapitalizado = diaTexto.replaceFirstChar { it.uppercase() }

            // 1. Día y Actividad (En negrita)
            val encabezado = "$diaCapitalizado\n${act.nombre}\n"
            val startEncabezado = sb.length
            sb.append(encabezado)
            sb.setSpan(StyleSpan(Typeface.BOLD), startEncabezado, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // 2. Parámetros (Etiquetas en negrita, nombres normales)
            agregarParametro(sb, "Director: ", act.director)
            agregarParametro(sb, "Predicador: ", act.predicador)
            agregarParametro(sb, "Ujier: ", act.ujier)
            agregarParametro(sb, "Sonidista: ", act.sonidista)
            agregarParametro(sb, "Nota: ", act.nota)

            sb.append("\n\n") // Espacio entre días
        }

        tvResumenCompleto.text = sb
    }

    private fun agregarParametro(sb: SpannableStringBuilder, etiqueta: String, valor: String) {
        if (valor.isNotBlank()) {
            val start = sb.length
            sb.append(etiqueta) // Ej: "Director: "
            sb.setSpan(StyleSpan(Typeface.BOLD), start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.append("$valor\n") // El nombre va normal
        }
    }

    private fun guardarEnPrefs() {
        val jsonObject = JSONObject()
        for ((fecha, act) in actividadesGuardadas) {
            val obj = JSONObject()
            obj.put("nombre", act.nombre)
            obj.put("director", act.director)
            obj.put("predicador", act.predicador)
            obj.put("ujier", act.ujier)
            obj.put("sonidista", act.sonidista)
            obj.put("nota", act.nota)
            jsonObject.put(fecha, obj)
        }
        prefs.edit().putString("actividades", jsonObject.toString()).apply()
    }

    private fun cargarDesdePrefs() {
        try {
            val jsonString = prefs.getString("actividades", "{}") ?: "{}"
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.getJSONObject(key)
                actividadesGuardadas[key] = Actividad(
                    obj.optString("nombre", ""),
                    obj.optString("director", ""),
                    obj.optString("predicador", ""),
                    obj.optString("ujier", ""),
                    obj.optString("sonidista", ""),
                    obj.optString("nota", "")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class CalendarioAdapter(
    private val dias: MutableList<DiaCalendario>,
    private val actividades: MutableMap<String, Actividad>,
    private val onGuardar: (String, Actividad) -> Unit
) : RecyclerView.Adapter<CalendarioAdapter.DiaViewHolder>() {

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

            val tieneGuardado = dia.fechaStr != null && actividades.containsKey(dia.fechaStr)

            if (tieneGuardado) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.verde_oscuro))
                holder.tvDia.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.blanco_fondo))
            } else {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.verde_claro))
                holder.tvDia.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.negro_contraste))
            }

            holder.itemView.setOnClickListener {
                if (dia.diaSemana == Calendar.TUESDAY || dia.diaSemana == Calendar.THURSDAY || dia.diaSemana == Calendar.SUNDAY) {
                    mostrarModalActividad(holder.itemView.context, dia)
                } else {
                    Toast.makeText(holder.itemView.context, "Este día aún no tiene actividades", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
        }
    }

    private fun mostrarModalActividad(context: Context, dia: DiaCalendario) {
        val builder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_actividad, null)

        val layoutPredicador = view.findViewById<View>(R.id.layoutPredicador)
        val etNombreActividad = view.findViewById<TextInputEditText>(R.id.etNombreActividad)
        val etDirector = view.findViewById<TextInputEditText>(R.id.etDirector)
        val etPredicador = view.findViewById<TextInputEditText>(R.id.etPredicador)
        val etUjier = view.findViewById<TextInputEditText>(R.id.etUjier)
        val etSonidista = view.findViewById<TextInputEditText>(R.id.etSonidista)
        val etNota = view.findViewById<TextInputEditText>(R.id.etNota)

        val esJueves = dia.diaSemana == Calendar.THURSDAY
        if (esJueves) {
            layoutPredicador.visibility = View.GONE
        } else {
            layoutPredicador.visibility = View.VISIBLE
        }

        val fecha = dia.fechaStr ?: return
        val datosGuardados = actividades[fecha]
        
        if (datosGuardados != null) {
            etNombreActividad.setText(datosGuardados.nombre)
            etDirector.setText(datosGuardados.director)
            etPredicador.setText(datosGuardados.predicador)
            etUjier.setText(datosGuardados.ujier)
            etSonidista.setText(datosGuardados.sonidista)
            etNota.setText(datosGuardados.nota)
        } else {
            val nombreDefecto = when (dia.diaSemana) {
                Calendar.TUESDAY -> "Culto de Enseñanza"
                Calendar.THURSDAY -> "Culto de Oración"
                Calendar.SUNDAY -> "Escuela Dominical"
                else -> ""
            }
            etNombreActividad.setText(nombreDefecto)
        }

        builder.setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombreActividad.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    val nuevaActividad = Actividad(
                        nombre,
                        etDirector.text.toString().trim(),
                        etPredicador.text.toString().trim(),
                        etUjier.text.toString().trim(),
                        etSonidista.text.toString().trim(),
                        etNota.text.toString().trim()
                    )
                    onGuardar(fecha, nuevaActividad)
                    Toast.makeText(context, "Cronograma guardado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "El nombre de la actividad es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun getItemCount(): Int = dias.size
}
