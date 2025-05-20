package com.example.aplicacion_actividad2

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

    private val listaPaises = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cargar)

        // Verificar que las vistas existen
        spinnerPaises = findViewById(R.id.spinnerPaises) ?: run {
            Toast.makeText(this, "Error: No se encontró el Spinner", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        layoutNuevoPais = findViewById(R.id.layoutNuevoPais) ?: run {
            Toast.makeText(this, "Error: No se encontró el layout de nuevo país", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        editTextNuevoPais = findViewById(R.id.editTextNuevoPais)
        editTextNombreCiudad = findViewById(R.id.editTextNombreCiudad)
        editTextPoblacion = findViewById(R.id.editTextPoblacion)

        val btnAgregarNuevoPais: Button = findViewById(R.id.btnAgregarNuevoPais)
        val btnCancelarNuevoPais: Button = findViewById(R.id.btnCancelarNuevoPais)
        val btnGuardarCiudad: Button = findViewById(R.id.btnGuardarCiudad)

        // Inicializar base de datos
        baseDatos = BaseDatos(this)

        // Configurar Spinner con datos iniciales
        configurarSpinnerPaises()

        // Configurar listeners
        btnAgregarNuevoPais.setOnClickListener { agregarNuevoPais() }
        btnCancelarNuevoPais.setOnClickListener { ocultarPopupPais() }
        btnGuardarCiudad.setOnClickListener { guardarCiudad() }
    }
    // Agrega esta función a tu CargarCiudadActivity


    // Función auxiliar para actualizar la vista (si muestras las ciudades)
    private fun configurarSpinnerPaises() {
        try {
            Log.d("DEBUG", "Configurando spinner de países")

            // 1. Obtener países de la BD
            val paisesBD = baseDatos.obtenerPaises()
            Log.d("DEBUG", "Países obtenidos de BD: ${paisesBD.size}")

            // 2. Preparar lista
            listaPaises.clear()
            listaPaises.addAll(paisesBD.map { it.nombre })

            // Agregar "Otro" solo si no existe ya
            if (!listaPaises.contains("Otro")) {
                listaPaises.add("Otro")
            }
            Log.d("DEBUG", "Lista completa para spinner: $listaPaises")

            // 3. Configurar adaptador
            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listaPaises
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinnerPaises.adapter = adapter

            // 4. Configurar listener mejorado
            spinnerPaises.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val seleccion = parent?.getItemAtPosition(position)?.toString() ?: return
                    Log.d("DEBUG", "País seleccionado: $seleccion")

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

            // Seleccionar primer item por defecto
            if (listaPaises.isNotEmpty()) {
                spinnerPaises.setSelection(0)
            }

        } catch (e: Exception) {
            Log.e("ERROR", "Error al configurar spinner: ${e.message}")
            Toast.makeText(this, "Error al cargar países", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarPopupPais() {
        Log.d("DEBUG", "Mostrando popup para nuevo país")
        layoutNuevoPais.visibility = View.VISIBLE
        editTextNuevoPais.requestFocus()

        // Mostrar teclado
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextNuevoPais, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun ocultarPopupPais() {
        Log.d("DEBUG", "Ocultando popup de país")
        layoutNuevoPais.visibility = View.GONE
        editTextNuevoPais.text?.clear()

        // Ocultar teclado
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextNuevoPais.windowToken, 0)
    }

    private fun agregarNuevoPais() {
        val nombrePais = editTextNuevoPais.text.toString().trim()
        Log.d("DEBUG", "Intentando agregar país: $nombrePais")

        if (nombrePais.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre de país", Toast.LENGTH_SHORT).show()
            return
        }

        val resultado = baseDatos.insertarPais(nombrePais)
        Log.d("DEBUG", "Resultado de insertar país: $resultado")

        if (resultado != -1L) {
            // Actualizar Spinner manteniendo la selección
            val seleccionActual = spinnerPaises.selectedItemPosition

            listaPaises.remove("Otro")
            listaPaises.add(nombrePais)
            listaPaises.add("Otro")

            adapter.notifyDataSetChanged()

            // Restaurar selección o seleccionar el nuevo país
            spinnerPaises.setSelection(seleccionActual)

            ocultarPopupPais()
            Toast.makeText(this, "País agregado correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar país", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCiudad() {
        val nombreCiudad = editTextNombreCiudad.text.toString().trim()
        val poblacionText = editTextPoblacion.text.toString()
        val paisSeleccionado = spinnerPaises.selectedItem?.toString() ?: ""

        Log.d("DEBUG", "Intentando guardar ciudad: $nombreCiudad, $poblacionText, $paisSeleccionado")

        // Validaciones mejoradas
        when {
            nombreCiudad.isEmpty() -> {
                editTextNombreCiudad.error = "Ingrese nombre de ciudad"
                return
            }
            poblacionText.isEmpty() -> {
                editTextPoblacion.error = "Ingrese la población"
                return
            }
            paisSeleccionado == "Otro" -> {
                Toast.makeText(this, "Complete primero el nuevo país", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val poblacion = poblacionText.toIntOrNull() ?: 0
        if (poblacion <= 0) {
            editTextPoblacion.error = "La población debe ser mayor a 0"
            return
        }

        // Obtener ID del país seleccionado
        val paises = baseDatos.obtenerPaises()
        val pais = paises.find { it.nombre == paisSeleccionado }

        if (pais == null) {
            Toast.makeText(this, "Error: País no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar ciudad
        val resultado = baseDatos.insertarCiudad(nombreCiudad, poblacion, pais.id)
        Log.d("DEBUG", "Resultado de insertar ciudad: $resultado")

        if (resultado != -1L) {
            Toast.makeText(this, "¡Ciudad guardada exitosamente!", Toast.LENGTH_SHORT).show()
            limpiarFormulario()
        } else {
            Toast.makeText(this, "Error al guardar la ciudad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarFormulario() {
        editTextNombreCiudad.text?.clear()
        editTextPoblacion.text?.clear()
        spinnerPaises.setSelection(0)
    }

    override fun onDestroy() {
        baseDatos.close()
        super.onDestroy()
    }
}