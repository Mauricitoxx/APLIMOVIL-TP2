package com.example.aplicacion_actividad1

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var dbHelper: BaseDatos
    private var userId: Int = -1
    private var currentScore = 0
    private var bestScore = 0
    private var failedAttempts = 0
    private lateinit var inputNumber: EditText
    private lateinit var guessButton: Button
    private lateinit var saveButton: Button
    private lateinit var scoreText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var scoreMaxText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_layout)

        dbHelper = BaseDatos(this)
        userId = intent.getIntExtra("userId", -1)

        inputNumber = findViewById(R.id.input_number)
        guessButton = findViewById(R.id.button_guess)
        saveButton = findViewById(R.id.button_save)
        scoreText = findViewById(R.id.text_score)
        feedbackText = findViewById(R.id.text_feedback)
        scoreMaxText = findViewById(R.id.text_score_max)

        bestScore = getBestScoreFromDB()
        scoreText.text = "PUNTAJE: $currentScore"
        scoreMaxText.text = "PUNTAJE MAXIMO: $bestScore"

        guessButton.setOnClickListener {
            val userInput = inputNumber.text.toString().trim().toIntOrNull()

            if (userInput == null || userInput !in 1..5) {
                Toast.makeText(this, "Ingresa un número entre 1 y 5", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val randomNumber = Random.nextInt(1, 6)
            if (userInput == randomNumber) {
                currentScore += 10
                failedAttempts = 0
                feedbackText.text = "¡Correcto! Número: $randomNumber"
            } else {
                failedAttempts++
                feedbackText.text = "Incorrecto. Número: $randomNumber"
            }

            scoreText.text = "PUNTAJE: $currentScore"
            inputNumber.text.clear()

            if (failedAttempts >= 5) {
                currentScore = 0
                Toast.makeText(this, "¡Perdiste! 5 fallos seguidos.", Toast.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 2000)
            }
        }

        saveButton.setOnClickListener {
            val updated = saveScoreToDB()
            val msg =
                if (updated) "¡Nuevo puntaje máximo guardado!" else "Puntaje guardado, no superó el máximo"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun getBestScoreFromDB(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT puntaje FROM jugadores WHERE id = ?", arrayOf(userId.toString()))
        var score = 0
        if (cursor.moveToFirst()) {
            score = cursor.getInt(0)
        }
        cursor.close()
        return score
    }

    private fun saveScoreToDB(): Boolean {
        if (currentScore > bestScore) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("puntaje", currentScore)
            }
            db.update("jugadores", values, "id = ?", arrayOf(userId.toString()))
            return true
        }
        return false
    }
}
