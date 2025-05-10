package com.example.aplicacion_actividad1

import android.content.ContentValues
import android.os.Bundle
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

        bestScore = getBestScoreFromDB()

        guessButton.setOnClickListener {
            val userInput = inputNumber.text.toString().toIntOrNull()

            if (userInput == null || userInput !in 1..5) {
                Toast.makeText(this, "Ingresa un número entre 1 y 5", Toast.LENGTH_SHORT).show()
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

            scoreText.text = "Puntaje: $currentScore"

            if (failedAttempts >= 5) {
                currentScore = 0
                Toast.makeText(this, "¡Perdiste! 5 fallos seguidos.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        saveButton.setOnClickListener {
            saveScoreToDB()
            Toast.makeText(this, "Puntaje guardado", Toast.LENGTH_SHORT).show()
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

    private fun saveScoreToDB() {
        if (currentScore > bestScore) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("puntaje", currentScore)
            }
            db.update("jugadores", values, "id = ?", arrayOf(userId.toString()))
        }
    }
}
