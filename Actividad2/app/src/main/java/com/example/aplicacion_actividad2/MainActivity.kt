package com.example.aplicacion_actividad2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sqlite.BaseDatos
import com.example.sqlite.Ciudad
import com.example.sqlite.Pais

class MainActivity : AppCompatActivity() {

    private lateinit var baseDatos: BaseDatos
    private lateinit var editTextBuscarPais: EditText
    private lateinit var layoutPaises: LinearLayout
    private lateinit var btnCrearCiudad: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Inicialización de vistas
        editTextBuscarPais = findViewById(R.id.editTextBuscarPais)
        layoutPaises = findViewById(R.id.layoutPaises)
        btnCrearCiudad = findViewById(R.id.btnCrearCiudad)

        // Inicializar base de datos
        baseDatos = BaseDatos(this)

        // Configurar buscador
        editTextBuscarPais.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                cargarPaises(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        editTextBuscarPais
        // Configurar botón de crear ciudad
        btnCrearCiudad.setOnClickListener {
            val intent = Intent(this, CargarCiudadActivity::class.java)
            startActivity(intent)
        }

        // Cargar todos los países al iniciar
        cargarPaises("")

    }

    private fun cargarPaises(filtro: String) {
        layoutPaises.removeAllViews()

        val paises = if (filtro.isBlank()) {
            baseDatos.obtenerPaises()
        } else {
            baseDatos.buscarPaisesYCiudades(filtro)
        }.sortedBy { it.nombre }

        if (paises.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No hay países que coincidan con la búsqueda"
                setPadding(0, 16.dpToPx(), 0, 0)
            }
            layoutPaises.addView(tvEmpty)
            return
        }

        paises.forEach { pais ->
            val viewPais = LayoutInflater.from(this).inflate(R.layout.item_pais, null) as LinearLayout
            val tvNombrePais = viewPais.findViewById<TextView>(R.id.textViewNombrePais)
            val btnEliminar = viewPais.findViewById<ImageButton>(R.id.btnEliminarCiudades)
            val btnExpandir = viewPais.findViewById<ImageButton>(R.id.btnExpandir)
            val layoutCiudades = viewPais.findViewById<LinearLayout>(R.id.layoutCiudades)

            tvNombrePais.text = pais.nombre

            btnExpandir.setOnClickListener {
                if (layoutCiudades.visibility == View.VISIBLE) {
                    layoutCiudades.visibility = View.GONE
                    btnExpandir.setImageResource(android.R.drawable.arrow_down_float)
                } else {
                    cargarCiudades(pais.id, layoutCiudades)
                    layoutCiudades.visibility = View.VISIBLE
                    btnExpandir.setImageResource(android.R.drawable.arrow_up_float)
                }
            }

            btnEliminar.setOnClickListener {
                mostrarDialogoConfirmacionEliminar(pais)
            }

            layoutPaises.addView(viewPais)
        }
    }

    private fun cargarCiudades(idPais: Int, layoutCiudades: LinearLayout) {
        layoutCiudades.removeAllViews()

        // Obtenemos las ciudades usando obtenerCiudadPorNombre (adaptado)
        // Nota: Sería ideal tener un obtenerCiudadesPorPais() en BaseDatos
        val ciudades = obtenerCiudadesDePais(idPais)

        if (ciudades.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No hay ciudades en este país"
                setPadding(16.dpToPx(), 8.dpToPx(), 0, 8.dpToPx())
            }
            layoutCiudades.addView(tvEmpty)
            return
        }
