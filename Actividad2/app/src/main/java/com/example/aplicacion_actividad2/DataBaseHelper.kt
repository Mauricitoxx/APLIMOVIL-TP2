package com.example.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class BaseDatos(context: Context) : SQLiteOpenHelper(context, "paises.db", null, 1) {

    companion object {
        private const val TAG = "BaseDatos"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Crear tabla pais
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS pais(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nombre TEXT NOT NULL UNIQUE" +
                        ")"
            )

            // Crear tabla ciudad
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS ciudad(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nombre TEXT NOT NULL," +
                        "habitantes INTEGER NOT NULL," +
                        "idPais INTEGER," +
                        "FOREIGN KEY(idPais) REFERENCES pais(id) ON DELETE CASCADE" +
                        ")"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear tablas", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS ciudad")
            db.execSQL("DROP TABLE IF EXISTS pais")
            onCreate(db)
        } catch (e: Exception) {
            Log.e(TAG, "Error en onUpgrade", e)
        }
    }

    // Insertar país
    fun insertarPais(nombre: String): Long {
        if (nombre.isBlank()) return -1

        return try {
            val db = writableDatabase
            val valores = ContentValues().apply {
                put("nombre", nombre.trim())
            }
            val resultado = db.insert("pais", null, valores)
            db.close()
            resultado
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando país", e)
            -1
        }
    }

    // Insertar ciudad
    fun insertarCiudad(nombre: String, habitantes: Int, idPais: Int): Long {
        if (nombre.isBlank() || habitantes < 0 || idPais <= 0) return -1

        return try {
            val db = writableDatabase
            val valores = ContentValues().apply {
                put("nombre", nombre.trim())
                put("habitantes", habitantes)
                put("idPais", idPais)
            }
            val resultado = db.insert("ciudad", null, valores)
            db.close()
            resultado
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando ciudad", e)
            -1
        }
    }

    // Obtener todos los países
    fun obtenerPaises(): List<Pais> {
        val paises = mutableListOf<Pais>()
        var cursor: android.database.Cursor? = null
        var db: SQLiteDatabase? = null

        try {
            db = readableDatabase
            cursor = db.rawQuery("SELECT id, nombre FROM pais ORDER BY nombre", null)

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                paises.add(Pais(id, nombre))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo países", e)
        } finally {
            cursor?.close()
            db?.close()
        }

        return paises
    }

    fun buscarPaisesYCiudades(filtro: String): List<Pair<Pais, List<Ciudad>>> {
        val resultados = mutableListOf<Pair<Pais, List<Ciudad>>>()
        var cursor: android.database.Cursor? = null
        var db: SQLiteDatabase? = null

        try {
            db = readableDatabase
            val query = """
                SELECT 
                    p.id as pais_id, 
                    p.nombre as pais_nombre,
                    c.id as ciudad_id,
                    c.nombre as ciudad_nombre,
                    c.habitantes
                FROM pais p
                LEFT JOIN ciudad c ON c.idPais = p.id
                WHERE p.nombre LIKE ? OR c.nombre LIKE ?
                ORDER BY p.nombre, c.nombre
            """.trimIndent()

            val filtroLike = "%${filtro.trim()}%"
            cursor = db.rawQuery(query, arrayOf(filtroLike, filtroLike))

            val paisesMap = mutableMapOf<Int, Pais>()
            val ciudadesMap = mutableMapOf<Int, MutableList<Ciudad>>()

            while (cursor.moveToNext()) {
                try {
                    val paisId = cursor.getInt(cursor.getColumnIndexOrThrow("pais_id"))
                    val paisNombre = cursor.getString(cursor.getColumnIndexOrThrow("pais_nombre"))

                    val pais = paisesMap.getOrPut(paisId) { Pais(paisId, paisNombre) }

                    if (!cursor.isNull(cursor.getColumnIndexOrThrow("ciudad_id"))) {
                        val ciudad = Ciudad(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("ciudad_id")),
                            nombre = cursor.getString(cursor.getColumnIndexOrThrow("ciudad_nombre")),
                            habitantes = cursor.getInt(cursor.getColumnIndexOrThrow("habitantes")),
                            idPais = paisId
                        )
                        ciudadesMap.getOrPut(paisId) { mutableListOf() }.add(ciudad)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando fila", e)
                }
            }

            // Construir resultados
            paisesMap.values.sortedBy { it.nombre }.forEach { pais ->
                val ciudades = ciudadesMap[pais.id]?.sortedBy { it.nombre } ?: emptyList()
                resultados.add(pais to ciudades)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error en buscarPaisesYCiudades", e)
        } finally {
            cursor?.close()
            db?.close()
        }

        return resultados
    }
    // Eliminar ciudad por ID
    fun eliminarCiudad(id: Int): Int {
        return try {
            val db = writableDatabase
            val filasAfectadas = db.delete("ciudad", "id = ?", arrayOf(id.toString()))
            db.close()
            filasAfectadas
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando ciudad", e)
            0
        }
    }

    fun actualizarPoblacionCiudad(idCiudad: Int, nuevaPoblacion: Int): Int {
        if (nuevaPoblacion < 0) return -1

        return try {
            val db = writableDatabase
            val valores = ContentValues().apply {
                put("habitantes", nuevaPoblacion)
            }
            val resultado = db.update(
                "ciudad",
                valores,
                "id = ?",
                arrayOf(idCiudad.toString())
            )
            db.close()
            resultado
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando población", e)
            -1
        }
    }
}