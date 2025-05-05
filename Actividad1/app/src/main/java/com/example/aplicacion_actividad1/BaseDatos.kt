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
                puntaje_max INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS jugadores")
        onCreate(db)
    }
}