// En el método donde configuras tus items de ciudad (en cargarCiudades())
        ciudades.forEach { ciudad ->
            val viewCiudad = LayoutInflater.from(this).inflate(R.layout.item_ciudad, null)
            val tvNombreCiudad = viewCiudad.findViewById<TextView>(R.id.textViewNombreCiudad)
            val tvHabitantes = viewCiudad.findViewById<TextView>(R.id.textViewHabitantes)
            val btnEliminar = viewCiudad.findViewById<ImageButton>(R.id.btnEliminarCiudad)
// En el método donde configuras tus items de ciudad (en cargarCiudades())
            val btnEditar = viewCiudad.findViewById<ImageButton>(R.id.btnEditarCiudad)
            btnEditar.setOnClickListener {
                mostrarPopupActualizarPoblacion(ciudad)
            }
            tvNombreCiudad.text = ciudad.nombre
            tvHabitantes.text = "Habitantes: ${ciudad.habitantes}"

            btnEliminar.setOnClickListener {
                mostrarDialogoConfirmacionEliminarCiudad(ciudad)
            }

            layoutCiudades.addView(viewCiudad)
        }
    }

    // Método temporal hasta agregar obtenerCiudadesPorPais a BaseDatos
    private fun obtenerCiudadesDePais(idPais: Int): List<Ciudad> {
        // Esta es una solución temporal usando obtenerCiudadPorNombre
        // Lo ideal sería agregar un método obtenerCiudadesPorPais a BaseDatos
        val db = baseDatos.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ciudad WHERE idPais = ?", arrayOf(idPais.toString()))
        val ciudades = mutableListOf<Ciudad>()

        while (cursor.moveToNext()) {
            ciudades.add(Ciudad(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                habitantes = cursor.getInt(cursor.getColumnIndexOrThrow("habitantes")),
                idPais = idPais
            ))
        }

        cursor.close()
        return ciudades
    }

    private fun mostrarDialogoConfirmacionEliminar(pais: Pais) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar ciudades del pais")
            .setMessage("¿Eliminar todas sus ciudades?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCiudadesDePais(pais)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun mostrarPopupActualizarPoblacion(ciudad: Ciudad) {
        // Inflar el layout del popup
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_actualizar_poblacion, null)

        // Configurar el diálogo
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Obtener referencias a las vistas del popup
        val editNuevaPoblacion = dialogView.findViewById<EditText>(R.id.editTextNuevaPoblacion)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarActualizacion)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarPoblacion)

        // Mostrar la población actual como hint
        editNuevaPoblacion.hint = "Actual: ${ciudad.habitantes}"

        // Configurar listeners
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nuevaPoblacion = editNuevaPoblacion.text.toString().toIntOrNull() ?: 0

            if (nuevaPoblacion <= 0) {
                editNuevaPoblacion.error = "La población debe ser mayor a 0"
                return@setOnClickListener
            }

            // Actualizar en la base de datos
            val resultado = baseDatos.actualizarPoblacionCiudad(ciudad.id, nuevaPoblacion)

            if (resultado > 0) {
                Toast.makeText(this, "Población actualizada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                // Actualizar la vista si es necesario
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }

        // Mostrar el diálogo
        dialog.show()
    }
    private fun eliminarCiudadesDePais(pais: Pais) {
        val db = baseDatos.writableDatabase
        try {
            db.beginTransaction()

            // Solo eliminar ciudades asociadas al país
            db.delete("ciudad", "idPais = ?", arrayOf(pais.id.toString()))

            db.setTransactionSuccessful()
            Toast.makeText(this, "Ciudades de ${pais.nombre} eliminadas", Toast.LENGTH_SHORT).show()
            cargarPaises(editTextBuscarPais.text.toString())
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar ciudades", Toast.LENGTH_SHORT).show()
        } finally {
            db.endTransaction()
        }
    }


    private fun mostrarDialogoConfirmacionEliminarCiudad(ciudad: Ciudad) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar ciudad")
            .setMessage("¿Eliminar ${ciudad.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Usamos el método eliminarCiudad de BaseDatos
                val resultado = baseDatos.eliminarCiudad(ciudad.id)
                if (resultado > 0) {
                    Toast.makeText(this, "Ciudad eliminada", Toast.LENGTH_SHORT).show()
                    cargarPaises(editTextBuscarPais.text.toString())
                } else {
                    Toast.makeText(this, "Error al eliminar ciudad", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Extensión para convertir dp a px
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        baseDatos.close()
        super.onDestroy()
    }
}

