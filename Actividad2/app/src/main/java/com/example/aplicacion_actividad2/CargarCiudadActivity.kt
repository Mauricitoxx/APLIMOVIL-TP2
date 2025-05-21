package com.example.aplicacion_actividad2

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sqlite.BaseDatos
import com.example.sqlite.Pais

class CargarCiudadActivity : AppCompatActivity() {

    private lateinit var baseDatos: BaseDatos
    private lateinit var spinnerPaises: Spinner
    private lateinit var layoutNuevoPais: LinearLayout
    private lateinit var editTextNuevoPais: EditText
    private lateinit var editTextNombreCiudad: EditText
    private lateinit var editTextPoblacion: EditText
    private lateinit var viewSeparador1: View
    private lateinit var viewSeparador2: View

    private val listaPaises = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    // Colores para los separadores
    private val originalSeparatorColor = Color.BLACK
    private val errorSeparatorColor = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cargar)

        // Inicializar vistas
        spinnerPaises = findViewById(R.id.spinnerPaises)
        layoutNuevoPais = findViewById(R.id.layoutNuevoPais)
        editTextNuevoPais = findViewById(R.id.editTextNuevoPais)
        editTextNombreCiudad = findViewById(R.id.editTextNombreCiudad)
        editTextPoblacion = findViewById(R.id.editTextPoblacion)
        viewSeparador1 = findViewById(R.id.viewSeparador1)
        viewSeparador2 = findViewById(R.id.viewSeparador2)

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // Inicializar base de datos
        baseDatos = BaseDatos(this)

        // Configurar Spinner
        configurarSpinnerPaises()

        // Configurar listeners
        findViewById<Button>(R.id.btnAgregarNuevoPais).setOnClickListener { agregarNuevoPais() }
        findViewById<Button>(R.id.btnCancelarNuevoPais).setOnClickListener { ocultarPopupPais() }
        findViewById<Button>(R.id.btnGuardarCiudad).setOnClickListener { guardarCiudad() }
    }

    private fun resetSeparatorColors() {
        viewSeparador1.setBackgroundColor(originalSeparatorColor)
        viewSeparador2.setBackgroundColor(originalSeparatorColor)
    }

    private fun showErrorOnSeparators() {
        viewSeparador1.setBackgroundColor(errorSeparatorColor)
        viewSeparador2.setBackgroundColor(errorSeparatorColor)
    }

    private fun configurarSpinnerPaises() {
        try {
            val paisesBD = baseDatos.obtenerPaises()
            listaPaises.clear()
            listaPaises.addAll(paisesBD.map { it.nombre })

            if (!listaPaises.contains("Otro")) {
                listaPaises.add("Otro")
            }

            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listaPaises
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinnerPaises.adapter = adapter

            spinnerPaises.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val seleccion = parent?.getItemAtPosition(position)?.toString() ?: return
                    if (seleccion == "Otro") {
                        mostrarPopupPais()
                    } else {
                        ocultarPopupPais()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    ocultarPopupPais()
                }
            }

            if (listaPaises.isNotEmpty()) {
                spinnerPaises.setSelection(0)
            }

        } catch (e: Exception) {
            Log.e("ERROR", "Error al configurar spinner: ${e.message}")
            Toast.makeText(this, "Error al cargar países", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarPopupPais() {
        layoutNuevoPais.visibility = View.VISIBLE
        editTextNuevoPais.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextNuevoPais, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun ocultarPopupPais() {
        layoutNuevoPais.visibility = View.GONE
        editTextNuevoPais.text?.clear()
        resetSeparatorColors()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextNuevoPais.windowToken, 0)
    }

    private fun agregarNuevoPais() {
        val nombrePais = editTextNuevoPais.text.toString().trim()

        if (nombrePais.isEmpty()) {
            editTextNuevoPais.error = "Ingrese un nombre de país"
            showErrorOnSeparators()
            return
        }

        val resultado = baseDatos.insertarPais(nombrePais)

        if (resultado != -1L) {
            val seleccionActual = spinnerPaises.selectedItemPosition
            listaPaises.remove("Otro")
            listaPaises.add(nombrePais)
            listaPaises.add("Otro")
            adapter.notifyDataSetChanged()
            spinnerPaises.setSelection(seleccionActual)
            ocultarPopupPais()
            Toast.makeText(this, "País agregado correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar país", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCiudad() {
        var hasErrors = false
        resetSeparatorColors()

        val nombreCiudad = editTextNombreCiudad.text.toString().trim()
        val poblacionText = editTextPoblacion.text.toString()
        val paisSeleccionado = spinnerPaises.selectedItem?.toString() ?: ""

        // Validaciones
        if (nombreCiudad.isEmpty()) {
            editTextNombreCiudad.error = "Ingrese nombre de ciudad"
            hasErrors = true
        }

        if (poblacionText.isEmpty()) {
            editTextPoblacion.error = "Ingrese la población"
            hasErrors = true
        }

        if (paisSeleccionado == "Otro") {
            val nuevoPais = editTextNuevoPais.text.toString().trim()
            if (nuevoPais.isEmpty()) {
                editTextNuevoPais.error = "Ingrese un nombre de país"
                hasErrors = true
            }
        }

        if (hasErrors) {
            showErrorOnSeparators()
            return
        }

        val poblacion = poblacionText.toIntOrNull() ?: 0
        if (poblacion <= 0) {
            editTextPoblacion.error = "La población debe ser mayor a 0"
            showErrorOnSeparators()
            return
        }

        // Obtener ID del país seleccionado
        val paises = baseDatos.obtenerPaises()
        val pais = if (paisSeleccionado == "Otro") {
            val nuevoPaisNombre = editTextNuevoPais.text.toString().trim()
            paises.find { it.nombre == nuevoPaisNombre } ?: run {
                // Insertar el nuevo país si no existe
                val nuevoPaisId = baseDatos.insertarPais(nuevoPaisNombre)
                if (nuevoPaisId != -1L) {
                    Pais(nuevoPaisId.toInt(), nuevoPaisNombre)
                } else {
                    Toast.makeText(this, "Error al crear el nuevo país", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        } else {
            paises.find { it.nombre == paisSeleccionado }
        }

        if (pais == null) {
            Toast.makeText(this, "Error: País no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar ciudad
        val resultado = baseDatos.insertarCiudad(nombreCiudad, poblacion, pais.id)
        Log.d("DEBUG", "Resultado de insertar ciudad: $resultado")

        if (resultado != -1L) {
            Toast.makeText(this, "¡Ciudad guardada exitosamente!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish() // Cierra la actividad y regresa a MainActivity
        } else {
            Toast.makeText(this, "Error al guardar la ciudad", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        baseDatos.close()
        super.onDestroy()
    }
}