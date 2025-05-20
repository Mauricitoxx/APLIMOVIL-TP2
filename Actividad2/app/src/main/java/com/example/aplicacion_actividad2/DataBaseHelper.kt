package com.example.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(context: Context) : SQLiteOpenHelper(context, "paises.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear tabla pais
        db?.execSQL(
            "CREATE TABLE pais(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL" +
                    ")"
        )

        // Crear tabla ciudad
        db?.execSQL(
            "CREATE TABLE ciudad(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "habitantes INTEGER NOT NULL," +
                    "idPais INTEGER," +
                    "FOREIGN KEY(idPais) REFERENCES pais(id)" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Eliminar tablas si existen y recrearlas
        db?.execSQL("DROP TABLE IF EXISTS ciudad")
        db?.execSQL("DROP TABLE IF EXISTS pais")
        onCreate(db)
    }

    // Insertar país
    fun insertarPais(nombre: String): Long {
        if (nombre.isBlank()) return -1

        val db = writableDatabase
        val valores = ContentValues().apply {
            put("nombre", nombre)
        }
        val resultado = db.insert("pais", null, valores)
        db.close()
        return resultado
    }

    // Insertar ciudad
    fun insertarCiudad(nombre: String, habitantes: Int, idPais: Int): Long {
        if (nombre.isBlank() || habitantes < 0) return -1

        val db = writableDatabase
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("habitantes", habitantes)
            put("idPais", idPais)
        }
        val resultado = db.insert("ciudad", null, valores)
        db.close()
        return resultado
    }

    // Obtener todos los países
    fun obtenerPaises(): List<Pais> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pais", null)
        val paises = mutableListOf<Pais>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            paises.add(Pais(id, nombre))
        }

        cursor.close()
        return paises
    }

    fun buscarPaisesYCiudades(filtro: String): List<Pais> {
        val db = readableDatabase
        val paises = mutableSetOf<Pais>() // evitar duplicados

        val query = """
        SELECT DISTINCT p.id, p.nombre 
        FROM pais p
        LEFT JOIN ciudad c ON c.idPais = p.id
        WHERE p.nombre LIKE ? OR c.nombre LIKE ?
    """.trimIndent()

        val filtroLike = "%$filtro%"
        val cursor = db.rawQuery(query, arrayOf(filtroLike, filtroLike))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            paises.add(Pais(id, nombre))
        }

        cursor.close()
        return paises.toList()
    }



    // Eliminar ciudad por ID
    fun eliminarCiudad(id: Int): Int {
        val db = writableDatabase
        val filasAfectadas = db.delete("ciudad", "id = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas
    }

    fun actualizarPoblacionCiudad(idCiudad: Int, nuevaPoblacion: Int): Int {
        if (nuevaPoblacion < 0) return -1

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
        return resultado
    }
}
