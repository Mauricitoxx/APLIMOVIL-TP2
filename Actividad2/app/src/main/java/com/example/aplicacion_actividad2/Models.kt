package com.example.sqlite

data class Pais(val id: Int, val nombre: String)

data class Ciudad(val id: Int, val nombre: String, val habitantes: Int, val idPais: Int)