package com.example.aplicacion_actividad1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class StartActivity() : ComponentActivity() {

    override fun onCreate(savedIntanceState: Bundle?){
        super.onCreate(savedIntanceState)
        setContentView(R.layout.inicio_layout)

        val comenza_juego = findViewById<Button>(R.id.button_start)
        val ranking = findViewById<Button>(R.id.button_ranking)

        comenza_juego.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

        ranking.setOnClickListener{
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }
    }
}


