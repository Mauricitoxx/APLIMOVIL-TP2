package com.example.aplicacion_actividad1

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity

class RankingActivity : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JugadorAdapter
    private lateinit var baseDatos: BaseDatos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_layout)

        recyclerView = findViewById(R.id.view_ranking)
        recyclerView.layoutManager = LinearLayoutManager(this)

        baseDatos = BaseDatos(this)
        val jugadores = baseDatos.obtenerJugadoresOrdenadoPorPuntaje()

        adapter = JugadorAdapter(jugadores)
        this.recyclerView.adapter = adapter

        findViewById<Button>(R.id.salir_boton).setOnClickListener {
            finish()
        }
    }
}

