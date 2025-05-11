package com.example.aplicacion_actividad1

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class UserActivity : AppCompatActivity() {

    private lateinit var dbHelper: BaseDatos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usuario_layout)

        dbHelper = BaseDatos(this)

        val carga_nombre = findViewById<EditText>(R.id.nombre_usuario)
        val boton_jugar = findViewById<Button>(R.id.button_start)

        boton_jugar.setOnClickListener {
            val nombre = carga_nombre.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingresa un nombre válido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val cursor: Cursor = db.rawQuery(
                "SELECT * FROM jugadores WHERE nombre = ?", arrayOf(nombre)
            )

            var userId: Int

            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                Toast.makeText(this, "Bienvenido de nuevo, $nombre.", Toast.LENGTH_LONG).show()
            } else {
                val valores = ContentValues().apply {
                    put("nombre", nombre)
                }
                val result = db.insert("jugadores", null, valores)
                if (result != -1L) {
                    userId = result.toInt()
                    Toast.makeText(this, "Nuevo jugador creado: $nombre", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al crear el jugador", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            cursor.close()
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}
