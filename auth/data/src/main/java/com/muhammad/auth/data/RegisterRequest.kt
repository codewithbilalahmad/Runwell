package com.muhammad.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email : String,
    val password : String
)
