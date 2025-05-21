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

    companion object {
        private const val REQUEST_CODE_CARGAR_CIUDAD = 1001
    }

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
                val filtro = s.toString().trim()
                if (filtro.length >= 2 || filtro.isEmpty()) {
                    cargarPaises(filtro)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Configurar botón de crear ciudad
        btnCrearCiudad.setOnClickListener {
            val intent = Intent(this, CargarCiudadActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CARGAR_CIUDAD)
        }

        // Cargar todos los países al iniciar
        cargarPaises("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CARGAR_CIUDAD && resultCode == RESULT_OK) {
            // Recargar los datos cuando regresamos de crear una ciudad
            cargarPaises(editTextBuscarPais.text.toString())
        }
    }

    private fun cargarPaises(filtro: String) {
        layoutPaises.removeAllViews()

        val resultados = if (filtro.isBlank()) {
            baseDatos.obtenerPaises().map { pais ->
                pais to obtenerCiudadesDePais(pais.id)
            }
        } else {
            baseDatos.buscarPaisesYCiudades(filtro)
        }

        if (resultados.isEmpty()) {
            mostrarMensajeNoEncontrado()
            return
        }

        resultados.forEach { (pais, ciudades) ->
            val viewPais = LayoutInflater.from(this).inflate(R.layout.item_pais, null) as LinearLayout
            val tvNombrePais = viewPais.findViewById<TextView>(R.id.textViewNombrePais)
            val btnEliminar = viewPais.findViewById<ImageButton>(R.id.btnEliminarCiudades)
            val btnExpandir = viewPais.findViewById<ImageButton>(R.id.btnExpandir)
            val layoutCiudades = viewPais.findViewById<LinearLayout>(R.id.layoutCiudades)

            tvNombrePais.text = pais.nombre

            if (ciudades.isNotEmpty()) {
                btnExpandir.setOnClickListener {
                    if (layoutCiudades.visibility == View.VISIBLE) {
                        layoutCiudades.visibility = View.GONE
                        btnExpandir.setImageResource(android.R.drawable.arrow_down_float)
                    } else {
                        mostrarCiudades(ciudades, layoutCiudades)
                        layoutCiudades.visibility = View.VISIBLE
                        btnExpandir.setImageResource(android.R.drawable.arrow_up_float)
                    }
                }
            } else {
                btnExpandir.visibility = View.INVISIBLE
            }

            if (filtro.isNotBlank() && ciudades.isNotEmpty()) {
                mostrarCiudades(ciudades, layoutCiudades)
                layoutCiudades.visibility = View.VISIBLE
                btnExpandir.setImageResource(android.R.drawable.arrow_up_float)
            }

            btnEliminar.setOnClickListener {
                mostrarDialogoConfirmacionEliminar(pais)
            }

            layoutPaises.addView(viewPais)
        }
    }

    private fun mostrarCiudades(ciudades: List<Ciudad>, layoutCiudades: LinearLayout) {
        layoutCiudades.removeAllViews()

        ciudades.forEach { ciudad ->
            val viewCiudad = LayoutInflater.from(this).inflate(R.layout.item_ciudad, null)
            viewCiudad.findViewById<TextView>(R.id.textViewNombreCiudad).text = ciudad.nombre
            viewCiudad.findViewById<TextView>(R.id.textViewHabitantes).text = "Habitantes: ${ciudad.habitantes}"

            viewCiudad.findViewById<ImageButton>(R.id.btnEditarCiudad).setOnClickListener {
                mostrarPopupActualizarPoblacion(ciudad)
            }

            viewCiudad.findViewById<ImageButton>(R.id.btnEliminarCiudad).setOnClickListener {
                mostrarDialogoConfirmacionEliminarCiudad(ciudad)
            }

            layoutCiudades.addView(viewCiudad)
        }
    }

    private fun mostrarMensajeNoEncontrado() {
        val tvEmpty = TextView(this).apply {
            text = "No se encontraron resultados"
            setPadding(0, 16.dpToPx(), 0, 0)
        }
        layoutPaises.addView(tvEmpty)
    }

    private fun obtenerCiudadesDePais(idPais: Int): List<Ciudad> {
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
            .setTitle("Eliminar ciudades del país")
            .setMessage("¿Eliminar todas las ciudades de ${pais.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCiudadesDePais(pais)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCiudadesDePais(pais: Pais) {
        val db = baseDatos.writableDatabase
        try {
            db.beginTransaction()
            db.delete("ciudad", "idPais = ?", arrayOf(pais.id.toString()))
            db.setTransactionSuccessful()
            Toast.makeText(this, "Ciudades eliminadas", Toast.LENGTH_SHORT).show()
            cargarPaises(editTextBuscarPais.text.toString())
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar ciudades", Toast.LENGTH_SHORT).show()
        } finally {
            db.endTransaction()
        }
    }

    private fun mostrarPopupActualizarPoblacion(ciudad: Ciudad) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_actualizar_poblacion, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val editNuevaPoblacion = dialogView.findViewById<EditText>(R.id.editTextNuevaPoblacion)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarActualizacion)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarPoblacion)

        editNuevaPoblacion.hint = "Actual: ${ciudad.habitantes}"

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val nuevaPoblacion = editNuevaPoblacion.text.toString().toIntOrNull() ?: 0

            if (nuevaPoblacion <= 0) {
                editNuevaPoblacion.error = "La población debe ser mayor a 0"
                return@setOnClickListener
            }

            val resultado = baseDatos.actualizarPoblacionCiudad(ciudad.id, nuevaPoblacion)

            if (resultado > 0) {
                Toast.makeText(this, "Población actualizada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                cargarPaises(editTextBuscarPais.text.toString())
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmacionEliminarCiudad(ciudad: Ciudad) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar ciudad")
            .setMessage("¿Eliminar ${ciudad.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                if (baseDatos.eliminarCiudad(ciudad.id) > 0) {
                    Toast.makeText(this, "Ciudad eliminada", Toast.LENGTH_SHORT).show()
                    cargarPaises(editTextBuscarPais.text.toString())
                } else {
                    Toast.makeText(this, "Error al eliminar ciudad", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        baseDatos.close()
        super.onDestroy()
    }
}