package com.example.aplicacion_actividad1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(context: Context) : SQLiteOpenHelper(
    context,
    "jugadores.db",
    null,
    1

    ){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE jugadores (
                id INTERGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE NOT NULL,
                puntaje INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS jugadores")
        onCreate(db)
    }

    fun obtenerJugadoresOrdenadoPorPuntaje(): List<Jugador> {
        val lista = mutableListOf<Jugador>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, nombre, puntaje_max FROM jugadores ORDER BY puntaje_max ASC", null)

        if (cursor.moveToFirst()){
            do {
                val jugador = Jugador(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    puntaje = cursor.getInt(cursor.getColumnIndexOrThrow("puntaje"))
                )
                lista.add(jugador)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
}
