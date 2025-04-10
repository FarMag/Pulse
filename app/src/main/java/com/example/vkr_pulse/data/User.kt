package com.example.vkr_pulse.data

data class User(
    val username: String,
    val email: String,
    val password: String,
    val birth_date: String,  // Формат "DD.MM.YYYY"
    val gender: String       // "male" или "female"
)