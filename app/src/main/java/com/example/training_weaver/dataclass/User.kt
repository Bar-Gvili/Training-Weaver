package com.example.training_weaver.dataclass

import java.util.UUID

data class User(
    val userID: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String
)
